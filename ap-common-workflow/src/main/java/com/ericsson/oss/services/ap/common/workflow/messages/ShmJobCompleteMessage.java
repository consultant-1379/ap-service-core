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
package com.ericsson.oss.services.ap.common.workflow.messages;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.oss.services.shm.model.events.JobResult;
import com.ericsson.oss.services.shm.model.events.ShmJobStatusEvent;

public class ShmJobCompleteMessage {

    private static final String ADDITIONAL_INFO_KEY = "additionalInfo";
    private static final String JOB_SUCCESS_KEY = "jobSuccess";
    private static final String SHM_JOB_COMPLETED_MESSAGE_KEY = "SHM_JOB_COMPLETED";

    private ShmJobCompleteMessage() {

    }

    /**
     * Correlation message key indicating a create backup has been received.
     *
     * @return the node up message key
     */
    public static String getMessageKey() {
        return SHM_JOB_COMPLETED_MESSAGE_KEY;
    }

    /**
     * Workflow variables key for the message from the SHM job status event
     *
     * @return the key for the message from the event
     */
    public static String getAdditionalInfoKey() {
        return ADDITIONAL_INFO_KEY;
    }

    /**
     * Workflow variables key for the success or failure of the SHM job status event
     *
     * @return the key for the success variable
     */
    public static String getJobSuccessKey() {
        return JOB_SUCCESS_KEY;
    }

    /**
     * The message variables to correlate the message with. The result of the job, and message of the job.
     *
     * @return the workflow variables
     */
    public static Map<String, Object> getMessageVariables(final ShmJobStatusEvent event) {
        final Map<String, Object> createBackupVariables = new HashMap<>();
        createBackupVariables.put(JOB_SUCCESS_KEY, getSuccessBoolean(event.getResult()));
        createBackupVariables.put(ADDITIONAL_INFO_KEY, event.getMessage());
        return createBackupVariables;
    }

    private static boolean getSuccessBoolean(final JobResult result) {
        if (result.equals(JobResult.SUCCESS)) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }
}
