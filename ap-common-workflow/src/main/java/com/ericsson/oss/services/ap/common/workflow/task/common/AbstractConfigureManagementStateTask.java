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
package com.ericsson.oss.services.ap.common.workflow.task.common;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;

import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

public abstract class AbstractConfigureManagementStateTask extends AbstractServiceTask {

    @Override
    public void executeTask(final TaskExecution taskExecution){
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) taskExecution.getVariable(WORKFLOW_VARIABLES_KEY);
        final String apNodeFdn = workflowVariables.getApNodeFdn();
        final String maintenanceValue = (String) taskExecution.getVariable("managementState");
        configureManagementState(apNodeFdn, maintenanceValue);
    }

    public abstract void configureManagementState(final String apNodeFdn, final String maintenanceValue);
}