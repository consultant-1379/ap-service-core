/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.restore;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link WorkflowRestoreCriteria}.
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkflowRestoreCriteriaTest {

    @Mock
    private NodeCondition nodeCondition;

    @InjectMocks
    private WorkflowRestoreCriteria workflowRestoreCriteria;

    @Test
    public void whenNodeIsNotSynced_andNodeIsWaitingForNodeUp_andRestorePeriodHasExpired_thenWorkflowIsResumable() {
        when(nodeCondition.isNodeSynchronized(NODE_FDN)).thenReturn(false);
        when(nodeCondition.isWaitingForNodeUp(NODE_FDN)).thenReturn(true);
        final boolean resumeWorkflow = workflowRestoreCriteria.isWorkflowResumable(NODE_FDN, true);
        assertTrue("Workflow was resumable unexpectedly", resumeWorkflow);
    }

    @Test
    public void whenNodeIsNotSynced_andNodeIsWaitingForNodeUp_andRestorePeriodHasNotExpired_thenWorkflowIsNotResumable() {
        when(nodeCondition.isNodeSynchronized(NODE_FDN)).thenReturn(false);
        when(nodeCondition.isWaitingForNodeUp(NODE_FDN)).thenReturn(true);
        final boolean resumeWorkflow = workflowRestoreCriteria.isWorkflowResumable(NODE_FDN, false);
        assertFalse("Workflow was resumable unexpectedly", resumeWorkflow);
    }

    @Test
    public void whenNodeIsSynced_andNodeIsWaitingForNodeUp_andRestorePeriodHasNotExpired_thenWorkflowIsNotResumable() {
        when(nodeCondition.isNodeSynchronized(NODE_FDN)).thenReturn(true);
        when(nodeCondition.isWaitingForNodeUp(NODE_FDN)).thenReturn(true);
        final boolean resumeWorkflow = workflowRestoreCriteria.isWorkflowResumable(NODE_FDN, false);
        assertFalse("Workflow was resumable unexpectedly", resumeWorkflow);
    }

    @Test
    public void whenNodeIsSynced_andNodeIsNotWaitingForNodeUp_andRestorePeriodHasNotExpired_thenWorkflowIsNotResumable() {
        when(nodeCondition.isNodeSynchronized(NODE_FDN)).thenReturn(true);
        when(nodeCondition.isWaitingForNodeUp(NODE_FDN)).thenReturn(false);
        final boolean resumeWorkflow = workflowRestoreCriteria.isWorkflowResumable(NODE_FDN, false);
        assertFalse("Workflow was resumable unexpectedly", resumeWorkflow);
    }

    @Test
    public void whenNodeIsSynced_thenWorkflowIsCancellable() {
        when(nodeCondition.isNodeSynchronized(NODE_FDN)).thenReturn(true);
        final boolean cancelWorkflow = workflowRestoreCriteria.isWorkflowCancellable(NODE_FDN, false);
        assertTrue("Workflow was not cancelled as expected", cancelWorkflow);
    }

    @Test
    public void whenNodeIsNotSynced_andIsNotWaitingForNodeUp_andRestorePeriodHasExpired_thenWorkflowIsCancellable() {
        when(nodeCondition.isNodeSynchronized(NODE_FDN)).thenReturn(false);
        when(nodeCondition.isWaitingForNodeUp(NODE_FDN)).thenReturn(false);
        final boolean cancelWorkflow = workflowRestoreCriteria.isWorkflowCancellable(NODE_FDN, true);
        assertTrue("Workflow was not cancelled as expected", cancelWorkflow);
    }

    @Test
    public void whenNodeIsNotSynced_andIsWaitingForNodeUp_andRestorePeriodHasExpired_thenWorkflowIsNotCancellable() {
        when(nodeCondition.isNodeSynchronized(NODE_FDN)).thenReturn(false);
        when(nodeCondition.isWaitingForNodeUp(NODE_FDN)).thenReturn(true);
        final boolean cancelWorkflow = workflowRestoreCriteria.isWorkflowCancellable(NODE_FDN, true);
        assertFalse("Workflow was cancelled unexpectedly", cancelWorkflow);
    }

    @Test
    public void whenNodeIsNotSynced_andIsNotWaitingForNodeUp_andRestorePeriodHasNotExpired_thenWorkflowIsNotCancellable() {
        when(nodeCondition.isNodeSynchronized(NODE_FDN)).thenReturn(false);
        when(nodeCondition.isWaitingForNodeUp(NODE_FDN)).thenReturn(false);
        final boolean cancelWorkflow = workflowRestoreCriteria.isWorkflowCancellable(NODE_FDN, false);
        assertFalse("Workflow was cancelled unexpectedly", cancelWorkflow);
    }
}
