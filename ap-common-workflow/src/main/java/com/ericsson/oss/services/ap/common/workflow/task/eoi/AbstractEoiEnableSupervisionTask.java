/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.workflow.task.eoi;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;

import com.ericsson.oss.services.ap.common.workflow.task.common.BpmnErrorKeyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.common.model.SupervisionMoType;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.ap.common.workflow.BpmnErrorKey;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

public abstract class AbstractEoiEnableSupervisionTask extends AbstractServiceTask {
    protected final ServiceFinderBean serviceFinder = new ServiceFinderBean();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void executeTask(TaskExecution taskExecution) {
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) taskExecution.getVariable(WORKFLOW_VARIABLES_KEY);
        logger.info("Executing {} for node {}", this.getClass().getSimpleName(), workflowVariables.getApNodeFdn());
        final String apNodeFdn = workflowVariables.getApNodeFdn();

        for (final SupervisionMoType supervisionType : SupervisionMoType.values()) {
            if (!SupervisionMoType.INVENTORY.equals(supervisionType)) {
                enableSupervision(workflowVariables, supervisionType);
            }
        }
    }
    private void enableSupervision(final AbstractWorkflowVariables workflowVariables, final SupervisionMoType supervisionToEnable) {
        logger.info("Enabling supervision {} for node {}", supervisionToEnable, workflowVariables.getApNodeFdn());
        try {
            if (workflowVariables.isEnableSupervision(supervisionToEnable)) {
                logger.info("Executing supervision for {}", supervisionToEnable);
                eoiEnableSupervision(workflowVariables.getApNodeFdn(), supervisionToEnable);
            }
        } catch (final Exception e) {
            logger.error("Error enabling {} EoiEnableSupervisionTask is failed for the node fdn  {}: {}", supervisionToEnable, workflowVariables.getApNodeFdn(), e.getMessage(), e);
            BpmnErrorKeyHandler.handleWorkflowForWarnings(workflowVariables);
        }
    }

    protected abstract void eoiEnableSupervision(final String apNodeFdn, final SupervisionMoType supervisionToEnable);

}
