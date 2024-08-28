/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
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
 * Listener attached to the <b>end</b> event for the <code>Node Synchronization Notification</code> BPMN wait point.
 * <p>
 * Updates the existing status entry on the AP node.
 */
public class SyncCompleteEndListener extends TaskExecutionListener {

    @Override
    protected void updateStatus(final String nodeFdn) {
        getStatusEntryManager().notificationReceived(nodeFdn, StatusEntryNames.SYNC_NODE_NOTIFICATION.toString());
    }
}
