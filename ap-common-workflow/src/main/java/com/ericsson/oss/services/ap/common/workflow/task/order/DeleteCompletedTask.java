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
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * This task sets the AP <code>NodeStatus</code> <i>state</i> to <b>DELETE_FAILED</b> if delete completes with failure.
 * <p>
 * There is no need to update the state in a successful scenario, as the AP node MO will have been deleted.
 */
public class DeleteCompletedTask extends AbstractServiceTask {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ServiceFinderBean serviceFinder = new ServiceFinderBean(); // NOPMD

    @Override
    public void executeTask(final TaskExecution execution) {
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        final String apNodeFdn = workflowVariables.getApNodeFdn();

        logger.info("Executing {} for node {}", getClass().getSimpleName(), apNodeFdn);

        if (workflowVariables.isUnorderOrRollbackError()) {
            setNodeStateToDeleteFailed(apNodeFdn);
        }
    }

    private void setNodeStateToDeleteFailed(final String apNodeFdn) {
        final StateTransitionManagerLocal stateTransitionManager = serviceFinder.find(StateTransitionManagerLocal.class);
        stateTransitionManager.validateAndSetNextState(apNodeFdn, StateTransitionEvent.DELETE_FAILED);
    }
}
