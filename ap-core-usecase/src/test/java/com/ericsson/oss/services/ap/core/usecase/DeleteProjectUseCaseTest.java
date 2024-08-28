/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
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
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.PartialProjectDeletionException;
import com.ericsson.oss.services.ap.api.exception.ProjectNotFoundException;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal;
import com.ericsson.oss.services.ap.common.artifacts.generated.GeneratedArtifactHandler;
import com.ericsson.oss.services.ap.common.artifacts.raw.RawArtifactHandler;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.core.usecase.delete.DeleteNodeWorkflowHelper;
import com.ericsson.oss.services.ap.core.usecase.delete.HealthCheckReportDeleter;
import com.ericsson.oss.services.ap.core.usecase.workflow.WorkflowOperations;
import com.ericsson.oss.services.ap.ejb.api.CoreExecutorLocal;

/**
 * Unit tests for {@link DeleteProjectUseCase}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeleteProjectUseCaseTest { // NOPMD TooManyFields

    private static final String NODE_STATUS_FDN = NODE_FDN + ",Status=1";

    @Mock
    private DdpTimer ddpTimer; // NOPMD

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private RawArtifactHandler rawArtifactHandler;

    @Mock
    private GeneratedArtifactHandler generatedArtifactHandler;

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private StateTransitionManagerLocal stateTransitionManager;

    @Mock
    private ServiceFinderBean serviceFinder;

    @Mock
    private CoreExecutorLocal coreExecutorLocal;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private DpsQueryExecutor<ManagedObject> nodeDpsQueryExecutor;

    @Mock
    private DpsQueryExecutor<ManagedObject> nodeStatusDpsQueryExecutor;

    @Mock
    private WorkflowOperations workflowOperations;

    @Mock
    private DpsOperations dpsOperations;

    @Mock
    private DataPersistenceService dpsService;

    @Mock
    private ManagedObject nodeMo;

    @Mock
    private ArtifactResourceOperations artifactResourceOperations;

    @Mock
    private DeleteNodeWorkflowHelper deleteNodeWorkflowHelper;

    @Mock
    private SystemRecorder recorder; //NOPMD

    @Mock
    private HealthCheckReportDeleter healthCheckReportDeleter;

    @InjectMocks
    private DeleteProjectUseCase deleteProjectUseCase;

    @Before
    public void setUp() {
        when(serviceFinder.find(StateTransitionManagerLocal.class)).thenReturn(stateTransitionManager);
        when(dpsOperations.getDataPersistenceService()).thenReturn(dpsService);
        when(dpsOperations.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        when(dpsQueries.findChildMosOfTypes(PROJECT_FDN, Namespace.AP.toString(), MoType.NODE_STATUS.toString()))
            .thenReturn(nodeStatusDpsQueryExecutor);
        when(liveBucket.findMoByFdn(NODE_FDN)).thenReturn(nodeMo);
        when(artifactResourceOperations.directoryExistAndNotEmpty(any(String.class)))
            .thenReturn(false);
        doNothing().when(healthCheckReportDeleter).deleteHealthCheckReports(any(String.class));
        addNodeMos(1);
        deleteProjectUseCase.init();
    }

    @Test(expected = ProjectNotFoundException.class)
    public void deleteNonExistentNodeShouldFail() {
        when(liveBucket.findMoByFdn(PROJECT_FDN)).thenReturn(null);
        deleteProjectUseCase.execute(PROJECT_FDN, false);
    }

    @Test
    public void when_delete_project_with_four_deletable_nodes_then_delete_success() {
        final List<ManagedObject> nodeStatusMos = new ArrayList<>();
        final ManagedObject nodeStatusMo = mock(ManagedObject.class);
        final ManagedObject nodeStatusMo2 = mock(ManagedObject.class);
        final ManagedObject nodeStatusMo3 = mock(ManagedObject.class);
        final ManagedObject nodeStatusMo4 = mock(ManagedObject.class);
        nodeStatusMos.add(nodeStatusMo);
        nodeStatusMos.add(nodeStatusMo2);
        nodeStatusMos.add(nodeStatusMo3);
        nodeStatusMos.add(nodeStatusMo4);
        mockProject(PROJECT_FDN);

        when(nodeStatusDpsQueryExecutor.execute()).thenReturn(nodeStatusMos.iterator());
        when(nodeStatusMo.getFdn()).thenReturn(NODE_STATUS_FDN);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.ORDER_FAILED.toString());
        when(nodeStatusMo2.getFdn()).thenReturn(NODE_STATUS_FDN);
        when(nodeStatusMo2.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.INTEGRATION_COMPLETED.toString());
        when(nodeStatusMo3.getFdn()).thenReturn(NODE_STATUS_FDN);
        when(nodeStatusMo3.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.INTEGRATION_COMPLETED_WITH_WARNING.toString());
        when(nodeStatusMo4.getFdn()).thenReturn(NODE_STATUS_FDN);
        when(nodeStatusMo4.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.INTEGRATION_CANCELLED.toString());
        doNothing().when(stateTransitionManager).validateAndSetNextState(NODE_FDN, StateTransitionEvent.DELETE_STARTED);
        doNothing().when(dpsOperations).deleteMo(PROJECT_FDN);

        final int result = deleteProjectUseCase.execute(PROJECT_FDN, false);

        assertEquals(4, result);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = PartialProjectDeletionException.class)
    public void when_delete_project_one_node_success_one_node_fails_then_PartialProjectDeletionException_is_expected()
        throws InterruptedException, ExecutionException {
        final ManagedObject nodeStatusMo = mock(ManagedObject.class);
        final ManagedObject nodeStatusMo2 = mock(ManagedObject.class);
        final Future<Boolean> nodeResult1 = mock(Future.class);
        final List<ManagedObject> nodeStatusMos = new ArrayList<>();
        mockProject(PROJECT_FDN);
        nodeStatusMos.add(nodeStatusMo);
        nodeStatusMos.add(nodeStatusMo2);
        when(nodeStatusDpsQueryExecutor.execute()).thenReturn(nodeStatusMos.iterator());
        when(nodeStatusMo.getFdn()).thenReturn(NODE_STATUS_FDN);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.ORDER_FAILED.toString());
        when(nodeStatusMo2.getFdn()).thenReturn(NODE_STATUS_FDN);
        when(nodeStatusMo2.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.ORDER_COMPLETED.toString());
        when(coreExecutorLocal.execute(any(Callable.class))).thenReturn(nodeResult1);
        when(nodeResult1.get()).thenReturn(false);
        deleteProjectUseCase.execute(PROJECT_FDN, false);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = ApApplicationException.class)
    public void when_delete_project_two_node_fails_then_ApApplicationException_is_expected() throws InterruptedException, ExecutionException {
        final ManagedObject nodeStatusMo = mock(ManagedObject.class);
        final ManagedObject nodeStatusMo2 = mock(ManagedObject.class);
        final Future<Boolean> nodeResult1 = mock(Future.class);
        final List<ManagedObject> nodeStatusMos = new ArrayList<>();
        nodeStatusMos.add(nodeStatusMo);
        nodeStatusMos.add(nodeStatusMo2);
        when(nodeStatusMo.getFdn()).thenReturn(NODE_STATUS_FDN);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.INTEGRATION_STARTED.toString());
        when(nodeStatusMo2.getFdn()).thenReturn(NODE_STATUS_FDN);
        when(nodeStatusMo2.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.ORDER_COMPLETED.toString());
        when(coreExecutorLocal.execute(any(Callable.class))).thenReturn(nodeResult1);
        when(nodeResult1.get()).thenReturn(false);
        deleteProjectUseCase.execute(PROJECT_FDN, false);
    }

    @Test
    public void when_delete_project_with_no_nodes_is_successful_then_raw_and_generated_directories_and_Mos_are_deleted_in_correct_order() {
        final List<ManagedObject> nodeStatusMos = new ArrayList<>();
        when(nodeStatusDpsQueryExecutor.execute()).thenReturn(nodeStatusMos.iterator());
        mockProject(PROJECT_FDN);

        final int numOfDeletedNodes = deleteProjectUseCase.execute(PROJECT_FDN, false);

        final InOrder order = inOrder(dpsOperations, generatedArtifactHandler, rawArtifactHandler);
        order.verify(rawArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        order.verify(generatedArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        order.verify(dpsOperations).deleteMo(PROJECT_FDN);
        assertEquals(0, numOfDeletedNodes);
    }

    @Test
    public void when_delete_of_raw_directory_throws_any_exception_then_delete_project_continues_with_best_effort() {
        final ManagedObject nodeStatusMo = mock(ManagedObject.class);
        final List<ManagedObject> nodeStatusMos = new ArrayList<>();
        nodeStatusMos.add(nodeStatusMo);
        mockProject(PROJECT_FDN);
        when(nodeStatusDpsQueryExecutor.execute()).thenReturn(nodeStatusMos.iterator());
        when(nodeStatusMo.getFdn()).thenReturn(NODE_STATUS_FDN);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.ORDER_FAILED.toString());
        doThrow(Exception.class).when(rawArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);

        deleteProjectUseCase.execute(PROJECT_FDN, false);

        verify(rawArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
    }

    @Test
    public void when_delete_of_generated_directories_throws_any_exception_then_delete_project_continues_with_best_effort() {
        final ManagedObject nodeStatusMo = mock(ManagedObject.class);
        final List<ManagedObject> nodeStatusMos = new ArrayList<>();
        nodeStatusMos.add(nodeStatusMo);
        mockProject(PROJECT_FDN);

        when(nodeStatusDpsQueryExecutor.execute()).thenReturn(nodeStatusMos.iterator());
        when(nodeStatusMo.getFdn()).thenReturn(NODE_STATUS_FDN);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.ORDER_FAILED.toString());

        doThrow(Exception.class).when(generatedArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);

        deleteProjectUseCase.execute(PROJECT_FDN, false);

        verify(generatedArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
    }

    @Test
    public void when_delete_hardware_replace_project_then_succeeds_and_workflow_deleted_ignore_networkElement() {
        mockProject(PROJECT_FDN);
        final List<ManagedObject> nodeMos = new ArrayList<>();
        final ManagedObject replaceMo = mock(ManagedObject.class);
        final List<ManagedObject> nodeStatusMos = new ArrayList<>();
        final ManagedObject nodeStatusMo = mock(ManagedObject.class);
        nodeMos.add(replaceMo);
        nodeStatusMos.add(nodeStatusMo);

        when(nodeDpsQueryExecutor.execute()).thenReturn(nodeMos.iterator());
        when(dpsQueries.findChildMosOfTypes(PROJECT_FDN, Namespace.AP.toString(), MoType.NODE.toString()))
            .thenReturn(nodeDpsQueryExecutor);
        when(dpsQueries.findChildMosOfTypes(PROJECT_FDN, Namespace.AP.toString(), MoType.NODE_STATUS.toString()))
            .thenReturn(nodeStatusDpsQueryExecutor);
        when(nodeStatusDpsQueryExecutor.execute()).thenReturn(nodeStatusMos.iterator());
        when(nodeStatusMo.getFdn()).thenReturn(NODE_FDN);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.HARDWARE_REPLACE_FAILED.toString());
        when(liveBucket.findMoByFdn(NODE_FDN)).thenReturn(replaceMo);
        when(replaceMo.getAttribute(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString())).thenReturn(true);

        deleteProjectUseCase.execute(PROJECT_FDN, false);

        final InOrder inOrder = inOrder(rawArtifactHandler, generatedArtifactHandler, dpsOperations);
        inOrder.verify(rawArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        inOrder.verify(generatedArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        inOrder.verify(dpsOperations).deleteMo(PROJECT_FDN);
    }

    @Test
    public void when_Hardware_Replace_Node_Attribute_is_null_no_exception_thrown_and_delete_continues() {

        final List<ManagedObject> listMo = new ArrayList<>();
        listMo.add(nodeMo);
        when(nodeMo.getFdn()).thenReturn(NODE_FDN);
        when(dpsQueries.findChildMosOfTypes(PROJECT_FDN, Namespace.AP.toString(), MoType.NODE.toString())).thenReturn(nodeDpsQueryExecutor);
        when(nodeDpsQueryExecutor.execute()).thenReturn(listMo.iterator());
        when(nodeMo.getAttribute(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString())).thenReturn(null);
        mockProject(PROJECT_FDN);

        when(dpsQueries.findChildMosOfTypes(PROJECT_FDN, Namespace.AP.toString(), MoType.NODE_STATUS.toString()))
            .thenReturn(nodeStatusDpsQueryExecutor);
        when(liveBucket.findMoByFdn(NODE_FDN)).thenReturn(nodeMo);
        final ManagedObject nodeStatusMo = mock(ManagedObject.class);
        final List<ManagedObject> nodeStatusMos = new ArrayList<>();
        nodeStatusMos.add(nodeStatusMo);
        when(nodeStatusMo.getFdn()).thenReturn(NODE_STATUS_FDN);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.ORDER_FAILED.toString());
        when(nodeStatusDpsQueryExecutor.execute()).thenReturn(nodeStatusMos.iterator());

        deleteProjectUseCase.execute(PROJECT_FDN, false);

    }

    @Test
    public void whenDeleteProjectContainsExpansionCancelledNodeThenDeleteSuccessed() {
        final List<ManagedObject> nodeStatusMos = new ArrayList<>();
        final ManagedObject nodeStatusMo = mock(ManagedObject.class);
        nodeStatusMos.add(nodeStatusMo);
        mockProject(PROJECT_FDN);

        when(nodeStatusDpsQueryExecutor.execute()).thenReturn(nodeStatusMos.iterator());
        when(nodeStatusMo.getFdn()).thenReturn(NODE_STATUS_FDN);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.EXPANSION_CANCELLED.toString());

        deleteProjectUseCase.execute(PROJECT_FDN, false);

        final InOrder inOrder = inOrder(rawArtifactHandler, generatedArtifactHandler, dpsOperations);
        inOrder.verify(rawArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        inOrder.verify(generatedArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        inOrder.verify(dpsOperations).deleteMo(PROJECT_FDN);
        verify(workflowOperations, never()).executeDeleteWorkflow(eq(PROJECT_FDN), eq(false), anyString());
    }

    @Test
    public void whenDeleteProjectContainsExpansionCompletedNodeThenDeleteSuccessed() {
        final List<ManagedObject> nodeStatusMos = new ArrayList<>();
        final ManagedObject nodeStatusMo = mock(ManagedObject.class);
        nodeStatusMos.add(nodeStatusMo);

        mockProject(PROJECT_FDN);

        when(nodeStatusDpsQueryExecutor.execute()).thenReturn(nodeStatusMos.iterator());
        when(nodeStatusMo.getFdn()).thenReturn(NODE_STATUS_FDN);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.EXPANSION_COMPLETED.toString());

        deleteProjectUseCase.execute(PROJECT_FDN, false);

        final InOrder inOrder = inOrder(rawArtifactHandler, generatedArtifactHandler, dpsOperations);
        inOrder.verify(rawArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        inOrder.verify(generatedArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        inOrder.verify(dpsOperations).deleteMo(PROJECT_FDN);
        verify(workflowOperations, never()).executeDeleteWorkflow(eq(PROJECT_FDN), eq(false), anyString());
    }

    @Test
    public void whenDeleteProjectContainsExpansionFailedNodeThenDeleteSuccessed() {
        final List<ManagedObject> nodeStatusMos = new ArrayList<>();
        final ManagedObject nodeStatusMo = mock(ManagedObject.class);
        nodeStatusMos.add(nodeStatusMo);
        mockProject(PROJECT_FDN);

        when(nodeStatusDpsQueryExecutor.execute()).thenReturn(nodeStatusMos.iterator());
        when(nodeStatusMo.getFdn()).thenReturn(NODE_STATUS_FDN);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.EXPANSION_FAILED.toString());

        deleteProjectUseCase.execute(PROJECT_FDN, false);

        final InOrder inOrder = inOrder(rawArtifactHandler, generatedArtifactHandler, dpsOperations);
        inOrder.verify(rawArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        inOrder.verify(generatedArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        inOrder.verify(dpsOperations).deleteMo(PROJECT_FDN);
        verify(workflowOperations, never()).executeDeleteWorkflow(eq(PROJECT_FDN), eq(false), anyString());
    }

    @Test
    public void whenDeleteProjectContainsExpansionStartedNodeThenDeleteSuccessed() {
        final List<ManagedObject> nodeStatusMos = new ArrayList<>();
        final ManagedObject nodeStatusMo = mock(ManagedObject.class);
        nodeStatusMos.add(nodeStatusMo);
        mockProject(PROJECT_FDN);

        when(nodeStatusDpsQueryExecutor.execute()).thenReturn(nodeStatusMos.iterator());
        when(nodeStatusMo.getFdn()).thenReturn(NODE_STATUS_FDN);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.EXPANSION_STARTED.toString());

        deleteProjectUseCase.execute(PROJECT_FDN, false);

        final InOrder inOrder = inOrder(rawArtifactHandler, generatedArtifactHandler, dpsOperations);
        inOrder.verify(rawArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        inOrder.verify(generatedArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        inOrder.verify(dpsOperations).deleteMo(PROJECT_FDN);
        verify(workflowOperations, never()).executeDeleteWorkflow(eq(PROJECT_FDN), eq(false), anyString());
    }

    @Test
    public void whenDeleteProjectContainsExpansionSuspendedNodeThenDeleteSuccessed() {
        final List<ManagedObject> nodeStatusMos = new ArrayList<>();
        final ManagedObject nodeStatusMo = mock(ManagedObject.class);
        nodeStatusMos.add(nodeStatusMo);
        mockProject(PROJECT_FDN);

        when(nodeStatusDpsQueryExecutor.execute()).thenReturn(nodeStatusMos.iterator());
        when(nodeStatusMo.getFdn()).thenReturn(NODE_STATUS_FDN);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.EXPANSION_SUSPENDED.toString());

        deleteProjectUseCase.execute(PROJECT_FDN, false);

        final InOrder inOrder = inOrder(rawArtifactHandler, generatedArtifactHandler, dpsOperations);
        inOrder.verify(rawArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        inOrder.verify(generatedArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        inOrder.verify(dpsOperations).deleteMo(PROJECT_FDN);
        verify(workflowOperations, never()).executeDeleteWorkflow(eq(PROJECT_FDN), eq(false), anyString());
    }

    @Test
    public void whenDeleteProjectContainsReadyForExpansionThenDeleteSuccessed() {
        final List<ManagedObject> nodeStatusMos = new ArrayList<>();
        final ManagedObject nodeStatusMo = mock(ManagedObject.class);
        nodeStatusMos.add(nodeStatusMo);
        mockProject(PROJECT_FDN);

        when(nodeStatusDpsQueryExecutor.execute()).thenReturn(nodeStatusMos.iterator());
        when(nodeStatusMo.getFdn()).thenReturn(NODE_STATUS_FDN);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.READY_FOR_EXPANSION.toString());

        deleteProjectUseCase.execute(PROJECT_FDN, false);

        final InOrder inOrder = inOrder(rawArtifactHandler, generatedArtifactHandler, dpsOperations);
        inOrder.verify(rawArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        inOrder.verify(generatedArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        inOrder.verify(dpsOperations).deleteMo(PROJECT_FDN);
        verify(workflowOperations, never()).executeDeleteWorkflow(eq(PROJECT_FDN), eq(false), anyString());
    }

    @Test
    public void whenDeleteProjectContainsProfileThenDeleteProfileArtifactsAndDeleteSuccessed() {
        final List<ManagedObject> nodeStatusMos = new ArrayList<>();
        when(nodeStatusDpsQueryExecutor.execute()).thenReturn(nodeStatusMos.iterator());
        mockProject(PROJECT_FDN);

        when(artifactResourceOperations.directoryExistAndNotEmpty(any(String.class)))
            .thenReturn(true);

        final int numOfDeletedNodes = deleteProjectUseCase.execute(PROJECT_FDN, false);

        final InOrder order = inOrder(
            dpsOperations,
            generatedArtifactHandler,
            rawArtifactHandler,
            artifactResourceOperations);
        order.verify(rawArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        order.verify(generatedArtifactHandler).deleteAllForProjectWithNoModelUpdate(PROJECT_FDN);
        order.verify(artifactResourceOperations).directoryExistAndNotEmpty(any(String.class));
        order.verify(artifactResourceOperations).deleteDirectory(any(String.class));
        order.verify(dpsOperations).deleteMo(PROJECT_FDN);
        assertEquals(0, numOfDeletedNodes);
    }

    private void addNodeMos(final int numberOfNodes) {
        final List<ManagedObject> nodeMos = new ArrayList<>();
        for (int i = 0; i < numberOfNodes; i++) {
            nodeMos.add(nodeMo);
        }

        when(nodeDpsQueryExecutor.execute()).thenReturn(nodeMos.iterator());
        when(dpsQueries.findChildMosOfTypes(PROJECT_FDN, Namespace.AP.toString(), MoType.NODE.toString()))
            .thenReturn(nodeDpsQueryExecutor);

    }

    private void mockProject(final String projectFDN) {
        final ManagedObject project = mock(ManagedObject.class);
        when(project.getFdn()).thenReturn(projectFDN);
        when(liveBucket.findMoByFdn(projectFDN)).thenReturn(project);
    }
}
