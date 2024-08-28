/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core;

import java.util.concurrent.Callable;

import com.ericsson.oss.services.ap.api.exception.ApServiceException;

/**
 * Class used to execute a usecase from {@link AutoProvisioningServiceCoreBean}.
 * <p>
 * Used to extract common handling out of the {@link AutoProvisioningServiceCoreBean}.
 */
final class UseCaseExecutor {

    private UseCaseExecutor() {

    }

    /**
     * Executes a usecase.
     * <p>
     * Takes in a {@link Callable} with the usecase behaviour and executes it.
     * <p>
     * Also handles any {@link Exception} and records the error to the ENM command log using the {@link UseCaseRecorder}.
     * 
     * @param recorder
     *            the {@link UseCaseRecorder} that will record any errors for the usecase.
     * @param useCaseCallable
     *            the implementation of the usecase
     * @return returns an object of the generic type of the input {@link Callable}
     */
    static <T> T execute(final UseCaseRecorder recorder, final Callable<T> useCaseCallable) {
        try {
            return useCaseCallable.call();
        } catch (final ApServiceException e) {
            recorder.error(e);
            throw e;
        } catch (final Exception e) {
            recorder.error(e);
            throw new ApServiceException(e);
        }
    }
}