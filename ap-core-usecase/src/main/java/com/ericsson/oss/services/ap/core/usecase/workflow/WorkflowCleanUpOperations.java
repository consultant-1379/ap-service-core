/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.workflow;

import static com.ericsson.oss.services.wfs.api.query.instance.WorkflowInstanceQueryAttributes.QueryParameters.BUSINESS_KEY;
import static com.ericsson.oss.services.wfs.api.query.instance.WorkflowInstanceQueryAttributes.QueryParameters.WORKFLOW_DEFINITION_ID;
import static com.ericsson.oss.services.wfs.api.query.instance.WorkflowInstanceQueryAttributes.QueryParameters.WORKFLOW_INSTANCE_ID;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.api.workflow.AutoProvisioningWorkflowService;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.workflow.BusinessKeyGenerator;
import com.ericsson.oss.services.wfs.api.WorkflowServiceException;
import com.ericsson.oss.services.wfs.api.query.Query;
import com.ericsson.oss.services.wfs.api.query.QueryBuilderFactory;
import com.ericsson.oss.services.wfs.api.query.QueryType;
import com.ericsson.oss.services.wfs.api.query.RestrictionBuilder;
import com.ericsson.oss.services.wfs.api.query.WorkflowObject;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;
import com.ericsson.oss.services.wfs.jee.api.WorkflowQueryServiceLocal;

/**
 * Operation to cancel existing workflows.
 */
public class WorkflowCleanUpOperations {

    @Inject
    private Logger logger;

    @Inject
    private ModelReader modelReader;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    private Map<String, AutoProvisioningWorkflowService> apWorkflowServices;

    private WorkflowInstanceServiceLocal wfsInstanceService;

    protected ServiceFinderBean serviceFinder = new ServiceFinderBean(); // NOPMD

    @PostConstruct
    public void init() {
        wfsInstanceService = serviceFinder.find(WorkflowInstanceServiceLocal.class);
        final Collection<String> supportedNodeTypes = modelReader.getSupportedNodeTypes();
        apWorkflowServices = new HashMap<>();
        for (final String nodeType : supportedNodeTypes) {
            final String ejbQualifier = nodeTypeMapper.getInternalEjbQualifier(nodeType);
            if (!apWorkflowServices.containsKey(ejbQualifier)) {
                final AutoProvisioningWorkflowService nodeTypeWorkflowService = serviceFinder.find(AutoProvisioningWorkflowService.class, ejbQualifier);
                apWorkflowServices.put(ejbQualifier, nodeTypeWorkflowService);
            }
        }
    }

    /**
     * Cancels a workflow if one already exists for the given node
     *
     * @param nodeFdn
     *            the fdn of the node in the AP model
     */
    public void cancelWorkflowInstanceIfItAlreadyExists(final String nodeFdn) {
        final WorkflowQueryServiceLocal workflowQueryService = serviceFinder.find(WorkflowQueryServiceLocal.class);
        final Query query = buildQueryToFindInstancesForNode(nodeFdn);
        final List<WorkflowObject> existingInstances = workflowQueryService.executeQuery(query);

        if (!existingInstances.isEmpty()) {
            cancelMatchingInstances(existingInstances);
            resetStatusEntries(nodeFdn);
        }
    }

    private static Query buildQueryToFindInstancesForNode(final String nodeFdn) {
        final String businessKey = BusinessKeyGenerator.generateBusinessKeyFromFdn(nodeFdn);

        final Query query = QueryBuilderFactory.getDefaultQueryBuilder().createTypeQuery(QueryType.WORKFLOW_INSTANCE_QUERY);
        final RestrictionBuilder restrictionBuilder = query.getRestrictionBuilder();
        query.setRestriction(restrictionBuilder.isEqual(BUSINESS_KEY, businessKey));
        return query;
    }

    private void cancelMatchingInstances(final List<WorkflowObject> matchingInstances) {
        for (final WorkflowObject matchingInstance : matchingInstances) {
            final String matchingInstanceId = (String) matchingInstance.getAttribute(WORKFLOW_INSTANCE_ID);
            logger.info("Found pre-existing workflow instance with ID '{}'", matchingInstanceId);
            if (isParentWorkflow(matchingInstance)) {
                cancelWorkflowInstance(matchingInstanceId);
            }
        }
    }

    private boolean isParentWorkflow(final WorkflowObject workflowObject) {
        for (final AutoProvisioningWorkflowService apwfs : apWorkflowServices.values()) {
            if (apwfs.getAllWorkflowNames().contains(workflowObject.getAttribute(WORKFLOW_DEFINITION_ID))) {
                return true;
            }
        }
        return false;
    }

    private void cancelWorkflowInstance(final String instanceIdToCancel) {
        try {
            wfsInstanceService.cancelWorkflowInstance(instanceIdToCancel, true);
            logger.debug("Cancelled workflow instance with ID '{}'", instanceIdToCancel);
        } catch (final WorkflowServiceException e) {
            throw new ApApplicationException("Unable to cancel pre-existing workflow instance with ID: " + instanceIdToCancel, e);
        }
    }

    // Cancelling a workflow may trigger the end listener for a BPMN wait point, so status should be reset for the new replace flow
    private void resetStatusEntries(final String nodeFdn) {
        final StatusEntryManagerLocal statusEntryManager = serviceFinder.find(StatusEntryManagerLocal.class);
        statusEntryManager.clearStatusEntries(nodeFdn);
    }
}
