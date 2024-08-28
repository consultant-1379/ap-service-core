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

import com.ericsson.oss.services.ap.common.workflow.task.common.BpmnErrorKeyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.common.model.SupervisionMoType;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Abstract class for a service task to enable FM/PM/Inventory supervision for an AP node.
 */
public abstract class AbstractEnableSupervisionTask extends AbstractServiceTask {

    protected final ServiceFinderBean serviceFinder = new ServiceFinderBean();

    private DdpTimer ddpTimer = new DdpTimer(); // NOPMD
    private Logger logger = LoggerFactory.getLogger(getClass()); // NOPMD

    @Override
    public void executeTask(final TaskExecution execution) {
        ddpTimer.start(CommandLogName.ENABLE_SUPERVISION.toString());
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        logger.info("Executing {} for node {}", this.getClass().getSimpleName(), workflowVariables.getApNodeFdn());

        for (final SupervisionMoType supervisionType : SupervisionMoType.values()) {
            if (!SupervisionMoType.CM.equals(supervisionType)) {
                enableSupervision(workflowVariables, supervisionType);
            }
        }
        ddpTimer.end(workflowVariables.getApNodeFdn());
    }

    private void enableSupervision(final AbstractWorkflowVariables workflowVariables, final SupervisionMoType supervisionToEnable) {
        try {
            if (workflowVariables.isEnableSupervision(supervisionToEnable)) {
                enableSupervision(workflowVariables.getApNodeFdn(), supervisionToEnable);
            }
        } catch (final Exception e) {
            logger.warn("Error enabling {} supervision for node {}: {}", supervisionToEnable, workflowVariables.getApNodeFdn(), e.getMessage(), e);
            BpmnErrorKeyHandler.handleWorkflowForWarnings(workflowVariables);
        }
    }

    /**
     * Enable supervision for an AP node.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param supervisionToEnable
     *            the supervision MO to enable
     */
    protected abstract void enableSupervision(final String apNodeFdn, final SupervisionMoType supervisionToEnable);
}
