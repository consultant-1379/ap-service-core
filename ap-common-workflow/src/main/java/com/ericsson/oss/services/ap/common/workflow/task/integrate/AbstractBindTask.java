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
package com.ericsson.oss.services.ap.common.workflow.task.integrate;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Abstract class that binds a node using the <code>ap bind</code> usecase.
 */
public abstract class AbstractBindTask extends AbstractServiceTask {

    protected final ServiceFinderBean serviceFinder = new ServiceFinderBean();

    private final DdpTimer ddpTimer = new DdpTimer();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void executeTask(final TaskExecution execution) {
        ddpTimer.start(CommandLogName.BIND.toString());
        final String hardwareSerialNumber = (String) execution.getVariable(NodeAttribute.HARDWARE_SERIAL_NUMBER.toString());
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        workflowVariables.setHardwareSerialNumber(hardwareSerialNumber);

        final String apNodeFdn = workflowVariables.getApNodeFdn();
        logger.info("Executing {} for node {}", getClass().getSimpleName(), apNodeFdn);
        executeBindNode(apNodeFdn, hardwareSerialNumber, workflowVariables);
    }

    private void executeBindNode(final String apNodeFdn, final String hardwareSerialNumber, final AbstractWorkflowVariables workflowVariables) {
        try {
            bindNode(apNodeFdn, hardwareSerialNumber);
            ddpTimer.end(apNodeFdn);
            workflowVariables.setBindSuccessful(true);
        } catch (final Exception e) {
            logger.warn("Bind failed for node {} with hardware serial number {}: {}", apNodeFdn, hardwareSerialNumber, e.getMessage(), e);
            workflowVariables.setBindSuccessful(false);
            ddpTimer.endWithError(apNodeFdn);
        }
    }

    /**
     * Binds the supplied hardware serial number to the given node.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param hardwareSerialNumber
     *            the hardware serial number to bind
     */
    protected abstract void bindNode(final String apNodeFdn, final String hardwareSerialNumber);
}
