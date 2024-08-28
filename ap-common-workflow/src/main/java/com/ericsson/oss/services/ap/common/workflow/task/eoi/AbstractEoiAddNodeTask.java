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

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.ap.common.workflow.BpmnErrorKey;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;

public abstract class AbstractEoiAddNodeTask extends AbstractServiceTask {
    protected final ServiceFinderBean serviceFinder = new ServiceFinderBean();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void executeTask(TaskExecution taskExecution) {

        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) taskExecution.getVariable(WORKFLOW_VARIABLES_KEY);
        final String apNodeFdn = workflowVariables.getApNodeFdn();
        final String nodeType = workflowVariables.getNodeType();
        try {
            eoiAddNode(apNodeFdn, nodeType);
        } catch (final Exception e) {
            logger.warn("Error executing {} for node {}: {}", getClass().getSimpleName(), apNodeFdn, e.getMessage(), e);
            throwBpmnError(BpmnErrorKey.EOI_INTEGRATION_WORKFLOW_ERROR_KEY, e.getMessage(), taskExecution);
        }
    }

    protected abstract void eoiAddNode(final String apNodeFdn, final String nodeType);

}

