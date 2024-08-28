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

import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.security.cryptography.CryptographyService;
import com.ericsson.oss.services.ap.api.account.ApAccount;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.ApAccountAttribute;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;

/**
 * Retrieves an <code>AutoProvisioningAccount</code> MO from DPS.
 */
public class ApAccountMoRetriever {

    @Inject
    private CryptographyService cyptographyService;

    @Inject
    private DpsQueries dpsQueries;

    /**
     * Retrieves an <code>AutoProvisioningAccount</code> MO from DPS, based on the supplied account type and node name.
     * <p>
     * The encrypted password from DPS will be decrypted before being returned.
     *
     * @param accountType
     *            the type of the account (LDAP, SMRS, etc)
     * @param nodeName
     *            the type of the node to which this account is linked
     * @return an {@link ApAccount} with the account information, or null if MO doesn't exist
     */
    public ApAccount getAccount(final String accountType, final String nodeName) {
        final Iterator<ManagedObject> accountMos = dpsQueries.findMoByName(nodeName, MoType.AP_ACCOUNT.toString(), Namespace.AP.toString()).execute();

        while (accountMos.hasNext()) {
            final ManagedObject accountMo = accountMos.next();
            final String retrievedAccountType = accountMo.getAttribute(ApAccountAttribute.ACCOUNT_TYPE.toString());

            if (accountType.equals(retrievedAccountType)) {
                return extractDetails(accountMo);
            }
        }

        return null;
    }

    private ApAccount extractDetails(final ManagedObject apAccountMo) {
        final String accountType = apAccountMo.getAttribute(ApAccountAttribute.ACCOUNT_TYPE.toString());
        final String nodeType = apAccountMo.getAttribute(ApAccountAttribute.NODE_TYPE.toString());
        final String userName = apAccountMo.getAttribute(ApAccountAttribute.USERNAME.toString());
        final String nodeName = apAccountMo.getAttribute(ApAccountAttribute.NODENAME.toString());
        final String encryptedPassword = apAccountMo.getAttribute(ApAccountAttribute.PASSWORD.toString());
        final String decryptedPassword = decryptPassword(encryptedPassword.getBytes(StandardCharsets.ISO_8859_1)); // Using ISO-8859-1 to avoid data loss when converting from String to byte[]

        return new ApAccount(accountType, nodeType, userName, decryptedPassword, nodeName);
    }

    private String decryptPassword(final byte[] encryptedPassword) {
        final byte[] decryptedPassword = cyptographyService.decrypt(encryptedPassword);
        return new String(decryptedPassword, StandardCharsets.ISO_8859_1);
    }
}
