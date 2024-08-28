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
package com.ericsson.oss.services.ap.common.workflow.recording;

import com.ericsson.oss.itpf.sdk.context.classic.ContextServiceBean;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommandRecorderTest {

    @InjectMocks
    CommandRecorder commandRecorder;

    @Mock
    SystemRecorder systemRecorder;

    @Mock
    ContextServiceBean contextServiceBean;

    @Mock
    protected AbstractWorkflowVariables workflowVariables;

    @Mock
    protected TaskExecution execution;

    @Before
    public void setUp() {
        when(execution.getVariable(WORKFLOW_VARIABLES_KEY)).thenReturn(workflowVariables);
        when(workflowVariables.getApNodeFdn()).thenReturn(NODE_FDN);
        when(workflowVariables.getUserId()).thenReturn("userId");
        when(workflowVariables.getNodeName()).thenReturn("Node1");
    }

    @Test
    public void testMigrationFailedRecorder() {
        doNothing().when(systemRecorder).recordCommand(Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        doNothing().when(contextServiceBean).setContextValue(Mockito.any(), Mockito.anyString());
        commandRecorder.migrationFailed(workflowVariables);
        assertEquals("Project=Project1,Node=Node1", workflowVariables.getApNodeFdn());
    }

    @Test
    public void testMigrationCompletedWithWarnings() {
        doNothing().when(systemRecorder).recordCommand(Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        doNothing().when(contextServiceBean).setContextValue(Mockito.any(), Mockito.anyString());
        commandRecorder.migrationCompletedWithWarnings(workflowVariables);
        assertEquals("Project=Project1,Node=Node1", workflowVariables.getApNodeFdn());
    }

    @Test
    public void testMigrationSuccessful() {
        doNothing().when(systemRecorder).recordCommand(Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        doNothing().when(contextServiceBean).setContextValue(Mockito.any(), Mockito.anyString());
        commandRecorder.migrationSuccessful(workflowVariables);
        assertEquals("Project=Project1,Node=Node1", workflowVariables.getApNodeFdn());
    }

    @Test
    public void testMigrationCancelled() {
        doNothing().when(systemRecorder).recordCommand(Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        doNothing().when(contextServiceBean).setContextValue(Mockito.any(), Mockito.anyString());
        commandRecorder.migrationCancelled(workflowVariables);
        assertEquals("Project=Project1,Node=Node1", workflowVariables.getApNodeFdn());
    }
}
