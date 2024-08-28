/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.workflow.task.migration;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;

/**
 * Task executed at the beginning of the node migration, which sets the AP <code>NodeStatus</code> <i>state</i> to <b>MIGRATION_STARTED</b>.
 */
public class MigrationStartedTask extends AbstractServiceTask {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private ServiceFinderBean serviceFinder = new ServiceFinderBean();

    @Override
    public void executeTask(final TaskExecution execution) {
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        final String apNodeFdn = workflowVariables.getApNodeFdn();

        logger.info("Executing {} for node {}, for migration", getClass().getSimpleName(), apNodeFdn);
        final StateTransitionManagerLocal stateTransitionManager = serviceFinder.find(StateTransitionManagerLocal.class);
        stateTransitionManager.validateAndSetNextState(apNodeFdn, StateTransitionEvent.MIGRATION_STARTED);
        workflowVariables.setMigrationStartTime(System.currentTimeMillis());
    }
}
