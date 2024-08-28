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
package com.ericsson.oss.services.ap.api.workflow;


/**
 Interface that fetches available upgradepackage name from SHM given a NodeType.
 */
public interface UpgradePackageSearchService {

    /**
     * CLient logic which consumes SHM search service and fetch the upgradePackage name
     *
     * @param nodeName
     *            the input data to be used to construct validate request
     * @param nodeTypeIdForPackageName
     *            the input data to be used to construct validate request
     * @return upgradePackageName.
     */
    String getUpgradePackageName(final String nodeName, final String nodeTypeIdForPackageName);

    /**
     * CLient logic which consumes SHM search service and fetch the upgradePackage name
     *
     * @param nodeName
     *            the input data to be used to construct validate request
     * @param nodeTypeIdForPackageName
     *            the input data to be used to construct validate request
     * @param softwareVersion
     *            the software version
     * @return upgradePackageName.
     */
    String getUpgradePackageName(final String nodeName, final String nodeTypeIdForPackageName, final String softwareVersion);

    /**
     * CLient logic which consumes SHM inventory service and fetch the backup software version
     *
     * @param nodeName
     *            the input data to be used to construct validate request
     * @param backupName
     *            the backup name
     * @return backupSoftwareVersion.
     */
    String getBackupSoftwareVersion(final String nodeName, final String backupName);

}