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
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;

public class EoiDataGenerationFailedTask extends AbstractServiceTask {
    private ServiceFinderBean serviceFinder = new ServiceFinderBean(); // NOPMD
    private DpsOperations dpsOperations = new DpsOperations();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void executeTask(TaskExecution taskExecution) {
        logger.info("EoiDataGenerationFailed task");
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) taskExecution.getVariable(WORKFLOW_VARIABLES_KEY);
        final String apNodeFdn = workflowVariables.getApNodeFdn();
        final boolean eoiRollbackSuccess=!workflowVariables.isEoiRollbackError();
        setNodeStateToOrderFailed(apNodeFdn,eoiRollbackSuccess);
        dpsOperations.updateMo(apNodeFdn, NodeAttribute.ACTIVE_WORKFLOW_INSTANCE_ID.toString(), null);
    }

    private void setNodeStateToOrderFailed(final String apNodeFdn,final boolean eoiRollbackSuccess) {
        final StateTransitionManagerLocal stateTransitionManager = serviceFinder.find(StateTransitionManagerLocal.class);
        final StateTransitionEvent event = eoiRollbackSuccess ? StateTransitionEvent.EOI_INTEGRATION_FAILED : StateTransitionEvent.EOI_INTEGRATION_ROLLBACK_FAILED;
        stateTransitionManager.validateAndSetNextState(apNodeFdn, event);

    }
}
