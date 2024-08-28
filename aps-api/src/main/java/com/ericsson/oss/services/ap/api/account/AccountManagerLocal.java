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
package com.ericsson.oss.services.ap.api.account;

import javax.ejb.Local;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;

/**
 * Interface used to retrieve <code>AutoProvisioningAccount</code> MOs.
 */
@EService
@Local
public interface AccountManagerLocal {

    /**
     * Retrieves an {@link ApAccount} containing account information for an <code>AutoProvisioningAccount</code> MO. There will be a common account
     * per node type, so as to reduce the number of accounts created during AutoProvisioning.
     *
     * @param accountType
     *            the type of the account (LDAP, SMRS, etc)
     * @param nodeType
     *            the type of the node to which this account is linked
     * @return an {@link ApAccount} with the account information
     */

    ApAccount getAccount(final String accountType, final String nodeType);


    /**
     * Retrieves an {@link ApAccount} containing account information for an <code>AutoProvisioningAccount</code> MO. There will be a single account
     * per node.
     *
     * @param accountType
     *            the type of the account (LDAP, SMRS, etc)
     * @param nodeType
     *            the type of the node to which this account is linked
     * @param nodeName
     *            the name of the node to which this account is linked
     * @return an {@link ApAccount} with the account information
     */


    ApAccount getAccount(final String accountType, final String nodeType, final String nodeName);
}
