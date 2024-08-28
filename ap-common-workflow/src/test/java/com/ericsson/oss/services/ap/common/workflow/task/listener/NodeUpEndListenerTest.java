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
package com.ericsson.oss.services.ap.common.workflow.task.listener;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.api.status.StatusEntryNames;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Unit tests for {@link NodeUpEndListener}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeUpEndListenerTest {

    @Mock
    private AbstractWorkflowVariables workflowVariables;

    @Mock
    private StatusEntryManagerLocal statusEntryManager;

    @Mock
    private TaskExecution execution;

    @InjectMocks
    private NodeUpEndListener nodeUpEndListener;

    @Before
    public void setUp() {
        when(execution.getVariable(WORKFLOW_VARIABLES_KEY)).thenReturn(workflowVariables);
        when(workflowVariables.getApNodeFdn()).thenReturn(NODE_FDN);
    }

    @Test
    public void whenExecutingListenerAndNotificationIsReceivedThenStatusIsUpdatedToReceived() {
        when(execution.getVariable("NodeUpNotification")).thenReturn("Received");
        nodeUpEndListener.executeTask(execution);
        verify(statusEntryManager, times(1)).notificationReceived(NODE_FDN, StatusEntryNames.NODE_UP.toString());
    }

    @Test
    public void whenExecutingListenerAndNotificationIsNotReceivedThenStatusIsUpdatedToCancelled() {
        when(execution.getVariable("NodeUpNotification")).thenReturn("SomethingElse");
        nodeUpEndListener.executeTask(execution);
        verify(statusEntryManager, times(1)).notificationCancelled(NODE_FDN, StatusEntryNames.NODE_UP.toString());
    }
}
