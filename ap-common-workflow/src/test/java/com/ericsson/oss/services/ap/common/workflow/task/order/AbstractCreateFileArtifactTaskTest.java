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
 * Unit tests for {@link AbstractCreateFileArtifactTask}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractCreateFileArtifactTaskTest {

    @InjectMocks
    private final ArtifactCreate artifactCreate = new ArtifactCreate();

    @Mock
    private TaskExecutionImpl taskExecutionImpl;

    @Mock
    private AbstractWorkflowVariables workflowVariables;

    @Before
    public void setUp() {
        when(taskExecutionImpl.getTaskId()).thenReturn("12__type_34");
        when(taskExecutionImpl.getVariable(WORKFLOW_VARIABLES_KEY)).thenReturn(workflowVariables);
    }

    @Test
    public void whenCreateFileArtifactSuccessful() {
        when(workflowVariables.getApNodeFdn()).thenReturn(NodeDescriptor.NODE_FDN);
        artifactCreate.executeTask(taskExecutionImpl);
        verify(taskExecutionImpl, times(0)).throwBpmnError(Mockito.any(), Mockito.any());
    }

    @Test
    public void whenCreateFileArtifactFailedForMigrationUseCaseAndErrorIsThrown() {
        when(workflowVariables.getApNodeFdn()).thenReturn(NodeDescriptor.NODE_FDN +"fail");
        when(workflowVariables.isMigrationNodeUsecase()).thenReturn(true);
        artifactCreate.executeTask(taskExecutionImpl);
        verify(taskExecutionImpl, times(1)).throwBpmnError(Mockito.any(), Mockito.any());
    }

    private final class ArtifactCreate extends AbstractCreateFileArtifactTask {

        /**
         * Creates the artifact of the given type for the specified AP node.
         *
         * @param apNodeFdn    the FDN of the AP node
         * @param artifactType
         */
        @Override
        protected void createArtifact(String apNodeFdn, String artifactType) {
            if (null != apNodeFdn && apNodeFdn.contains("fail")) {
                throw new ApApplicationException("error");
            }
        }
    }
}
