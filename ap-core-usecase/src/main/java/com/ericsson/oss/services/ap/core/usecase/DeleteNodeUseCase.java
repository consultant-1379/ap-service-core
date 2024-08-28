/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.ap.core.usecase.delete.DeleteSkipStateOptions;
import com.ericsson.oss.services.ap.core.usecase.delete.ApModelDeleter;
import com.ericsson.oss.services.ap.core.usecase.delete.DeleteNodeWorkflowHelper;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * UseCase to delete a single node.
 */
@UseCase(name = UseCaseName.DELETE_NODE)
public class DeleteNodeUseCase extends DeleteSkipStateOptions {

    private static final String VNF_INTERNAL_TYPE = "vnf";

    @Inject
    private DpsOperations dpsOperations;

    @Inject
    private DeleteNodeWorkflowHelper workflowHelper;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    @Inject
    private ApModelDeleter apModelDeleter;

    /**
     * Deletes the specified node along with all associated raw and generated files on the filesystem. If integration workflow is active it will be
     * cancelled. If the ignoreNetworkElement is true, then it will not delete the NetworkElement, otherwise, it will.
     *
     * @param nodeFdn
     *            the FDN of the AP node
     * @param ignoreNetworkElement
     *            ignore the deletion of NetworkElement
     */
    public void execute(final String nodeFdn, final boolean ignoreNetworkElement) {

        final String initialNodeState = getCurrentNodeState(nodeFdn);
        stateTransitionManager.validateAndSetNextState(nodeFdn, StateTransitionEvent.DELETE_STARTED);

        try {
            if (isExecuteDeleteWorkflow(initialNodeState)) {
                final boolean isVnfNode = isVnfNode(nodeFdn);
                final String dhcpClientId = workflowHelper.getWorkflowVariable(nodeFdn, AbstractWorkflowVariables.DHCP_CLIENT_ID_TO_REMOVE_KEY);

                workflowHelper.cancelOrderWorkflowWithRetries(nodeFdn);
                workflowHelper.cancelDeleteWorkflowIfAlreadyExists(nodeFdn);
                workflowHelper.executeDeleteWorkflow(nodeFdn, ignoreNetworkElement, dhcpClientId);

                if (!isVnfNode) {
                    apModelDeleter.deleteApNodeData(nodeFdn);
                }
            } else {
                apModelDeleter.deleteApNodeData(nodeFdn);
            }
        } catch (final Exception e) {
            stateTransitionManager.validateAndSetNextState(nodeFdn, StateTransitionEvent.DELETE_FAILED);
            throw e;
        }
    }

    private String getCurrentNodeState(final String nodeFdn) {
        final String nodeStatusFdn = nodeFdn + "," + MoType.NODE_STATUS.toString() + "=1";
        final ManagedObject nodeStatusMo = getNodeStatusMO(nodeStatusFdn);
        return nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString());
    }

    private static boolean isExecuteDeleteWorkflow(final String nodeState) {
        return !SKIP_DELETE_WORKFLOW_STATES.contains(nodeState);
    }

    private ManagedObject getNodeStatusMO(final String nodeFdn) {
        final ManagedObject managedObject = dpsOperations.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
        if (managedObject == null) {
            throw new NodeNotFoundException(String.format("Node with FDN [%s] could not be found.", nodeFdn));
        }
        return managedObject;
    }

    private boolean isVnfNode(final String nodeFdn) {
        final ManagedObject nodeMo = dpsOperations.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
        final String nodeType = nodeMo.getAttribute(NodeAttribute.NODE_TYPE.toString());
        final String internalNodeType = nodeTypeMapper.getInternalEjbQualifier(nodeType);

        return VNF_INTERNAL_TYPE.equalsIgnoreCase(internalNodeType);
    }
}
