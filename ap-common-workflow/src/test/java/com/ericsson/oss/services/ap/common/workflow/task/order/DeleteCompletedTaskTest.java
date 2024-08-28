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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Unit tests for {@link DeleteCompletedTask}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeleteCompletedTaskTest {

    @Mock
    private AbstractWorkflowVariables workflowVariables;

    @Mock
    private ServiceFinderBean serviceFinder;

    @Mock
    private TaskExecution execution;

    @Mock
    private StateTransitionManagerLocal stateTransitionManager;

    @InjectMocks
    private DeleteCompletedTask deleteCompletedTask;

    @Before
    public void setUp() {
        when(execution.getVariable(WORKFLOW_VARIABLES_KEY)).thenReturn(workflowVariables);
        when(workflowVariables.getApNodeFdn()).thenReturn(NODE_FDN);
        when(serviceFinder.find(StateTransitionManagerLocal.class)).thenReturn(stateTransitionManager);
    }

    @Test
    public void whenDeleteCompletedAndDeleteWasSuccessfulthenNoStateChangeOccurs() {
        when(workflowVariables.isUnorderOrRollbackError()).thenReturn(false);
        deleteCompletedTask.executeTask(execution);
        verify(stateTransitionManager, never()).validateAndSetNextState(NODE_FDN, StateTransitionEvent.DELETE_FAILED);
    }

    @Test
    public void whenDeleteCompletedAndDeleteFailedThenApNodeStateChangesToDeleteFailed() {
        when(workflowVariables.isUnorderOrRollbackError()).thenReturn(true);
        deleteCompletedTask.executeTask(execution);
        verify(stateTransitionManager, times(1)).validateAndSetNextState(NODE_FDN, StateTransitionEvent.DELETE_FAILED);
    }
}
