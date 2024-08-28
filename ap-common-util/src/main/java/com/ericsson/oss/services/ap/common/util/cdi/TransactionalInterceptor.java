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
package com.ericsson.oss.services.ap.common.util.cdi;

import javax.ejb.ApplicationException;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.jms.IllegalStateException;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.slf4j.Logger;

/**
 * Transactional interceptor which is bound to {@link Transactional} annotation.
 * <p>
 * For txType REQUIRES_NEW, any active transaction will be suspended and the method will be invoked in a new transaction. The original transaction
 * will be resumed once execution has completed. In case there is no active transaction, a new transaction will be started.
 * <p>
 * For txType REQUIRED, a new transaction will be started if there is no active transaction ongoing. If there is an active transaction then the
 * interceptor will do nothing and the method will be invoked in the currently active transaction.
 *
 * @see Transactional
 */
@Interceptor
@Transactional
public class TransactionalInterceptor {

    @Inject
    private JNDIUtil jndiUtil;

    @Inject
    private Logger logger;

    @AroundInvoke
    public Object invoke(final InvocationContext ctx) throws Exception { //NOPMD
        if (!isTransactionalAnnotationPresent(ctx)) {
            return ctx.proceed();
        }

        final Transactional transactionalAnnotation = ctx.getMethod().getAnnotation(Transactional.class);

        logger.info("Intercepted method {} in class {}", ctx.getMethod().getName(), ctx.getMethod().getDeclaringClass().getName());

        final TransactionManager tm = jndiUtil.doLookup("java:/jboss/TransactionManager");

        switch (transactionalAnnotation.txType()) {
            case REQUIRES:
                return processRequiredTxType(ctx, tm);
            case REQUIRES_NEW:
                return processRequiresNewTxType(ctx, tm);
            default:
                throw new IllegalStateException("Unexpected TxType -> " + transactionalAnnotation.txType());
        }

    }

    private static boolean isTransactionalAnnotationPresent(final InvocationContext ctx) {
        return ctx.getMethod().isAnnotationPresent(Transactional.class);
    }

    private Object processRequiresNewTxType(final InvocationContext ctx, final TransactionManager tm) throws Exception { //NOPMD
        logger.debug("Suspending transaction -> {}", tm.getTransaction());
        final Transaction callerTx = tm.suspend(); //will return null if no associated tx

        try {
            return invokeInNewTx(ctx, tm);
        } finally {
            if (callerTx != null) {
                tm.resume(callerTx);
                logger.debug("Resumed caller tx -> {}", callerTx);
            }
        }
    }

    private Object processRequiredTxType(final InvocationContext ctx, final TransactionManager tm) throws Exception { //NOPMD
        if (tm.getTransaction() == null) {
            return invokeInNewTx(ctx, tm);
        } else {
            return ctx.proceed();
        }
    }

    private Object invokeInNewTx(final InvocationContext ctx, final TransactionManager tm) throws Exception { //NOPMD
        tm.begin();
        final Transaction newTransaction = tm.getTransaction();
        logger.debug("Proceeding in new tx -> {}", newTransaction);

        try {
            return ctx.proceed();
        } catch (final Exception e) {
            handleErrorInNewTx(tm, e);
        } finally {
            endTransaction(tm, newTransaction);
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

    private static boolean isApplicationExceptionWithNoRollback(final Exception e) {
        for (Class<?> clazz = e.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            final ApplicationException ae = clazz.getAnnotation(ApplicationException.class);
            if (ae != null) {
                if (ae.inherited()) {
                    return !ae.rollback();
                } else {
                    break;
                }
            }
        }
        return false;
    }

    private static void endTransaction(final TransactionManager tm, final Transaction tx) throws Exception { //NOSONAR
        if (Status.STATUS_MARKED_ROLLBACK == tx.getStatus()) {
            tm.rollback();
        } else {
            tm.commit();
        }
    }
}
