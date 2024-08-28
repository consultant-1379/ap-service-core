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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.object.builder.ManagedObjectBuilder;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.security.cryptography.CryptographyService;
import com.ericsson.oss.services.ap.api.account.ApAccount;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
/**
 * Unit tests for {@link ApAccountMoCreator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApAccountMoCreatorTest {

    private static final String ACCOUNT_TYPE = "LDAP";
    private static final String USER_NAME = "userName";
    private static final String PASSWORD = "password";
    private static final String NODENAME = "nodeName";
    private static final String AP_ACCOUNT_FDN = "AutoProvisioningAccounts=1,AutoProvisioningAccount=" + NODENAME;

    private static final ApAccount AP_ACCOUNT = new ApAccount(ACCOUNT_TYPE, VALID_NODE_TYPE, USER_NAME, PASSWORD, NODENAME);

    @Mock
    private DataBucket liveBucket;

    @Mock
    private DpsOperations dps;

    @Mock
    private DataPersistenceService dpsService;

    @Mock
    private CryptographyService cryptopgraphyService;

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private ManagedObject apAccountMo;

    @Mock
    private ManagedObject apAccountsMo;

    @Mock
    private ManagedObjectBuilder moBuilder;

    @Mock
    private ModelReader modelReader;

    @Mock
    private ApAccountsMoRetriever apAccountsetriever;

    @InjectMocks
    private ApAccountMoCreator apAccountMoCreator;

    @Before
    public void setUp() {
        final ModelData modelData = new ModelData("ap", "1.0.0");
        when(modelReader.getLatestPrimaryTypeModel(Namespace.AP.toString(), MoType.AP_ACCOUNT.toString())).thenReturn(modelData);
        when(dps.getDataPersistenceService()).thenReturn(dpsService);
        when(dps.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.getManagedObjectBuilder()).thenReturn(moBuilder);
        when(moBuilder.parent(any(ManagedObject.class))).thenReturn(moBuilder);
        when(moBuilder.type(anyString())).thenReturn(moBuilder);
        when(moBuilder.name(anyString())).thenReturn(moBuilder);
        when(moBuilder.addAttributes(anyMapOf(String.class, Object.class))).thenReturn(moBuilder);
        when(apAccountMo.getFdn()).thenReturn(AP_ACCOUNT_FDN);
        when(cryptopgraphyService.encrypt(any(byte[].class))).thenReturn(PASSWORD.getBytes());
        when(apAccountsetriever.getAccountsMo()).thenReturn(apAccountsMo);
    }

    @Test
    public void whenCreateMo_andMoDoesNotAlreadyExist_thenMoIsCreatedAndReturned() {
        when(moBuilder.create()).thenReturn(apAccountMo);
        final ManagedObject result = apAccountMoCreator.create(AP_ACCOUNT);
        assertEquals(AP_ACCOUNT_FDN, result.getFdn());
    }

    @Test
    public void whenCreateMo_andMoAlreadyExists_thenMoCreateFails_andMoIsRetrievedAndReturned() {
        doThrow(Exception.class).when(moBuilder).create();
        when(liveBucket.findMoByFdn(AP_ACCOUNT_FDN)).thenReturn(apAccountMo);

        final ManagedObject result = apAccountMoCreator.create(AP_ACCOUNT);
        assertEquals(AP_ACCOUNT_FDN, result.getFdn());
    }

    @Test(expected = ApServiceException.class)
    public void whenCreateMo_andCreateFails_thenCreateRetries_thenApServiceExceptionIsThrownAfterMaxAttempts() {
        doThrow(Exception.class).when(moBuilder).create();
        when(liveBucket.findMoByFdn(AP_ACCOUNT_FDN)).thenReturn(null);
        apAccountMoCreator.create(AP_ACCOUNT);
    }
}
