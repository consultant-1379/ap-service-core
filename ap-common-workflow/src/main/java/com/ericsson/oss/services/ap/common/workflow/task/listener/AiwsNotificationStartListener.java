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
package com.ericsson.oss.services.ap.common.workflow.task.listener;

import com.ericsson.oss.services.ap.api.status.StatusEntryNames;

/**
 * Listener attached to the <b>start</b> event for the <code>Waiting for Node Connection to AIWS Notification</code> BPMN wait point.
 * <p>
 * Creates a new status entry on the AP node.
 */
public class AiwsNotificationStartListener extends TaskExecutionListener {

    @Override
    public void updateStatus(final String nodeFdn) {
        getStatusEntryManager().waitingForNotification(nodeFdn, StatusEntryNames.AIWS_NOTIFICATION.toString());
    }
}
