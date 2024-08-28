/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.util.log;

import com.ericsson.oss.itpf.sdk.recording.ErrorSeverity;
import com.ericsson.oss.itpf.sdk.recording.EventLevel;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.itpf.sdk.recording.classic.SystemRecorderNonCDIImpl;
import com.ericsson.oss.services.ap.api.status.StatusEntryProgress;
import com.ericsson.oss.services.ap.common.util.string.FDN;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Logger for output of status entries
 */
public class TaskOutputLogger {

    public static final String AP_TASK_OUTPUT = "AUTO_PROVISIONING.TASK_OUTPUT";

    private SystemRecorder recorder = new SystemRecorderNonCDIImpl(); // NOPMD

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Logs info about end state status entries. Failed states are logged as error
     *
     * @param nodeFdn FDN of the node
     * @param statusEntryName task reaching an end state
     * @param progress progress of the task
     * @param timestamp that would be printed to the status output
     * @param additionalInformation for status output. An empty string if not applicable.
     */
    public void log(final String nodeFdn, final String statusEntryName, final StatusEntryProgress progress, final String timestamp, final String additionalInformation) {
        final String nodeName = FDN.get(nodeFdn).getRdnValue();
        switch(progress) {
            case COMPLETED: case RECEIVED:
                recorder.recordEvent(AP_TASK_OUTPUT, EventLevel.COARSE, nodeName, nodeFdn,
                        buildEventData(nodeName, statusEntryName, progress.toString(), timestamp, additionalInformation));
                return;
            case FAILED:
                recorder.recordError(AP_TASK_OUTPUT, ErrorSeverity.ERROR, nodeName, nodeFdn,
                        buildEventData(nodeName, statusEntryName, progress.toString(), timestamp, additionalInformation));
                return;
            default:
                logger.debug("Progress: {}  for Node: {} is not an end state for Logger for output of status entries, Ignoring", progress, nodeFdn);
        }
    }

    private String buildEventData(final String nodeName, final String statusEntryName, final String progress, final String timestamp, final String additionalInformation) {
        if ("".equals(additionalInformation)){
            return String.format("NODE=%s, TASK=%s, PROGRESS=%s, TIMESTAMP=%s", nodeName, statusEntryName, progress, timestamp);
        }
        return String.format("NODE=%s, TASK=%s, PROGRESS=%s, TIMESTAMP=%s, ADDITIONAL_INFO=%s", nodeName, statusEntryName, progress, timestamp, additionalInformation);
    }
}

