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
package com.ericsson.oss.services.ap.core.usecase;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.ericsson.oss.services.ap.common.workflow.ActivityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal;
import com.ericsson.oss.services.ap.api.workflow.AutoProvisioningWorkflowService;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.workflow.BusinessKeyGenerator;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.order.WorkflowInstanceIdUpdater;
import com.ericsson.oss.services.ap.core.usecase.workflow.ApWorkflowServiceResolver;
import com.ericsson.oss.services.ap.core.usecase.workflow.WorkflowCleanUpOperations;
import com.ericsson.oss.services.wfs.api.WorkflowServiceException;
import com.ericsson.oss.services.wfs.api.instance.WorkflowInstance;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;

/**
 * Abstract Class implemented by any usecase that executes a new workflow.
 */
public abstract class AbstractWorkflowExecutableUseCase {

    private static final String ACTIVITY_KEY = "activity";
    private static final String AUTO_RESTORE_ON_FAIL_KEY = "autoRestoreOnFail";
    private static final String FDN_KEY = "fdn";
    private static final String USE_CASE_START_TIME_KEY = "useCaseStartTime";
    private static final String VALIDATION_REQUIRED_KEY = "validationRequired";
    private static final String WORK_ORDER_ID_KEY = "workOrderId";
    private static final String EMAIL_KEY = "email";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected ServiceFinderBean serviceFinder = new ServiceFinderBean();

    @Inject
    protected DpsOperations dps;

    protected StateTransitionManagerLocal stateTransitionManager;

    protected WorkflowInstanceServiceLocal wfsInstanceService;

    @Inject
    protected NodeTypeMapper nodeTypeMapper;

    @Inject
    protected WorkflowInstanceIdUpdater workflowInstanceIdUpdater;

    @Inject
    private ApWorkflowServiceResolver apWorkflowServiceResolver;

    @Inject
    protected WorkflowCleanUpOperations workflowCleanUpOperations;

    @PostConstruct
    public void init() {
        wfsInstanceService = serviceFinder.find(WorkflowInstanceServiceLocal.class);
        stateTransitionManager = serviceFinder.find(StateTransitionManagerLocal.class);
    }

    /**
     * Executes the workflow.
     *
     * @param workflowName
     *            the name of the workflow to execute
     * @param nodeFdn
     *            the AP node Fdn
     * @param validationRequired
     *            is validation required
     * @param nodeInfo
     *            nodeInfo object containing node configuration data
     * @return the ID of the workflow
     */
    protected String executeWorkflow(final String workflowName, final String nodeFdn, final boolean validationRequired, final NodeInfo nodeInfo) {
        logger.trace("Starting workflow {} for node {}", workflowName, nodeFdn);

        final String businessKey = BusinessKeyGenerator.generateBusinessKeyFromFdn(nodeFdn);
        final Map<String, Object> workflowVariables = getWorkflowVariables(nodeFdn);

        workflowVariables.put(VALIDATION_REQUIRED_KEY, validationRequired);
        workflowVariables.put(ACTIVITY_KEY, nodeInfo.getActivity().getActivityName());
        workflowVariables.put(AUTO_RESTORE_ON_FAIL_KEY, nodeInfo.isAutoRestoreOnFail());
        if (nodeInfo.getWorkOrderId() != null) {
            workflowVariables.put(WORK_ORDER_ID_KEY, nodeInfo.getWorkOrderId());
        }
        final Map<String, Object> notifications = nodeInfo.getNotifications();
        if (notifications != null && notifications.get(EMAIL_KEY) != null) {
            workflowVariables.put(EMAIL_KEY, notifications.get(EMAIL_KEY));
        }

        try {
            final WorkflowInstance workflowInstance = wfsInstanceService.startWorkflowInstanceByDefinitionId(workflowName, businessKey,
                    workflowVariables);
            return workflowInstance.getId();
        } catch (final WorkflowServiceException e) {
            throw new ApApplicationException(String.format("Error starting workflow %s with business key %s", workflowName, businessKey), e);
        } catch (final IllegalStateException e) {
            throw new ApServiceException(String.format("WorkflowService not available: %s", e.getMessage()), e);
        }
    }

    protected String executeEoiWorkflow(final String workflowName, final String nodeFdn, final String baseUrl, final String sessionId) {
        logger.trace("Starting workflow {} for node {}", workflowName, nodeFdn);

        final String businessKey = BusinessKeyGenerator.generateBusinessKeyFromFdn(nodeFdn);
        final Map<String, Object> workflowVariables = getWorkflowVariables(nodeFdn);

        workflowVariables.put(ACTIVITY_KEY, ActivityType.EOI_INTEGRATION_ACTIVITY.getActivityName());
        workflowVariables.put("baseUrl", baseUrl);
        workflowVariables.put("sessionId", sessionId);
        try {
            final WorkflowInstance workflowInstance = wfsInstanceService.startWorkflowInstanceByDefinitionId(workflowName, businessKey,
                workflowVariables);
            return workflowInstance.getId();
        } catch (final WorkflowServiceException e) {
            throw new ApApplicationException(String.format("Error starting workflow %s with business key %s", workflowName, businessKey), e);
        } catch (final IllegalStateException e) {
            throw new ApServiceException(String.format("WorkflowService not available: %s", e.getMessage()), e);
        }
    }


    private static Map<String, Object> getWorkflowVariables(final String apNodeFdn) {
        final Map<String, Object> workflowVariables = new HashMap<>();
        final long useCaseStartTime = System.currentTimeMillis();
        workflowVariables.put(FDN_KEY, apNodeFdn);
        workflowVariables.put(USE_CASE_START_TIME_KEY, useCaseStartTime);

        return workflowVariables;
    }

    /**
     * Returns the AP node {@link ManagedObject} for the given FDN.
     *
     * @param nodeFdn
     *            the FDN of the AP node to retrieve
     * @return the AP node MO
     */
    protected ManagedObject getNodeMo(final String nodeFdn) {
        try {
            return dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
        } catch (final Exception e) {
            throw new ApApplicationException(String.format("Error reading MO for %s", nodeFdn), e);
        }
    }

    /**
     * Returns an instance of the {@link AutoProvisioningWorkflowService} for the given node type.
     * <p>
     * Can be used to retrieve workflow names and supported commands for a given node type.
     *
     * @param nodeType
     *            the type of the node
     * @return an instance of the AutoProvisioningWorkflowService
     */
    protected AutoProvisioningWorkflowService getApWorkflowService(final String nodeType) {
        return apWorkflowServiceResolver.getApWorkflowService(nodeType);
    }

    /**
     * Executes the usecase.
     *
     * @param nodeFdn
     *            the FDN of the AP node
     */
    public abstract void execute(final String nodeFdn);

}
