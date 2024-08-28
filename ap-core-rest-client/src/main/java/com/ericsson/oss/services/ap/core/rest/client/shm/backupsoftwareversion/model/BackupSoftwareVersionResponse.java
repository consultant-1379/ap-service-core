/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

/**
 * Class containing response of the SHM backupSoftwareVersions service.
 * The message contains backupSwVersionDetails
 */
package com.ericsson.oss.services.ap.core.rest.client.shm.backupsoftwareversion.model;

import java.util.Collections;
import java.util.List;

public class BackupSoftwareVersionResponse {
    private List<BackupSoftwareVersionDetail> backupSwVersionDetails;

    /**
     * @return the backupSwVersionDetails
     */
    public List<BackupSoftwareVersionDetail> getBackupSwVersionDetails() {
        return Collections.unmodifiableList(backupSwVersionDetails);
    }

    /**
     * @param backupSwVersionDetails
     *            the backupSwVersionDetails to set
     */
    public void setBackupSwVersionDetails(final List<BackupSoftwareVersionDetail> backupSwVersionDetails) {
        this.backupSwVersionDetails = Collections.unmodifiableList(backupSwVersionDetails);
    }

}
