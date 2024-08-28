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
package com.ericsson.oss.services.ap.core.usecase;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

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
import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal;
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.api.workflow.AutoProvisioningWorkflowService;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.model.ProjectAttribute;
import com.ericsson.oss.services.ap.common.workflow.ActivityType;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.order.WorkflowInstanceIdUpdater;
import com.ericsson.oss.services.ap.core.usecase.workflow.ApWorkflowServiceResolver;
import com.ericsson.oss.services.ap.core.usecase.workflow.WorkflowCleanUpOperations;
import com.ericsson.oss.services.wfs.api.instance.WorkflowInstance;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;

/**
 * Unit tests for {@link OrderNodeUseCase}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderNodeUseCaseTest {

    private static final String ORDER_WORKFLOW_NAME = "order_workflow";
    private static final String MIGRATION_WORKFLOW_NAME = "migration_workflow";
    private static final String EXPANSION_WORKFLOW_NAME = "expansion_workflow";
    private static final String HARDWARE_REPLACE_WORKFLOW_NAME = "hardware_replace_workflow";
    private static final String WORKFLOW_INSTANCE_ID = "workflowId";

    @Mock
    private ApWorkflowServiceResolver apWorkflowServiceResolver;

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
    private ManagedObject projectMo;

    @Mock
    private ManagedObject nodeStatusMo;

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    @Mock
    private ServiceFinderBean serviceFinder;

    @Mock
    private StatusEntryManagerLocal statusEntryManager;

    @Mock
    private StateTransitionManagerLocal stateTransitionManager;

    @Mock
    private WorkflowCleanUpOperations workflowCleanUpOperations; //NOPMD

    @Mock
    private WorkflowInstanceIdUpdater workflowIdUpdater;

    @Mock
    private WorkflowInstanceServiceLocal workflowInstanceService;

    @Mock
    private NodeInfo nodeInfo;

    @InjectMocks
    private OrderNodeUseCase orderNodeUseCase;

    @Before
    public void setUp() {
        when(dps.getDataPersistenceService()).thenReturn(dpsService);
        when(dps.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findMoByFdn(NODE_FDN)).thenReturn(nodeMo);
        when(liveBucket.findMoByFdn(getNodeStatusMoFdn())).thenReturn(nodeStatusMo);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.READY_FOR_ORDER.name());
        when(nodeMo.getAttribute("nodeType")).thenReturn(VALID_NODE_TYPE);
        when(nodeMo.getFdn()).thenReturn(NODE_FDN);
        when(nodeMo.getParent()).thenReturn(projectMo);
        when(projectMo.getAttribute(ProjectAttribute.GENERATED_BY.toString())).thenReturn("ECT");
        when(nodeTypeMapper.getInternalEjbQualifier(VALID_NODE_TYPE)).thenReturn(VALID_NODE_TYPE.toLowerCase());
        when(apWorkflowServiceResolver.getApWorkflowService(VALID_NODE_TYPE.toLowerCase())).thenReturn(apWorkflowService);
        when(serviceFinder.find(StatusEntryManagerLocal.class)).thenReturn(statusEntryManager);
        when(apWorkflowService.getOrderWorkflowName()).thenReturn(ORDER_WORKFLOW_NAME);
        when(workflowInstanceService.startWorkflowInstanceByDefinitionId(anyString(), anyString(), anyMapOf(String.class, Object.class)))
        .thenReturn(new WorkflowInstance("", WORKFLOW_INSTANCE_ID, ""));
        final List<ManagedObject> artifactMos = new ArrayList<>();
        artifactMos.add(nodeMo);
    }

    @Test
    public void whenOrderNode_andNoPreviousWorkflowInstanceExists_thenWorkflowIsExecuted_andWorkflowIdIsUpdated() {
        orderNodeUseCase.execute(NODE_FDN);
        verify(workflowInstanceService, never()).cancelWorkflowInstance(WORKFLOW_INSTANCE_ID);
        verify(workflowIdUpdater, times(1)).update(WORKFLOW_INSTANCE_ID, NODE_FDN);
    }

    @Test(expected = InvalidNodeStateException.class)
    public void whenOrderNode_andNodeIsInStateOrderStarted_thenNodeStateIsNotChanged_andExceptionIsPropagated() {
        doThrow(InvalidNodeStateException.class).when(stateTransitionManager).validateAndSetNextState(NODE_FDN, StateTransitionEvent.ORDER_STARTED);
        try {
            orderNodeUseCase.execute(NODE_FDN);
        } catch (final Exception e) {
            verify(stateTransitionManager, never()).validateAndSetNextState(NODE_FDN, StateTransitionEvent.ORDER_FAILED);
            throw e;
        }
    }

    @Test(expected = ApApplicationException.class)
    public void whenOrderNode_andNodeMoDoesNotExist_thenApApplicationExceptionIsThrown() {
        doThrow(ApApplicationException.class).when(dpsService).getLiveBucket();
        orderNodeUseCase.execute(NODE_FDN);
    }

    @Test
    public void whenMigrationNodeAndThenWorkflowIsExecutedAndWorkflowIdIsUpdated() {
        when(apWorkflowService.getMigrationWorkflowName()).thenReturn(MIGRATION_WORKFLOW_NAME);
        when(nodeInfo.getActivity()).thenReturn(ActivityType.MIGRATION_ACTIVITY);
        nodeInfo.setActivity(ActivityType.MIGRATION_ACTIVITY);
        orderNodeUseCase.execute(NODE_FDN,true,nodeInfo);
        verify(workflowInstanceService, never()).cancelWorkflowInstance(WORKFLOW_INSTANCE_ID);
        verify(workflowIdUpdater, times(1)).update(WORKFLOW_INSTANCE_ID, NODE_FDN);
    }

    @Test
    public void whenMigrationNodeExecuteFromPreMigrationFailedStateAndThenWorkflowIsExecutedAndWorkflowIdIsUpdated() {
        when(apWorkflowService.getMigrationWorkflowName()).thenReturn(MIGRATION_WORKFLOW_NAME);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.PRE_MIGRATION_FAILED.name());
        when(nodeInfo.getActivity()).thenReturn(ActivityType.MIGRATION_ACTIVITY);
        nodeInfo.setActivity(ActivityType.MIGRATION_ACTIVITY);
        orderNodeUseCase.execute(NODE_FDN, true, nodeInfo);
        verify(workflowIdUpdater, times(1)).update(WORKFLOW_INSTANCE_ID, NODE_FDN);
    }

    @Test
    public void whenOrderNodeExecutionThrowExceptionInPreMigrationStartedStateThenNodeTransitToPreMigrationFailedState() {
        doThrow(InvalidNodeStateException.class).when(stateTransitionManager).validateAndSetNextState(NODE_FDN,
                StateTransitionEvent.PRE_MIGRATION_STARTED);
        try {
            orderNodeUseCase.execute(NODE_FDN);
        } catch (final Exception e) {
            verify(stateTransitionManager, times(1)).validateAndSetNextState(NODE_FDN, StateTransitionEvent.PRE_MIGRATION_FAILED);
            throw e;
        }
    }

    @Test
    public void whenExpansionNodeAndThenWorkflowIsExecutedAndWorkflowIdIsUpdated() {
        when(apWorkflowService.getExpansionOrderWorkflowName()).thenReturn(EXPANSION_WORKFLOW_NAME);
        when(nodeInfo.getActivity()).thenReturn(ActivityType.EXPANSION_ACTIVITY);
        nodeInfo.setActivity(ActivityType.EXPANSION_ACTIVITY);
        orderNodeUseCase.execute(NODE_FDN,true,nodeInfo);
        verify(workflowInstanceService, never()).cancelWorkflowInstance(WORKFLOW_INSTANCE_ID);
        verify(workflowIdUpdater, times(1)).update(WORKFLOW_INSTANCE_ID, NODE_FDN);
    }

    @Test
    public void whenHardwareReplaceNodeAndthenWorkflowIsExecutedAndWorkflowIdIsUpdated() {
        when(apWorkflowService.getHardwareReplaceWorkflowName()).thenReturn(HARDWARE_REPLACE_WORKFLOW_NAME);
        when(nodeInfo.getActivity()).thenReturn(ActivityType.HARDWARE_REPLACE_ACTIVITY);
        nodeInfo.setActivity(ActivityType.HARDWARE_REPLACE_ACTIVITY);
        orderNodeUseCase.execute(NODE_FDN,true,nodeInfo);
        verify(workflowInstanceService, never()).cancelWorkflowInstance(WORKFLOW_INSTANCE_ID);
        verify(workflowIdUpdater, times(1)).update(WORKFLOW_INSTANCE_ID, NODE_FDN);
    }


    private static String getNodeStatusMoFdn() {
        return NODE_FDN + "," + MoType.NODE_STATUS.toString() + "=1";
    }
}
