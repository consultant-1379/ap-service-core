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

import java.util.HashMap;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.util.cdi.Transactional;
import com.ericsson.oss.services.ap.common.util.cdi.Transactional.TxType;

/**
 * Creates the root <code>AutoProvisioningAccounts</code> MO to hold all <code>AutoProvisioningAccount</code> child MOs.
 */
public class ApAccountsMoCreator {

    @Inject
    private DpsOperations dps;

    @Inject
    private Logger logger;

    @Inject
    private ModelReader modelReader;

    /**
     * Creates the root <code>AutoProvisioningAccounts</code> MO. This will get executed in its own transaction.
     *
     * @return the created <code>AutoProvisioningAccounts</code> {@link ManagedObject}
     */
    @Transactional(txType = TxType.REQUIRES_NEW)
    public ManagedObject create() {
        final ModelData apAccountsModelData = modelReader.getLatestPrimaryTypeModel(Namespace.AP.toString(), MoType.AP_ACCOUNTS.toString());
        logger.info("Creating {} root MO", MoType.AP_ACCOUNTS);

        final RetriableCommand<ManagedObject> createMoCommand = createRetriableCreateMoCommand(apAccountsModelData);
        return ApAccountMoCreationHandler.executeRetriableCommand(createMoCommand);
    }

    private RetriableCommand<ManagedObject> createRetriableCreateMoCommand(final ModelData apAccountsModelData) {
        return (final RetryContext retryContext) -> {
            try {
                return createMo(apAccountsModelData);
            } catch (final Exception e) {
                logger.warn("Error creating {} MO [{}], attempting to retrieve from DPS", MoType.AP_ACCOUNTS, e.getMessage());
                final ManagedObject apAccountsMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(MoType.AP_ACCOUNTS.toString() + "=1");

                if (apAccountsMo != null) {
                    return apAccountsMo;
                }
                throw e;
            }
        };
    }

    private ManagedObject createMo(final ModelData apAccountsModelData) {
        return dps.getDataPersistenceService().getLiveBucket()
                .getMibRootBuilder()
                .namespace(apAccountsModelData.getNameSpace())
                .version(apAccountsModelData.getVersion())
                .type(MoType.AP_ACCOUNTS.toString())
                .name("1")
                .addAttributes(new HashMap<String, Object>())
                .create();
    }
}
