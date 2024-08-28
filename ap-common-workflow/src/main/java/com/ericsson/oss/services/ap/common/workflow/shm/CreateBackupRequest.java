/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.workflow.shm;

import java.io.Serializable;

public class CreateBackupRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    private final String jobId;
    private final String backupName;

    public CreateBackupRequest(final String jobId, final String backupName) {
        this.jobId = jobId;
        this.backupName = backupName;
    }

    public String getJobId() {
        return jobId;
    }

    public String getBackupName() {
        return backupName;
    }
}
