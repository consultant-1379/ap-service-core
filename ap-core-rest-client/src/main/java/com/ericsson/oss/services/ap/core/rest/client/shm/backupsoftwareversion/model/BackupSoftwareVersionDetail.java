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

public class BackupSoftwareVersionDetail {
    private String nodeFdn;
    private String backupId;
    private String location;
    private String swVersion;

    /**
     * @return the nodeFdn
     */
    public String getNodeFdn() {
        return nodeFdn;
    }

    /**
     * @param nodeFdn
     *            the nodeFdn to set
     */
    public void setNodeFdn(final String nodeFdn) {
        this.nodeFdn = nodeFdn;
    }

    /**
     * @return the backupId
     */
    public String getBackupId() {
        return backupId;
    }

    /**
     * @param backupId
     *            the backupId to set
     */
    public void setBackupId(final String backupId) {
        this.backupId = backupId;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location
     *            the location to set
     */
    public void setLocation(final String location) {
        this.location = location;
    }

    /**
     * @return the swVersion
     */
    public String getSwVersion() {
        return swVersion;
    }

    /**
     * @param swVersion
     *            the swVersion to set
     */
    public void setSwVersion(final String swVersion) {
        this.swVersion = swVersion;
    }

}
