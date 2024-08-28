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
package com.ericsson.oss.services.ap.common.workflow.task.order;

import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.impl.TaskExecutionImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AbstractCreateNodeUserCredentialsTask}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractCreateNodeUserCredentialsTaskTest {

    @Mock
    private TaskExecutionImpl taskExecutionImpl;

    @Mock
    private AbstractWorkflowVariables workflowVariables;

    @InjectMocks
    private NodeCredentialsCreation nodeCredentialsCreation = new NodeCredentialsCreation();

    @Before
    public void setUp() {
        when(taskExecutionImpl.getVariable(WORKFLOW_VARIABLES_KEY)).thenReturn(workflowVariables);
    }

    @Test
    public void whenNodeCredentialsCreatedSuccessfully() {
        when(workflowVariables.getApNodeFdn()).thenReturn(NodeDescriptor.NODE_FDN);
        nodeCredentialsCreation.executeTask(taskExecutionImpl);
        verify(taskExecutionImpl, times(0)).throwBpmnError(Mockito.any(), Mockito.any());
    }


    @Test
    public void whenNodeCredentialsCreationFailedThenBpmnErrorIsThrownForMigrationUseCase() {
        when(workflowVariables.getApNodeFdn()).thenReturn(NodeDescriptor.NODE_FDN + "fail");
        when(workflowVariables.isMigrationNodeUsecase()).thenReturn(true);
        nodeCredentialsCreation.executeTask(taskExecutionImpl);
        verify(taskExecutionImpl, times(1)).throwBpmnError(Mockito.any(), Mockito.any());
    }

    @Test
    public void whenNodeCredentialsCreationFailedThenBpmnErrorIsThrownForIntegrationUseCase() {
        when(workflowVariables.getApNodeFdn()).thenReturn(NodeDescriptor.NODE_FDN + "fail");
        when(workflowVariables.isMigrationNodeUsecase()).thenReturn(false);
        nodeCredentialsCreation.executeTask(taskExecutionImpl);
        verify(taskExecutionImpl, times(1)).throwBpmnError(Mockito.any(), Mockito.any());
    }

    private final class NodeCredentialsCreation extends AbstractCreateNodeUserCredentialsTask {

        /**
         * Creates the node user credentials for the specified AP node.
         *
         * @param apNodeFdn the FDN of the AP node
         */
        @Override
        protected void createNodeUserCredentials(String apNodeFdn) {
            if (null != apNodeFdn && apNodeFdn.contains("fail")) {
                throw new ApApplicationException("error");
            }
        }
    }
}
