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
package com.ericsson.oss.services.ap.core.usecase;

import static com.ericsson.oss.services.ap.api.status.State.EXPANSION_FAILED;
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_FAILED;
import static com.ericsson.oss.services.ap.api.status.State.READY_FOR_EXPANSION;
import static com.ericsson.oss.services.ap.api.status.State.READY_FOR_HARDWARE_REPLACE;
import static com.ericsson.oss.services.ap.api.status.State.READY_FOR_ORDER;
import static com.ericsson.oss.services.ap.api.status.State.READY_FOR_PRE_MIGRATION;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_TYPE;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException;
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.model.ProjectAttribute;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.log.MRDefinition;
import com.ericsson.oss.services.ap.common.util.log.MRExecutionRecorder;
import com.ericsson.oss.services.ap.common.workflow.ActivityType;
import com.ericsson.oss.services.ap.common.workflow.recording.CommandRecorder;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

import javax.inject.Inject;

/**
 * Starts the order integration workflow for a node.
 */
@UseCase(name = UseCaseName.ORDER_NODE)
public class OrderNodeUseCase extends AbstractWorkflowExecutableUseCase {
    @Inject
    private MRExecutionRecorder recorder;

    private final CommandRecorder commandRecorder = new CommandRecorder();

    private static final Map<String, StateTransitionEvent> nodeStateStartEventMap = new HashMap<>();
    private static final Map<String, StateTransitionEvent> nodeStateFailedEventMap = new HashMap<>();
    private static final Map<String, ActivityType> nodeStateActivityTypeMap = new HashMap<>();

    static {
        nodeStateStartEventMap.put(READY_FOR_ORDER.name(), StateTransitionEvent.ORDER_STARTED);
        nodeStateStartEventMap.put(READY_FOR_EXPANSION.name(), StateTransitionEvent.EXPANSION_STARTED);
        nodeStateStartEventMap.put(EXPANSION_FAILED.name(), StateTransitionEvent.EXPANSION_STARTED);
        nodeStateStartEventMap.put(READY_FOR_HARDWARE_REPLACE.name(), StateTransitionEvent.HARDWARE_REPLACE_STARTED);
        nodeStateStartEventMap.put(READY_FOR_PRE_MIGRATION.name(), StateTransitionEvent.PRE_MIGRATION_STARTED);
        nodeStateStartEventMap.put(PRE_MIGRATION_FAILED.name(), StateTransitionEvent.PRE_MIGRATION_STARTED);

        nodeStateFailedEventMap.put(READY_FOR_ORDER.name(), StateTransitionEvent.ORDER_FAILED);
        nodeStateFailedEventMap.put(READY_FOR_EXPANSION.name(), StateTransitionEvent.EXPANSION_FAILED);
        nodeStateFailedEventMap.put(EXPANSION_FAILED.name(), StateTransitionEvent.EXPANSION_FAILED);
        nodeStateFailedEventMap.put(READY_FOR_HARDWARE_REPLACE.name(), StateTransitionEvent.HARDWARE_REPLACE_FAILED);
        nodeStateFailedEventMap.put(READY_FOR_PRE_MIGRATION.name(), StateTransitionEvent.PRE_MIGRATION_FAILED);
        nodeStateFailedEventMap.put(PRE_MIGRATION_FAILED.name(), StateTransitionEvent.PRE_MIGRATION_FAILED);

        nodeStateActivityTypeMap.put(READY_FOR_ORDER.name(), ActivityType.GREENFIELD_ACTIVITY);
        nodeStateActivityTypeMap.put(READY_FOR_EXPANSION.name(), ActivityType.EXPANSION_ACTIVITY);
        nodeStateActivityTypeMap.put(EXPANSION_FAILED.name(), ActivityType.EXPANSION_ACTIVITY);
        nodeStateActivityTypeMap.put(READY_FOR_HARDWARE_REPLACE.name(), ActivityType.HARDWARE_REPLACE_ACTIVITY);
        nodeStateActivityTypeMap.put(READY_FOR_PRE_MIGRATION.name(), ActivityType.MIGRATION_ACTIVITY);
        nodeStateActivityTypeMap.put(PRE_MIGRATION_FAILED.name(), ActivityType.MIGRATION_ACTIVITY);
    }

    /**
     * Executes the usecase.
     *
     * @param nodeFdn
     *            the FDN of the AP node
     */
    @Override
    public void execute(final String nodeFdn) {
        execute(nodeFdn, true, new NodeInfo());
    }

    /**
     * Starts the order integration workflow for the specified node.
     *
     * @param nodeFdn
     *            the FDN of the node in AP model
     * @param validationRequired
     *            is validation required
     * @param nodeInfo
     *            nodeInfo object containing node configuration data
     */
    public void execute(final String nodeFdn, final boolean validationRequired, final NodeInfo nodeInfo) {
        final ManagedObject nodeStatusMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(getNodeStatusMoFdn(nodeFdn));
        final String nodeStatusMoState = nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString());
        try {
            stateTransitionManager.validateAndSetNextState(nodeFdn, getStartEvent(nodeStatusMoState));
            updateActivityFromState(nodeInfo, nodeStatusMoState);
            startWorkflow(nodeFdn, validationRequired, nodeInfo);
        } catch (final InvalidNodeStateException e) {
            throw e;
        } catch (final Exception e) {
            stateTransitionManager.validateAndSetNextState(nodeFdn, getFailedEvent(nodeStatusMoState));
            throw e;
        }
    }

    private void startWorkflow(final String nodeFdn, final boolean validationRequired, final NodeInfo nodeInfo) {
        final String workflowName = getWorkflowName(nodeFdn, nodeInfo.getActivity());
        if (workflowName.equals(ActivityType.MIGRATION_ACTIVITY.getActivityName())){
            recorder.recordMRExecution(MRDefinition.AP_INTEGRATION_MIGRATION);
        }
        workflowCleanUpOperations.cancelWorkflowInstanceIfItAlreadyExists(nodeFdn);
        final String workflowId = executeWorkflow(workflowName, nodeFdn, validationRequired, nodeInfo);
        workflowInstanceIdUpdater.update(workflowId, nodeFdn);

        final ManagedObject projectMo = getNodeMo(nodeFdn).getParent();
        commandRecorder.activityStarted(nodeInfo.getActivity().getActivityName(), projectMo.getAttribute(ProjectAttribute.GENERATED_BY.toString()),
                false);
        logger.debug("Execution of workflow instance {} with ID {} successfully started for node {}", workflowName, workflowId, nodeFdn);
    }

    private String getWorkflowName(final String nodeFdn, final ActivityType type) {
        final ManagedObject nodeMo = getNodeMo(nodeFdn);
        final String nodeType = nodeMo.getAttribute(NODE_TYPE.toString());
        final String internalNodeType = nodeTypeMapper.getInternalEjbQualifier(nodeType);

        if (ActivityType.EXPANSION_ACTIVITY.equals(type)) {
            return getApWorkflowService(internalNodeType).getExpansionOrderWorkflowName();
        } else if (ActivityType.HARDWARE_REPLACE_ACTIVITY.equals(type)) {
            return getApWorkflowService(internalNodeType).getHardwareReplaceWorkflowName();
        } else if (ActivityType.MIGRATION_ACTIVITY.equals(type)) {
            return getApWorkflowService(internalNodeType).getMigrationWorkflowName();
        }  else {
            return getApWorkflowService(internalNodeType).getOrderWorkflowName();
        }
    }

    private static String getNodeStatusMoFdn(final String apNodeFdn) {
        return apNodeFdn + "," + MoType.NODE_STATUS.toString() + "=1";
    }

    private StateTransitionEvent getStartEvent(final String nodeStatusMoState) {
        StateTransitionEvent stateTransitionEvent = nodeStateStartEventMap.get(nodeStatusMoState);
        if (stateTransitionEvent == null) {
            stateTransitionEvent = StateTransitionEvent.ORDER_STARTED;
        }

        return stateTransitionEvent;
    }

    private StateTransitionEvent getFailedEvent(final String nodeStatusMoState) {
        StateTransitionEvent stateTransitionEvent = nodeStateFailedEventMap.get(nodeStatusMoState);
        if (stateTransitionEvent == null) {
            stateTransitionEvent = StateTransitionEvent.ORDER_FAILED;
        }

        return stateTransitionEvent;
    }

    private void updateActivityFromState(final NodeInfo nodeInfo, final String nodeStatusMoState) {
        final ActivityType activity = nodeStateActivityTypeMap.get(nodeStatusMoState);
        if (activity != null && nodeInfo.getActivity() != activity) {
            nodeInfo.setActivity(activity);
        }
    }
}
