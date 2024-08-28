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
 * Class containing BackupSoftwareVersionQuery.
 * The message contains BackupSoftwareVersionQuery value to query shm
 */
package com.ericsson.oss.services.ap.core.rest.client.shm.backupsoftwareversion.model;

import java.util.Collections;
import java.util.List;

public class BackupSoftwareVersionQuery {

    List<NodeBackupConfigurationQuery> nodeBackupConfigurationQueries;

    /**
     * @return the nodeBackupConfigurationQueries
     */
    public List<NodeBackupConfigurationQuery> getNodeBackupConfigurationQueries() {
        return Collections.unmodifiableList(nodeBackupConfigurationQueries);
    }

    /**
     * @param nodeBackupConfigurationQueries
     *            the nodeBackupConfigurationQueries to set
     */
    public void setNodeBackupConfigurationQueries(final List<NodeBackupConfigurationQuery> nodeBackupConfigurationQueries) {
        this.nodeBackupConfigurationQueries = Collections.unmodifiableList(nodeBackupConfigurationQueries);
    }

}
