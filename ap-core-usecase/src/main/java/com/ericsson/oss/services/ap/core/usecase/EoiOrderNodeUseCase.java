package com.ericsson.oss.services.ap.core.usecase;

import static com.ericsson.oss.services.ap.api.status.State.READY_FOR_EOI_INTEGRATION;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_TYPE;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException;
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.workflow.ActivityType;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * Starts the order integration workflow for a node.
 */
@UseCase(name = UseCaseName.EOI_ORDER_NODE) //usecaseName should be EOI_INTEGRATE_NODE or EOI_ORDER_NODE
public class EoiOrderNodeUseCase extends AbstractWorkflowExecutableUseCase {

    private static final Map<String, StateTransitionEvent> nodeStateStartEventMap = new HashMap<>();
    private static final Map<String, StateTransitionEvent> nodeStateFailedEventMap = new HashMap<>();
    private static final Map<String, ActivityType> nodeStateActivityTypeMap = new HashMap<>();
    private static final Map<String, String> intermediateValues = new HashMap<>();

    static {
        nodeStateStartEventMap.put(READY_FOR_EOI_INTEGRATION.name(), StateTransitionEvent.EOI_INTEGRATION_STARTED);

        nodeStateFailedEventMap.put(READY_FOR_EOI_INTEGRATION.name(), StateTransitionEvent.EOI_INTEGRATION_FAILED);

        nodeStateActivityTypeMap.put(READY_FOR_EOI_INTEGRATION.name(), ActivityType.EOI_INTEGRATION_ACTIVITY);
    }

    /**
     * Executes the usecase.
     *
     * @param nodeFdn
     *            the FDN of the AP node
     */

    public void execute(final String nodeFdn, final String baseUrl, final String sessionId) {
        intermediateValues.put("baseUrl", baseUrl);
        intermediateValues.put("sessionId", sessionId);
        execute(nodeFdn);
    }

    @Override
    public void execute(final String nodeFdn) {
        final ManagedObject nodeStatusMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(getNodeStatusMoFdn(nodeFdn));
        final String nodeStatusMoState = nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString());
        try {
            stateTransitionManager.validateAndSetNextState(nodeFdn, getStartEvent(nodeStatusMoState));
            startWorkflow(nodeFdn, intermediateValues.get("baseUrl"), intermediateValues.get("sessionId"));
        } catch (final InvalidNodeStateException e) {
            throw e;
        } catch (final Exception e) {
            stateTransitionManager.validateAndSetNextState(nodeFdn, getFailedEvent(nodeStatusMoState));
            throw e;
        }
    }

    /**
     * Starts the order integration workflow for the specified EOI node.
     *
     * @param nodeFdn
     *            the FDN of the node in AP model
     */

    private void startWorkflow(final String nodeFdn, final String baseUrl, final String sessionId) {
        final String workflowName = getWorkflowName(nodeFdn);
        workflowCleanUpOperations.cancelWorkflowInstanceIfItAlreadyExists(nodeFdn);
        final String workflowId = executeEoiWorkflow(workflowName, nodeFdn, baseUrl, sessionId);
        workflowInstanceIdUpdater.update(workflowId, nodeFdn);
        logger.debug("Execution of workflow instance {} with ID {} successfully started for node {}", workflowName, workflowId, nodeFdn);
    }

    private String getWorkflowName(final String nodeFdn) {
        final ManagedObject nodeMo = getNodeMo(nodeFdn);
        final String nodeType = nodeMo.getAttribute(NODE_TYPE.toString());
        final String internalNodeType = nodeTypeMapper.getInternalEjbQualifier(nodeType);
        return getApWorkflowService(internalNodeType).getEoiIntegrationWorkflow();
    }

    private StateTransitionEvent getStartEvent(final String nodeStatusMoState) {
        StateTransitionEvent stateTransitionEvent = nodeStateStartEventMap.get(nodeStatusMoState);
        if (stateTransitionEvent == null) {
            stateTransitionEvent = StateTransitionEvent.EOI_INTEGRATION_STARTED;
        }

        return stateTransitionEvent;
    }
    private StateTransitionEvent getFailedEvent(final String nodeStatusMoState) {
        StateTransitionEvent stateTransitionEvent = nodeStateFailedEventMap.get(nodeStatusMoState);
        if (stateTransitionEvent == null) {
            stateTransitionEvent = StateTransitionEvent.EOI_INTEGRATION_FAILED;
        }

        return stateTransitionEvent;
    }


    private static String getNodeStatusMoFdn(final String apNodeFdn) {
        return apNodeFdn + "," + MoType.NODE_STATUS.toString() + "=1";
    }
}
