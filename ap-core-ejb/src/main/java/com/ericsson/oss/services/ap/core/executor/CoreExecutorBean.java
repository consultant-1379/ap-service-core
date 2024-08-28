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
package com.ericsson.oss.services.ap.core.executor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.ejb.api.CoreExecutorLocal;

@Stateless
public class CoreExecutorBean implements CoreExecutorLocal {

    @Override
    @Asynchronous
    public <T> Future<T> execute(final Callable<T> callable) {
        T result = null;
        try {
            result = callable.call();
        } catch (final ApServiceException e) {
            throw e;
        } catch (final Exception e) {
            throw new ApApplicationException(e);
        }
        return new AsyncResult<>(result);
    }
}
