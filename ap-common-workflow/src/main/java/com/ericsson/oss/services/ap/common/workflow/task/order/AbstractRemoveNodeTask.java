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

import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Abstract class for a service task to delete a <code>NetworkElement</code> for an AP node.
 */
public abstract class AbstractRemoveNodeTask extends AbstractServiceTask {

    protected final ServiceFinderBean serviceFinder = new ServiceFinderBean();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final DdpTimer ddpTimer = new DdpTimer();

    @Override
    public void executeTask(final TaskExecution execution) {
        ddpTimer.start(CommandLogName.REMOVE_NODE.toString());
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        final String apNodeFdn = workflowVariables.getApNodeFdn();
        logger.info("Executing {} for node {}", getClass().getSimpleName(), apNodeFdn);

        try {
            removeNode(apNodeFdn);
            ddpTimer.end(apNodeFdn);
        } catch (final Exception e) {
            logger.warn("Error executing {} for node {}: {}", getClass().getSimpleName(), apNodeFdn, e.getMessage(), e);
            workflowVariables.setUnorderOrRollbackError(true);
            ddpTimer.endWithError(apNodeFdn);
        }
    }

    /**
     * Deletes the <code>NetworkElement</code> for the specified AP node.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     */
    protected abstract void removeNode(final String apNodeFdn);
}
