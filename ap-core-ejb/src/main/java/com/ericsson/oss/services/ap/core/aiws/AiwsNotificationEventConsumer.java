/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.aiws;

import static com.ericsson.oss.services.ap.common.model.Namespace.AP;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.eventbus.model.annotation.Modeled;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.api.status.StatusEntryNames;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.model.ProjectAttribute;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.workflow.ActivityType;
import com.ericsson.oss.services.ap.common.workflow.BusinessKeyGenerator;
import com.ericsson.oss.services.ap.common.workflow.messages.AiwsMessage;
import com.ericsson.oss.services.ap.common.workflow.recording.CommandRecorder;
import com.ericsson.oss.services.ap.core.status.NodeStatusMoUpdater;
import com.ericsson.oss.services.model.autoprovisioning.AiwsNotification;
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;

/**
 * This class consumes messages sent from the Auto-Integration Web Service (AIWS) when a node contacts the web service to obtain the site install
 * file. The node's AP status is updated to reflect that this contact has been made.
 */
@Singleton
@LocalBean
public class AiwsNotificationEventConsumer {

    private static final EnumSet<State> VALID_STATES = EnumSet.of(State.ORDER_COMPLETED, State.BIND_STARTED, State.BIND_COMPLETED,
            State.HARDWARE_REPLACE_STARTED, State.HARDWARE_REPLACE_BIND_COMPLETED, State.INTEGRATION_FAILED, State.PRE_MIGRATION_BIND_STARTED,
            State.PRE_MIGRATION_BIND_COMPLETED, State.PRE_MIGRATION_COMPLETED, State.MIGRATION_FAILED);
    private static final String BIND_TYPE = "Bind Type: ";
    private static final String SERIAL_NUMBER = "Hardware Serial Number";
    private static final String NODE_NAME = "Node Name";

    private static final String AIWS_NOTIFICATION_EVENT_URN = "//global/AiwsNotificationChannel/1.0.0";

    @EServiceRef
    private WorkflowInstanceServiceLocal wfsInstanceService;

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private NodeStatusMoUpdater nodeStatusMoUpdater;

    @Inject
    private StatusEntryManagerLocal statusEntryManager;

    @Inject
    private SystemRecorder systemRecorder;

    @Inject
    private CommandRecorder commandRecorder;

    @Inject
    private Logger logger;

    /**
     * Consumes a notification from the AIWS and updates the node's AP status to reflect that the AIWS was contacted by the node to obtain the site
     * install file.
     *
     * @param aiwsNotification
     *            a notification from AIWS that contains a serial number or node name identifying the node.
     */
    @Lock(LockType.READ)
    @Asynchronous
    public void listenToAiwsNotifications(@Observes @Modeled(eventUrn = AIWS_NOTIFICATION_EVENT_URN) final AiwsNotification aiwsNotification) {
        logger.info("AIWS notification received");
        final ManagedObject apNodeMo = findApNodeByBindType(aiwsNotification);
        if (apNodeMo != null) {
            systemRecorder.recordEventData(CommandLogName.AIWS_DOWNLOAD_CONFIGURATION_FILE.toString(), getEventData(apNodeMo));
            final String nodeState = getNodeState(apNodeMo);
            if (isValidState(nodeState)) {
                try {
                    if (null == statusEntryManager.getStatusEntryByName(apNodeMo.getFdn(), StatusEntryNames.AIWS_NOTIFICATION.toString())) {
                        commandRecorder.activityStarted(getAcivityName(apNodeMo),
                                apNodeMo.getParent().getAttribute(ProjectAttribute.GENERATED_BY.toString()),
                                true);
                    }

                    statusEntryManager.notificationReceived(
                            apNodeMo.getFdn(), StatusEntryNames.AIWS_NOTIFICATION.toString(), BIND_TYPE + aiwsNotification.getBindType());
                    if (isHardwareReplace(apNodeMo)) {
                        correlateMessage(apNodeMo.getName());
                    } else if (isMigrationNode(apNodeMo)) {
                        setMigrationStartedState(nodeState, apNodeMo.getFdn());
                    } else {
                        setIntegrationStartedState(nodeState, apNodeMo.getFdn());
                    }
                } catch (final WorkflowMessageCorrelationException e) {
                    logger.error("Error correlating AIWS message for node {}: {}", apNodeMo.getName(), e.getMessage(), e);
                }
            }
        }
    }

    private void setMigrationStartedState(final String nodeState, final String fdn) {
        if (!State.MIGRATION_STARTED.toString().equals(nodeState)) {
            nodeStatusMoUpdater.setState(fdn, NodeStatusAttribute.STATE.toString(), State.MIGRATION_STARTED.name());
        }
    }

    private static boolean isMigrationNode(final ManagedObject apNodeMo) {
        if (apNodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString()) != null) {
            return (boolean) apNodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString());
        }
        return false;
    }

    private boolean isHardwareReplace(final ManagedObject apNodeMo){
        return apNodeMo.getAttribute(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString());
    }

    private static String getNodeState(final ManagedObject apNodeMo) {
        final ManagedObject apNodeStatusMo = apNodeMo.getChild(MoType.NODE_STATUS + "=1");
        return apNodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString());
    }

    private Map<String, Object> getEventData(final ManagedObject apNodeMo) {
        final Map<String, Object> eventData = new HashMap<>();
        eventData.put("NODE_NAME", apNodeMo.getName());
        eventData.put("FDN", apNodeMo.getFdn());
        eventData.put("PROJECT_NAME", apNodeMo.getParent().getName());

        return eventData;
    }

    private void correlateMessage(final String nodeName) throws WorkflowMessageCorrelationException {
        logger.debug("Correlating AIWS message for hardware replace node {}", nodeName);
        final String businessKey = BusinessKeyGenerator.generateBusinessKeyFromNodeName(nodeName);
        wfsInstanceService.correlateMessage(AiwsMessage.getMessageKey(), businessKey);
    }

    private ManagedObject findApNodeByBindType(final AiwsNotification aiwsNotification) {
        final String bindType = aiwsNotification.getBindType();
        final String bindValue = bindType.equals(SERIAL_NUMBER) ? aiwsNotification.getHardwareSerialNumber() : aiwsNotification.getNodeName();
        Iterator<ManagedObject> apNodeIterator;
        switch (bindType) {
            case SERIAL_NUMBER:
                logger.info("AIWS notification received for hardware serial number: {} and bind type: {}", bindValue, bindType);
                apNodeIterator =  dpsQueries.findMosWithAttributeValue(NodeAttribute.HARDWARE_SERIAL_NUMBER.toString(), bindValue,
                        Namespace.AP.toString(), MoType.NODE.toString()).execute();
                return (apNodeIterator.hasNext() ? apNodeIterator.next() : null);

            case NODE_NAME:
                logger.info("AIWS notification received for node name: {} and bind type: {}", bindValue, bindType);
                apNodeIterator =  dpsQueries.findMoByName(bindValue, MoType.NODE.toString(), AP.toString()).execute();
                return (apNodeIterator.hasNext() ? apNodeIterator.next() : null);

            default:
                logger.error("Could not find AP Node for bind value: {} , and bind type: {}",
                        bindValue, bindType);
                return null;
        }
    }

    private void setIntegrationStartedState(final String nodeState, final String apNodeFdn) {
        if (!State.INTEGRATION_STARTED.toString().equals(nodeState)){
            nodeStatusMoUpdater.setState(apNodeFdn, NodeStatusAttribute.STATE.toString(), State.INTEGRATION_STARTED.name());
        }
    }

    private static boolean isValidState(final String nodeState) {
        for (final State state : VALID_STATES) {
            if (state.toString().equals(nodeState)) {
                return true;
            }
        }
        return false;
    }

    private String getAcivityName(final ManagedObject apNodeMo) {
        if (isHardwareReplace(apNodeMo)) {
            return ActivityType.HARDWARE_REPLACE_ACTIVITY.getActivityName();
        } else if (isMigrationNode(apNodeMo)) {
            return ActivityType.MIGRATION_ACTIVITY.getActivityName();
        }
        return ActivityType.GREENFIELD_ACTIVITY.getActivityName();
    }
}
