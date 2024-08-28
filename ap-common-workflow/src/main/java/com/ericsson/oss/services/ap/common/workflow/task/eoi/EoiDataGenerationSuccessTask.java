package com.ericsson.oss.services.ap.common.workflow.task.eoi;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;


public class EoiDataGenerationSuccessTask extends AbstractServiceTask {
    private ServiceFinderBean serviceFinder = new ServiceFinderBean(); // NOPMD
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void executeTask(TaskExecution taskExecution) {
        logger.info("EoiDataGenerationSuccessTask");
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) taskExecution.getVariable(WORKFLOW_VARIABLES_KEY);
        final String apNodeFdn = workflowVariables.getApNodeFdn();
        setNodeStateToEoiCompleted(apNodeFdn);
    }

    private void setNodeStateToEoiCompleted(final String apNodeFdn) {
        final StateTransitionManagerLocal stateTransitionManager = serviceFinder.find(StateTransitionManagerLocal.class);
        stateTransitionManager.validateAndSetNextState(apNodeFdn, StateTransitionEvent.EOI_INTEGRATION_SUCCESSFUL);
    }
}
