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
package com.ericsson.oss.services.ap.core.restore;

import static com.ericsson.oss.services.wfs.api.query.instance.WorkflowInstanceQueryAttributes.QueryResult.WORKFLOW_INSTANCE_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommandException;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy.RetryPolicyBuilder;
import com.ericsson.oss.itpf.sdk.core.retry.classic.RetryManagerNonCDIImpl;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsProjectionQueryExecutor;
import com.ericsson.oss.services.wfs.api.query.Query;
import com.ericsson.oss.services.wfs.api.query.WorkflowObject;
import com.ericsson.oss.services.wfs.jee.api.WorkflowQueryServiceLocal;

/**
 * Unit tests for {@link SuspendedWorkflowResolver}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SuspendedWorkflowResolverTest {

    private static final String WORKFLOW_ID_1 = "wfId";
    private static final String WORKFLOW_ID_2 = "wfId2";

    @Mock
    private WorkflowQueryServiceLocal workflowQueryService;

    @Mock
    private WorkflowObject workflowInstanceObject, workflowInstanceObject2;

    @Mock
    private ServiceFinderBean serviceFinder;

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private DpsProjectionQueryExecutor dpsQueriesExecutor;

    @Mock
    private RetryContext retryContext; // NOPMD

    @Mock
    private RetryPolicyBuilder policyBuilder; // NOPMD

    @Spy
    private final RetryManagerNonCDIImpl retryManager = new RetryManagerNonCDIImpl(); // NOPMD

    @InjectMocks
    private final SuspendedWorkflowResolver suspendedWorkflowResolver = new SuspendedWorkflowResolver();

    private final List<WorkflowObject> workflowInstances = new ArrayList<>();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void when_workflowQueryService_unavailable_then_retry() {
        suspendedWorkflowResolver.setRetryInterval(0);

        when(serviceFinder.find(WorkflowQueryServiceLocal.class)).thenThrow(IllegalStateException.class).thenReturn(workflowQueryService);
        when(workflowInstanceObject.getAttribute(WORKFLOW_INSTANCE_ID)).thenReturn(WORKFLOW_ID_1);
        when(workflowInstanceObject2.getAttribute(WORKFLOW_INSTANCE_ID)).thenReturn(WORKFLOW_ID_2);
        workflowInstances.add(workflowInstanceObject);
        workflowInstances.add(workflowInstanceObject2);
        when(workflowQueryService.executeQuery(any(Query.class))).thenReturn(workflowInstances);

        final List activeIds = new ArrayList<>();
        activeIds.add(WORKFLOW_ID_1);

        when(dpsQueriesExecutor.execute()).thenReturn(activeIds);
        when(dpsQueries.getMoAttributeValues(anyString(), anyString(), anyString())).thenReturn(dpsQueriesExecutor);

        final List<String> suspendedWorkflows = suspendedWorkflowResolver.getSuspendedWorkflows();
        assertEquals("Workflow list does not contain only one workflow object", 1, suspendedWorkflows.size());
        assertEquals(WORKFLOW_ID_1, suspendedWorkflows.get(0));
        verify(serviceFinder, times(2)).find(WorkflowQueryServiceLocal.class);
    }

    @Test(expected = RetriableCommandException.class)
    public void when_workflowQueryService_unavailable_after_5_retries_then_throw_exception() {
        suspendedWorkflowResolver.setRetryInterval(0);
        doThrow(IllegalStateException.class).when(serviceFinder).find(WorkflowQueryServiceLocal.class);
        suspendedWorkflowResolver.getSuspendedWorkflows();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void when_suspended_workflows_found_then_wfInstanceIds_returned() {
        final List activeIds = new ArrayList<>();
        activeIds.add(WORKFLOW_ID_1);
        when(serviceFinder.find(WorkflowQueryServiceLocal.class)).thenReturn(workflowQueryService);
        when(workflowInstanceObject.getAttribute(WORKFLOW_INSTANCE_ID)).thenReturn(WORKFLOW_ID_1);
        when(workflowInstanceObject2.getAttribute(WORKFLOW_INSTANCE_ID)).thenReturn(WORKFLOW_ID_2);
        workflowInstances.add(workflowInstanceObject);
        workflowInstances.add(workflowInstanceObject2);
        when(workflowQueryService.executeQuery(any(Query.class))).thenReturn(workflowInstances);
        when(dpsQueriesExecutor.execute()).thenReturn(activeIds);
        when(dpsQueries.getMoAttributeValues(anyString(), anyString(), anyString())).thenReturn(dpsQueriesExecutor);

        final List<String> suspendedWorkflows = suspendedWorkflowResolver.getSuspendedWorkflows();

        assertEquals("Workflow list does not contain only one workflow object", suspendedWorkflows.size(), 1);
        assertEquals(WORKFLOW_ID_1, suspendedWorkflows.get(0));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    public void when_no_suspended_workflows_then_empty_list_returned() {
        final List activeIds = new ArrayList<>();
        when(dpsQueriesExecutor.execute()).thenReturn(activeIds);
        when(dpsQueries.getMoAttributeValues(anyString(), anyString(), anyString())).thenReturn(dpsQueriesExecutor);
        when(serviceFinder.find(WorkflowQueryServiceLocal.class)).thenReturn(workflowQueryService);
        when(workflowQueryService.executeQuery(any(Query.class))).thenReturn(Collections.<WorkflowObject> emptyList());

        final List<String> suspendedWorkflows = suspendedWorkflowResolver.getSuspendedWorkflows();

        assertTrue("Workflow list is not empty", suspendedWorkflows.isEmpty());
    }
}
