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
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.workflow.messages.NodeUpMessage;

/**
 * Listener attached to the <b>start</b> event for the <code>Node Up</code> BPMN wait point.
 * <p>
 * Creates a new status entry on the AP node.
 */
public class NodeUpStartListener extends TaskExecutionListener {
    private final DpsOperations dpsOperations = new DpsOperations();

    @Override
    public void updateStatus(final String nodeFdn) {
        getStatusEntryManager().waitingForNotification(nodeFdn, StatusEntryNames.NODE_UP.toString());
        dpsOperations.updateMo(nodeFdn, NodeAttribute.WAITING_FOR_MESSAGE.toString(), NodeUpMessage.getMessageKey());
    }
}
