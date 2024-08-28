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
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.security.cryptography.CryptographyService;
import com.ericsson.oss.services.ap.api.account.ApAccount;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.ApAccountAttribute;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.util.cdi.Transactional;
import com.ericsson.oss.services.ap.common.util.cdi.Transactional.TxType;

/**
 * Creates an <code>AutoProvisioningAccount</code> MO with integration account information (LDAP, SMRS, etc) for AP.
 */
public class ApAccountMoCreator {

    @Inject
    private CryptographyService cyptographyService;

    @Inject
    private DpsOperations dps;

    @Inject
    private Logger logger;

    @Inject
    private ApAccountsMoRetriever apAccountsRetriever;

    /**
     * Creates an <code>AutoProvisioningAccount</code> MO with the supplied {@link ApAccount}. The MO ID will be the node type supplied.
     * <p>
     * The given password is expected to be in plaintext, and will be encrypted using the {@link CryptographyService} before being saved to DPS.
     * <p>
     * This method will get executed within in its own Transaction
     *
     * @param apAccount
     *            the {@link ApAccount} containing the account information
     * @return the created <code>AutoProvisioningAccount</code> {@link ManagedObject}
     */
    @Transactional(txType = TxType.REQUIRES_NEW)
    public ManagedObject create(final ApAccount apAccount) {
        final ManagedObject rootApAccountMo = apAccountsRetriever.getAccountsMo();
        return create(rootApAccountMo, apAccount.getAccountType(), apAccount.getNodeType(), apAccount.getUserName(), apAccount.getPassword(), apAccount.getNodeName());
    }

    /**
     * Creates an <code>AutoProvisioningAccount</code> MO with the supplied data. The MO ID will be the node type supplied.
     * <p>
     * The given password is expected to be in plaintext, and will be encrypted using the {@link CryptographyService} before being saved to DPS.
     *
     * @param rootApAccountMo
     *            the parent <code>AutoProvisioningAccounts</code> MO
     * @param accountType
     *            the type of the account (LDAP, SMRS, etc)
     * @param nodeType
     *            the type of the node to which this account is linked
     * @param userName
     *            the userName of the account
     * @param unencryptedPassword
     *            the password of the account in plaintext, which will be encrypted
     * @return the created <code>AutoProvisioningAccount</code> {@link ManagedObject}
     */
    private ManagedObject create(final ManagedObject rootApAccountMo, final String accountType, final String nodeType, final String userName,
                                 final String unencryptedPassword, final String nodeName) {
        logger.info("Creating {} MO of type {} for node name {}", MoType.AP_ACCOUNT, accountType, nodeName);
        final String encryptedPassword = encryptPassword(unencryptedPassword.getBytes(StandardCharsets.ISO_8859_1)); // Using ISO-8859-1 to avoid data loss when converting from byte[] to String
        final Map<String, Object> accountAttributes = new HashMap<>();

        accountAttributes.put(ApAccountAttribute.ACCOUNT_TYPE.toString(), accountType);
        accountAttributes.put(ApAccountAttribute.NODE_TYPE.toString(), nodeType);
        accountAttributes.put(ApAccountAttribute.USERNAME.toString(), userName);
        accountAttributes.put(ApAccountAttribute.PASSWORD.toString(), encryptedPassword);
        accountAttributes.put(ApAccountAttribute.NODENAME.toString(), nodeName);

        final RetriableCommand<ManagedObject> createMoCommand = createRetriableCreateMoCommand(rootApAccountMo, nodeName, accountAttributes);
        return ApAccountMoCreationHandler.executeRetriableCommand(createMoCommand);
    }

    private RetriableCommand<ManagedObject> createRetriableCreateMoCommand(final ManagedObject rootApAccountMo, final String nodeName,
                                                                           final Map<String, Object> accountAttributes) {
        return (final RetryContext retryContext) -> {
            try {
                return createMo(rootApAccountMo, nodeName, accountAttributes);
            } catch (final Exception e) {
                logger.warn("Error creating {} MO [{}], attempting to retrieve from DPS", MoType.AP_ACCOUNT, e.getMessage());
                final String apAccountFdn = String.format("%s=1,%s=%s", MoType.AP_ACCOUNTS.toString(), MoType.AP_ACCOUNT.toString(), nodeName);
                final ManagedObject apAccountMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(apAccountFdn);

                if (apAccountMo != null) {
                    return apAccountMo;
                }
                throw e;
            }
        };
    }

    private ManagedObject createMo(final ManagedObject rootApAccountMo, final String nodeName, final Map<String, Object> accountAttributes) {
        return dps.getDataPersistenceService().getLiveBucket().getManagedObjectBuilder()
                .type(MoType.AP_ACCOUNT.toString())
                .name(nodeName)
                .parent(rootApAccountMo)
                .addAttributes(accountAttributes)
                .create();
    }

    private String encryptPassword(final byte[] decryptedPassword) {
        final byte[] encryptedPassword = cyptographyService.encrypt(decryptedPassword);
        return new String(encryptedPassword, StandardCharsets.ISO_8859_1);
    }
}
