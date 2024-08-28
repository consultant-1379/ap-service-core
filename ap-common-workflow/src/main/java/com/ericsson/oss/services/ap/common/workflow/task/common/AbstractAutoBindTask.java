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
package com.ericsson.oss.services.ap.common.workflow.task.common;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.common.util.capability.NodeCapabilityModel;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Abstract class that binds a node during an order or replace flow. The Task handles the Early Bind and the Easy Late Bind flows
 */
public abstract class AbstractAutoBindTask extends AbstractServiceTask {

    private static final String AP_BIND_NODENAME = "autoProvisioning_bind_nodeName";
    private static final String BIND_WITH_NODE_NAME = "BIND_WITH_NODE_NAME";
    private static final String IS_SUPPORTED = "isSupported";
    protected final ServiceFinderBean serviceFinder = new ServiceFinderBean();

    private final Logger logger = LoggerFactory.getLogger(getClass()); // NOPMD

    @Override
    public void executeTask(final TaskExecution execution) {

        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        final String hardwareSerialNumber = workflowVariables.getHardwareSerialNumber();
        final String apNodeFdn = workflowVariables.getApNodeFdn();
        final boolean bindWithNodeName = performBindWithNodeName(workflowVariables);

        if (StringUtils.isNotBlank(hardwareSerialNumber) || bindWithNodeName) {
            logger.info("Executing Bind on class {} for node {}", getClass().getSimpleName(), apNodeFdn);
            performHardwareBind(hardwareSerialNumber, bindWithNodeName, apNodeFdn, execution, workflowVariables);
        }
    }

    private boolean performBindWithNodeName(final AbstractWorkflowVariables workflowVariables) {
        final boolean isBindWithNodeNameSupported = NodeCapabilityModel.INSTANCE.getAttributeAsBoolean(workflowVariables.getNodeType(), BIND_WITH_NODE_NAME,
            IS_SUPPORTED);
        if (isBindWithNodeNameSupported) {
            logger.info("Bind with Node Name Check for System.getProperty(AP_BIND_NODENAME) = {}", System.getProperty(AP_BIND_NODENAME));
            final String autoProvisioningBindNodeName = System.getProperty(AP_BIND_NODENAME);
            if (StringUtils.isNotBlank(autoProvisioningBindNodeName)) {
                return autoProvisioningBindNodeName.equalsIgnoreCase("true");
            }
        }
        return false;
    }

    private void performHardwareBind(final String hardwareSerialNumber, final boolean bindWithNodeName, final String apNodeFdn,
        final TaskExecution execution, final AbstractWorkflowVariables workflowVariables) {
        try {
            bindNode(apNodeFdn, hardwareSerialNumber, bindWithNodeName);
        } catch (final Exception e) {
            logger.error("Bind failed for node {}: {}", apNodeFdn, e.getMessage(), e);
            final String errorKey = BpmnErrorKeyHandler.handleWorkflowForFailedUseCase(workflowVariables);
            throwBpmnError(errorKey, e.getMessage(), execution);
        }
    }

    /**
     * Binds the supplied hardware serial number to the given node.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param hardwareSerialNumber
     *            the hardware serial number to bind
     * @param bindWithNodeName
     *            performs Bind with node name if true
     */
    protected abstract void bindNode(final String apNodeFdn, final String hardwareSerialNumber, final boolean bindWithNodeName);
}
