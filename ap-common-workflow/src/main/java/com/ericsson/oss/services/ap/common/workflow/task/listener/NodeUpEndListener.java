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

import static com.ericsson.oss.services.ap.api.status.StatusEntryProgress.RECEIVED;
import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.api.status.StatusEntryNames;
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpUserUpdater;
import com.ericsson.oss.services.ap.common.util.capability.NodeCapabilityModel;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Listener attached to the <b>end</b> event for the <code>Node Up</code> BPMN wait point.
 * <p>
 * Updates the existing status entry on the AP node.
 */
public class NodeUpEndListener extends AbstractServiceTask {

    private static final String NODE_UP_NOTIFICATION = "NodeUpNotification";
    private static final String CONFIGURE_SNMP_SECURITY_WITH_NODE_NAME = "CONFIGURE_SNMP_SECURITY_WITH_NODE_NAME";
    private static final String IS_SUPPORTED = "isSupported";
    private boolean isConfigureSnmpSecurityWithNodeNameSupported = false;
    private StatusEntryManagerLocal statusEntryManager;
    private SnmpUserUpdater snmpUserUpdater = new SnmpUserUpdater();

    @Override
    public void executeTask(final TaskExecution execution) {
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        final String nodeFdn = workflowVariables.getApNodeFdn();
        isConfigureSnmpSecurityWithNodeNameSupported = NodeCapabilityModel.INSTANCE.getAttributeAsBoolean(workflowVariables.getNodeType(), CONFIGURE_SNMP_SECURITY_WITH_NODE_NAME, IS_SUPPORTED);
        updateStatus(nodeFdn, (String) execution.getVariable(NODE_UP_NOTIFICATION));
    }

    private void updateStatus(final String nodeFdn, final String nodeUpNotification) {
        if (RECEIVED.toString().equals(nodeUpNotification)) {
            getStatusEntryManager().notificationReceived(nodeFdn, StatusEntryNames.NODE_UP.toString());
            if(isConfigureSnmpSecurityWithNodeNameSupported) {
                snmpUserUpdater.setNodeSnmpUserToUndefined(nodeFdn);
            }
        } else {
            getStatusEntryManager().notificationCancelled(nodeFdn, StatusEntryNames.NODE_UP.toString());
        }
    }

    protected StatusEntryManagerLocal getStatusEntryManager() {
        if (statusEntryManager == null) {
            final ServiceFinderBean serviceFinder = new ServiceFinderBean();
            statusEntryManager = serviceFinder.find(StatusEntryManagerLocal.class);
        }
        return statusEntryManager;
    }
}
