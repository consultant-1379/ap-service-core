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

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.sdk.recording.CommandPhase;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;

/**
 * Unit tests for {@link RestoreLogger}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RestoreLoggerTest {

    private static final String NODE_NAME_2 = "Node2";
    private static final String SUSPENDED_WORKFLOW_ID = "wkfw3";

    private static final String EXPECTED_LOG_CANCELLED = "\nCancelled workflows:\n"
            + "Workflows have been cancelled as part of the Auto Provisioning restore process for the following nodes :\n"
            + NODE_NAME;

    private static final String EXPECTED_LOG_RESUMED = "\nResumed workflows:\n"
            + "Workflows have been resumed as part of the Auto Provisioning restore process for the following nodes :\n"
            + NODE_NAME_2;

    private static final String EXPECTED_LOG_UNRESOLVED = "\nUnresolved workflows:\n"
            + "The following workflows were not resolved as part of the Auto Provisioning restore process:\n"
            + SUSPENDED_WORKFLOW_ID;

    @Mock
    private SystemRecorder systemRecorder;

    @InjectMocks
    private final RestoreLogger restoreLogger = new RestoreLogger();

    private final List<WorkflowRestoreResult> cancelledResults = new ArrayList<>();
    private final List<WorkflowRestoreResult> resumedResults = new ArrayList<>();
    private final List<String> unresolvedWorkflowIds = new ArrayList<>();

    private WorkflowRestoreResult cancelledResult;
    private WorkflowRestoreResult resumedResult;

    @Before
    public void setUp() {
        cancelledResult = new WorkflowRestoreResult(RestoreResult.CANCELLED, "wkfl1", NODE_NAME);
        resumedResult = new WorkflowRestoreResult(RestoreResult.RESUMED, "wkfl2", NODE_NAME_2);
    }

    @Test
    public void when_no_cancelled_pending_or_unresolved_workflows_then_log_is_empty() {
        restoreLogger.logToCommandLog(resumedResults, cancelledResults, unresolvedWorkflowIds);
        verify(systemRecorder, times(1)).recordCommand(CommandLogName.RESTORE.toString(), CommandPhase.FINISHED_WITH_SUCCESS,
                "Auto Provisioning Restore Service", "N/A", "");
    }

    @Test
    public void when_only_cancelled_workflows_then_log_consist_of_only_cancelled_nodes() {
        cancelledResults.add(cancelledResult);
        restoreLogger.logToCommandLog(resumedResults, cancelledResults, unresolvedWorkflowIds);
        verify(systemRecorder, times(1)).recordCommand(CommandLogName.RESTORE.toString(), CommandPhase.FINISHED_WITH_SUCCESS,
                "Auto Provisioning Restore Service", "N/A", EXPECTED_LOG_CANCELLED);
    }

    @Test
    public void when_only_resumed_workflows_then_log_consist_of_only_cancelled_nodes() {
        resumedResults.add(resumedResult);
        restoreLogger.logToCommandLog(resumedResults, cancelledResults, unresolvedWorkflowIds);
        verify(systemRecorder, times(1)).recordCommand(CommandLogName.RESTORE.toString(), CommandPhase.FINISHED_WITH_SUCCESS,
                "Auto Provisioning Restore Service", "N/A", EXPECTED_LOG_RESUMED);
    }

    @Test
    public void when_only_unresolved_workflows_then_log_consist_of_only_unresolved_workflows() {
        unresolvedWorkflowIds.add(SUSPENDED_WORKFLOW_ID);
        restoreLogger.logToCommandLog(resumedResults, cancelledResults, unresolvedWorkflowIds);
        verify(systemRecorder, times(1)).recordCommand(CommandLogName.RESTORE.toString(), CommandPhase.FINISHED_WITH_SUCCESS,
                "Auto Provisioning Restore Service", "N/A", EXPECTED_LOG_UNRESOLVED);
    }

    @Test
    public void when_resumed_and_cancelled_workflows_then_log_consist_of_only_resumed_and_cancelled_workflows() {
        cancelledResults.add(cancelledResult);
        resumedResults.add(resumedResult);
        final String expectedLogMessage = EXPECTED_LOG_RESUMED + EXPECTED_LOG_CANCELLED;
        restoreLogger.logToCommandLog(resumedResults, cancelledResults, unresolvedWorkflowIds);
        verify(systemRecorder, times(1)).recordCommand(CommandLogName.RESTORE.toString(), CommandPhase.FINISHED_WITH_SUCCESS,
                "Auto Provisioning Restore Service", "N/A", expectedLogMessage);
    }

    @Test
    public void when_resumed_and_unresolved_workflows_then_log_consist_of_only_resumed_and_resolved_workflows() {
        resumedResults.add(resumedResult);
        unresolvedWorkflowIds.add(SUSPENDED_WORKFLOW_ID);
        final String expectedLogMessage = EXPECTED_LOG_RESUMED + EXPECTED_LOG_UNRESOLVED;
        restoreLogger.logToCommandLog(resumedResults, cancelledResults, unresolvedWorkflowIds);
        verify(systemRecorder, times(1)).recordCommand(CommandLogName.RESTORE.toString(), CommandPhase.FINISHED_WITH_SUCCESS,
                "Auto Provisioning Restore Service", "N/A", expectedLogMessage);
    }

    @Test
    public void when_cancelled_and_unresolved_workflows_then_log_consist_of_only_cancelled_and_unresolved_workflows() {
        cancelledResults.add(cancelledResult);
        unresolvedWorkflowIds.add(SUSPENDED_WORKFLOW_ID);
        final String expectedLogMessage = EXPECTED_LOG_CANCELLED + EXPECTED_LOG_UNRESOLVED;
        restoreLogger.logToCommandLog(resumedResults, cancelledResults, unresolvedWorkflowIds);
        verify(systemRecorder, times(1)).recordCommand(CommandLogName.RESTORE.toString(), CommandPhase.FINISHED_WITH_SUCCESS,
                "Auto Provisioning Restore Service", "N/A", expectedLogMessage);
    }

    @Test
    public void when_resumed_cancelled_and_unresolved_workflows_then_log_consist_of_resumed_cancelled_and_unresolved_workflows() {
        resumedResults.add(resumedResult);
        cancelledResults.add(cancelledResult);
        unresolvedWorkflowIds.add(SUSPENDED_WORKFLOW_ID);
        final String expectedLogMessage = EXPECTED_LOG_RESUMED + EXPECTED_LOG_CANCELLED + EXPECTED_LOG_UNRESOLVED;
        restoreLogger.logToCommandLog(resumedResults, cancelledResults, unresolvedWorkflowIds);
        verify(systemRecorder, times(1)).recordCommand(CommandLogName.RESTORE.toString(), CommandPhase.FINISHED_WITH_SUCCESS,
                "Auto Provisioning Restore Service", "N/A", expectedLogMessage);
    }
}
