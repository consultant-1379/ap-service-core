/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/



/**
 * Retrieves the correctly formatted Upgrade Package Name from NetworkElement.
 */
package com.ericsson.oss.services.ap.core.upgradepackage;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.services.ap.api.workflow.UpgradePackageSearchService;
import com.ericsson.oss.services.ap.core.rest.client.shm.ShmRestClient;

@Stateless
@EService
@Local
public class ShmUpgradePackageSearchService implements UpgradePackageSearchService{

    @Inject
    private ShmRestClient shmRestClient;

    /**
     * Retrieves the Upgrade Package Name
     * This is done by retrieving The NetworkElement MO, attribute neProductVersion.
     * The Upgrade Package for the Node is stored in List of Maps (made up of Revision and Identity Name/Value Pairs).
     * The Revision is extracted to make up the Upgrade Package Name.
     *
     * @param nodeName
     *            the name of the AP node
     * @param nodeTypeIdForPackageName
     *            the type of the AP node
     * @return the Upgrade Package name
     */
    @SuppressWarnings("unchecked")
    @Override
    public String getUpgradePackageName(final String nodeName, final String nodeTypeIdForPackageName) {
        return shmRestClient.getUpgradePackageName(nodeName, nodeTypeIdForPackageName);
    }

    @Override
    public String getUpgradePackageName(final String nodeName, final String nodeTypeIdForPackageName, final String softwareVersion) {
        return shmRestClient.getUpgradePackageName(nodeName, nodeTypeIdForPackageName, softwareVersion);
    }

    @Override
    public String getBackupSoftwareVersion(final String nodeName, final String backupName) {
        return shmRestClient.getBackupSoftwareVersion(nodeName, backupName);
    }
}
