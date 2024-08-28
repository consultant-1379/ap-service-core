/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.exception.general.DpsPersistenceException;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.status.IntegrationPhase;
import com.ericsson.oss.services.ap.api.status.ApNodeGroupStatus;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsProjectionQueryExecutor;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;

/**
 * Unit tests for {@link StatusProjectUseCase}.
 */
@RunWith(MockitoJUnitRunner.class)
public class StatusAllProjectsUseCaseTest {

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private DpsQueryExecutor<ManagedObject> dpsQueryExecutor;

    @Mock
    private DpsProjectionQueryExecutor dpsProjectionQueryExecutor;

    @InjectMocks
    private StatusAllProjectsUseCase viewAllProjectStatusesUseCase;

    @Mock
    private ManagedObject projectMo;

    private static final List<Object[]> PROJECT1_NODE_STATUSES = new ArrayList<>();
    private static final List<Object[]> PROJECT2_NODE_STATUSES = new ArrayList<>();

    static {
        PROJECT1_NODE_STATUSES.add(new Object[] { "Project=Project1,Node=Node1,NodeStatus=1", "READY_FOR_ORDER" });
        PROJECT1_NODE_STATUSES.add(new Object[] { "Project=Project1,Node=Node2,NodeStatus=1", "ORDER_STARTED" });
        PROJECT1_NODE_STATUSES.add(new Object[] { "Project=Project1,Node=Node3,NodeStatus=1", "ORDER_FAILED" });
        PROJECT1_NODE_STATUSES.add(new Object[] { "Project=Project1,Node=Node4,NodeStatus=1", "INTEGRATION_COMPLETED" });

        PROJECT2_NODE_STATUSES.add(new Object[] { "Project=Project2,Node=Node1,NodeStatus=1", "READY_FOR_ORDER" });
        PROJECT2_NODE_STATUSES.add(new Object[] { "Project=Project2,Node=Node2,NodeStatus=1", "ORDER_STARTED" });
        PROJECT2_NODE_STATUSES.add(new Object[] { "Project=Project2,Node=Node3,NodeStatus=1", "ORDER_FAILED" });
        PROJECT2_NODE_STATUSES.add(new Object[] { "Project=Project2,Node=Node4,NodeStatus=1", "INTEGRATION_COMPLETED" });
    }

    @Test
    public void when_no_project_exists_then_return_empty_list() {
        when(dpsQueries.findMosByType(MoType.PROJECT.toString(), Namespace.AP.toString())).thenReturn(dpsQueryExecutor);
        doReturn(Collections.EMPTY_LIST.iterator()).when(dpsQueryExecutor).execute();

        when(dpsQueries.getMoAttributeValues(Namespace.AP.toString(), MoType.NODE_STATUS.toString(), "fdn", NodeStatusAttribute.STATE.toString()))
                .thenReturn(dpsProjectionQueryExecutor);
        doReturn(Collections.EMPTY_LIST.iterator()).when(dpsQueryExecutor).execute();

        final List<ApNodeGroupStatus> projectsReturned = viewAllProjectStatusesUseCase.execute();
        assertTrue(projectsReturned.isEmpty());
    }

    @Test
    public void when_single_project_exists_with_four_nodes_then_return_one_project_status() {
        final List<ManagedObject> projectMos = new ArrayList<>();
        projectMos.add(projectMo);

        when(dpsQueries.findMosByType(MoType.PROJECT.toString(), Namespace.AP.toString())).thenReturn(dpsQueryExecutor);
        doReturn(projectMos.iterator()).when(dpsQueryExecutor).execute();
        when(projectMo.getName()).thenReturn("Project1");

        when(dpsQueries.getMoAttributeValues(Namespace.AP.toString(), MoType.NODE_STATUS.toString(), "fdn", NodeStatusAttribute.STATE.toString()))
                .thenReturn(dpsProjectionQueryExecutor);
        when(dpsProjectionQueryExecutor.execute()).thenReturn(PROJECT1_NODE_STATUSES);

        final List<ApNodeGroupStatus> statusForAllProjects = viewAllProjectStatusesUseCase.execute();

        assertEquals(1, statusForAllProjects.size());

        verifyProjectStatusValues(statusForAllProjects.iterator().next(), "Project1", 4, new int[] { 2, 1, 1 });
    }

    @Test
    public void when_two_projects_exist_each_with_four_nodes_then_return_two_project_status() {
        final List<ManagedObject> projectMos = new ArrayList<>();
        projectMos.add(projectMo);
        projectMos.add(projectMo);

        when(dpsQueries.findMosByType(MoType.PROJECT.toString(), Namespace.AP.toString())).thenReturn(dpsQueryExecutor);
        doReturn(projectMos.iterator()).when(dpsQueryExecutor).execute();
        when(projectMo.getName()).thenReturn("Project1").thenReturn("Project2");

        final List<Object[]> nodeStatuses = new ArrayList<>();
        nodeStatuses.addAll(PROJECT1_NODE_STATUSES);
        nodeStatuses.addAll(PROJECT2_NODE_STATUSES);

        when(dpsQueries.getMoAttributeValues(Namespace.AP.toString(), MoType.NODE_STATUS.toString(), "fdn", NodeStatusAttribute.STATE.toString()))
                .thenReturn(dpsProjectionQueryExecutor);
        when(dpsProjectionQueryExecutor.execute()).thenReturn(nodeStatuses);

        final List<ApNodeGroupStatus> statusForAllProjects = viewAllProjectStatusesUseCase.execute();

        assertEquals(2, statusForAllProjects.size());

        verifyProjectStatusValues(statusForAllProjects.get(0), "Project1", 4, new int[] { 2, 1, 1 });
        verifyProjectStatusValues(statusForAllProjects.get(1), "Project2", 4, new int[] { 2, 1, 1 });
    }

    @Test
    public void when_single_projects_exists_with_no_nodes_then_return_one_project_status() {
        final List<ManagedObject> projectMos = new ArrayList<>();
        projectMos.add(projectMo);

        when(dpsQueries.findMosByType(MoType.PROJECT.toString(), Namespace.AP.toString())).thenReturn(dpsQueryExecutor);
        doReturn(projectMos.iterator()).when(dpsQueryExecutor).execute();
        when(projectMo.getName()).thenReturn("Project1");

        when(dpsQueries.getMoAttributeValues(Namespace.AP.toString(), MoType.NODE_STATUS.toString(), "fdn", NodeStatusAttribute.STATE.toString()))
                .thenReturn(dpsProjectionQueryExecutor);
        when(dpsProjectionQueryExecutor.execute()).thenReturn(Collections.<Object[]> emptyList());

        final List<ApNodeGroupStatus> statusForAllProjects = viewAllProjectStatusesUseCase.execute();

        verifyProjectStatusValues(statusForAllProjects.get(0), "Project1", 0, new int[] { 0, 0, 0, 0 });
    }

    @Test(expected = ApApplicationException.class)
    public void when_error_reading_node_status_mos_then_fail_with_exception() {
        when(dpsQueries.getMoAttributeValues(Namespace.AP.toString(), MoType.NODE_STATUS.toString(), "fdn", NodeStatusAttribute.STATE.toString()))
                .thenReturn(dpsProjectionQueryExecutor);
        doThrow(DpsPersistenceException.class).when(dpsProjectionQueryExecutor).execute();

        viewAllProjectStatusesUseCase.execute();
    }

    private void verifyProjectStatusValues(final ApNodeGroupStatus projectStatus, final String projectName, final int expectedNodeQuantity,
            final int[] expectedStateCount) {
        assertEquals("Project name is not correct", projectStatus.getApNodeGroupName(), projectName);
        final int nodeQuantity = expectedNodeQuantity;
        final int nodeStarted = expectedStateCount[0];
        final int nodeComplete = expectedStateCount[1];
        final int nodeFailed = expectedStateCount[2];

        assertEquals("Node Quantity is not correct", nodeQuantity, projectStatus.getNumberOfNodes());
        assertEquals("Node Started count is not correct", nodeStarted, projectStatus.getIntegrationPhaseSummary().get(IntegrationPhase.IN_PROGRESS)
                .intValue());
        assertEquals("Node Complete count is not correct", nodeComplete, projectStatus.getIntegrationPhaseSummary().get(IntegrationPhase.SUCCESSFUL)
                .intValue());
        assertEquals("Node Failed count is not correct", nodeFailed, projectStatus.getIntegrationPhaseSummary().get(IntegrationPhase.FAILED)
                .intValue());
    }
}