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

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ejb.TimerConfig;
import javax.ejb.TimerService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.cluster.restore.RestoreManager;
import com.ericsson.oss.itpf.sdk.cluster.restore.ServiceRestoreCompletionStatus;
import com.ericsson.oss.itpf.sdk.cluster.restore.ServiceRestoreResponse;
import com.ericsson.oss.itpf.sdk.cluster.restore.ServiceRestoreStatus;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.itpf.sdk.core.retry.classic.RetryManagerNonCDIImpl;

/**
 * Unit tests for {@link RestoreControllerEjb}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RestoreControllerEjbTest {

    private final ServiceRestoreResponse restoreAllowedResponse = new ServiceRestoreResponse(ServiceRestoreStatus.ALLOWED);
    private final ServiceRestoreResponse restoreNotAllowedResponse = new ServiceRestoreResponse(ServiceRestoreStatus.NOT_ALLOWED);

    private List<String> suspendedWorkflows;
    private List<WorkflowRestoreResult> workflowRestoreResults;

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private RestoreManager restoreManager;

    @Mock
    private RestoreExecutor restoreService;

    @Mock
    private SuspendedWorkflowResolver suspendedWorkflowResolver;

    @Mock
    private TimerService timerService;

    @Spy
    private final RetryManagerNonCDIImpl retryManager = new RetryManagerNonCDIImpl();

    @Mock
    private RestoreLogger restoreLogger;

    @InjectMocks
    private final RestoreControllerEjb restoreController = new RestoreControllerEjb();

    @Before
    public void setup() {
        workflowRestoreResults = new ArrayList<>();
        suspendedWorkflows = new ArrayList<>();
        suspendedWorkflows.add("wfid");
    }

    @Test
    public void when_restore_controller_started_then_restore_scheduled_to_execute_after_five_mintues() {
        restoreController.init();
        verify(timerService).createSingleActionTimer(eq(MINUTES.toMillis(5L)), any(TimerConfig.class));
    }

    @Test
    public void when_restore__not_allowed_then_do_nothing() {
        when(restoreManager.tryRestore()).thenReturn(restoreNotAllowedResponse);
        verify(suspendedWorkflowResolver, never()).getSuspendedWorkflows();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void when_no_suspended_workflows_then_report_restore_success_and_restore_not_attempted() {
        when(restoreManager.tryRestore()).thenReturn(restoreAllowedResponse);
        when(suspendedWorkflowResolver.getSuspendedWorkflows()).thenReturn(Collections.<String> emptyList());
        restoreController.startRestore();
        verify(restoreManager, times(1)).finishRestoreWith(ServiceRestoreCompletionStatus.SUCCESS);
        verify(retryManager, never()).executeCommand(any(RetryPolicy.class), any(RetriableCommand.class));
        verify(restoreLogger, times(1)).logToCommandLogNoWorkflowsToRestore();
    }

    @Test
    public void when_no_pending_restores_then_restore_finishes() {
        when(restoreManager.tryRestore()).thenReturn(restoreAllowedResponse);
        suspendedWorkflows.add("wfid2");
        when(suspendedWorkflowResolver.getSuspendedWorkflows()).thenReturn(suspendedWorkflows);

        final WorkflowRestoreResult cancelledResult = new WorkflowRestoreResult(RestoreResult.CANCELLED, "wfid", null);
        final WorkflowRestoreResult resumedResult = new WorkflowRestoreResult(RestoreResult.RESUMED, "wfid2", null);

        workflowRestoreResults.add(cancelledResult);
        workflowRestoreResults.add(resumedResult);
        final List<WorkflowRestoreResult> cancelled = new ArrayList<>();
        cancelled.add(cancelledResult);
        final List<WorkflowRestoreResult> resumed = new ArrayList<>();
        resumed.add(resumedResult);
        when(restoreService.execute(anyListOf(String.class), anyBoolean())).thenReturn(workflowRestoreResults);

        restoreController.startRestore();
        verify(restoreService, times(1)).execute(anyListOf(String.class), anyBoolean());
        verify(restoreManager, times(1)).finishRestoreWith(ServiceRestoreCompletionStatus.SUCCESS);

        verify(restoreLogger, times(1)).logToCommandLog(eq(resumed), eq(cancelled), anyListOf(String.class));
    }

    @Test
    public void when_restore_not_completed_then_restore_executed_at_intervals_until_max_restore_execution_reached() {
        when(restoreManager.tryRestore()).thenReturn(restoreAllowedResponse);
        when(suspendedWorkflowResolver.getSuspendedWorkflows()).thenReturn(suspendedWorkflows);

        final WorkflowRestoreResult pendingResult = new WorkflowRestoreResult(RestoreResult.PENDING, "wfid", null);
        workflowRestoreResults.add(pendingResult);

        when(restoreService.execute(anyListOf(String.class), anyBoolean())).thenReturn(workflowRestoreResults);

        restoreController.setRestoreRetryInterval(0);

        restoreController.startRestore();
        verify(restoreService, times(24)).execute(anyListOf(String.class), anyBoolean());
        verify(restoreManager, times(1)).finishRestoreWith(ServiceRestoreCompletionStatus.SUCCESS);
        verify(restoreLogger, times(1)).logToCommandLog(anyListOf(WorkflowRestoreResult.class), anyListOf(WorkflowRestoreResult.class),
                anyListOf(String.class));
    }

}