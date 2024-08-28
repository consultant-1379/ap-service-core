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
package com.ericsson.oss.services.ap.common.workflow.recording;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.itpf.sdk.recording.classic.SystemRecorderBean;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.api.status.StatusEntryNames;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * A class responsible to record all the ZT integration timings for the DDP instrumentation.
 */
public class ZeroTouchRecorder {

    private final SystemRecorder systemRecorder = new SystemRecorderBean();
    private final DpsOperations dpsOperations = new DpsOperations();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String NODE_TYPE_KEY = "nodeType";
    private static final String DATE_FORMAT = "yyyy-MM-dd kk:mm:ss.SSS";

    public void processResponseTime(final String apNodeFdn, final StatusEntryManagerLocal statusEntryManager ) {
        final long validateConfigurationTime = convertTimestampToLong(statusEntryManager.getStatusEntryByName(apNodeFdn, StatusEntryNames.VALIDATE_CONFIGURATIONS_TASK.toString()));
        final long aiwsNotificationTime = convertTimestampToLong(statusEntryManager.getStatusEntryByName(apNodeFdn, StatusEntryNames.AIWS_NOTIFICATION.toString()));
        long integrationCompletedTime = convertTimestampToLong(statusEntryManager.getStatusEntryByName(apNodeFdn, State.INTEGRATION_COMPLETED.getDisplayName()));
        if (integrationCompletedTime <= 0) {
            integrationCompletedTime = System.currentTimeMillis();
        }

        if (aiwsNotificationTime > 0) {
            recordResponseTime(apNodeFdn, integrationCompletedTime - aiwsNotificationTime, integrationCompletedTime - validateConfigurationTime);
        }
    }

    private long convertTimestampToLong(final StatusEntry task) {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        long milliseconds = 0L;
        if (task != null && task.getTimeStamp() != null) {
            try {
                milliseconds = sdf.parse(task.getTimeStamp()).getTime();
            } catch (ParseException e) {
                logger.error("Error converting timestamp: ", e);
            }
        }
        return milliseconds;
    }

    private void recordResponseTime(final String apNodeFdn, final long aiwsTime, final long totalTime) {
        final Map<String, Object> attributes = dpsOperations.readMoAttributes(apNodeFdn);
        final String nodeName = FDN.get(apNodeFdn).getRdnValue();
        final String projectName = FDN.get(apNodeFdn).getRoot().split("=")[1];

        final Map<String, Object> eventData = new LinkedHashMap<>();
        eventData.put("PROJECT_NAME", projectName);
        eventData.put("NODE_NAME", nodeName);
        eventData.put("NODE_TYPE", attributes.get(NODE_TYPE_KEY));
        eventData.put("TIME_FROM_AIWS_TO_INTEGRATION_COMPLETE", aiwsTime);
        eventData.put("TIME_FROM_START_TO_INTEGRATION_COMPLETE", totalTime);
        systemRecorder.recordEventData("ZT_INTEGRATION_TIME_RESPONSE", eventData);
    }
}
