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
package com.ericsson.oss.services.ap.core.restore;

/**
 * Contains the result of the restore process for a particular workflow.
 * <p>
 * Result can be {@link RestoreResult#CANCELLED}, {@link RestoreResult#PENDING}, {@link RestoreResult#RESUMED}.
 */
public class WorkflowRestoreResult {

    private final RestoreResult result;
    private final String apNodeFdn;
    private final String suspendedWfInstanceId;

    public WorkflowRestoreResult(final RestoreResult result, final String suspendedWfInstanceId, final String apNodeFdn) {
        this.result = result;
        this.suspendedWfInstanceId = suspendedWfInstanceId;
        this.apNodeFdn = apNodeFdn;
    }

    /**
     * Return result of the restore.
     *
     * @return {@link RestoreResult}
     */
    public RestoreResult getResult() {
        return result;
    }

    /**
     * Return the FDN of the AP node.
     *
     * @return FDN of the AP node
     */
    public String getApNodeFdn() {
        return apNodeFdn;
    }

    /**
     * Return the suspended workflow instance ID.
     *
     * @return the suspended workflow instance ID
     */
    public String getSuspendedWorkflowInstanceId() {
        return suspendedWfInstanceId;
    }
}
