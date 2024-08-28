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
package com.ericsson.oss.services.ap.core.usecase.workflow;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.anyBoolean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.api.workflow.AutoProvisioningWorkflowService;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.wfs.api.WorkflowServiceException;
import com.ericsson.oss.services.wfs.api.query.Query;
import com.ericsson.oss.services.wfs.api.query.WorkflowObject;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;
import com.ericsson.oss.services.wfs.jee.api.WorkflowQueryServiceLocal;

/**
 * Unit tests for {@link WorkflowCleanUpOperations}.
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkflowCleanUpOperationsTest {

    private static final String ACTIVE_WORKFLOW_INSTANCE_ID = "workflowId";
    private static final String AP_WORKFLOW_NAME = "ap_workflow";
    private static final String NODE_TYPE_1 = "NodeType1";
    private static final String NODE_TYPE_2 = "NodeType2";
    private static final String NODE_TYPE_3 = "NodeType3";
    private static final String WFS_EJB_QUALIFIER_1 = "qualifier1";
    private static final String WFS_EJB_QUALIFIER_2 = "qualifier2";

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private Instance<AutoProvisioningWorkflowService> apWorkflowServices;

    @Mock
    private AutoProvisioningWorkflowService autoProvisioningWorkflowService;

    @Mock
    private ServiceFinderBean serviceFinder;

    @Mock
    private StatusEntryManagerLocal statusEntryManager;

    @Mock
    private WorkflowInstanceServiceLocal wfsInstanceService;

    @Mock
    private WorkflowObject workflowObject;

    @Mock
    private WorkflowQueryServiceLocal workflowQueryService;

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    @Mock
    private ModelReader modelReader;

    @InjectMocks
    private WorkflowCleanUpOperations workflowCleanUpOperations;

    @Before
    public void setUp() {
        when(serviceFinder.find(WorkflowQueryServiceLocal.class)).thenReturn(workflowQueryService);
        when(serviceFinder.find(StatusEntryManagerLocal.class)).thenReturn(statusEntryManager);
        when(serviceFinder.find(AutoProvisioningWorkflowService.class, WFS_EJB_QUALIFIER_1)).thenReturn(autoProvisioningWorkflowService);
        when(serviceFinder.find(AutoProvisioningWorkflowService.class, WFS_EJB_QUALIFIER_2)).thenReturn(autoProvisioningWorkflowService);
        when(serviceFinder.find(WorkflowInstanceServiceLocal.class)).thenReturn(wfsInstanceService);
        final List<AutoProvisioningWorkflowService> apwfsList = new ArrayList<>();
        apwfsList.add(autoProvisioningWorkflowService);
        when(apWorkflowServices.iterator()).thenReturn(apwfsList.iterator());

        final List<String> workflowNames = new ArrayList<>();
        workflowNames.add(AP_WORKFLOW_NAME);
        when(autoProvisioningWorkflowService.getAllWorkflowNames()).thenReturn(workflowNames);

        final List<String> nodeTypes = new ArrayList<>();
        nodeTypes.add(NODE_TYPE_1);
        nodeTypes.add(NODE_TYPE_2);
        nodeTypes.add(NODE_TYPE_3);
        when(modelReader.getSupportedNodeTypes()).thenReturn(nodeTypes);

        when(nodeTypeMapper.getInternalEjbQualifier(NODE_TYPE_1)).thenReturn(WFS_EJB_QUALIFIER_1);
        when(nodeTypeMapper.getInternalEjbQualifier(NODE_TYPE_2)).thenReturn(WFS_EJB_QUALIFIER_1);
        when(nodeTypeMapper.getInternalEjbQualifier(NODE_TYPE_3)).thenReturn(WFS_EJB_QUALIFIER_2);
    }

    @Test
    public void whenCancelInstance_andNoPreviousWorkflowInstanceExists_thenNoWorkflowIsCancelled() {
        when(workflowQueryService.executeQuery(any(Query.class))).thenReturn(Collections.<WorkflowObject> emptyList());
        workflowCleanUpOperations.cancelWorkflowInstanceIfItAlreadyExists(NODE_FDN);
        verify(statusEntryManager, never()).clearStatusEntries(NODE_FDN);
    }

    @Test(expected = ApApplicationException.class)
    public void whenCancelInstance_andPreviousWorkflowInstanceExistsForNode_andInstanceCancellationFails_thenApApplicationExceptionIsThrown() {
        final List<WorkflowObject> existingInstances = new ArrayList<>();
        when(workflowObject.getAttribute("workflowInstanceId")).thenReturn(ACTIVE_WORKFLOW_INSTANCE_ID);
        when(workflowObject.getAttribute("workflowDefinitionId")).thenReturn(AP_WORKFLOW_NAME);
        existingInstances.add(workflowObject);
        when(workflowQueryService.executeQuery(any(Query.class))).thenReturn(existingInstances);
        doThrow(WorkflowServiceException.class).when(wfsInstanceService).cancelWorkflowInstance(anyString(), anyBoolean());

        workflowCleanUpOperations.init();
        workflowCleanUpOperations.cancelWorkflowInstanceIfItAlreadyExists(NODE_FDN);
    }

    @Test
    public void whenCancelInstance_andPreviousWorkflowInstancesExistsForNode_thenAllWorkflowsCancelled() {
        final List<WorkflowObject> existingInstances = new ArrayList<>();
        final WorkflowObject parentInstance = Mockito.mock(WorkflowObject.class);
        final WorkflowObject childInstance = Mockito.mock(WorkflowObject.class);
        when(parentInstance.getAttribute("workflowInstanceId")).thenReturn(ACTIVE_WORKFLOW_INSTANCE_ID);
        when(parentInstance.getAttribute("workflowDefinitionId")).thenReturn(AP_WORKFLOW_NAME);
        when(childInstance.getAttribute("workflowInstanceId")).thenReturn("otherId");
        when(childInstance.getAttribute("workflowDefinitionId")).thenReturn("otherDefinition");
        existingInstances.add(parentInstance);
        existingInstances.add(childInstance);
        when(workflowQueryService.executeQuery(any(Query.class))).thenReturn(existingInstances);

        workflowCleanUpOperations.init();
        workflowCleanUpOperations.cancelWorkflowInstanceIfItAlreadyExists( NODE_FDN);

        verify(wfsInstanceService, times(1)).cancelWorkflowInstance(ACTIVE_WORKFLOW_INSTANCE_ID, true);
        verify(wfsInstanceService, never()).cancelWorkflowInstance("otherId", true);
    }
}
