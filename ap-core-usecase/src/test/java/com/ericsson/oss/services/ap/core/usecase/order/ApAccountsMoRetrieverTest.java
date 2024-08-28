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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor;

/**
 * Unit tests for {@link ApAccountsMoRetriever}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApAccountsMoRetrieverTest {

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private DpsQueryExecutor<ManagedObject> dpsQueryExecutor;

    @Mock
    private ManagedObject apAccountsMo;

    @InjectMocks
    private ApAccountsMoRetriever apAccountsMoRetriever;

    @Before
    public void setUp() {
        when(dpsQueries.findMosByType("AutoProvisioningAccounts", "ap")).thenReturn(dpsQueryExecutor);
    }

    @Test
    public void whenGetAccountsMo_thenApAccountsMoIsReturned() {
        final List<ManagedObject> apAccountsMos = new ArrayList<>();
        apAccountsMos.add(apAccountsMo);
        when(dpsQueryExecutor.execute()).thenReturn(apAccountsMos.iterator());

        final ManagedObject result = apAccountsMoRetriever.getAccountsMo();

        assertEquals(apAccountsMo, result);
    }

    @Test
    public void whenGetAccountsMo_andNoMoExists_thenNullIsReturned() {
        when(dpsQueryExecutor.execute()).thenReturn(Collections.<ManagedObject> emptyIterator());
        final ManagedObject result = apAccountsMoRetriever.getAccountsMo();
        assertNull(result);
    }
}
