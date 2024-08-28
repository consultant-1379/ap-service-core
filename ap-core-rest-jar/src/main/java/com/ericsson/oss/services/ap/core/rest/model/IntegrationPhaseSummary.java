/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.model;

/**
 * Defines the progress of a project.
 */
public class IntegrationPhaseSummary {

    private int cancelled;
    private int failed;
    private int inProgress;
    private int successful;
    private int suspended;

    public IntegrationPhaseSummary(final int cancelled, final int failed, final int inProgress, final int successful, final int suspended) {
        this.cancelled = cancelled;
        this.failed = failed;
        this.inProgress = inProgress;
        this.successful = successful;
        this.suspended = suspended;
    }

    public int getCancelled() {
        return cancelled;
    }

    public int getFailed() {
        return failed;
    }

    public int getInProgress() {
        return inProgress;
    }

    public int getSuccessful() {
        return successful;
    }

    public int getSuspended() {
        return suspended;
    }
}
