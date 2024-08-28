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
package com.ericsson.oss.services.ap.ejb.api;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * An asynchronous executor.
 */
public interface CoreExecutorLocal {

    /**
     * execute the callable call method in a new thread.
     *
     * @param callable
     * @return Future
     */
    <T> Future<T> execute(final Callable<T> callable);
}
