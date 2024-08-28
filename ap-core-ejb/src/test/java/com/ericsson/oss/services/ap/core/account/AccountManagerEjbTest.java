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
package com.ericsson.oss.services.ap.core.account;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.security.identitymgmtservices.IdentityManagementService;
import com.ericsson.oss.itpf.security.identitymgmtservices.ProxyAgentAccountData;
import com.ericsson.oss.services.ap.api.account.ApAccount;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.core.usecase.order.ApAccountMoCreator;
import com.ericsson.oss.services.ap.core.usecase.order.ApAccountMoRetriever;
import com.ericsson.oss.services.ap.core.usecase.order.ApAccountsMoCreator;
import com.ericsson.oss.services.ap.core.usecase.order.ApAccountsMoRetriever;

/**
 * Unit tests for {@link AccountManagerEjb}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AccountManagerEjbTest {

    private static final String ERBS_NODE_TYPE = "ERBS";
    private static final String RADIONODE_NODE_TYPE = "RadioNode";
    private static final String LDAP_ACCOUNT_TYPE = "LDAP";
    private static final String NODE_ID = "Node1";
    private static final ApAccount LDAP_ERBS_AP_ACCOUNT = new ApAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE, "userName", "password");
    private static final ApAccount LDAP_ERBS_AP_ACCOUNT1 = new ApAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE, "userName", "password", NODE_ID);

    @Mock
    private ApAccountsMoCreator apAccountsCreator;

    @Mock
    private ApAccountMoCreator apAccountCreator;

    @Mock
    private ApAccountMoRetriever apAccountRetriever;

    @Mock
    private ApAccountsMoRetriever apAccountsRetriever;

    @Mock
    private IdentityManagementService idmService;

    @Mock
    private ManagedObject apAccountsMo;

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private AccountManagerEjb accountManager1;

    @InjectMocks
    private AccountManagerEjb accountManager;

    @Test
    public void whenLdapAccountExistsInCache_thenAccountShouldBeReturnedWithoutCheckingDps_andNoNewLdapAccountShouldBeCreated() {
        when(apAccountRetriever.getAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE)).thenReturn(LDAP_ERBS_AP_ACCOUNT);
        accountManager.getAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE); // Will add to cache

        accountManager.getAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE);
        verify(apAccountRetriever, times(1)).getAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE); // 1 time to add to cache
        verify(idmService, never()).createProxyAgentAccount();
    }

    @Test
    public void whenLdapAccount1ExistsInCache_thenAccountShouldBeReturnedWithoutCheckingDps_andNoNewLdapAccountShouldBeCreated() {
        when(apAccountRetriever.getAccount(LDAP_ACCOUNT_TYPE, NODE_ID)).thenReturn(LDAP_ERBS_AP_ACCOUNT1);
        accountManager.getAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE, NODE_ID); // Will add to cache

        accountManager.getAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE, NODE_ID);
        verify(apAccountRetriever, times(1)).getAccount(LDAP_ACCOUNT_TYPE, NODE_ID); // 1 time to add to cache
        verify(idmService, never()).createProxyAgentAccount();
    }

    @Test
    public void whenLdapAccountDoesNotExistInCache_andAccountExistsInDps_thenAccountShouldBeReturned_andNoNewLdapAccountShouldBeCreated() {
        when(apAccountRetriever.getAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE)).thenReturn(LDAP_ERBS_AP_ACCOUNT);

        accountManager.getAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE);

        verify(apAccountRetriever, times(1)).getAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE);
        verify(idmService, never()).createProxyAgentAccount();
    }

    @Test
    public void whenLdapAccount1DoesNotExistInCache_andAccountExistsInDps_thenAccountShouldBeReturned_andNoNewLdapAccountShouldBeCreated() {
        when(apAccountRetriever.getAccount(LDAP_ACCOUNT_TYPE, NODE_ID)).thenReturn(LDAP_ERBS_AP_ACCOUNT1);

        accountManager.getAccount(LDAP_ACCOUNT_TYPE, NODE_ID);

        verify(apAccountRetriever, times(1)).getAccount(LDAP_ACCOUNT_TYPE, NODE_ID);
        verify(idmService, never()).createProxyAgentAccount();
    }

    @Test
    public void whenLdapAccountIsNotInCacheOrDps_andRootAccountsMoExists_thenNewAccountShouldBeCreated_andNewAccountMoCreatedInDps() {
        when(apAccountsRetriever.getAccountsMo()).thenReturn(apAccountsMo);
        when(apAccountRetriever.getAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE)).thenReturn(null);
        when(idmService.createProxyAgentAccount()).thenReturn(new ProxyAgentAccountData("userName", "password"));

        accountManager.getAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE);

        verify(apAccountsCreator, never()).create();
        verify(apAccountCreator, times(1)).create(any(ApAccount.class));
        verify(idmService, times(1)).createProxyAgentAccount();
    }

    @Test
    public void whenLdapAccount1IsNotInCacheOrDps_andRootAccountsMoExists_thenNewAccountShouldBeCreated_andNewAccountMoCreatedInDps() {
        when(apAccountsRetriever.getAccountsMo()).thenReturn(apAccountsMo);
        when(apAccountRetriever.getAccount(LDAP_ACCOUNT_TYPE, NODE_ID)).thenReturn(null);
        when(idmService.createProxyAgentAccount()).thenReturn(new ProxyAgentAccountData("userName", "password"));

        accountManager.getAccount(LDAP_ACCOUNT_TYPE, NODE_ID);

        verify(apAccountsCreator, never()).create();
        verify(apAccountCreator, times(1)).create(any(ApAccount.class));
        verify(idmService, times(1)).createProxyAgentAccount();
    }

    @Test
    public void whenLdapAccountIsNotInCacheOrDps_andRootAccountMoDoesNotExist_thenNewAccountShouldBeCreated_andNewAccountsMo_andNewAccountMoCreatedInDps() {
        when(apAccountsRetriever.getAccountsMo()).thenReturn(null);
        when(apAccountRetriever.getAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE)).thenReturn(null);
        when(idmService.createProxyAgentAccount()).thenReturn(new ProxyAgentAccountData("userName", "password"));
        when(apAccountsCreator.create()).thenReturn(apAccountsMo);

        accountManager.getAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE);

        verify(apAccountsCreator, times(1)).create();
        verify(apAccountCreator, times(1)).create(any(ApAccount.class));
        verify(idmService, times(1)).createProxyAgentAccount();
    }

    @Test
    public void whenLdapAccount1IsNotInCacheOrDps_andRootAccountMoDoesNotExist_thenNewAccountShouldBeCreated_andNewAccountsMo_andNewAccountMoCreatedInDps() {
        when(apAccountsRetriever.getAccountsMo()).thenReturn(null);
        when(apAccountRetriever.getAccount(LDAP_ACCOUNT_TYPE, NODE_ID)).thenReturn(null);
        when(idmService.createProxyAgentAccount()).thenReturn(new ProxyAgentAccountData("userName", "password"));
        when(apAccountsCreator.create()).thenReturn(apAccountsMo);

        accountManager.getAccount(LDAP_ACCOUNT_TYPE, NODE_ID);

        verify(apAccountsCreator, times(1)).create();
        verify(apAccountCreator, times(1)).create(any(ApAccount.class));
        verify(idmService, times(1)).createProxyAgentAccount();
    }

    @Test(expected = ApApplicationException.class)
    public void whenRetrievingAccountOfInvalidType_thenApApplicationExceptionIsThrown() {
        accountManager.getAccount("invalidAccountType", ERBS_NODE_TYPE);
    }
    @Test(expected = ApApplicationException.class)
    public void whenRetrievingAccount1OfInvalidType_thenApApplicationExceptionIsThrown() {
        accountManager.getAccount("invalidAccountType", ERBS_NODE_TYPE, NODE_ID);
    }

    @Test
    public void whenCreatingNewLdapAccount_andAccountIsCreated_andFailureSavingMoToDps_thenAccountShouldBeDeleted_andApApplicationExceptionThrown() {
        when(apAccountsRetriever.getAccountsMo()).thenReturn(apAccountsMo);
        when(apAccountRetriever.getAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE)).thenReturn(null);
        when(idmService.createProxyAgentAccount()).thenReturn(new ProxyAgentAccountData("userName", "password"));
        doThrow(IllegalStateException.class).when(apAccountCreator).create(any(ApAccount.class));

        try {
            accountManager.getAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE);
        } catch (final Exception e) {
            assertEquals(ApApplicationException.class, e.getClass());
        }

        verify(apAccountCreator, times(5)).create(any(ApAccount.class));
        verify(idmService, times(1)).createProxyAgentAccount();
        verify(idmService, times(1)).deleteProxyAgentAccount(anyString());
    }

    @Test
    public void whileCreatingNewLdapAccount_FailureSavingMoToDps_thenAccountShouldBeDeleted_andApApplicationExceptionThrown() {
        when(apAccountsRetriever.getAccountsMo()).thenReturn(apAccountsMo);
        when(apAccountRetriever.getAccount(LDAP_ACCOUNT_TYPE, NODE_ID)).thenReturn(null);
        when(idmService.createProxyAgentAccount()).thenReturn(new ProxyAgentAccountData("userName", "password"));
        doThrow(IllegalStateException.class).when(apAccountCreator).create(any(ApAccount.class));

        try {
            accountManager.getAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE, NODE_ID);
        } catch (final Exception e) {
            assertEquals(ApApplicationException.class, e.getClass());
            logger.info(e.getMessage());
        }

        verify(apAccountCreator, times(5)).create(any(ApAccount.class));
        verify(idmService, times(1)).createProxyAgentAccount();
        verify(idmService, times(1)).deleteProxyAgentAccount(anyString());
    }


    @Test
    public void whenRetrievingLdapAccountsForTwoNodeTypes_andAccountsDoNotExist_twoLdapAccountsTwoApAccountMosOneApAccountsMoShouldBeCreated() {
        when(apAccountsRetriever.getAccountsMo()).thenReturn(null).thenReturn(apAccountsMo);
        when(idmService.createProxyAgentAccount()).thenReturn(new ProxyAgentAccountData("userName", "password"));

        accountManager.getAccount(LDAP_ACCOUNT_TYPE, ERBS_NODE_TYPE);
        accountManager.getAccount(LDAP_ACCOUNT_TYPE, RADIONODE_NODE_TYPE, NODE_ID);

        verify(apAccountRetriever, times(2)).getAccount(eq(LDAP_ACCOUNT_TYPE), anyString());
        verify(apAccountsCreator, times(1)).create();
        verify(apAccountCreator, times(2)).create(any(ApAccount.class));
        verify(idmService, times(2)).createProxyAgentAccount();
    }

}
