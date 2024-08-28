/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.status;

import static com.ericsson.oss.services.ap.api.status.StatusEntryProgress.COMPLETED;
import static com.ericsson.oss.services.ap.api.status.StatusEntryProgress.FAILED;
import static com.ericsson.oss.services.ap.api.status.StatusEntryProgress.RECEIVED;
import static com.ericsson.oss.services.ap.api.status.StatusEntryProgress.STARTED;
import static com.ericsson.oss.services.ap.api.status.StatusEntryProgress.WAITING;
import static java.lang.String.format;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException;
import com.ericsson.oss.services.ap.api.exception.StatusEntryUpdateException;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.api.status.StatusEntryNames;
import com.ericsson.oss.services.ap.api.status.StatusEntryProgress;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.util.cdi.Transactional;
import com.ericsson.oss.services.ap.common.util.cdi.Transactional.TxType;
import com.ericsson.oss.services.ap.common.util.log.TaskOutputLogger;

/**
 * Handles updating the AP <code>NodeStatus</code> MO.
 * <p>
 * This class should only be called from StatusEntryManagerEjb or NodeStateTransitionManagerEjb which is controlling the transaction scope for all
 * status update operations.
 */
public class NodeStatusMoUpdater {

    private static final int VALID_ENTRY_INDEX = -1;
    private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        }
    };

    private static final List<String> VARIABLE_STATUS_ENTRIES = Arrays.asList(StatusEntryNames.AIWS_NOTIFICATION.toString(),
        StatusEntryNames.NODE_ESTABLISHING_CONTACT.toString(), StatusEntryNames.NODE_DOWNLOADING_CONFIGURATIONS.toString(),
        StatusEntryNames.NODE_INSTALLING_SOFTWARE.toString(),
        StatusEntryNames.NODE_STARTING_SOFTWARE.toString(), StatusEntryNames.NODE_APPLYING_CONFIGURATION.toString(),
        StatusEntryNames.NODE_SENDING_NODE_UP.toString(), StatusEntryNames.NODE_UP.toString(),
        StatusEntryNames.ENROLL_IPSEC_CERTIFICATE.toString(), StatusEntryNames.ENROLL_OAM_CERTIFICATE.toString(),
        StatusEntryNames.SYNC_NODE_NOTIFICATION.toString());

    private static final List<String> NON_VARIABLE_NOTIFICATION_ENTRIES = Arrays.asList(StatusEntryNames.EXPANSION_NOTIFICATION.toString());

    private static final String NODE_UP_NOTIFICATION_TASK = StatusEntryNames.NODE_UP.toString();

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final StatusEntryFormatter formatter = new StatusEntryFormatter();

    @Inject
    private DpsOperations dps;

    @Inject
    private TaskOutputLogger taskOutputLogger;

    /**
     * Update an existing status entry when the task is ongoing. In other words, the {@link StatusEntryProgress} of the entry is either
     * {@link StatusEntryProgress#STARTED}, {@link StatusEntryProgress#COMPLETED} or {@link StatusEntryProgress#WAITING}.
     * <p>
     * Otherwise, add a new status entry.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param statusEntryName
     *            the name of the task to add or update
     * @param statusEntryProgress
     *            the progress of the entry
     * @param additionalInfo
     *            the additional information for the given task
     */
    @Transactional(txType = TxType.REQUIRES_NEW)
    public void addOrUpdateEntry(final String apNodeFdn, final String statusEntryName, final StatusEntryProgress statusEntryProgress,
            final String additionalInfo) {
        final ManagedObject nodeStatusMo = readNodeStatusMo(apNodeFdn);
        if (nodeStatusMo != null) {
            final List<String> nodeStatusEntries = readNodeStatusEntries(nodeStatusMo);

            final String timeStamp = DATE_FORMAT.get().format(new Date());
            final StatusEntry updatedStatusEntry = new StatusEntry(statusEntryName, statusEntryProgress.toString(), timeStamp, additionalInfo);
            final List<String> updatedEntries = updateOngoingStatusEntry(updatedStatusEntry, nodeStatusEntries);
            taskOutputLogger.log(apNodeFdn, statusEntryName, statusEntryProgress, timeStamp, additionalInfo);
            updateNodeStatusMo(nodeStatusMo, NodeStatusAttribute.STATUS_ENTRIES.toString(), updatedEntries);
        }
    }

    /**
     * Update an existing status entry task. In other words, the {@link StatusEntryProgress} of the entry is either
     * {@link StatusEntryProgress#FAILED} or {@link StatusEntryProgress#COMPLETED} or {@link StatusEntryProgress#STARTED}.
     * <p>
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param statusEntryName
     *            the name of the task to add or update
     * @param statusEntryProgress
     *            the taskProgress to set
     * @param additionalInfo
     *            the additional information for the given task
     */
    @Transactional(txType = TxType.REQUIRES_NEW)
    public void updateExistingEntry(final String apNodeFdn, final String statusEntryName, final StatusEntryProgress statusEntryProgress,
            final String additionalInfo) {
        final ManagedObject nodeStatusMo = readNodeStatusMo(apNodeFdn);
        if (nodeStatusMo != null) {
            final List<String> nodeStatusEntries = readNodeStatusEntries(nodeStatusMo);

            final String timeStamp = DATE_FORMAT.get().format(new Date());
            final StatusEntry updatedStatusEntry = new StatusEntry(statusEntryName, statusEntryProgress.toString(), timeStamp, additionalInfo);
            final List<String> updatedEntries = updateExistingStatusEntry(updatedStatusEntry, nodeStatusEntries);
            taskOutputLogger.log(apNodeFdn, statusEntryName, statusEntryProgress, timeStamp, additionalInfo);
            updateNodeStatusMo(nodeStatusMo, NodeStatusAttribute.STATUS_ENTRIES.toString(), updatedEntries);
        }
    }

    /**
     * Update an existing status entry when the task is in a started state. In other words, the {@link StatusEntryProgress} of the entry is in
     * {@link StatusEntryProgress#STARTED}.
     * <p>
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param statusEntryName
     *            the name of the task to add or update
     * @param statusEntryProgress
     *            the taskProgress to set
     * @param additionalInfo
     *            the additional information for the given task
     */
    @Transactional(txType = TxType.REQUIRES_NEW)
    public void updateExistingStartedEntry(final String apNodeFdn, final String statusEntryName, final StatusEntryProgress statusEntryProgress,
                                           final String additionalInfo) {
        final ManagedObject nodeStatusMo = readNodeStatusMo(apNodeFdn);
        if (nodeStatusMo != null) {
            final List<String> nodeStatusEntries = readNodeStatusEntries(nodeStatusMo);

            final String timeStamp = DATE_FORMAT.get().format(new Date());
            final StatusEntry updatedStatusEntry = new StatusEntry(statusEntryName, statusEntryProgress.toString(), timeStamp, additionalInfo);
            final List<String> updatedEntries = updateStartedStatusEntry(updatedStatusEntry, nodeStatusEntries);
            taskOutputLogger.log(apNodeFdn, statusEntryName, statusEntryProgress, timeStamp, additionalInfo);
            updateNodeStatusMo(nodeStatusMo, NodeStatusAttribute.STATUS_ENTRIES.toString(), updatedEntries);
        }
    }

    /**
     * Updates the "Additional Information" of an existing status entry task from the <code>NodeStatus</code> MO <i>statusEntry</i> attribute for an
     * AP node.
     * <p>
     * Does not change the "Task Progress".
     *
     * @param apNodeFdn
     *            the FDN of the node
     * @param taskName
     *            the name of the task
     * @param newAdditionalInfo
     *            the additional information to update the status entry with
     */
    @Transactional(txType = TxType.REQUIRES_NEW)
    public void updateAdditionalInformation(final String apNodeFdn, final String taskName, final String newAdditionalInfo) {
        final ManagedObject nodeStatusMo = readNodeStatusMo(apNodeFdn);
        if (nodeStatusMo != null) {
            final List<String> nodeStatusEntries = readNodeStatusEntries(nodeStatusMo);
            final StatusEntry statusEntry = findStatusEntryByTaskName(nodeStatusEntries, taskName);
            if (statusEntry == null) {
                logger.warn("No status entry with name {} found for {}", taskName, apNodeFdn);
                return;
            }

            final String timeStamp = DATE_FORMAT.get().format(new Date());
            final StatusEntry updatedStatusEntry = new StatusEntry(taskName, statusEntry.getTaskProgress(), timeStamp, newAdditionalInfo);
            final List<String> updatedEntries = updateOngoingStatusEntry(updatedStatusEntry, nodeStatusEntries);
            updateNodeStatusMo(nodeStatusMo, NodeStatusAttribute.STATUS_ENTRIES.toString(), updatedEntries);
        }
    }

    /**
     * Clears all status entries from the <code>NodeStatus</code> MO <i>statusEntry</i> attribute for an AP node.
     *
     * @param apNodeFdn
     *            the FDN of the node to update
     */
    @Transactional(txType = TxType.REQUIRES_NEW)
    public void clearAllEntries(final String apNodeFdn) {
        final ManagedObject nodeStatusMo = readNodeStatusMo(apNodeFdn);
        if (nodeStatusMo != null) {
            updateNodeStatusMo(nodeStatusMo, NodeStatusAttribute.STATUS_ENTRIES.toString(), new ArrayList<String>());
        }
    }

    @Transactional(txType = TxType.REQUIRES_NEW)
    public void setState(final String apNodeFdn, final String attrName, final Object attrValue) {
        final ManagedObject nodeStatusMo = readNodeStatusMo(apNodeFdn);
        if (nodeStatusMo != null) {
            updateNodeStatusMo(nodeStatusMo, attrName, attrValue);
        }
    }

    @Transactional(txType = TxType.REQUIRES_NEW)
    public void validateAndSetNextState(final String apNodeFdn, final StateTransitionEvent event) {
        final ManagedObject nodeStatusMo = readNodeStatusMo(apNodeFdn);
        if (nodeStatusMo != null) {
            final String currentState = nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString());

            logger.info("Change event for node {}, {}->{}", apNodeFdn, currentState, event);
            final State nextState = getNextState(currentState, event);

            if (nextState == null) {
                throw new InvalidNodeStateException(String.format("No valid transition for event %s from current state %s", event, currentState),
                        currentState);
            }

            logger.info("Change state for node {}, {}->{}", apNodeFdn, currentState, nextState);
            updateNodeStatusMo(nodeStatusMo, NodeStatusAttribute.STATE.toString(), nextState.name());
        }
    }

    private static State getNextState(final String currentState, final StateTransitionEvent event) {
        for (final StateTransition transition : NodeStateTransitions.getTransitions()) {
            final String fromState = transition.from();
            if ((fromState.equals(currentState) || "*".equals(fromState)) && transition.getEvent().equals(event.name())) {
                return State.valueOf(transition.to());
            }
        }
        return null;
    }

    private static List<String> readNodeStatusEntries(final ManagedObject nodeStatusMo) {
        final List<String> immutableStatusEntries = nodeStatusMo.getAttribute(NodeStatusAttribute.STATUS_ENTRIES.toString());
        final List<String> mutableStatusEntries = new ArrayList<>(immutableStatusEntries.size());

        for (final String statusEntry : immutableStatusEntries) {
            mutableStatusEntries.add(statusEntry);
        }
        return mutableStatusEntries;
    }

    private final List<String> updateOngoingStatusEntry(final StatusEntry updatedStatusEntry, final List<String> nodeStatusEntries) {
        final String updatedStatus = formatter.toJsonString(updatedStatusEntry);
        final int indexOfOngoingEntryToUpdate = getOngoingStatusEntryIndexByEntryName(updatedStatusEntry.getTaskName(), nodeStatusEntries);

        if (VALID_ENTRY_INDEX == indexOfOngoingEntryToUpdate) {
            nodeStatusEntries.add(updatedStatus);
        } else {
            if (NODE_UP_NOTIFICATION_TASK.equals(updatedStatusEntry.getTaskName()) || VARIABLE_STATUS_ENTRIES.contains(updatedStatusEntry.getTaskName())) {
                nodeStatusEntries.remove(indexOfOngoingEntryToUpdate);
                nodeStatusEntries.add(updatedStatus);
            } else {
                nodeStatusEntries.set(indexOfOngoingEntryToUpdate, updatedStatus);
            }
        }
        return nodeStatusEntries;
    }

    private final List<String> updateExistingStatusEntry(final StatusEntry updatedStatusEntry, final List<String> nodeStatusEntries) {
        final String updatedStatus = formatter.toJsonString(updatedStatusEntry);
        final int indexOfEndStateEntryToUpdate = getStatusEntryIndexByEntryName(updatedStatusEntry.getTaskName(), nodeStatusEntries);

        if (VALID_ENTRY_INDEX == indexOfEndStateEntryToUpdate) {
            nodeStatusEntries.add(updatedStatus);
        } else {
            if (VARIABLE_STATUS_ENTRIES.contains(updatedStatusEntry.getTaskName())) {
                nodeStatusEntries.remove(indexOfEndStateEntryToUpdate);
                nodeStatusEntries.add(updatedStatus);
            } else {
                nodeStatusEntries.set(indexOfEndStateEntryToUpdate, updatedStatus);
            }
        }
        return nodeStatusEntries;
    }

    private final List<String> updateStartedStatusEntry(final StatusEntry updatedStatusEntry, final List<String> nodeStatusEntries) {
        final String updatedStatus = formatter.toJsonString(updatedStatusEntry);
        final int indexOfEndStateEntryToUpdate = getStartedStatusEntryIndexByEntryName(updatedStatusEntry.getTaskName(), nodeStatusEntries);

        if (VALID_ENTRY_INDEX == indexOfEndStateEntryToUpdate) {
            nodeStatusEntries.add(updatedStatus);
        } else {
            if (VARIABLE_STATUS_ENTRIES.contains(updatedStatusEntry.getTaskName())) {
                nodeStatusEntries.remove(indexOfEndStateEntryToUpdate);
                nodeStatusEntries.add(updatedStatus);
            } else {
                nodeStatusEntries.set(indexOfEndStateEntryToUpdate, updatedStatus);
            }
        }
        return nodeStatusEntries;
    }

    private int getOngoingStatusEntryIndexByEntryName(final String entryName, final List<String> statusEntries) {
        for (int i = statusEntries.size() - 1; i >= 0; --i) { // Reverse iteration, so the most recent task name is returned
            final StatusEntry entry = formatter.fromJsonString(statusEntries.get(i));
            if (entry.getTaskName().equals(entryName) && (isOngoingTask(entry.getTaskProgress()) || VARIABLE_STATUS_ENTRIES.contains(entryName))) {
                return i;
            }
        }
        return VALID_ENTRY_INDEX;
    }

    private int getStatusEntryIndexByEntryName(final String entryName, final List<String> statusEntries) {
        for (int i = statusEntries.size() - 1; i >= 0; --i) { // Reverse iteration, so the most recent task name is returned
            final StatusEntry entry = formatter.fromJsonString(statusEntries.get(i));
            if (entry.getTaskName().equals(entryName)) {
                if (isNonVariableNotificationNotInWaitingState(entryName, entry)) {
                    return VALID_ENTRY_INDEX;
                }
                if (isOngoingTask(entry.getTaskProgress()) && VARIABLE_STATUS_ENTRIES.contains(entryName)) {
                    return i;
                }
                if (isEndStateTask(entry.getTaskProgress()) || isNotification(entry.getTaskProgress())) {
                    return i;
                }
            }
        }
        return VALID_ENTRY_INDEX;
    }

    private boolean isNonVariableNotificationNotInWaitingState(final String entryName, final StatusEntry entry) {
        // need to check if existing non variable status entry is in WAITING progress, then the existing task should be updated. Otherwise a new entry is added
        return NON_VARIABLE_NOTIFICATION_ENTRIES.contains(entryName) && !entry.getTaskProgress().equals(WAITING.toString());
    }

    private int getStartedStatusEntryIndexByEntryName(final String entryName, final List<String> statusEntries) {
        for (int i = statusEntries.size() - 1; i >= 0; --i) { // Reverse iteration, so the most recent task name is returned
            final StatusEntry entry = formatter.fromJsonString(statusEntries.get(i));
            if (entry.getTaskName().equals(entryName)) {
                return i;
            }
        }
        return VALID_ENTRY_INDEX;
    }

    private StatusEntry findStatusEntryByTaskName(final List<String> statusEntries, final String taskName) {
        for (int i = statusEntries.size() - 1; i >= 0; --i) {
            final StatusEntry statusEntry = formatter.fromJsonString(statusEntries.get(i));
            if (statusEntry.getTaskName().equals(taskName)) {
                return statusEntry;
            }
        }
        return null;
    }

    private static boolean isOngoingTask(final String taskProgress) {
        return STARTED.toString().equals(taskProgress) || WAITING.toString().equals(taskProgress);
    }

    private static boolean isEndStateTask(final String taskProgress) {
        return FAILED.toString().equals(taskProgress) || COMPLETED.toString().equals(taskProgress);
    }

    private static boolean isNotification(final String taskProgress) {
        return RECEIVED.toString().equals(taskProgress) || WAITING.toString().equals(taskProgress);
    }

    private ManagedObject readNodeStatusMo(final String apNodeFdn) {
        final String nodeStatusMoFdn = apNodeFdn + "," + MoType.NODE_STATUS.toString() + "=1";
        final ManagedObject nodeStatusMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeStatusMoFdn);
        if (nodeStatusMo == null) {
            logger.warn("Cannot find {} MO", nodeStatusMoFdn);
        }
        return nodeStatusMo;
    }

    private void updateNodeStatusMo(final ManagedObject nodeStatusMo, final String attrName, final Object attrValue) {
        try {
            nodeStatusMo.setAttribute(attrName, attrValue);
            logger.debug("Updated {}, name={}, value={}", nodeStatusMo.getFdn(), attrName, attrValue);
        } catch (final Exception e) {
            final String errorMessage = format("Error updating %s, attrName -> %s, value -> %s. %s", nodeStatusMo.getFdn(), attrName, attrValue,
                    e.getMessage());
            logger.warn(errorMessage, e);
            throw new StatusEntryUpdateException(errorMessage);
        }
    }
}
