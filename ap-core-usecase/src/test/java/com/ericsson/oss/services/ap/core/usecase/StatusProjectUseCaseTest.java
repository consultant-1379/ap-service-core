/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase;

import static com.ericsson.oss.services.ap.api.status.State.INTEGRATION_COMPLETED;
import static com.ericsson.oss.services.ap.api.status.State.ORDER_COMPLETED;
import static com.ericsson.oss.services.ap.api.status.State.ORDER_FAILED;
import static com.ericsson.oss.services.ap.api.status.State.READY_FOR_ORDER;
import static com.ericsson.oss.services.ap.common.model.MoType.NODE_STATUS;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NodeDescriptorBuilder.createDefaultNode;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME;
import static com.ericsson.oss.services.ap.model.NodeType.ERBS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.exception.general.DpsPersistenceException;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.status.ApNodeGroupStatus;
import com.ericsson.oss.services.ap.api.status.IntegrationPhase;
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor;
import com.ericsson.oss.services.ap.common.test.stubs.dps.StubbedDpsGenerator;

/**
 * Units tests for {@link StatusProjectUseCase}.
 */
@RunWith(MockitoJUnitRunner.class)
public class StatusProjectUseCaseTest {

    private static final String NODE_FDN_PREFIX = ",Node=";
    private static final String NODE1_FDN = PROJECT_FDN + NODE_FDN_PREFIX + "Node1";
    private static final String NODE2_FDN = PROJECT_FDN + NODE_FDN_PREFIX + "Node2";
    private static final String NODE3_FDN = PROJECT_FDN + NODE_FDN_PREFIX + "Node3";
    private static final String NODE4_FDN = PROJECT_FDN + NODE_FDN_PREFIX + "Node4";

    @Mock
    private Logger logger; // NOPMD

    @InjectMocks
    private DpsQueries dpsQueries;

    @InjectMocks
    private StatusProjectUseCase viewProjectStatusUseCase;

    final StubbedDpsGenerator dpsGenerator = new StubbedDpsGenerator();

    @Before
    public void setUp() {
        final DataPersistenceService dps = dpsGenerator.getStubbedDps();
        Whitebox.setInternalState(viewProjectStatusUseCase, "dpsQueries", dpsQueries);
        Whitebox.setInternalState(dpsQueries, "dps", dps);
    }

    @Test
    public void when_project_is_not_empty_then_return_the_node_quantity() {
        createNodeAndStatusMos();
        final ApNodeGroupStatus projectStatus = viewProjectStatusUseCase.execute(PROJECT_FDN);
        assertEquals(4, projectStatus.getNumberOfNodes());
    }

    @Test
    public void when_project_is_not_empty_then_return_summary_of_state_for_all_nodes() {
        createNodeAndStatusMos();

        final ApNodeGroupStatus projectStatus = viewProjectStatusUseCase.execute(PROJECT_FDN);

        final Map<IntegrationPhase, Integer> phaseSummary = projectStatus.getIntegrationPhaseSummary();
        assertEquals(Integer.valueOf(2), phaseSummary.get(IntegrationPhase.IN_PROGRESS));
        assertEquals(Integer.valueOf(1), phaseSummary.get(IntegrationPhase.FAILED));
        assertEquals(Integer.valueOf(1), phaseSummary.get(IntegrationPhase.SUCCESSFUL));
    }

    @Test
    public void when_project_is_not_empty_then_return_the_status_for_each_node() {
        createNodeAndStatusMos();

        final ApNodeGroupStatus projectStatus = viewProjectStatusUseCase.execute(PROJECT_FDN);

        assertEquals(4, projectStatus.getNodesStatus().size());
        final NodeStatus firstNodeStatus = projectStatus.getNodesStatus().get(0);
        assertEquals(PROJECT_NAME, firstNodeStatus.getProjectName());
        assertEquals(NODE_NAME, firstNodeStatus.getNodeName());
        assertEquals(IntegrationPhase.IN_PROGRESS, firstNodeStatus.getIntegrationPhase());
    }

    @Test
    public void when_project_is_empty_then_no_status_returned_for_nodes() {
        final ApNodeGroupStatus projectStatus = viewProjectStatusUseCase.execute(PROJECT_FDN);

        assertEquals(PROJECT_NAME, projectStatus.getApNodeGroupName());
        assertEquals(0, projectStatus.getNumberOfNodes());
        assertTrue(projectStatus.getNodesStatus().isEmpty());
    }

    @Test(expected = ApApplicationException.class)
    public void when_error_reading_status_mos_then_fail_with_exception() {
        final DpsQueries dpsQueries = Mockito.mock(DpsQueries.class);
        Whitebox.setInternalState(viewProjectStatusUseCase, "dpsQueries", dpsQueries);
        doThrow(DpsPersistenceException.class).when(dpsQueries).findChildMosOfTypes(PROJECT_FDN, NODE_STATUS.toString(), AP.toString());

        viewProjectStatusUseCase.execute(PROJECT_FDN);
    }

    private void createNodeAndStatusMos() {
        createNode(NODE1_FDN, READY_FOR_ORDER);
        createNode(NODE2_FDN, ORDER_COMPLETED);
        createNode(NODE3_FDN, ORDER_FAILED);
        createNode(NODE4_FDN, INTEGRATION_COMPLETED);
    }

    private void createNode(final String nodeFdn, final State state) {
        final NodeDescriptor nodeDescriptor = createDefaultNode(ERBS)
                .withNodeStatus(state)
                .withNodeFdn(nodeFdn)
                .build();
        dpsGenerator.generate(nodeDescriptor);
    }
}
