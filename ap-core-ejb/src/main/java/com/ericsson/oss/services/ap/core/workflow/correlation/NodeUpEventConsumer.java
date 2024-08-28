/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.workflow.correlation;

import static com.ericsson.oss.services.ap.api.status.State.INTEGRATION_STARTED;
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_STARTED;
import static com.ericsson.oss.services.ap.api.status.State.ORDER_STARTED;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;

import java.util.Date;
import java.util.Iterator;

import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.eventbus.model.annotation.Modeled;
import com.ericsson.oss.itpf.sdk.recording.EventLevel;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.mediation.nodediscovery.event.NodeDiscoveryNodeUpEvent;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.api.status.StatusEntryNames;
import com.ericsson.oss.services.ap.api.status.StatusEntryProgress;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.workflow.BusinessKeyGenerator;
import com.ericsson.oss.services.ap.common.workflow.messages.NodeUpMessage;
import com.ericsson.oss.services.ap.core.status.NodeStatusMoUpdater;
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;

/**
 * This class is responsible for consuming a Node Up message received from a mediation flow.
 */
@Singleton
@LocalBean
public class NodeUpEventConsumer {

    private static final String NODE_UP_EVENT_URN = "//global/NodeDiscoveryNodeUpChannel/1.0.0";

    @EServiceRef
    private WorkflowInstanceServiceLocal wfsInstanceService;

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private NodeStatusMoUpdater nodeStatusMoUpdater;

    @Inject
    private Logger logger;

    @Inject
    private SystemRecorder recorder;

    @Inject
    private StatusEntryManagerLocal statusEntryManager;

    /**
     * This method is responsible for consuming a nodeUp message received from mediation flow.
     *
     * <b>Note:</b> VNF nodes will not be set to INTEGRATION_STARTED as the order workflow is not completed when the notification is received.
     *
     * @param nodeUpEvent
     *            the event with node up details
     */
    @Lock(LockType.READ)
    @Asynchronous
    public void listenForNodeUpEvents(@Observes @Modeled(eventUrn = NODE_UP_EVENT_URN) final NodeDiscoveryNodeUpEvent nodeUpEvent) {
        final String nodeName = nodeUpEvent.getNodeName();
        logger.info("NodeUp notification received in consumer for node {}", nodeName);
        final ManagedObject apNodeMo = getApNodeMo(nodeName);
        if (apNodeMo != null && !isNodeUpNotificationReceived(apNodeMo)) {
            setIntegrationStartedIfRequired(apNodeMo);
            setMigrationStarted(apNodeMo);
            updateApplyingConfigurationEvent(apNodeMo.getFdn());
            setSendingNodeUpTaskToCompletedIfRequired(apNodeMo);
            resumeWorkflow(nodeName);
        }
    }

    private void setMigrationStarted(ManagedObject apNodeMo) {
        if(isMigrationNode(apNodeMo) && !isMigrationRollback(apNodeMo)){
            logger.info("In NodeUp Event consumer and the state is set to Migration Started and node name is {} ", apNodeMo.getName());
            recorder.recordEvent("In NodeUp Event consumer and the state is set to Migration Started", EventLevel.COARSE, "NODE_DISCOVERY", apNodeMo.getName(), "Setting migration started state");
            nodeStatusMoUpdater.setState(apNodeMo.getFdn(), NodeStatusAttribute.STATE.toString(), MIGRATION_STARTED.name());
        }
    }

    private ManagedObject getApNodeMo(final String nodeName) {
        final Iterator<ManagedObject> apNodeMos = dpsQueries.findMoByName(nodeName, MoType.NODE.toString(), AP.toString()).execute();
        if (apNodeMos.hasNext()) {
            return apNodeMos.next();
        }
        logger.debug("Ignoring NodeUp notification, node {} is not in AP model", nodeName);
        return null;
    }

    private boolean isNodeUpNotificationReceived(final ManagedObject apNodeMo) {
        final StatusEntry nodeUpStatusEntry = statusEntryManager.getStatusEntryByName(apNodeMo.getFdn(), StatusEntryNames.NODE_UP.toString());
        if (nodeUpStatusEntry != null) {
            final String taskProgress = nodeUpStatusEntry.getTaskProgress();
            if (StatusEntryProgress.RECEIVED.toString().equals(taskProgress)) {
                logger.debug("Ignoring NodeUp notification, NodeUp notification has already been received for node {}", apNodeMo.getFdn());
                return true;
            }
        }
        return false;
    }

    private void setIntegrationStartedIfRequired(final ManagedObject apNodeMo) {
        if (isNodeInValidStateForStatusUpdate(apNodeMo) && !isNodeHardwareReplace(apNodeMo) && !isMigrationNode(apNodeMo)) {
            nodeStatusMoUpdater.setState(apNodeMo.getFdn(), NodeStatusAttribute.STATE.toString(), INTEGRATION_STARTED.name());
        }
    }

    private static boolean isNodeInValidStateForStatusUpdate(final ManagedObject apNodeMo) {
        final String nodeState = getNodeState(apNodeMo);
        return !(INTEGRATION_STARTED.toString().equals(nodeState) || ORDER_STARTED.toString().equals(nodeState));
    }

    private static boolean isMigrationNode(final ManagedObject apNodeMo) {
        if (apNodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString()) != null) {
            return (boolean) apNodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString());
        }
        return false;
    }

    private static boolean isMigrationRollback(final ManagedObject apNodeMo) {
        if (apNodeMo.getAttribute(NodeAttribute.IS_ROLLBACK.toString()) != null) {
            return (boolean) apNodeMo.getAttribute(NodeAttribute.IS_ROLLBACK.toString());
        }
        return false;
    }

    private static final String getNodeState(final ManagedObject apNodeMo) {
        final ManagedObject apNodeStatusMo = apNodeMo.getChild(MoType.NODE_STATUS + "=1");
        return apNodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString());
    }

    private static boolean isNodeHardwareReplace(final ManagedObject apNodeMo) {
        return (boolean) apNodeMo.getAttribute("isHardwareReplaceNode") == true;
    }

    private void setSendingNodeUpTaskToCompletedIfRequired(final ManagedObject apNodeMo) {
        final StatusEntry nodeSendingNodeUpStatusEntry = statusEntryManager.getStatusEntryByName(apNodeMo.getFdn(),
                StatusEntryNames.NODE_SENDING_NODE_UP.toString());
        if (nodeSendingNodeUpStatusEntry != null) {
            nodeStatusMoUpdater.addOrUpdateEntry(apNodeMo.getFdn(), nodeSendingNodeUpStatusEntry.getTaskName(), StatusEntryProgress.COMPLETED, "");
        }
    }

    private void resumeWorkflow(final String nodeName) {
        recorder.recordEvent("NodeUp message received in consumer", EventLevel.COARSE, "NODE_DISCOVERY", nodeName, new Date().toString());
        try {
            correlateMessage(nodeName);
        } catch (final WorkflowMessageCorrelationException e) {
            logger.warn("Error correlating the NodeUp message for node {}: {}", nodeName, e.getMessage(), e);
        }
    }

    private void correlateMessage(final String nodeName) throws WorkflowMessageCorrelationException {
        final String businessKey = BusinessKeyGenerator.generateBusinessKeyFromNodeName(nodeName);
        wfsInstanceService.correlateMessage(NodeUpMessage.getMessageKey(), businessKey, NodeUpMessage.getMessageVariables());
    }

    /**
     * For nodes that only support Integration Events sent by the node.
     * StatusEntry will only be updated if {@link StatusEntryNames.NODE_APPLYING_CONFIGURATION} notification exist for current node integration.
     * Set {@link StatusEntryNames.NODE_APPLYING_CONFIGURATION} event to completed once Node Up has been received if not already set.
     * @param apNodeFdn
     *          The AP Node FDN
     */
    private void updateApplyingConfigurationEvent(final String apNodeFdn){
        final StatusEntry statusEntry = statusEntryManager.getStatusEntryByName(apNodeFdn, StatusEntryNames.NODE_APPLYING_CONFIGURATION.toString());
        if ((statusEntry != null) && (statusEntry.getTaskProgress() != StatusEntryProgress.COMPLETED.toString())){
           statusEntryManager.taskCompleted(apNodeFdn, StatusEntryNames.NODE_APPLYING_CONFIGURATION.toString());
        }
    }
}
