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
package com.ericsson.oss.services.ap.core.usecase.workflow;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.workflow.AutoProvisioningWorkflowService;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.wfs.api.query.WorkflowObject;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;
import com.ericsson.oss.services.wfs.jee.api.WorkflowQueryServiceLocal;

/**
 * Unit tests for {@link WorkflowOperations}.
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkflowOperationsTest {

    private static final String ACTIVE_WORKFLOW_INSTANCE_ID = "workflowId";
    private static final String DELETE_WORKFLOW_NAME = "deleteWorkflow";

    @Mock
    private AutoProvisioningWorkflowService apWorkflowService;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private DpsOperations dps;

    @Mock
    private DataPersistenceService dpsService;

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private ManagedObject nodeMo;

    @Mock
    private ManagedObject nodeStatusMo;

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    @Mock
    private ServiceFinderBean serviceFinder;

    @Mock
    private WorkflowObject workflowInstanceObject;

    @Mock
    private WorkflowInstanceServiceLocal wfsInstanceService;

    @InjectMocks
    private WorkflowOperations workflowOperations;

    @Mock
    private WorkflowQueryServiceLocal workflowQueryService;

    @Before
    public void setUp() {
        when(dps.getDataPersistenceService()).thenReturn(dpsService);
        when(dps.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findMoByFdn(NODE_FDN)).thenReturn(nodeMo);
        when(nodeTypeMapper.getInternalEjbQualifier(VALID_NODE_TYPE)).thenReturn(VALID_NODE_TYPE.toLowerCase());
        when(serviceFinder.find(AutoProvisioningWorkflowService.class, VALID_NODE_TYPE.toLowerCase())).thenReturn(apWorkflowService);
        when(apWorkflowService.getDeleteWorkflowName()).thenReturn(DELETE_WORKFLOW_NAME);
        when(nodeMo.getAttribute(NodeAttribute.NODE_TYPE.toString())).thenReturn(VALID_NODE_TYPE);
        when(serviceFinder.find(WorkflowQueryServiceLocal.class)).thenReturn(workflowQueryService);
    }

    @Test
    public void whenCancelIntegrationWorkflow_andApNodeHasNoActiveWorkflow_thenNoCancelIsAttempted() {
        when(nodeMo.getAttribute(NodeAttribute.ACTIVE_WORKFLOW_INSTANCE_ID.toString())).thenReturn("");
        workflowOperations.cancelIntegrationWorkflowIfActive(NODE_FDN);
        verify(wfsInstanceService, never()).cancelWorkflowInstance(anyString(), anyBoolean());
    }

    @Test
    public void whenCancelIntegrationWorkflow_thenInstanceIsCancelled_andNodeAttributeIsSetToNull() {
        when(nodeMo.getAttribute(NodeAttribute.ACTIVE_WORKFLOW_INSTANCE_ID.toString())).thenReturn(ACTIVE_WORKFLOW_INSTANCE_ID);
        workflowOperations.cancelIntegrationWorkflowIfActive(NODE_FDN);
        verify(wfsInstanceService).cancelWorkflowInstance(ACTIVE_WORKFLOW_INSTANCE_ID, true);
        verify(nodeMo).setAttribute(NodeAttribute.ACTIVE_WORKFLOW_INSTANCE_ID.toString(), null);
    }

    @Test(expected = ApApplicationException.class)
    public void whenCancelIntegrationWorkflow_andExceptionOccurs_thenApApplicationExceptionIsThrown() {
        when(nodeMo.getAttribute(NodeAttribute.ACTIVE_WORKFLOW_INSTANCE_ID.toString())).thenReturn(ACTIVE_WORKFLOW_INSTANCE_ID);
        doThrow(Exception.class).when(wfsInstanceService).cancelWorkflowInstance(anyString(), anyBoolean());
        workflowOperations.cancelIntegrationWorkflowIfActive(NODE_FDN);
    }

    @Test
    public void whenExecuteDeleteWorkflow_andNodeStateIsNotDeleteFailed_thenWorkflowIsStarted_andTrueIsReturned() {
        when(liveBucket.findMoByFdn(NODE_FDN + ",NodeStatus=1")).thenReturn(nodeStatusMo);
        when(nodeStatusMo.getAttribute("state")).thenReturn("DELETE_STARTED");

        final boolean result = workflowOperations.executeDeleteWorkflow(NODE_FDN, true, null);
        verify(wfsInstanceService).startWorkflowInstanceByDefinitionId(eq(DELETE_WORKFLOW_NAME), anyString(), anyMapOf(String.class, Object.class));
        assertTrue(result);
    }

    @Test
    public void whenExecuteDeleteWorkflowDHCPandNodeStateIsNotDeleteFailedThenWorkflowIsStartedAndTrueIsReturned() {
        when(liveBucket.findMoByFdn(NODE_FDN + ",NodeStatus=1")).thenReturn(nodeStatusMo);
        when(nodeStatusMo.getAttribute("state")).thenReturn("DELETE_STARTED");

        final boolean result = workflowOperations.executeDeleteWorkflow(NODE_FDN, true, "ABC1234567");
        verify(wfsInstanceService).startWorkflowInstanceByDefinitionId(eq(DELETE_WORKFLOW_NAME), anyString(), anyMapOf(String.class, Object.class));
        assertTrue(result);
    }

    @Test
    public void whenExecuteDeleteWorkflow_andNodeStateIsDeleteFailed_thenWorkflowIsStarted_andFalseIsReturned() {
        when(liveBucket.findMoByFdn(NODE_FDN + ",NodeStatus=1")).thenReturn(nodeStatusMo);
        when(nodeStatusMo.getAttribute("state")).thenReturn("DELETE_FAILED");

        final boolean result = workflowOperations.executeDeleteWorkflow(NODE_FDN, true, null);

        verify(wfsInstanceService).startWorkflowInstanceByDefinitionId(eq(DELETE_WORKFLOW_NAME), anyString(), anyMapOf(String.class, Object.class));
        assertFalse(result);
    }

    @Test(expected = ApApplicationException.class)
    public void whenExecuteDeleteWorkflow_andExceptionOccurs_thenApApplicationExceptionIsThrown() {
        when(liveBucket.findMoByFdn(NODE_FDN + ",NodeStatus=1")).thenReturn(nodeStatusMo);
        doThrow(Exception.class).when(wfsInstanceService).startWorkflowInstanceByDefinitionId(anyString(), anyString(),
                anyMapOf(String.class, Object.class));
        workflowOperations.executeDeleteWorkflow(NODE_FDN, true, null);
    }
}
