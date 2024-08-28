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
package com.ericsson.oss.services.ap.api.status;

/**
 * Possible progress states for a status entry for <code>ap status -n</code>.
 */
public enum StatusEntryProgress {

    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    EMPTY(""),
    FAILED("Failed"),
    RECEIVED("Received"),
    STARTED("Started"),
    WAITING("Waiting");

    private String progress;

    private StatusEntryProgress(final String progress) {
        this.progress = progress;
    }

    @Override
    public String toString() {
        return progress;
    }
}