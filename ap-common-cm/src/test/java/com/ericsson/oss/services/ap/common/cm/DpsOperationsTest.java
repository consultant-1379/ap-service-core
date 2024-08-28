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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.object.builder.MibRootBuilder;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.model.ModelData;

/**
 * Tests for {@link DpsOperations}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DpsOperationsTest {

    private static final String MECONTEXT_FDN = "MeContext=1";
    private static final String MANAGEDELEMENT_FDN = MECONTEXT_FDN + ",ManagedElement=1";

    @Mock
    private DpsOperations dps;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private ManagedObject mo;

    @Mock
    private TransactionalExecutor executor;

    @Mock
    private MibRootBuilder mibRootBuilder;

    @InjectMocks
    private DpsOperations dpsOperations;

    @Mock
    private DataPersistenceService dpsService;

    @Before
    public void setUp() {
        when(dps.getDataPersistenceService()).thenReturn(dpsService);
        when(dps.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
    }

    private final Answer<Object> transactionalExecutorAnswer = new Answer<Object>() {

        @SuppressWarnings("unchecked")
        @Override
        public Object answer(final InvocationOnMock invocation) throws Throwable {
            final Callable<Object> callable = (Callable<Object>) invocation.getArguments()[0];
            return callable.call();
        }
    };

    @SuppressWarnings("unchecked")
    @Test
    public void whenUpdateNonExistingMoThenDoNothing() throws Exception { // NOSONAR
        when(executor.execute(any(Callable.class))).then(transactionalExecutorAnswer);
        when(liveBucket.findMoByFdn(MECONTEXT_FDN)).thenReturn(null);

        dpsOperations.updateMo(MECONTEXT_FDN, "attr1", "value1");
        verify(mo, never()).setAttribute("attr1", "value1");
    }

    @SuppressWarnings("unchecked")
    @Test(expected = ApServiceException.class)
    public void whenUpdateMoWithFailureAndMoDoesNotExistThenApServiceExceptionIsPropagated() throws Exception { // NOSONAR
        when(executor.execute(any(Callable.class))).then(transactionalExecutorAnswer);
        when(liveBucket.findMoByFdn(MECONTEXT_FDN)).thenReturn(null);
        dpsOperations.updateMoWithFailure(MECONTEXT_FDN, "attr1", "value1");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenUpdateMoWithFailureAndMoExistsThenMoIsUpdated() throws Exception { // NOSONAR
        when(executor.execute(any(Callable.class))).then(transactionalExecutorAnswer);
        when(liveBucket.findMoByFdn(MECONTEXT_FDN)).thenReturn(mo);
        dpsOperations.updateMoWithFailure(MECONTEXT_FDN, "attr1", "value1");
        verify(mo).setAttribute("attr1", "value1");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenDeleteNonExistingMoThenDoNothing() throws Exception { // NOSONAR
        when(executor.execute(any(Callable.class))).then(transactionalExecutorAnswer);
        when(liveBucket.findMoByFdn(MECONTEXT_FDN)).thenReturn(null);

        dpsOperations.deleteMo(MECONTEXT_FDN);
        verify(liveBucket, never()).deletePo(mo);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenDeleteExistingMoThenMoIsSuccessfullyDeleted() throws Exception { // NOSONAR
        when(executor.execute(any(Callable.class))).then(transactionalExecutorAnswer);
        when(liveBucket.findMoByFdn(MECONTEXT_FDN)).thenReturn(mo);

        dpsOperations.deleteMo(MECONTEXT_FDN);
        verify(liveBucket).deletePo(mo);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenCreateRootMoWithNoParentThenMoIsSuccessfullyCreated() throws Exception { // NOSONAR
        final ModelData meContextModel = new ModelData("OSS_TOP", "1.0.0");
        final Map<String, Object> attributes = new HashMap<>();

        when(executor.execute(any(Callable.class))).then(transactionalExecutorAnswer);
        when(liveBucket.getMibRootBuilder()).thenReturn(mibRootBuilder);
        when(mibRootBuilder.name("1")).thenReturn(mibRootBuilder);
        when(mibRootBuilder.type("MeContext")).thenReturn(mibRootBuilder);
        when(mibRootBuilder.namespace("OSS_TOP")).thenReturn(mibRootBuilder);
        when(mibRootBuilder.version("1.0.0")).thenReturn(mibRootBuilder);
        when(mibRootBuilder.addAttributes(attributes)).thenReturn(mibRootBuilder);
        when(mibRootBuilder.create()).thenReturn(mo);

        final ManagedObject createdMo = dpsOperations.createRootMo(MECONTEXT_FDN, meContextModel, attributes);
        assertEquals(mo, createdMo);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenCreateRootMoWithParentThenMoIsSuccessfullyCreated() throws Exception { // NOSONAR
        final ModelData managedElementModel = new ModelData("CPP_MED", "1.0.0");
        final Map<String, Object> attributes = new HashMap<>();
        final ManagedObject parentMo = Mockito.mock(ManagedObject.class);

        when(executor.execute(any(Callable.class))).then(transactionalExecutorAnswer);
        when(liveBucket.getMibRootBuilder()).thenReturn(mibRootBuilder);
        when(mibRootBuilder.name("1")).thenReturn(mibRootBuilder);
        when(mibRootBuilder.type("ManagedElement")).thenReturn(mibRootBuilder);
        when(mibRootBuilder.namespace("CPP_MED")).thenReturn(mibRootBuilder);
        when(mibRootBuilder.version("1.0.0")).thenReturn(mibRootBuilder);
        when(mibRootBuilder.parent(parentMo)).thenReturn(mibRootBuilder);
        when(mibRootBuilder.addAttributes(attributes)).thenReturn(mibRootBuilder);
        when(mibRootBuilder.create()).thenReturn(mo);

        final ManagedObject createdMo = dpsOperations.createRootMo(MANAGEDELEMENT_FDN, managedElementModel, attributes);
        assertEquals(mo, createdMo);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void whenPerformActionThenActionIsSuccessfullyExecuted() throws Exception { // NOSONAR
        when(executor.execute(any(Callable.class))).then(transactionalExecutorAnswer);
        when(liveBucket.findMoByFdn(MECONTEXT_FDN)).thenReturn(mo);

        dpsOperations.performMoAction(MECONTEXT_FDN, "action1");
        verify(mo).performAction("action1", Collections.<String, Object> emptyMap());
    }
}
