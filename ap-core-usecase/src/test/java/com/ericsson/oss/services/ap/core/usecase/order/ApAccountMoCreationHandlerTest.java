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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;

/**
 * Unit tests for {@link ApAccountMoCreationHandler}.
 */
public class ApAccountMoCreationHandlerTest {

    private int failedExecutionCounter = 0;

    @Test
    public void whenRetriableCommandIsExecuted_andPasses_thenManagedObjectIsReturned() {
        final RetriableCommand<ManagedObject> validCommand = new RetriableCommand<ManagedObject>() {

            @Override
            public ManagedObject execute(final RetryContext retryContext) throws Exception { // NOPMD
                return null;
            }
        };

        final ManagedObject result = ApAccountMoCreationHandler.executeRetriableCommand(validCommand);

        assertNull(result); // Checking for null since ManagedObject has no impl, and we cannot inject a mock
    }

    @Test(expected = ApServiceException.class)
    public void whenRetriableCommandIsExecuted_andFailes_thenExecutionIsRetried_andExceptionIsPropagated() {

        final RetriableCommand<ManagedObject> invalidCommand = new RetriableCommand<ManagedObject>() {

            @Override
            public ManagedObject execute(final RetryContext retryContext) {
                failedExecutionCounter++;
                throw new IllegalStateException();
            }
        };

        try {
            ApAccountMoCreationHandler.executeRetriableCommand(invalidCommand);
        } catch (final Exception e) {
            assertEquals(3, failedExecutionCounter);
            throw e;
        }
    }
}
