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

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal;
import com.ericsson.oss.services.ap.common.workflow.task.common.AbstractFailedTaskTest;

/**
 * Unit tests for {@link OrderFailedTask}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderFailedTaskTest extends AbstractFailedTaskTest  {

    @InjectMocks
    private OrderFailedTask orderFailedTask;

    @Before
    public void setUp() {
        when(execution.getVariable(WORKFLOW_VARIABLES_KEY)).thenReturn(workflowVariables);
        when(workflowVariables.getApNodeFdn()).thenReturn(NODE_FDN);
        when(serviceFinder.find(StateTransitionManagerLocal.class)).thenReturn(stateTransitionManager);
    }

    @Test
    public void whenOrderFailedAndRollbackWasSuccessfulThenApNodeStateChangesToOrderFailed() {
        when(workflowVariables.isUnorderOrRollbackError()).thenReturn(false);
        orderFailedTask.executeTask(execution);

        verify(dpsOperations, times(1)).updateMo(NODE_FDN, "activeWorkflowInstanceId", null);
        verify(commandRecorder, times(1)).orderFailed(workflowVariables);
        verify(stateTransitionManager, times(1)).validateAndSetNextState(NODE_FDN, StateTransitionEvent.ORDER_FAILED);
    }

    @Test
    public void whenOrderFailedAndRollbackFailedThenApNodeStateChangesToOrderRollbackFailed() {
        when(workflowVariables.isUnorderOrRollbackError()).thenReturn(true);
        orderFailedTask.executeTask(execution);

        verify(dpsOperations, times(1)).updateMo(NODE_FDN, "activeWorkflowInstanceId", null);
        verify(commandRecorder, times(1)).orderRollbackFailed(workflowVariables);
        verify(stateTransitionManager, times(1)).validateAndSetNextState(NODE_FDN, StateTransitionEvent.ORDER_ROLLBACK_FAILED);
    }
}
