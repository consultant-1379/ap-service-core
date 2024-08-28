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
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.ap.common.workflow.recording.CommandRecorder;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * This task sets the AP <code>NodeStatus</code> <i>state</i> at the end of an order rollback. If the rollback was successful, the <i>state</i> is
 * updated to <b>ORDER_FAILED</b>. If the rollback failed, the <i>state</i> is updated to <b>ORDER_ROLLBACK_FAILED</b>.
 * <p>
 * As this is also the end state of the workflow, the AP <code>Node</code> {@link NodeAttribute#ACTIVE_WORKFLOW_INSTANCE_ID} attribute is set to
 * <b>null</b>.
 */
public class OrderFailedTask extends AbstractServiceTask {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private CommandRecorder commandRecorder = new CommandRecorder(); // NOPMD
    private DpsOperations dpsOperations = new DpsOperations(); // NOPMD
    private ServiceFinderBean serviceFinder = new ServiceFinderBean(); // NOPMD

    @Override
    public void executeTask(final TaskExecution execution) {
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        final String apNodeFdn = workflowVariables.getApNodeFdn();

        logger.info("Executing {} for node {}", getClass().getSimpleName(), apNodeFdn);

        final boolean orderRollbackSuccess = !workflowVariables.isUnorderOrRollbackError();
        setNodeStateToOrderFailed(apNodeFdn, orderRollbackSuccess);
        dpsOperations.updateMo(apNodeFdn, NodeAttribute.ACTIVE_WORKFLOW_INSTANCE_ID.toString(), null);

        if (orderRollbackSuccess) {
            commandRecorder.orderFailed(workflowVariables);
        } else {
            commandRecorder.orderRollbackFailed(workflowVariables);
        }
    }

    private void setNodeStateToOrderFailed(final String apNodeFdn, final boolean orderRollbackSuccess) {
        final StateTransitionManagerLocal stateTransitionManager = serviceFinder.find(StateTransitionManagerLocal.class);
        final StateTransitionEvent event = orderRollbackSuccess ? StateTransitionEvent.ORDER_FAILED : StateTransitionEvent.ORDER_ROLLBACK_FAILED;
        stateTransitionManager.validateAndSetNextState(apNodeFdn, event);
    }
}
