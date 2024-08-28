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
package com.ericsson.oss.services.ap.api.restore;

/**
 * Classes that will initiate and control a restore session should implement this interface.
 */
public interface RestoreController {

    /**
     * Set the max number of attempts for the restore execution.
     *
     * @param attempts
     *            the maximum number of attempts
     */
    void setMaxRestoreRetryAttempts(int attempts);

    /**
     * Set the interval in seconds between restore execution attempts.
     *
     * @param retryInSeconds
     *            the retry interval in seconds
     */
    void setRestoreRetryInterval(int retryInSeconds);

    /**
     * Start the restore process.
     */
    void startRestore();

}