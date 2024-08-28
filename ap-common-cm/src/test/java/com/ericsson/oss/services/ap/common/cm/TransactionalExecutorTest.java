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

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;

import javax.ejb.ApplicationException;
import javax.naming.NamingException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.common.util.cdi.JNDIUtil;

/**
 * Unit tests for {@link TransactionalExecutor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionalExecutorTest {

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private JNDIUtil jndiUtil;

    @Mock
    private TransactionManager tm;

    @Mock
    private Transaction callerTx;

    @Mock
    private Transaction newTx;

    @Mock
    private Callable<Boolean> callable;

    @InjectMocks
    private TransactionalExecutor txExecutor;

    @Before
    public void setup() throws NamingException {
        when(jndiUtil.doLookup("java:/jboss/TransactionManager")).thenReturn(tm);
    }

    @Test(expected = ApServiceException.class)
    public void whenNamingServiceErrorResolvingTransactionManagedThenThrowApServiceException() throws Exception { // NOPMD
        when(jndiUtil.doLookup("java:/jboss/TransactionManager")).thenThrow(new NamingException());

        txExecutor.execute(callable);
    }

    @Test
    public void whenNoExistingTxThenNoAttemptToResumeCallerTxAfterCallableExecutes() throws Exception { // NOPMD
        when(callable.call()).thenReturn(true);
        when(tm.suspend()).thenReturn(null);
        when(tm.getTransaction()).thenReturn(newTx);

        assertTrue(txExecutor.execute(callable));

        verify(tm).begin();
        verify(tm).commit();
        verify(tm, never()).resume(any(Transaction.class));
    }

    @Test
    public void whenExistingTxThenSuspendExistingTxAndExecuteCallableInNewTx() throws Exception { // NOPMD
        when(callable.call()).thenReturn(true);
        when(tm.suspend()).thenReturn(callerTx);
        when(tm.getTransaction()).thenReturn(newTx);

        assertTrue(txExecutor.execute(callable));

        verify(tm).begin();
        verify(tm).commit();
    }

    @Test
    public void whenExistingTxThenExistingTxIsResumedAfterCallableExecutes() throws Exception { // NOPMD
        when(callable.call()).thenReturn(true);
        when(tm.suspend()).thenReturn(callerTx);
        when(tm.getTransaction()).thenReturn(newTx);

        assertTrue(txExecutor.execute(callable));

        verify(tm).resume(callerTx);
    }

    @Test
    public void whenNewTxFailsWithApplicationExceptionMarkedWithNoRollbackThenTxIsCommitted() throws Exception { // NOPMD
        when(callable.call()).thenThrow(new NoRollbackApplicationException());
        when(tm.suspend()).thenReturn(null);
        when(tm.getTransaction()).thenReturn(newTx);

        try {
            txExecutor.execute(callable);
        } catch (final Exception e) {
            verify(tm, never()).setRollbackOnly();
            verify(tm).commit();
        }
    }

    @Test
    public void whenNewTxFailsWithApplicationExceptionMarkedWithRollbackThenTxIsRolledBack() throws Exception { // NOPMD
        when(callable.call()).thenThrow(new RollbackApplicationException());
        when(tm.suspend()).thenReturn(null);
        when(tm.getTransaction()).thenReturn(newTx);

        try {
            txExecutor.execute(callable);
        } catch (final Exception e) {
            verify(tm).setRollbackOnly();
        }
    }

    @Test
    public void whenNewTxFailsWithRuntimeExceptionThenTxIsRolledBack() throws Exception { // NOPMD
        when(callable.call()).thenThrow(new IllegalStateException());
        when(tm.suspend()).thenReturn(null);
        when(tm.getTransaction()).thenReturn(newTx);

        try {
            txExecutor.execute(callable);
        } catch (final Exception e) {
            verify(tm).setRollbackOnly();
        }
    }

    @ApplicationException(rollback = true)
    class RollbackApplicationException extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }

    @ApplicationException(rollback = false)
    class NoRollbackApplicationException extends RuntimeException {

        private static final long serialVersionUID = 1L;

    }

}
