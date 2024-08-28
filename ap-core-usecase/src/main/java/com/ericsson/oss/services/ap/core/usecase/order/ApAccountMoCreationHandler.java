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
package com.ericsson.oss.services.ap.core.usecase.order;

import java.util.concurrent.TimeUnit;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommandException;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.itpf.sdk.core.retry.classic.RetryManagerBean;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;

/**
 * Executes a {@link RetriableCommand} for the creation of <code>AutoProvisioningAccounts</code> and <code>AutoProvisioningAccount</code> MOs.
 */
final class ApAccountMoCreationHandler {

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_INTERVAL_IN_SECONDS = 2;

    private ApAccountMoCreationHandler() {

    }

    /**
     * Executes a {@link RetriableCommand}. Retries on any {@link Exception} up to {@link #MAX_RETRIES} times, and has a retry interval of
     * {@link #RETRY_INTERVAL_IN_SECONDS} seconds.
     *
     * @param retriableCommand
     *            the command to execute
     * @return the created {@link ManagedObject}
     */
    public static ManagedObject executeRetriableCommand(final RetriableCommand<ManagedObject> retriableCommand) {
        final RetryManager retryManager = new RetryManagerBean();

        try {
            return retryManager.executeCommand(getRetryPolicy(), retriableCommand);
        } catch (final RetriableCommandException e) {
            throw new ApServiceException(e.getMessage(), e);
        }
    }

    private static RetryPolicy getRetryPolicy() {
        return RetryPolicy.builder()
                .attempts(MAX_RETRIES)
                .waitInterval(RETRY_INTERVAL_IN_SECONDS, TimeUnit.SECONDS)
                .retryOn(Exception.class)
                .build();
    }
}
