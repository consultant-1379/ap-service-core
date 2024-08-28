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

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import javax.ejb.ApplicationException;
import javax.interceptor.InvocationContext;
import javax.naming.NamingException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.services.ap.common.util.cdi.Transactional.TxType;

/**
 * Unit tests for {@link TransactionalInterceptor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TransactionalInterceptorTest {

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
    private InvocationContext ic;

    @InjectMocks
    private TransactionalInterceptor txInterceptor;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws NamingException {
        when(jndiUtil.doLookup("java:/jboss/TransactionManager")).thenReturn(tm);
    }

    @Test
    public void whenMethodNotAnnotatedThenDoNothingAndProceed() throws Exception { // NOPMD
        when(ic.getMethod()).thenReturn(TestMethods.class.getMethod("nonTransactionalMethod"));
        txInterceptor.invoke(ic);
        verifyZeroInteractions(tm);
    }

    @Test
    public void whenRequiresNewAndNoExistingTxThenProceedInNewTx() throws Exception { // NOPMD
        when(ic.getMethod()).thenReturn(TestMethods.class.getMethod("requiresNewMethod"));
        when(tm.suspend()).thenReturn(null);
        when(ic.proceed()).thenReturn(true);
        when(tm.getTransaction()).thenReturn(newTx);

        final boolean result = (boolean) txInterceptor.invoke(ic);
        assertTrue(result);

        verify(tm).begin();
        verify(tm).commit();
    }

    @Test
    public void whenRequiresNewAndNoExistinTxThenNoAttemptToResumeCallerTx() throws Exception { // NOPMD
        when(ic.getMethod()).thenReturn(TestMethods.class.getMethod("requiresNewMethod"));
        when(tm.suspend()).thenReturn(null);
        when(tm.getTransaction()).thenReturn(newTx);

        txInterceptor.invoke(ic);

        verify(tm, never()).resume(any(Transaction.class));
    }

    @Test
    public void whenRequiresNewAndExistingTxThenSuspendExistingTxAndProceedInNewTx() throws Exception { // NOPMD
        when(ic.getMethod()).thenReturn(TestMethods.class.getMethod("requiresNewMethod"));
        when(tm.suspend()).thenReturn(callerTx);
        when(ic.proceed()).thenReturn(true);
        when(tm.getTransaction()).thenReturn(newTx);

        final boolean result = (boolean) txInterceptor.invoke(ic);
        assertTrue(result);

        verify(tm).begin();
        verify(tm).commit();
    }

    @Test
    public void whenRequiresNewAndExistingTxThenExistingTxIsResumedAfterNewTxCompletes() throws Exception { // NOPMD
        when(ic.getMethod()).thenReturn(TestMethods.class.getMethod("requiresNewMethod"));
        when(tm.suspend()).thenReturn(callerTx);
        when(tm.getTransaction()).thenReturn(newTx);

        txInterceptor.invoke(ic);

        verify(tm).resume(callerTx);
    }

    @Test
    public void whenRequiredAndExistingTxThenProceedInExistingTx() throws Exception { // NOPMD
        when(ic.getMethod()).thenReturn(TestMethods.class.getMethod("requiredMethod"));
        when(tm.suspend()).thenReturn(callerTx);
        when(ic.proceed()).thenReturn(true);
        when(tm.getTransaction()).thenReturn(callerTx);

        final boolean result = (boolean) txInterceptor.invoke(ic);
        assertTrue(result);

        verify(tm, never()).begin();
        verify(tm, never()).commit();
        verify(tm, never()).resume(any(Transaction.class));
    }

    @Test
    public void whenRequiredAndNoExistingTxThenProceedInNewTx() throws Exception { // NOPMD
        when(ic.getMethod()).thenReturn(TestMethods.class.getMethod("requiredMethod"));
        when(tm.suspend()).thenReturn(null);
        when(ic.proceed()).thenReturn(true);
        when(tm.getTransaction()).thenReturn(null).thenReturn(newTx);

        final boolean result = (boolean) txInterceptor.invoke(ic);
        assertTrue(result);

        verify(tm).begin();
        verify(tm).commit();
    }

    @Test
    public void whenNewTxFailsWithApplicationExceptionMarkedWithNoRollbackThenTxIsCommitted() throws Exception { // NOPMD
        when(ic.getMethod()).thenReturn(TestMethods.class.getMethod("requiresNewMethod"));
        when(tm.suspend()).thenReturn(null);
        when(ic.proceed()).thenThrow(new NoRollbackApplicationException());
        when(tm.getTransaction()).thenReturn(newTx);

        thrown.expect(NoRollbackApplicationException.class);

        txInterceptor.invoke(ic);

        verify(tm).begin();
        verify(tm).commit();
    }

    @Test
    public void whenNewTxFailsWithApplicationExceptionMarkedWithRollbackThenTxIsRolledBack() throws Exception { // NOPMD
        when(ic.getMethod()).thenReturn(TestMethods.class.getMethod("requiresNewMethod"));
        when(tm.suspend()).thenReturn(null);
        when(ic.proceed()).thenThrow(new RollbackApplicationException());
        when(tm.getTransaction()).thenReturn(newTx);

        thrown.expect(RollbackApplicationException.class);

        txInterceptor.invoke(ic);

        verify(tm).setRollbackOnly();
    }

    @Test
    public void whenNewTxFailsWithRuntimeexceptionThenTxIsRolledBack() throws Exception { // NOPMD
        when(ic.getMethod()).thenReturn(TestMethods.class.getMethod("requiresNewMethod"));
        when(tm.suspend()).thenReturn(null);
        when(ic.proceed()).thenThrow(new IllegalStateException());
        when(tm.getTransaction()).thenReturn(newTx);

        thrown.expect(IllegalStateException.class);

        txInterceptor.invoke(ic);

        verify(tm).setRollbackOnly();
    }

    class TestMethods {

        @Transactional(txType = TxType.REQUIRES_NEW)
        public void requiresNewMethod() {
            throw new UnsupportedOperationException();
        }

        @Transactional(txType = TxType.REQUIRES)
        public void requiredMethod() {
            throw new UnsupportedOperationException();
        }

        public void nonTransactionalMethod() {
            throw new UnsupportedOperationException();
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
