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
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AbstractEoiGenerateArtifactTask}.
 */
@RunWith(MockitoJUnitRunner.class)

public class AbstractEoiGenerateArtifactTaskTest {
    @Mock
    private TaskExecutionImpl taskExecutionImpl;

    @Mock
    private AbstractWorkflowVariables workflowVariables;

    @InjectMocks
    private EoiGenerateArtifactTask eoiGenerateArtifactTask = new EoiGenerateArtifactTask();

    @Before
    public void setUp() {
        when(taskExecutionImpl.getVariable(WORKFLOW_VARIABLES_KEY)).thenReturn(workflowVariables);
    }

    @Test
    public void testEoiGenerateArtifactTaskExecutedSuccessfully() {
        when(workflowVariables.getApNodeFdn()).thenReturn(NodeDescriptor.NODE_FDN);
        eoiGenerateArtifactTask.executeTask(taskExecutionImpl);
        verify(taskExecutionImpl, times(0)).throwBpmnError(Mockito.any(), Mockito.any());
    }

    @Test
    public void testEoiGenerateArtifactTaskFailedThenBpmnErrorIsThrown() {
        when(workflowVariables.getApNodeFdn()).thenReturn(NodeDescriptor.NODE_FDN + "fail");
        eoiGenerateArtifactTask.executeTask(taskExecutionImpl);
        verify(taskExecutionImpl, times(1)).throwBpmnError(Mockito.any(), Mockito.any());
    }

    private final class EoiGenerateArtifactTask extends AbstractEoiGenerateArtifactTask {
        @Override
        protected void eoiGenerateArtifact(String apNodeFdn) {
            if (null != apNodeFdn && apNodeFdn.contains("fail")) {
                throw new ApApplicationException("error");
            }
        }

    }
}
