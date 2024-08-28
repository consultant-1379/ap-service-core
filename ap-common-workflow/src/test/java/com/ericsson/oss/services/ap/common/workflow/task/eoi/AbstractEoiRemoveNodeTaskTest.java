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
import static org.mockito.Mockito.times;

/**
 * Unit tests for {@link AbstractEoiRemoveNodeTask}.
 */
@RunWith(MockitoJUnitRunner.class)

public class AbstractEoiRemoveNodeTaskTest {

    @Mock
    private TaskExecutionImpl taskExecutionImpl;

    @Mock
    private AbstractWorkflowVariables workflowVariables;

    @InjectMocks
    private EoiRemoveNodeTask eoiRemoveNodeTask = new EoiRemoveNodeTask();

    @Before
    public void setUp() {
        when(taskExecutionImpl.getVariable(WORKFLOW_VARIABLES_KEY)).thenReturn(workflowVariables);
    }

    @Test
    public void testEoiRemoveNodeTaskisExecutedSuccessfully() {
        when(workflowVariables.getApNodeFdn()).thenReturn(NodeDescriptor.NODE_FDN);
        eoiRemoveNodeTask.executeTask(taskExecutionImpl);
        verify(taskExecutionImpl, times(0)).throwBpmnError(Mockito.any(), Mockito.any());
    }

    @Test
    public void testEoiRemoveNodeTaskisFailedThenBpmnErrorIsThrown() {
        when(workflowVariables.getApNodeFdn()).thenReturn(NodeDescriptor.NODE_FDN + "fail");
        eoiRemoveNodeTask.executeTask(taskExecutionImpl);
        verify(taskExecutionImpl, times(1)).throwBpmnError(Mockito.any(), Mockito.any());
    }

    private final class EoiRemoveNodeTask extends AbstractEoiRemoveNodeTask {
        @Override
        protected void eoiRemoveNode(String apNodeFdn) {
            if (null != apNodeFdn && apNodeFdn.contains("fail")) {
                throw new ApApplicationException("error");
            }
        }
    }
}
