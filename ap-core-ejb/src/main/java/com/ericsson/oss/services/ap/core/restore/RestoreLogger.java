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

import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.recording.CommandPhase;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;

/**
 * Helper class for the RestoreController This class will take care of system logging for the restoration result of AP workflows.
 */
public class RestoreLogger {

    @Inject
    private SystemRecorder systemRecorder;

    public void logToCommandLogNoWorkflowsToRestore() {
        systemRecorder.recordCommand(CommandLogName.RESTORE.toString(), CommandPhase.FINISHED_WITH_SUCCESS, "Auto Provisioning Restore Service",
                "N/A", "No suspended workflows to restore");
    }

    public void logToCommandLog(final List<WorkflowRestoreResult> resumedWorkflowRestores,
            final List<WorkflowRestoreResult> cancelledWorkflowRestores, final List<String> suspendedWfInstanceIds) {
        final StringBuilder restoreLogMessage = new StringBuilder()
                .append(getResumedWorkflowsLog(resumedWorkflowRestores))
                .append(getCancelledWorkflowsLog(cancelledWorkflowRestores))
                .append(getUnresolvedWorkflowsLog(suspendedWfInstanceIds));

        systemRecorder.recordCommand(CommandLogName.RESTORE.toString(), CommandPhase.FINISHED_WITH_SUCCESS, "Auto Provisioning Restore Service",
                "N/A", restoreLogMessage.toString());
    }

    private static String getCancelledWorkflowsLog(final List<WorkflowRestoreResult> cancelledWorkflowRestores) {
        if (cancelledWorkflowRestores.isEmpty()) {
            return "";
        }

        final StringBuilder cancelledWorkflowsLog = new StringBuilder()
                .append("\nCancelled workflows:\n")
                .append("Workflows have been cancelled as part of the Auto Provisioning restore process for the following nodes :\n")
                .append(getRestoreWorkflowsLog(cancelledWorkflowRestores));

        return cancelledWorkflowsLog.toString();
    }

    private static String getResumedWorkflowsLog(final List<WorkflowRestoreResult> resumedWorkflowRestores) {
        if (resumedWorkflowRestores.isEmpty()) {
            return "";
        }

        final StringBuilder resumedWorkflowsLog = new StringBuilder()
                .append("\nResumed workflows:\n")
                .append("Workflows have been resumed as part of the Auto Provisioning restore process for the following nodes :\n")
                .append(getRestoreWorkflowsLog(resumedWorkflowRestores));
        return resumedWorkflowsLog.toString();
    }

    private static String getRestoreWorkflowsLog(final List<WorkflowRestoreResult> workflowRestoreResults) {
        final StringBuilder sb = new StringBuilder();
        for (final WorkflowRestoreResult workflowRestoreResult : workflowRestoreResults) {
            sb.append(workflowRestoreResult.getApNodeFdn()).append(", ");
        }

        final int lastCommentIndex = sb.lastIndexOf(", ");
        sb.replace(lastCommentIndex, sb.length(), "");
        return sb.toString();
    }

    private static String getUnresolvedWorkflowsLog(final List<String> suspendedWfInstanceIds) {
        if (suspendedWfInstanceIds.isEmpty()) {
            return "";
        }

        final StringBuilder unresolvedWorkflowsLog = new StringBuilder()
                .append("\nUnresolved workflows:\n")
                .append("The following workflows were not resolved as part of the Auto Provisioning restore process:\n");

        for (final String wfInstanceId : suspendedWfInstanceIds) {
            unresolvedWorkflowsLog.append(wfInstanceId);
            unresolvedWorkflowsLog.append(", ");
        }

        final int lastCommentIndex = unresolvedWorkflowsLog.lastIndexOf(", ");
        unresolvedWorkflowsLog.replace(lastCommentIndex, unresolvedWorkflowsLog.length(), "");
        return unresolvedWorkflowsLog.toString();
    }
}