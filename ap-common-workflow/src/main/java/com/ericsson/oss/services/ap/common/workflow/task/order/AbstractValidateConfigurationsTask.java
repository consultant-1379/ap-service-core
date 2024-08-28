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
package com.ericsson.oss.services.ap.common.workflow.task.order;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.ap.common.workflow.task.common.BpmnErrorKeyHandler;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Abstract class for a service task to validate BulkCM configuration files for an AP node.
 */
public abstract class AbstractValidateConfigurationsTask extends AbstractServiceTask {

    protected ServiceFinderBean serviceFinder = new ServiceFinderBean();

    private final DdpTimer ddpTimer = new DdpTimer();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void executeTask(final TaskExecution execution) {
        ddpTimer.start(CommandLogName.VALIDATE_CONFIGURATIONS.toString());
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        final String apNodeFdn = workflowVariables.getApNodeFdn();
        executeValidate(execution, apNodeFdn, workflowVariables);
        ddpTimer.end(apNodeFdn);
    }

    private void executeValidate(final TaskExecution execution, final String apNodeFdn, final AbstractWorkflowVariables workflowVariables) {
        try {
            validateNodeConfigurations(apNodeFdn);
        } catch (final Exception e) {
            logger.error("Error executing {} for node {}: {}", this.getClass().getSimpleName(), apNodeFdn, e.getMessage(), e);
            final String errorKey = BpmnErrorKeyHandler.handleWorkflowForFailedUseCase(workflowVariables);
            throwBpmnError(errorKey, e.getMessage(), execution);
        }
    }



    /**
     * Validates BulkCM configuration files for the specified AP node.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     */
    protected abstract void validateNodeConfigurations(final String apNodeFdn);
}
