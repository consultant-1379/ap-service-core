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
package com.ericsson.oss.services.ap.core.usecase.workflow;

import static com.ericsson.oss.services.ap.api.status.State.DELETE_FAILED;
import static com.ericsson.oss.services.ap.common.model.NodeStatusAttribute.STATE;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.workflow.AutoProvisioningWorkflowService;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.util.cdi.Transactional;
import com.ericsson.oss.services.ap.common.util.cdi.Transactional.TxType;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.ap.common.workflow.BusinessKeyGenerator;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;

/**
 * Operations to execute and cancel workflows.
 */
public class WorkflowOperations {

    @Inject
    private Logger logger;

    @Inject
    private DpsOperations dps;

    private WorkflowInstanceServiceLocal wfsInstanceService;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    private ServiceFinderBean serviceFinder = new ServiceFinderBean(); // NOPMD

    @PostConstruct
    public void init() {
        wfsInstanceService = new ServiceFinderBean().find(WorkflowInstanceServiceLocal.class);
    }

    /**
     * Cancels the integration workflow if active. Checks the node attribute activateWorkflowInstance to determine if the workflow is active.
     *
     * @param nodeFdn
     *            the FDN of the AP node MO whose integration workflow to cancel
     */
    @Transactional(txType = TxType.REQUIRES_NEW)
    public void cancelIntegrationWorkflowIfActive(final String nodeFdn) {
        final ManagedObject nodeMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
        final String activeWfInstanceId = nodeMo.getAttribute(NodeAttribute.ACTIVE_WORKFLOW_INSTANCE_ID.toString());

        if (StringUtils.isEmpty(activeWfInstanceId)) {
            logger.debug("No active workflow found for node {}", nodeFdn);
            return;
        }

        try {
            logger.debug("Cancelling workflow, nodeFdn={}, wfInstanceId={}", nodeFdn, activeWfInstanceId);
            wfsInstanceService.cancelWorkflowInstance(activeWfInstanceId, true);
            nodeMo.setAttribute(NodeAttribute.ACTIVE_WORKFLOW_INSTANCE_ID.toString(), null);
        } catch (final Exception e) {
            throw new ApApplicationException("Error cancelling workflow", e);
        }
    }

    /**
     * Executes the delete workflow for the node.
     *
     * @param nodeFdn
     *            the FDN of the AP node MO whose integration workflow to cancel
     * @param ignoreNetworkElement
     *            identify if ignore NetworkElement
     * @param dhcpClientId
     *            DHCP Client ID that to be removed
     * @return true if delete workflow executes successfully
     * @throws ApApplicationException
     *             thrown if an error occurs starting the delete workflow
     */
    @Transactional(txType = TxType.REQUIRES_NEW)
    public boolean executeDeleteWorkflow(final String nodeFdn, final boolean ignoreNetworkElement, final String dhcpClientId) {
        logger.debug("Executing delete workflow, nodeFdn={}", nodeFdn);

        try {
            final Map<String, Object> workflowVariables = new HashMap<>();
            workflowVariables.put("fdn", nodeFdn);
            workflowVariables.put(AbstractWorkflowVariables.DELETE_IGNORE_NETWORK_ELEMENT_KEY, ignoreNetworkElement);

            if (StringUtils.isNotBlank(dhcpClientId)) {
                workflowVariables.put(AbstractWorkflowVariables.DHCP_CLIENT_ID_TO_REMOVE_KEY, dhcpClientId);
            }

            final String businessKey = BusinessKeyGenerator.generateBusinessKeyFromFdn(nodeFdn);
            final String deleteWorkflowName = getDeleteWorkflowName(nodeFdn);
            wfsInstanceService.startWorkflowInstanceByDefinitionId(deleteWorkflowName, businessKey, workflowVariables);
        } catch (final Exception exception) {
            throw new ApApplicationException("Error starting delete workflow, nodeFdn=" + nodeFdn, exception);
        }

        return !isNodeStateDeleteFailed(nodeFdn);
    }

    public String getDeleteWorkflowName(final String nodeFdn) {
        final ManagedObject nodeMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
        final String nodeType = nodeMo.getAttribute(NodeAttribute.NODE_TYPE.toString());
        final String internalNodeType = nodeTypeMapper.getInternalEjbQualifier(nodeType);
        final AutoProvisioningWorkflowService apWorkflowService = serviceFinder.find(AutoProvisioningWorkflowService.class,
                internalNodeType);
        return apWorkflowService.getDeleteWorkflowName();
    }

    private boolean isNodeStateDeleteFailed(final String nodeFdn) {
        final String nodeStatusFdn = nodeFdn + "," + MoType.NODE_STATUS.toString() + "=1";
        final ManagedObject nodeStatusMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeStatusFdn);
        return nodeStatusMo != null && DELETE_FAILED.toString().equals(nodeStatusMo.getAttribute(STATE.toString()));
    }
}
