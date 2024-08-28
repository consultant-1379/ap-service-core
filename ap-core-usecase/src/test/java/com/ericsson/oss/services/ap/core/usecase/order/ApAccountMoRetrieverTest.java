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

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.security.cryptography.CryptographyService;
import com.ericsson.oss.services.ap.api.account.ApAccount;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor;
import com.ericsson.oss.services.ap.model.AccountType;

/**
 * Unit tests for {@link ApAccountMoRetriever}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ApAccountMoRetrieverTest {

    private static final String PASSWORD = "password";
    private static final String USER_NAME = "userName";
    private static final String NODENAME = "nodeName";

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private DpsQueryExecutor<ManagedObject> dpsQueryExecutor;

    @Mock
    private ManagedObject apAccountMo;

    @InjectMocks
    private ApAccountMoRetriever apAccountMoRetriever;

    @Spy
    private final CryptographyService cyptographyService = new CryptographyService() { // NOPMD
        @Override
        public byte[] decrypt(final byte[] bytes) {
            return PASSWORD.getBytes();
        }

        @Override
        public byte[] encrypt(final byte[] bytes) {
            return PASSWORD.getBytes();
        }
    };

    @Before
    public void setUp() {
        when(dpsQueries.findMoByName(VALID_NODE_TYPE, "AutoProvisioningAccount", "ap")).thenReturn(dpsQueryExecutor);
        when(apAccountMo.getAttribute("accountType")).thenReturn(AccountType.LDAP.toString());
        when(apAccountMo.getAttribute("nodeType")).thenReturn(VALID_NODE_TYPE);
        when(apAccountMo.getAttribute("userName")).thenReturn(USER_NAME);
        when(apAccountMo.getAttribute("password")).thenReturn(PASSWORD);
    }

    @Test
    public void whenGetAccountMo_thenAccountIsReturned() {
        final List<ManagedObject> apAccountMos = new ArrayList<>();
        apAccountMos.add(apAccountMo);
        when(dpsQueryExecutor.execute()).thenReturn(apAccountMos.iterator());

        final ApAccount result = apAccountMoRetriever.getAccount(AccountType.LDAP.toString(), VALID_NODE_TYPE);

        final ApAccount expectedAccount = new ApAccount(AccountType.LDAP.toString(), VALID_NODE_TYPE, USER_NAME, PASSWORD, NODENAME);

        new EqualsBuilder()
            .append(expectedAccount.getAccountType(), result.getAccountType())
            .append(expectedAccount.getNodeType(), result.getNodeType())
            .append(expectedAccount.getUserName(), result.getUserName())
            .append(expectedAccount.getPassword(), result.getPassword())
            .append(expectedAccount.getNodeName(), result.getNodeName())
            .isEquals();
    }

    @Test
    public void whenGetAccountMo_andNoAccountOfGivenTypeExists_thenNullIsReturned() {
        final List<ManagedObject> apAccountMos = new ArrayList<>();
        apAccountMos.add(apAccountMo);
        when(dpsQueryExecutor.execute()).thenReturn(apAccountMos.iterator());
        final ApAccount result = apAccountMoRetriever.getAccount("invalidAccountType", VALID_NODE_TYPE);
        assertNull(result);
    }

    @Test
    public void whenGetAccountMo_andNoAccountExists_thenNullIsReturned() {
        when(dpsQueryExecutor.execute()).thenReturn(Collections.<ManagedObject> emptyIterator());
        final ApAccount result = apAccountMoRetriever.getAccount(AccountType.LDAP.toString(), VALID_NODE_TYPE);
        assertNull(result);
    }
}
