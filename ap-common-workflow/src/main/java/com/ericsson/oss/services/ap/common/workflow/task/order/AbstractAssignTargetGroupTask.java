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
package com.ericsson.oss.services.ap.common.workflow.task.order;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.ap.common.workflow.BpmnErrorKey;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * This task sets the assigns target groups to node.
 */
public abstract class AbstractAssignTargetGroupTask extends AbstractServiceTask {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void executeTask(final TaskExecution execution) {
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        final String apNodeFdn = workflowVariables.getApNodeFdn();
        logger.info("Executing {} for node {}", getClass().getSimpleName(), apNodeFdn);
        executeAssignTargetGroups(execution, workflowVariables, apNodeFdn);
}

    private void executeAssignTargetGroups(final TaskExecution execution, final AbstractWorkflowVariables workflowVariables, final String apNodeFdn) {
        try {
            assignTargetGroups(apNodeFdn);
        } catch (final Exception e) {
            logger.warn("Error executing {} for node {}: {}", this.getClass().getSimpleName(), apNodeFdn, e.getMessage(), e);
            workflowVariables.setOrderSuccessful(false);
            throwBpmnError(BpmnErrorKey.ORDER_WORKFLOW_ERROR_KEY, e.getMessage(), execution);
        }
    }

    protected abstract void assignTargetGroups(final String apNodeFdn);
}

