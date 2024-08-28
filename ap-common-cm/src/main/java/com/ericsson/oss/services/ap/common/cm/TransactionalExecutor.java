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
package com.ericsson.oss.services.ap.common.cm;

import java.util.concurrent.Callable;

import javax.ejb.ApplicationException;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.common.util.cdi.JNDIUtil;

/**
 * Transactional executor to execute {@link Callable} implementations in a new transaction. Can be invoked in both CDI and NON-CDI contexts.
 * <p>
 * Any active transaction will be suspended and the {@link Callable} will be invoked in a new transaction. The original transaction will be resumed
 * once execution has completed. In case there is no active transaction, a new transaction will be started.
 */
public class TransactionalExecutor {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private JNDIUtil jndiUtil = new JNDIUtil(); //NOPMD

    /**
     * Executes the {@link Callable} in a new transaction.
     *
     * @param callable
     *             the callable to invoke
     * @param <T>
     *             the transaction
     * @return the result of the {@link Callable} invocation
     * @throws Exception
     *             if there are any errors executing the {@link Callable}
     */
    public <T> T execute(final Callable<T> callable) throws Exception { //NOPMD
        TransactionManager tm = null;

        try {
            tm = jndiUtil.doLookup("java:/jboss/TransactionManager");
        } catch (final NamingException e) {
            throw new ApServiceException("TransactionManager not found", e);
        }

        logger.debug("Suspending transaction -> {}", tm.getTransaction());
        final Transaction callerTx = tm.suspend(); //will return null if no associated tx

        try {
            return executeCallableInNewTx(callable, tm);
        } finally {
            if (callerTx != null) {
                tm.resume(callerTx);
                logger.debug("Resumed caller tx -> {}", callerTx);
            }
        }
    }

    private <T> T executeCallableInNewTx(final Callable<T> callable, final TransactionManager tm) throws Exception {
        tm.begin();
        final Transaction newTx = tm.getTransaction();
        logger.debug("Proceeding in new tx -> {}", newTx);

        try {
            return callable.call();
        } catch (final Exception e) {
            handleErrorInNewTx(tm, e);
        } finally {
            endTransaction(tm, newTx);
        }

        return null;
    }

    private static void handleErrorInNewTx(final TransactionManager tm, final Exception e) throws Exception { //NOSONAR
        if (isApplicationExceptionWithNoRollback(e)) {
            throw e;
        }

        tm.setRollbackOnly();
        throw e;
    }

    private static boolean isApplicationExceptionWithNoRollback(final Exception e) { //NOSONAR
        for (Class<?> exceptionClass = e.getClass(); exceptionClass != null; exceptionClass = exceptionClass.getSuperclass()) {
            final ApplicationException applicationException = exceptionClass.getAnnotation(ApplicationException.class);
            if (applicationException != null) {
                if (applicationException.inherited()) {
                    return !applicationException.rollback();
                } else {
                    break;
                }
            }
        }
        return false;
    }

    private static void endTransaction(final TransactionManager tm, final Transaction tx) throws Exception { //NOSONAR
        if (tx.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
            tm.rollback();
        } else {
            tm.commit();
        }
    }
}
