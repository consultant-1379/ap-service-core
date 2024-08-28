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
package com.ericsson.oss.services.ap.core.rest.client.shm.backupsoftwareversion.model;

/**
 * Class NodeBackupConfigurationQuery.
 * The message contains NodeBackupConfigurationQuery value to query shm
 */
public class NodeBackupConfigurationQuery {
    private String nodeFdn;
    private String location;
    private String backupId;

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

}
