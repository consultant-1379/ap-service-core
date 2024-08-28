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

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommandException;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.itpf.sdk.core.retry.classic.RetryManagerBean;
import com.ericsson.oss.itpf.security.identitymgmtservices.IdentityManagementService;
import com.ericsson.oss.itpf.security.identitymgmtservices.ProxyAgentAccountData;
import com.ericsson.oss.services.ap.api.account.AccountManagerLocal;
import com.ericsson.oss.services.ap.api.account.ApAccount;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.core.usecase.order.ApAccountMoCreator;
import com.ericsson.oss.services.ap.core.usecase.order.ApAccountMoRetriever;
import com.ericsson.oss.services.ap.core.usecase.order.ApAccountsMoCreator;
import com.ericsson.oss.services.ap.core.usecase.order.ApAccountsMoRetriever;

/**
 * Stateful EJB used to retrieve <code>AutoProvisioningAccount</code> MOs for given account and node types.
 * <p>
 * The flow to retrieve an account is:
 * <ol>
 * <li>Check local cache for account for given node type</li>
 * <li>If not in local cache, check DPS if account has already been persisted</li>
 * <li>If not in DPS, create new account, create MO in DPS and save to local cache</li>
 * </ol>
 */
@Singleton
@AccessTimeout(unit = TimeUnit.MINUTES, value = 1)
public class AccountManagerEjb implements AccountManagerLocal {

    private static final int MAX_RETRIES = 5;
    private static final int WAIT_INTERVAL_IN_SECONDS = 1;

    private final Map<String, ApAccount> ldapAccounts = new HashMap<>();

    @Inject
    private ApAccountsMoCreator apAccountsCreator;

    @Inject
    private ApAccountMoCreator apAccountCreator;

    @Inject
    private ApAccountsMoRetriever apAccountsRetriever;

    @Inject
    private ApAccountMoRetriever apAccountRetriever;

    @EServiceRef
    private IdentityManagementService idmService;

    @Inject
    private Logger logger;

    @Override
    public ApAccount getAccount(final String accountType, final String nodeType) {
        logger.info("Getting {} account for node type {}", accountType, nodeType);
        try {
            if (AccountType.LDAP.toString().equals(accountType)) {
                return getLdapAccount(nodeType);
            }
        } catch (final Exception e) {
            throw new ApApplicationException(format("Error retrieving %s account for node type %s: %s", accountType, nodeType, e.getMessage()), e);
        }

        throw new ApApplicationException("Invalid account type: " + accountType);
    }

    @Override
    public ApAccount getAccount(String accountType, String nodeType, String nodeName) {
        logger.info("Getting {} account for node type {} and node name {}", accountType, nodeType, nodeName);
        try {
            if (AccountType.LDAP.toString().equals(accountType)) {
                return getLdapAccount(nodeType, nodeName);
            }
        } catch (final Exception e) {
            throw new ApApplicationException(format("Error retrieving %s account for node type %s: of node name %s: %s", accountType, nodeType, nodeName, e.getMessage()), e);
        }

        throw new ApApplicationException("Invalid account type: " + accountType);
    }

    private ApAccount getLdapAccount(final String nodeType) {
        final ApAccount localCacheLdapAccount = getLdapAccountFromLocalCache(nodeType);
        if (localCacheLdapAccount != null) {
            return localCacheLdapAccount;
        }

        final ApAccount dpsLdapAccount = getLdapAccountFromDps(nodeType);
        if (dpsLdapAccount != null) {
            return dpsLdapAccount;
        }

        return createLdapAccount(nodeType);
    }

    private ApAccount getLdapAccount(final String nodeType, final String nodeName) {
        final ApAccount localCacheLdapAccount = getLdapAccountFromLocalCache(nodeName);
        if (localCacheLdapAccount != null) {
            return localCacheLdapAccount;
        }

        final ApAccount dpsLdapAccount = getLdapAccountFromDps(nodeName);
        if (dpsLdapAccount != null) {
            return dpsLdapAccount;
        }

        return createLdapAccount(nodeType, nodeName);
    }

    /* nodeAttribute will be either nodeName or nodeType as an argument for getLdapAccountFromLocalCache */

    private ApAccount getLdapAccountFromLocalCache(final String nodeAttribute) {
        if (ldapAccounts.containsKey(nodeAttribute)) {
            logger.info("{} account for node attribute {} found in cache", AccountType.LDAP, nodeAttribute);
            return ldapAccounts.get(nodeAttribute);
        }
        return null;
    }

    /* nodeAttribute will be either nodeName or nodeType as an argument for getLdapAccountFromDps */

    private ApAccount getLdapAccountFromDps(final String nodeAttribute) {
        final ApAccount ldapAccountFromDps = apAccountRetriever.getAccount(AccountType.LDAP.toString(), nodeAttribute);
        if (ldapAccountFromDps != null) {
            logger.info("{} account for node attribute {} retrieved from DPS", AccountType.LDAP, nodeAttribute);
            ldapAccounts.put(nodeAttribute, ldapAccountFromDps);
            return ldapAccountFromDps;
        }
        logger.info("Creating new {} AutoProvisioningAccount for node type {}", AccountType.LDAP, nodeAttribute);
        return null;
    }

    private ApAccount createLdapAccount(final String nodeType) {
        getParentApAccountsMo();
        final ProxyAgentAccountData proxyAccount = idmService.createProxyAgentAccount();

        try {
            return createLdapApAccountMo(nodeType, proxyAccount);
        } catch (final Exception e) {
            logger.error("Error creating AutoProvisioningAccount MO, deleting {} account", AccountType.LDAP);
            idmService.deleteProxyAgentAccount(proxyAccount.getUserDN());
            throw e;
        }
    }

    private ApAccount createLdapAccount(final String nodeType, final String nodeName) {
        getParentApAccountsMo();
        final ProxyAgentAccountData proxyAccount = idmService.createProxyAgentAccount();

        try {
            return createLdapApAccountMo(nodeType, nodeName, proxyAccount);
        } catch (final Exception e) {
            logger.error("Error creating AutoProvisioningAccount MO, deleting {} account", AccountType.LDAP);
            idmService.deleteProxyAgentAccount(proxyAccount.getUserDN());
            throw e;
        }
    }

    private ApAccount createLdapApAccountMo(final String nodeType, final ProxyAgentAccountData proxyAccount) {
        final ApAccount newApAccount = new ApAccount(AccountType.LDAP.toString(), nodeType, proxyAccount.getUserDN(),
            proxyAccount.getUserPassword());

        final RetriableCommand<Void> retriableCommand = new RetriableCommand<Void>() {

            @Override
            public Void execute(final RetryContext retryContext) {
                apAccountCreator.create(newApAccount);
                return null;
            }
        };
        executeRetriableCommand(retriableCommand);
        ldapAccounts.put(nodeType, newApAccount);
        return newApAccount;
    }

    private ApAccount createLdapApAccountMo(final String nodeType, final String nodeName, final ProxyAgentAccountData proxyAccount) {
        final ApAccount newApAccount = new ApAccount(AccountType.LDAP.toString(), nodeType, proxyAccount.getUserDN(),
            proxyAccount.getUserPassword(), nodeName);

        final RetriableCommand<Void> retriableCommand = new RetriableCommand<Void>() {

            @Override
            public Void execute(final RetryContext retryContext) {
                apAccountCreator.create(newApAccount);
                return null;
            }
        };
        executeRetriableCommand(retriableCommand);
        ldapAccounts.put(nodeName, newApAccount);
        return newApAccount;
    }

    private static void executeRetriableCommand(final RetriableCommand<Void> retriableCommand) {
        final RetryManager retryManager = new RetryManagerBean();
        final RetryPolicy policy = RetryPolicy.builder()
                .attempts(MAX_RETRIES)
                .waitInterval(WAIT_INTERVAL_IN_SECONDS, TimeUnit.SECONDS)
                .retryOn(Exception.class)
                .build();
        try {
            retryManager.executeCommand(policy, retriableCommand);
        } catch (final RetriableCommandException e) {
            throw new ApServiceException(e.getMessage(), e);
        }
    }

    private ManagedObject getParentApAccountsMo() {
        final ManagedObject rootApAccountsMo = apAccountsRetriever.getAccountsMo();

        if (rootApAccountsMo != null) {
            return rootApAccountsMo;
        }

        return apAccountsCreator.create();
    }
}
