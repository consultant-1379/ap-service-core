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

import java.util.Iterator;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;

/**
 * Retrieves an <code>AutoProvisioningAccounts</code> MO from DPS.
 */
public class ApAccountsMoRetriever {

    @Inject
    private DpsQueries dpsQueries;

    /**
     * Retrieves the root <code>AutoProvisioningAccounts</code> MO from DPS.
     *
     * @return the root <code>AutoProvisioningAccounts</code> {@link ManagedObject}, or null if MO doesn't exist
     */
    public ManagedObject getAccountsMo() {
        final Iterator<ManagedObject> rootApAccountMos = dpsQueries.findMosByType(MoType.AP_ACCOUNTS.toString(), Namespace.AP.toString()).execute();
        if (rootApAccountMos.hasNext()) {
            return rootApAccountMos.next();
        }
        return null;
    }
}
