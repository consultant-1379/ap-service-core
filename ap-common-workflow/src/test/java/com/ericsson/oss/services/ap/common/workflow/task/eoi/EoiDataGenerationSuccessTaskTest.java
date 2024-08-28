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

import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal;
import com.ericsson.oss.services.ap.common.workflow.task.common.AbstractFailedTaskTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EoiDataGenerationSuccessTask}.
 */
@RunWith(MockitoJUnitRunner.class)
public class EoiDataGenerationSuccessTaskTest extends AbstractFailedTaskTest {

    @InjectMocks
    private EoiDataGenerationSuccessTask eoiDataGenerationSuccessTask;


    @Before
    public void setUp() {
        when(execution.getVariable(WORKFLOW_VARIABLES_KEY)).thenReturn(workflowVariables);
        when(workflowVariables.getApNodeFdn()).thenReturn(NODE_FDN);
        when(serviceFinder.find(StateTransitionManagerLocal.class)).thenReturn(stateTransitionManager);
    }

    @Test
    public void whenEoiOrderIsSuccessful() {
        when(workflowVariables.isOrderSuccessful()).thenReturn(true);
        eoiDataGenerationSuccessTask.executeTask(execution);

        verify(stateTransitionManager, times(1)).validateAndSetNextState(NODE_FDN, StateTransitionEvent.EOI_INTEGRATION_SUCCESSFUL);
    }


}
