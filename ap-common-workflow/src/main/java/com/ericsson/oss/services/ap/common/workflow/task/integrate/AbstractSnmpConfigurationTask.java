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
package com.ericsson.oss.services.ap.common.workflow.task.integrate;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;

import com.ericsson.oss.services.ap.common.workflow.task.common.BpmnErrorKeyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.capability.NodeCapabilityModel;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Abstract class for a service task to configure the SNMP setting for the specified AP node.
 */
public abstract class AbstractSnmpConfigurationTask extends AbstractServiceTask {

    private static final String CONFIGURE_SNMP_SECURITY_WITH_NODE_NAME = "CONFIGURE_SNMP_SECURITY_WITH_NODE_NAME";
    private static final String IS_SUPPORTED = "isSupported";
    protected final ServiceFinderBean serviceFinder = new ServiceFinderBean();

    private DdpTimer ddpTimer = new DdpTimer();
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void executeTask(final TaskExecution execution) {
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        final boolean isConfigureSnmpSecurityWithNodeNameSupported = NodeCapabilityModel.INSTANCE.getAttributeAsBoolean(workflowVariables.getNodeType(), CONFIGURE_SNMP_SECURITY_WITH_NODE_NAME,
                IS_SUPPORTED);
        if (isConfigureSnmpSecurityWithNodeNameSupported) {
            ddpTimer.start(CommandLogName.SNMP_CONFIGURATION.toString());
            logger.info("Executing {} for node {}", this.getClass().getSimpleName(), workflowVariables.getApNodeFdn());
            executeSnmpConfiguration(workflowVariables);
            ddpTimer.end(workflowVariables.getApNodeFdn());
        }
    }

    private void executeSnmpConfiguration(final AbstractWorkflowVariables workflowVariables) {
        try {
            configureSnmp(workflowVariables.getApNodeFdn());
        } catch (final Exception e) {
            logger.warn("Error configuring SNMP for node {}: {}", workflowVariables.getApNodeFdn(), e.getMessage());
            BpmnErrorKeyHandler.handleWorkflowForWarnings(workflowVariables);
        }
    }

    /**
     * Creates the SNMP Configuration for the specified AP node.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     */
    protected abstract void configureSnmp(final String apNodeFdn);

}
