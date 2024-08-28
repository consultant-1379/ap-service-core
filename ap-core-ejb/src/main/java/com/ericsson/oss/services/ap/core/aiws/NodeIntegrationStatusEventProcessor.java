/*------------------------------------------------------------------------------
 ********************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.aiws;

import static com.ericsson.oss.services.ap.api.status.State.HARDWARE_REPLACE_BIND_COMPLETED;
import static com.ericsson.oss.services.ap.api.status.State.HARDWARE_REPLACE_FAILED;
import static com.ericsson.oss.services.ap.api.status.State.HARDWARE_REPLACE_STARTED;
import static com.ericsson.oss.services.ap.api.status.State.INTEGRATION_FAILED;
import static com.ericsson.oss.services.ap.api.status.State.INTEGRATION_STARTED;
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_FAILED;
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_STARTED;
import static com.ericsson.oss.services.ap.api.status.State.ORDER_COMPLETED;
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_COMPLETED;
import static com.ericsson.oss.services.ap.api.status.State.UNKNOWN;
import static com.ericsson.oss.services.model.autoprovisioning.Phase.APPLYING_CONFIGURATION;
import static com.ericsson.oss.services.model.autoprovisioning.Phase.DOWNLOADING_SOFTWARE;
import static com.ericsson.oss.services.model.autoprovisioning.Phase.ESTABLISHING_CONTACT_MANAGEMENTSYSTEM;
import static com.ericsson.oss.services.model.autoprovisioning.Phase.INSTALLING_SOFTWARE;
import static com.ericsson.oss.services.model.autoprovisioning.Phase.SENDING_NODE_UP;
import static com.ericsson.oss.services.model.autoprovisioning.Phase.STARTING_SOFTWARE;
import static com.ericsson.oss.services.model.autoprovisioning.State.FAILED;
import static com.ericsson.oss.services.model.autoprovisioning.State.FINISHED;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommandException;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.itpf.sdk.core.retry.classic.RetryManagerBean;
import com.ericsson.oss.services.ap.api.exception.CommonServiceRetryException;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.api.status.StatusEntryNames;
import com.ericsson.oss.services.ap.api.status.StatusEntryProgress;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.util.log.MRDefinition;
import com.ericsson.oss.services.ap.common.util.log.MRExecutionRecorder;
import com.ericsson.oss.services.ap.core.status.NodeStatusMoUpdater;
import com.ericsson.oss.services.model.autoprovisioning.NodeIntegrationStatusEvent;
import com.ericsson.oss.services.model.autoprovisioning.Phase;

/**
 * This class processes the node integration events sent by the node during integration.
 * AP tasks and the AP states are updated to reflect the node integration phase and state.
 */
public class NodeIntegrationStatusEventProcessor {

    @EServiceRef
    private StatusEntryManagerLocal statusEntryManager;

    @Inject
    protected NodeStatusMoUpdater nodeStatusMoUpdater;

    @Inject
    private MRExecutionRecorder recorder;

    @Inject
    private Logger logger;

    private static final EnumMap<Phase, String> phases = new EnumMap<>(Phase.class);
    private static final EnumMap<Phase, String> PreviousStatusEntryMap = new EnumMap<>(Phase.class);

    private static final int MAX_RETRIES_CHECK = 3;
    private static final int RETRY_INTERVAL_CHECK = 200;
    private static final String WAITING_FOR_RESUME = "Waiting for Resume";
    private static final String NODE_REBOOT_INITIATED = "Node reboot initiated";

    static {
        phases.put(ESTABLISHING_CONTACT_MANAGEMENTSYSTEM, StatusEntryNames.NODE_ESTABLISHING_CONTACT.toString());
        phases.put(DOWNLOADING_SOFTWARE, StatusEntryNames.NODE_DOWNLOADING_CONFIGURATIONS.toString());
        phases.put(INSTALLING_SOFTWARE, StatusEntryNames.NODE_INSTALLING_SOFTWARE.toString());
        phases.put(STARTING_SOFTWARE, StatusEntryNames.NODE_STARTING_SOFTWARE.toString());
        phases.put(APPLYING_CONFIGURATION, StatusEntryNames.NODE_APPLYING_CONFIGURATION.toString());
        phases.put(SENDING_NODE_UP, StatusEntryNames.NODE_SENDING_NODE_UP.toString());
    }

    static {
        PreviousStatusEntryMap.put(DOWNLOADING_SOFTWARE, StatusEntryNames.NODE_ESTABLISHING_CONTACT.toString());
        PreviousStatusEntryMap.put(INSTALLING_SOFTWARE, StatusEntryNames.NODE_DOWNLOADING_CONFIGURATIONS.toString());
        PreviousStatusEntryMap.put(STARTING_SOFTWARE, StatusEntryNames.NODE_INSTALLING_SOFTWARE.toString());
        PreviousStatusEntryMap.put(APPLYING_CONFIGURATION, StatusEntryNames.NODE_STARTING_SOFTWARE.toString());
        PreviousStatusEntryMap.put(SENDING_NODE_UP, StatusEntryNames.NODE_APPLYING_CONFIGURATION.toString());
    }

    private static final List<String> NODE_STATES_REQUIRED_UPDATE = Arrays.asList(ORDER_COMPLETED.toString(),
            INTEGRATION_FAILED.toString(),
            INTEGRATION_STARTED.toString(),
            PRE_MIGRATION_COMPLETED.toString(),
            MIGRATION_FAILED.toString(),
            MIGRATION_STARTED.toString(),
            HARDWARE_REPLACE_BIND_COMPLETED.toString(),
            HARDWARE_REPLACE_FAILED.toString(),
            HARDWARE_REPLACE_STARTED.toString());

    /**
     * Processes a notification from the AIWS for Node Integration Status Events
     *
     * @param apNodeMo
     *            {@link ManagedObject} Node - the AP node MO.
     * @param event
     *            a notification from AIWS that contains details for Node Integration Status Events
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void processNodeIntegrationStatusEvent(final ManagedObject apNodeMo, final NodeIntegrationStatusEvent event) {
        logger.info("Processing node integration status event with phase {}, state: {}, progress: {} for node {}",
                event.getPhase(), event.getState(), event.getProgress(), apNodeMo.getName());

        recordMainRequirementEvent(apNodeMo);
        setNodeStatusState(apNodeMo, event);
        processEvent(apNodeMo, event);
    }

    private void setNodeStatusState(final ManagedObject apNodeMo, final NodeIntegrationStatusEvent event) {
        if (NODE_STATES_REQUIRED_UPDATE.contains(getNodeState(apNodeMo))) {
            final State stateToUpdate = getStateToUpdate(apNodeMo, event);
            if (stateToUpdate != UNKNOWN) {
                updateNodeStatusState(apNodeMo, stateToUpdate);
            }
        }
    }

    private State getStateToUpdate(final ManagedObject apNodeMo, final NodeIntegrationStatusEvent event) {
        State stateToUpate = UNKNOWN;

        if (isMigrationNode(apNodeMo)) {
            if (!isMigrationRollback(apNodeMo)) {
                stateToUpate = FAILED.equals(event.getState()) ? MIGRATION_FAILED : MIGRATION_STARTED;
            }
        } else if (isHardwareReplaceNode(apNodeMo)) {
            stateToUpate = FAILED.equals(event.getState()) ? HARDWARE_REPLACE_FAILED : HARDWARE_REPLACE_STARTED;
        } else {
            stateToUpate = FAILED.equals(event.getState()) ? INTEGRATION_FAILED : INTEGRATION_STARTED;
        }

        return stateToUpate;
    }

    private void updateNodeStatusState(final ManagedObject apNodeMo, final State state) {
        final String currentNodeState = getNodeState(apNodeMo);
        if (!state.toString().equals(currentNodeState)) {
            nodeStatusMoUpdater.setState(apNodeMo.getFdn(), NodeStatusAttribute.STATE.toString(), state.toString());
        }
    }

    private void recordMainRequirementEvent(final ManagedObject apNodeMo) {
        final StatusEntry nodeEstablishingContact = statusEntryManager.getStatusEntryByNameInNewTx(apNodeMo.getFdn(), phases.get(ESTABLISHING_CONTACT_MANAGEMENTSYSTEM));
        final StatusEntry nodeDownloadingConfigurations = statusEntryManager.getStatusEntryByNameInNewTx(apNodeMo.getFdn(), phases.get(DOWNLOADING_SOFTWARE));
        if (nodeEstablishingContact == null && nodeDownloadingConfigurations == null) {
            if (isNodeStateOrderCompleted(apNodeMo)) {
                recorder.recordMRExecution(MRDefinition.AP_AI_STATUS_IMPROVEMENT);
            } else if (isHardwareReplaceNode(apNodeMo)) {
                recorder.recordMRExecution(MRDefinition.AP_HARDWAREREPLACE_STATUS_IMPROVEMENT);
            }
        }
    }

    private boolean isNodeStateOrderCompleted(final ManagedObject apNodeMo) {
        return ORDER_COMPLETED.toString().equals(getNodeState(apNodeMo));
    }

    private boolean isHardwareReplaceNode(final ManagedObject apNodeMo) {
        if (apNodeMo.getAttribute(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString()) != null) {
            return (boolean) apNodeMo.getAttribute(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString());
        }
        return false;
    }

    private static boolean isMigrationRollback(final ManagedObject apNodeMo) {
        if (apNodeMo.getAttribute(NodeAttribute.IS_ROLLBACK.toString()) != null) {
            return (boolean) apNodeMo.getAttribute(NodeAttribute.IS_ROLLBACK.toString());
        }
        return false;
    }

    private static boolean isMigrationNode(final ManagedObject apNodeMo) {
        if (apNodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString()) != null) {
            return (boolean) apNodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString());
        }
        return false;
    }

    private void processEvent(final ManagedObject apNodeMo, final NodeIntegrationStatusEvent event) {
        if (existingTasksFailed(apNodeMo.getFdn())) {
            processExistingEvent(apNodeMo, event);
        } else {
            processNewEvent(apNodeMo, event);
        }
    }

    /*
     * This event is an existing event that has either failed or needs to be re-processed due to another task failure.
     */
    private void processExistingEvent(final ManagedObject apNodeMo, final NodeIntegrationStatusEvent event) {
        final String apNodeFdn = apNodeMo.getFdn();
        final StatusEntry statusEntry = getStatusEntry(apNodeFdn, phases.get(event.getPhase()));
        if (statusEntry != null) {
            logger.debug("Process Existing Status Entry values, Name: {}, Progress: {} AdditionalInfo {}",
                    statusEntry.getTaskName(), statusEntry.getTaskProgress(), statusEntry.getAdditionalInfo());
            updateExistingStatusEntry(apNodeFdn, statusEntry, event);
        } else {
            processNewEvent(apNodeMo, event);
        }
    }

    private boolean existingTasksFailed(final String apNodeFdn) {
        final List<StatusEntry> statusEntryList = statusEntryManager.getAllStatusEntriesInNewTx(apNodeFdn);
        for (final StatusEntry statusEntry : statusEntryList) {
            if (statusEntry.getTaskProgress().equals(StatusEntryProgress.FAILED.toString())) {
                return true;
            }
        }
        return false;
    }

    /*
     * This event is it new to AP and has not previously been processed.
     */
    private void processNewEvent(final ManagedObject apNodeMo, final NodeIntegrationStatusEvent event) {
        final String apNodeFdn = apNodeMo.getFdn();
        logger.debug("Process new event with phase {}, state: {}, progress: {} for node {}", event.getPhase(), event.getState(), event.getProgress(),
                apNodeFdn);

        if (event.getState().equals(FAILED)){
            // if any event is a failure then update straight away.
            updateNewStatusEntryState(apNodeFdn, event);
            return;
        }

        switch (event.getPhase()) {
            case ESTABLISHING_CONTACT_MANAGEMENTSYSTEM:
                handleEstablishingContactEvent(apNodeFdn, event);
                break;

            case DOWNLOADING_SOFTWARE:
                handleDownloadingSoftwareEvent(apNodeFdn, event);
                break;

            case INSTALLING_SOFTWARE:
                handleSubsequentStatusEvent(apNodeFdn, event);
                break;

            case STARTING_SOFTWARE:
                handleSubsequentStatusEvent(apNodeFdn, event);
                autoSetCompletedForHardwareReplacementLMT(apNodeMo, event);
                break;

            case APPLYING_CONFIGURATION:
                handleSubsequentStatusEvent(apNodeFdn, event);
                break;

            case SENDING_NODE_UP:
                final StatusEntry previousEntry = getStatusEntry(apNodeFdn, PreviousStatusEntryMap.get(event.getPhase()));
                if (isEntryTaskCompleted(previousEntry)){
                    updateNewStatusEntryState(apNodeFdn, event);
                }
                break;
            default:
                logger.debug("Ignoring NodeIntegrationStatusEvent, event phase is not valid {} ", event.getPhase());
        }
    }

    private void handleEstablishingContactEvent(final String apNodeFdn, final NodeIntegrationStatusEvent event) {
        if (FINISHED.equals(event.getState())) {
            completeTaskWithConfirm(apNodeFdn, StatusEntryNames.NODE_ESTABLISHING_CONTACT.toString(), event);
            return;
        }
        final StatusEntry currentEntry = getStatusEntry(apNodeFdn, StatusEntryNames.NODE_ESTABLISHING_CONTACT.toString());
        if (!isEntryTaskCompleted(currentEntry)) {
            updateNewStatusEntryState(apNodeFdn, event);
        }
    }

    private void handleDownloadingSoftwareEvent(final String apNodeFdn, final NodeIntegrationStatusEvent event) {
        if (FINISHED.equals(event.getState())) {
            completeTaskWithConfirm(apNodeFdn, StatusEntryNames.NODE_DOWNLOADING_CONFIGURATIONS.toString(), event);
            return;
        }
        if (isLmtIntegration(apNodeFdn) || isEstablishContactCompleted(apNodeFdn)) {
            updateTask(apNodeFdn, StatusEntryNames.NODE_DOWNLOADING_CONFIGURATIONS.toString(), event);
        }
    }

    private void handleSubsequentStatusEvent(final String apNodeFdn, final NodeIntegrationStatusEvent event) {
        final String taskName = phases.get(event.getPhase());
        if (FINISHED.equals(event.getState())) {
            completeTaskWithConfirm(apNodeFdn, taskName, event);
            return;
        }
        final StatusEntry previousEntry = getStatusEntry(apNodeFdn, PreviousStatusEntryMap.get(event.getPhase()));
        if (isEntryTaskCompleted(previousEntry)) {
            updateTask(apNodeFdn, taskName, event);
        }
    }

    private boolean isEstablishContactCompleted(final String apNodeFdn) {
        return isEntryTaskCompleted(getStatusEntry(apNodeFdn, StatusEntryNames.NODE_ESTABLISHING_CONTACT.toString()));
    }

    private boolean isLmtIntegration(final String apNodeFdn) {
        final StatusEntry establishContactEntry = getStatusEntry(apNodeFdn, StatusEntryNames.NODE_ESTABLISHING_CONTACT.toString());
        return establishContactEntry == null;
    }

    private void updateTask(final String apNodeFdn, final String taskName, final NodeIntegrationStatusEvent event) {
        final StatusEntry currentEntry = getStatusEntry(apNodeFdn, taskName);
        if (currentEntry != null) {
            logger.debug("Current StatusEntry Task for processing, Name: {}, Progress: {} AdditionalInfo {}", currentEntry.getTaskName(),
                    currentEntry.getTaskProgress(), currentEntry.getAdditionalInfo());
            updateExistingStatusEntry(apNodeFdn, currentEntry, event);
            return;
        }
        updateNewStatusEntryState(apNodeFdn, event);
    }

    private void completeTaskWithConfirm(final String apNodeFdn, final String taskEntryName, final NodeIntegrationStatusEvent event) {
        logger.info("Complete task {} for node {} with confirmation", taskEntryName, apNodeFdn);
        try {
            final RetryManager retryManager = new RetryManagerBean();
            final RetryPolicy retryPolicy = RetryPolicy.builder().attempts(MAX_RETRIES_CHECK)
                    .waitInterval(RETRY_INTERVAL_CHECK, TimeUnit.MILLISECONDS)
                    .retryOn(CommonServiceRetryException.class)
                    .build();

            retryManager.executeCommand(retryPolicy, new RetriableCommand<Void>() {
                @Override
                public Void execute(final RetryContext retryContext) throws Exception {
                    if (!isEntryTaskCompleted(getStatusEntry(apNodeFdn, taskEntryName))) {
                        logger.info("Status entry {} does not exist or not COMPLETED, complete it and check it later", taskEntryName);
                        setTaskCompleted(apNodeFdn, event);
                        throw (new CommonServiceRetryException(
                                String.format("Confirm status entry %s is completed for node %s", taskEntryName, apNodeFdn)));
                    }
                    logger.debug("Status entry {} is completed for node {}", taskEntryName, apNodeFdn);
                    return null;
                }
            });
        } catch (final RetriableCommandException e) {
            logger.warn("RetriableCommandException {}", e.getMessage());
        }
    }

    /*
     * updates first event occurrences only
     */
    private void updateNewStatusEntryState(final String apNodeFdn, final NodeIntegrationStatusEvent event) {

        switch (event.getState()) {
            case STARTED:
                setTaskStarted(apNodeFdn, event);
                break;
            case RUNNING:
                statusEntryManager.taskStarted(apNodeFdn, phases.get(event.getPhase()), constructAdditionalInformation(event));
                break;
            case FINISHED:
                setTaskCompleted(apNodeFdn, event);
                break;
            case FAILED:
                setTaskFailed(apNodeFdn, event);
                break;
            default:
                logger.debug("Ignoring Notification, event state is not valid {} ", event.getState());
        }
    }

    /*
     * Updates an existing StatusEntry task to reflect its new value
     */
    private void updateExistingStatusEntry(final String apNodeFdn, final StatusEntry statusEntry, final NodeIntegrationStatusEvent event) {
        final String taskName = statusEntry.getTaskName();
        switch (event.getState()) {
            case STARTED:
                if (event.getPhase().equals(SENDING_NODE_UP)){
                    statusEntryManager.taskStarted(apNodeFdn, statusEntry.getTaskName(), "");
                }
                break; // not updating as no way to determine if greenfield or retry run for phase other than SENDING NODE UP.
            case RUNNING:
                if (isEntryTaskCompleted(statusEntry)){
                    handleStatusEntryCompletedScenario(apNodeFdn, statusEntry, event);
                    break;
                }
                if (isEventProgressInOrder(statusEntry, event)) {
                    if (isEntryTaskStarted(statusEntry)){
                        statusEntryManager.updateAdditionalInformation(apNodeFdn, statusEntry.getTaskName(), constructAdditionalInformation(event));
                    } else {
                        statusEntryManager.updateStartedStateTask(apNodeFdn, taskName, StatusEntryProgress.STARTED, constructAdditionalInformation(event));
                    }
                }
                break;
            case FINISHED:
                if (isEntryTaskStarted(statusEntry)) {
                    statusEntryManager.taskCompleted(apNodeFdn, statusEntry.getTaskName());
                } else {
                    statusEntryManager.updateEndStateTask(apNodeFdn, taskName, StatusEntryProgress.COMPLETED, "");
                }
                break;
            case FAILED:
                setTaskFailed(apNodeFdn, event);
                break;
            default:
                logger.debug("Ignoring NodeIntegrationStatusEvent, event state is not valid {} ", event.getState());
        }
    }

    private void handleStatusEntryCompletedScenario(final String apNodeFdn, final StatusEntry statusEntry, final NodeIntegrationStatusEvent event){
        if (existingTasksFailed(apNodeFdn) && isEventProgressInOrder(statusEntry, event)) {
            final String taskName = statusEntry.getTaskName();
            if (isEntryTaskStarted(statusEntry)){
                statusEntryManager.updateAdditionalInformation(apNodeFdn, taskName, constructAdditionalInformation(event));
            } else {
                statusEntryManager.updateStartedStateTask(apNodeFdn, taskName, StatusEntryProgress.STARTED, constructAdditionalInformation(event));
            }
        }
    }

    private StatusEntry getStatusEntry(final String apNodeFdn, final String entryName) {
        return statusEntryManager.getStatusEntryByNameInNewTx(apNodeFdn, entryName);
    }

    private boolean isEventProgressInOrder(final StatusEntry statusEntry, final NodeIntegrationStatusEvent event){
        final int entryProgress = statusEntry == null || statusEntry.getAdditionalInfo() == null ? 0 : extractProgressFromAdditionalInfo(statusEntry.getAdditionalInfo());
        return event.getProgress() > entryProgress;
    }

    private int extractProgressFromAdditionalInfo(final String additionalInfo) {
        int progress = 0;
        if (StringUtils.isNotBlank(additionalInfo)) {
            final Matcher m = Pattern.compile("[^0-9]*([0-9]+)%.*").matcher(additionalInfo);
            if (m.matches()) {
                progress = Integer.valueOf(m.group(1));
            }
        }
        return progress;
    }

    private boolean isEntryTaskStarted(final StatusEntry statusEntry) {
        if (statusEntry != null){
            final String taskProgress = statusEntry.getTaskProgress();
            return StatusEntryProgress.STARTED.toString().equals(taskProgress);
        }
        return false;
    }

    private boolean isEntryTaskCompleted(final StatusEntry statusEntry){
        return statusEntry != null && StatusEntryProgress.COMPLETED.toString().equals(statusEntry.getTaskProgress());
    }

    private boolean isEntryTaskInEndState(final String apNodeFdn, final String taskName) {
        final StatusEntry statusEntry = getStatusEntry(apNodeFdn, taskName);
        if (statusEntry != null) {
            final String taskProgress = statusEntry.getTaskProgress();
            return StatusEntryProgress.FAILED.toString().equals(taskProgress) || StatusEntryProgress.COMPLETED.toString().equals(taskProgress);
        }
        return false;
    }

    private void setTaskStarted(final String apNodeFdn, final NodeIntegrationStatusEvent event) {
        if (isEntryTaskInEndState(apNodeFdn, phases.get(event.getPhase()))) {
            statusEntryManager.updateEndStateTask(apNodeFdn, phases.get(event.getPhase()), StatusEntryProgress.STARTED, event.getMessage());
        } else {
            statusEntryManager.taskStarted(apNodeFdn, phases.get(event.getPhase()), event.getMessage());
        }
    }

    private void setTaskCompleted(final String apNodeFdn, final NodeIntegrationStatusEvent event) {
        if (isEntryTaskInEndState(apNodeFdn, phases.get(event.getPhase()))) {
            statusEntryManager.updateEndStateTask(apNodeFdn, phases.get(event.getPhase()), StatusEntryProgress.COMPLETED, "");
        } else {
            statusEntryManager.taskCompleted(apNodeFdn, phases.get(event.getPhase()));
        }
    }

    private void setTaskFailed(final String apNodeFdn, final NodeIntegrationStatusEvent event) {
        if (isEntryTaskInEndState(apNodeFdn, phases.get(event.getPhase()))) {
            statusEntryManager.updateEndStateTask(apNodeFdn, phases.get(event.getPhase()), StatusEntryProgress.FAILED, event.getMessage());
        } else {
            statusEntryManager.taskFailed(apNodeFdn, phases.get(event.getPhase()), event.getMessage());
        }
    }

    private String constructAdditionalInformation(final NodeIntegrationStatusEvent event) {
        return new StringBuilder()
                .append(event.getProgress())
                .append("%")
                .append(" ")
                .append(event.getMessage())
                .toString();
    }

    private static String getNodeState(final ManagedObject apNodeMo) {
        final ManagedObject apNodeStatusMo = apNodeMo.getChild(MoType.NODE_STATUS + "=1");
        return apNodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString());
    }

    private boolean isWaitingForResume(final String apNodeFdn) {
        return isEntryTaskStarted(getStatusEntry(apNodeFdn, WAITING_FOR_RESUME));
    }

    private void autoSetCompletedForHardwareReplacementLMT(final ManagedObject apNodeMo, final NodeIntegrationStatusEvent event) {
        final String apNodeFdn = apNodeMo.getFdn();
        if (NODE_REBOOT_INITIATED.equals(event.getMessage()) && isHardwareReplaceNode(apNodeMo) && isLmtIntegration(apNodeFdn)
                && isWaitingForResume(apNodeFdn)) {
            final StatusEntry startSoftwareEntry = getStatusEntry(apNodeFdn, StatusEntryNames.NODE_STARTING_SOFTWARE.toString());
            if ((startSoftwareEntry != null) && (isEntryTaskCompleted(startSoftwareEntry))) {
                return;
            }
            setTaskCompleted(apNodeFdn, event);
        }
    }
}
