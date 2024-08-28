/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.delete;

import static com.ericsson.oss.services.wfs.api.query.instance.WorkflowInstanceQueryAttributes.QueryParameters.BUSINESS_KEY;
import static com.ericsson.oss.services.wfs.api.query.instance.WorkflowInstanceQueryAttributes.QueryResult.EXECUTION_ID;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.itpf.sdk.core.retry.classic.RetryManagerBean;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NetworkElementAttribute;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.common.workflow.BusinessKeyGenerator;
import com.ericsson.oss.services.ap.core.usecase.workflow.WorkflowCleanUpOperations;
import com.ericsson.oss.services.ap.core.usecase.workflow.WorkflowOperations;
import com.ericsson.oss.services.wfs.api.WorkflowServiceException;
import com.ericsson.oss.services.wfs.api.query.Query;
import com.ericsson.oss.services.wfs.api.query.QueryBuilderFactory;
import com.ericsson.oss.services.wfs.api.query.QueryType;
import com.ericsson.oss.services.wfs.api.query.RestrictionBuilder;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;
import com.ericsson.oss.services.wfs.jee.api.WorkflowQueryServiceLocal;
import com.ericsson.oss.services.ap.common.util.cdi.Transactional;
import com.ericsson.oss.services.ap.common.util.cdi.Transactional.TxType;

/**
 * Utility class used for DeleteNodeUseCase to deal with workflow related operations
 */
public class DeleteNodeWorkflowHelper {

    private static final int MAX_RETRIES = 15;
    private static final int RETRY_INTERVAL_IN_SECONDS = 3;
    private static final String CM_NODE_HEARTBEAT_SUPERVISION_FDN_FORMAT = MoType.NETWORK_ELEMENT.toString() + "=%s," + MoType.CM_NODE_HEARTBEAT_SUPERVISION + "=1";

    private WorkflowInstanceServiceLocal wfsInstanceService;

    @Inject
    private DpsOperations dpsOperations;

    @Inject
    private WorkflowOperations workflowOperations;

    @Inject
    private Logger logger;

    @Inject
    protected WorkflowCleanUpOperations workflowCleanUpOperations;

    protected ServiceFinderBean serviceFinder = new ServiceFinderBean(); // NOPMD

    @PostConstruct
    public void init() {
        wfsInstanceService = new ServiceFinderBean().find(WorkflowInstanceServiceLocal.class);
    }


    public void cancelOrderWorkflowWithRetries(final String nodeFdn) {
        try {
            final RetriableCommand<Void> retriableCommand = retryContext -> {
                workflowOperations.cancelIntegrationWorkflowIfActive(nodeFdn);
                return null;
            };

            final RetryPolicy retryPolicy = RetryPolicy.builder()
                                                       .attempts(MAX_RETRIES)
                                                       .waitInterval(RETRY_INTERVAL_IN_SECONDS, TimeUnit.SECONDS)
                                                       .retryOn(WorkflowServiceException.class)
                                                       .build();

            final RetryManager retryManager = new RetryManagerBean();
            retryManager.executeCommand(retryPolicy, retriableCommand);
        } catch (final Exception e) {
            throw new ApServiceException(e.getMessage(), e);
        }
    }

    public void cancelDeleteWorkflowIfAlreadyExists(final String nodeFdn) {
        workflowCleanUpOperations.cancelWorkflowInstanceIfItAlreadyExists(nodeFdn);
    }

    public void executeDeleteWorkflow(final String nodeFdn, final boolean ignoreNetworkElement, final String dhcpClientId) {
        boolean deleteSuccessful = false;
        final boolean isHardwareReplaceNode = isHardwareReplaceNode(nodeFdn);
        final boolean isMigrationNode = isMigrationNode(nodeFdn);
        final boolean isCmNodeHeartbeatSupervisionActive = getCmNodeHeartbeatSupervisionStatus(nodeFdn);
        final boolean shouldNetworkElementDeletionBeIgnored = isCmNodeHeartbeatSupervisionActive || ignoreNetworkElement || isHardwareReplaceNode || isMigrationNode;
        deleteSuccessful = workflowOperations.executeDeleteWorkflow(nodeFdn, shouldNetworkElementDeletionBeIgnored, dhcpClientId);

        if (!deleteSuccessful) {
            //DELETE_FAILED set in workflow
            throw new ApApplicationException("Error executing delete workflow");
        }
    }

    private boolean isMigrationNode(String nodeFdn) {
        final ManagedObject apNodeMo = getNodeMO(nodeFdn);
        if (apNodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString()) != null) {
            return (boolean) apNodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString());
        }
        return false;
      }

    private boolean isHardwareReplaceNode(final String nodeFdn) {
        final ManagedObject nodeMo = getNodeMO(nodeFdn);
        final Boolean isHardwareReplaceNode = nodeMo.getAttribute(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString());
        return isHardwareReplaceNode != null && isHardwareReplaceNode.booleanValue();
    }

    private ManagedObject getNodeMO(final String nodeFdn) {
        final ManagedObject managedObject = dpsOperations.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
        if (managedObject == null) {
            throw new NodeNotFoundException(String.format("Node with FDN [%s] could not be found.", nodeFdn));
        }
        return managedObject;
    }

    /**
     * Gets the CmNodeHeartbeatSupervision status (value of CmNodeHeartbeatSupervision.active attribute)
     *
     * @param nodeFdn
     *            The FDN of the node
     * @return the valude of CmNodeHeartbeatSupervision.active attribute
     */
    public boolean getCmNodeHeartbeatSupervisionStatus(final String nodeFdn) {
        final String nodeName = FDN.get(nodeFdn).getRdnValue();
        final String cmNodeHeartbeatSupervisionFdn = String.format(CM_NODE_HEARTBEAT_SUPERVISION_FDN_FORMAT, nodeName);
        final ManagedObject cmNodeHeartbeatSupervisionMo = dpsOperations.getDataPersistenceService().getLiveBucket().findMoByFdn(cmNodeHeartbeatSupervisionFdn);
        if( null != cmNodeHeartbeatSupervisionMo ) {
            final Boolean isActive = cmNodeHeartbeatSupervisionMo.getAttribute(NetworkElementAttribute.ACTIVE.toString());
            return isActive != null && isActive.booleanValue();
        }
        return false;
    }

    @Transactional(txType = TxType.REQUIRES_NEW)
    public String getWorkflowVariable(final String nodeFdn, final String variable) {
        String variableValue = null;
        try {
            final String wfExecutionId = getWorkflowExecutionId(nodeFdn);
            variableValue = (String) wfsInstanceService.getVariable(wfExecutionId, variable);
        } catch (final Exception exception) {
            logger.warn("Failed to get variable {} from node {}.", variable, nodeFdn, exception);
        }
        return variableValue;
    }

    private String getWorkflowExecutionId(final String nodeFdn) {
        String wfExecutionId = null;
        final WorkflowQueryServiceLocal workflowQueryService = serviceFinder.find(WorkflowQueryServiceLocal.class);
        final Query query = QueryBuilderFactory.getDefaultQueryBuilder().createTypeQuery(QueryType.WORKFLOW_INSTANCE_QUERY);
        final RestrictionBuilder restrictionBuilder = query.getRestrictionBuilder();
        query.setRestriction(restrictionBuilder.isEqual(BUSINESS_KEY, BusinessKeyGenerator.generateBusinessKeyFromFdn(nodeFdn)));

        wfExecutionId = (String) workflowQueryService.executeQuery(query).stream().findFirst()
                .map(workflowObject -> workflowObject.getAttribute(EXECUTION_ID)).orElse(null);
        return wfExecutionId;
    }
}
