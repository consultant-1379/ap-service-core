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
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.object.builder.MibRootBuilder;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
/**
 * Unit tests for {@link ApAccountsMoCreator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApAccountsMoCreatorTest {

    private static final String AP_ACCOUNTS_FDN = "AutoProvisioningAccounts=1";

    @Mock
    private DataBucket liveBucket;

    @Mock
    private DpsOperations dps;

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private ManagedObject apAccountsMo;

    @Mock
    private MibRootBuilder mibRootBuilder;

    @Mock
    private ModelReader modelReader;

    @InjectMocks
    private ApAccountsMoCreator apAccountsMoCreator;

    @Mock
    private DataPersistenceService dpsService;

    @Before
    public void setUp() {
        final ModelData modelData = new ModelData("ap", "1.0.0");
        when(modelReader.getLatestPrimaryTypeModel(Namespace.AP.toString(), MoType.AP_ACCOUNTS.toString())).thenReturn(modelData);
        when(dps.getDataPersistenceService()).thenReturn(dpsService);
        when(dps.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.getMibRootBuilder()).thenReturn(mibRootBuilder);
        when(mibRootBuilder.namespace(anyString())).thenReturn(mibRootBuilder);
        when(mibRootBuilder.version(anyString())).thenReturn(mibRootBuilder);
        when(mibRootBuilder.type(anyString())).thenReturn(mibRootBuilder);
        when(mibRootBuilder.name(anyString())).thenReturn(mibRootBuilder);
        when(mibRootBuilder.addAttributes(anyMapOf(String.class, Object.class))).thenReturn(mibRootBuilder);
        when(apAccountsMo.getFdn()).thenReturn(AP_ACCOUNTS_FDN);
    }

    @Test
    public void whenCreateMo_andMoDoesNotAlreadyExist_thenMoIsCreatedAndReturned() {
        when(mibRootBuilder.create()).thenReturn(apAccountsMo);
        final ManagedObject result = apAccountsMoCreator.create();
        assertEquals(AP_ACCOUNTS_FDN, result.getFdn());
    }

    @Test
    public void whenCreateMo_andMoAlreadyExists_thenMoCreateFails_andMoIsRetrievedAndReturned() {
        doThrow(Exception.class).when(mibRootBuilder).create();
        when(liveBucket.findMoByFdn(AP_ACCOUNTS_FDN)).thenReturn(apAccountsMo);

        final ManagedObject result = apAccountsMoCreator.create();
        assertEquals(AP_ACCOUNTS_FDN, result.getFdn());
    }

    @Test(expected = ApServiceException.class)
    public void whenCreateMo_andCreateFails_thenCreateRetries_thenApServiceExceptionIsThrownAfterMaxAttempts() {
        doThrow(Exception.class).when(mibRootBuilder).create();
        when(liveBucket.findMoByFdn(AP_ACCOUNTS_FDN)).thenReturn(null);
        apAccountsMoCreator.create();
    }
}
