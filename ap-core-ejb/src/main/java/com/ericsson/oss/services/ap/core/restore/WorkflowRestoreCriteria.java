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

import javax.inject.Inject;

/**
 * Using certain criteria this class determines if a node's workflow should be resumed or cancelled after a restore.
 */
class WorkflowRestoreCriteria {

    @Inject
    private NodeCondition nodeCondition;

    /**
     * Determine if a workflow associated with a node is resumable.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param isLastRestoreAttempt
     *            true if max restore duration has been reached
     * @return true if workflow should be resumed
     */
    public boolean isWorkflowResumable(final String apNodeFdn, final boolean isLastRestoreAttempt) {
        return isLastRestoreAttempt && !nodeCondition.isNodeSynchronized(apNodeFdn) && nodeCondition.isWaitingForNodeUp(apNodeFdn);
    }

    /**
     * Determine if a workflow associated with a node can be cancelled.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param isLastRestoreAttempt
     *            true if max restore duration has been reached
     * @return true if workflow should be cancelled
     */
    public boolean isWorkflowCancellable(final String apNodeFdn, final boolean isLastRestoreAttempt) {
        return nodeCondition.isNodeSynchronized(apNodeFdn) || (isLastRestoreAttempt && !nodeCondition.isWaitingForNodeUp(apNodeFdn));
    }
}
