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
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.ap.common.workflow.recording.CommandRecorder;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;

/**
 * This task sets the AP <code>NodeStatus</code> <i>state</i> at the end of an order rollback. If the rollback was successful, the <i>state</i> is
 * updated to <b>MIGRATION_FAILED</b>. If the rollback failed, the <i>state</i> is updated to <b>MIGRATION_ROLLBACK_FAILED</b>.
 * <p>
 * As this is also the end state of the workflow, the AP <code>Node</code> {@link NodeAttribute#ACTIVE_WORKFLOW_INSTANCE_ID} attribute is set to
 * <b>null</b>.
 */
public class MigrationFailedTask extends AbstractServiceTask {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private CommandRecorder commandRecorder = new CommandRecorder(); // NOPMD
    private DpsOperations dpsOperations = new DpsOperations(); // NOPMD
    private ServiceFinderBean serviceFinder = new ServiceFinderBean(); // NOPMD

    @Override
    public void executeTask(TaskExecution execution) {
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution
            .getVariable(WORKFLOW_VARIABLES_KEY);
        final String apNodeFdn = workflowVariables.getApNodeFdn();

        logger.info("Executing {} for node {}", getClass().getSimpleName(), apNodeFdn);

        final StateTransitionManagerLocal stateTransitionManager = serviceFinder
            .find(StateTransitionManagerLocal.class);
        final StateTransitionEvent event = StateTransitionEvent.MIGRATION_FAILED;
        stateTransitionManager.validateAndSetNextState(apNodeFdn, event);
        commandRecorder.migrationFailed(workflowVariables);
        dpsOperations.updateMo(apNodeFdn, NodeAttribute.ACTIVE_WORKFLOW_INSTANCE_ID.toString(), null);
    }
}
