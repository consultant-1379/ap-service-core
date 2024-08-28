/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api;

import java.util.Map;

public interface NotificationService {
    /**
     * Send notification to workflow.
     *
     * @param nodeName
     *            the AP node name
     * @param messageType
     *            the notification message type
     * @param workflowMessageVariables
     *            the variables for workflow
     */
    void sendNotification(String nodeName, String messageType, Map<String, Object> workflowMessageVariables);

    /**
     * Send notification to workflow.
     *
     * @param nodeName
     *            the AP node name
     * @param messageType
     *            the notification message type
     */
    void sendNotification(String nodeName, String messageType);
}
