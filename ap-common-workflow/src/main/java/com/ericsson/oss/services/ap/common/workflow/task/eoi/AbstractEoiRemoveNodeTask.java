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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.ap.common.workflow.BpmnErrorKey;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

public abstract class AbstractEoiRemoveNodeTask extends AbstractServiceTask {
    protected final ServiceFinderBean serviceFinder = new ServiceFinderBean();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void executeTask(TaskExecution taskExecution) {
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) taskExecution.getVariable(WORKFLOW_VARIABLES_KEY);
        final String apNodeFdn = workflowVariables.getApNodeFdn();
        try {
            eoiRemoveNode(apNodeFdn);
        } catch (final Exception e) {
            logger.error("EoiRemoveNodeTask is failed for the node fdn : {} with reason : {}", apNodeFdn, e.getMessage());
            workflowVariables.setEoiRollbackError(true);
            throwBpmnError(BpmnErrorKey.EOI_INTEGRATION_WORKFLOW_ERROR_KEY, e.getMessage(), taskExecution);
        }

    }
    protected abstract void eoiRemoveNode(final String apNodeFdn);

}
