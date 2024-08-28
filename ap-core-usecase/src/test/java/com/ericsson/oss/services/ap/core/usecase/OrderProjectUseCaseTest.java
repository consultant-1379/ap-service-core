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

import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
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
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.context.ContextService;
import com.ericsson.oss.itpf.sdk.context.classic.ContextConstants;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.importproject.ProjectInfo;

/**
 * Unit tests for {@link OrderProjectUseCase}.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderProjectUseCaseTest {

    private static final boolean VALIDATION_REQUIRED = true;
    private static final String NODE_FDN_PREFIX = "Node=";
    private static final String NODE1_NAME = "Node1";
    private static final String NODE2_NAME = "Node2";
    private static final String NODE3_NAME = "Node3";

    private static final String NODE1_FDN = PROJECT_FDN + "," + NODE_FDN_PREFIX + NODE1_NAME;
    private static final String NODE2_FDN = PROJECT_FDN + "," + NODE_FDN_PREFIX + NODE2_NAME;
    private static final String NODE3_FDN = PROJECT_FDN + "," + NODE_FDN_PREFIX + NODE3_NAME;
    private static final String USER_ID = "userId";

    final NodeInfo nodeInfo1 = new NodeInfo();
    final NodeInfo nodeInfo2 = new NodeInfo();
    final NodeInfo nodeInfo3 = new NodeInfo();
    final ProjectInfo projectInfo = new ProjectInfo();

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private DpsQueryExecutor<ManagedObject> dpsQueryExecutor;

    @Mock
    private OrderNodeUseCase orderNodeUseCase;

    @Mock
    private ContextService contextService;

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private SystemRecorder systemRecorder; // NOPMD

    @InjectMocks
    private OrderProjectUseCase orderProjectUseCase;

    private final List<ManagedObject> nodeMos = new ArrayList<>();

    @Before
    public void setUp() {
        final ManagedObject nodeMo = Mockito.mock(ManagedObject.class);
        nodeMos.add(nodeMo);
        nodeMos.add(nodeMo);
        nodeMos.add(nodeMo);

        when(dpsQueries.findChildMosOfTypes(PROJECT_FDN, AP.toString(), MoType.NODE.toString())).thenReturn(dpsQueryExecutor);
        when(nodeMo.getFdn()).thenReturn(NODE1_FDN).thenReturn(NODE2_FDN).thenReturn(NODE3_FDN);
        doReturn(nodeMos.iterator()).when(dpsQueryExecutor).execute();
        when(contextService.getContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY)).thenReturn(USER_ID);

        nodeInfo1.setName(NODE1_NAME);
        nodeInfo2.setName(NODE2_NAME);
        nodeInfo3.setName(NODE3_NAME);

        projectInfo.setName(PROJECT_NAME);
        projectInfo.addNodeInfo(nodeInfo1);
        projectInfo.addNodeInfo(nodeInfo2);
        projectInfo.addNodeInfo(nodeInfo3);
    }

    @Test
    public void testOrderWithThreeParmsSuccessfullyInitiatedForAllNodesInProject() {
        orderProjectUseCase.execute(PROJECT_FDN, VALIDATION_REQUIRED, projectInfo);

        verify(orderNodeUseCase).execute(NODE1_FDN, VALIDATION_REQUIRED, nodeInfo1);
        verify(orderNodeUseCase).execute(NODE2_FDN, VALIDATION_REQUIRED, nodeInfo2);
        verify(orderNodeUseCase).execute(NODE3_FDN, VALIDATION_REQUIRED, nodeInfo3);
    }

    @Test
    public void userContextIsSetAfterOrderingEachNode() {
        orderProjectUseCase.execute(PROJECT_FDN, VALIDATION_REQUIRED);
        verify(contextService, times(nodeMos.size())).setContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY, USER_ID);
    }

    @Test
    public void testAttemptToOrderAllNodesInProjectDespiteIndividualNodeFailures() {
        doThrow(ApApplicationException.class).when(orderNodeUseCase).execute(NODE1_FDN);
        doThrow(InvalidNodeStateException.class).when(orderNodeUseCase).execute(NODE2_FDN);

        orderProjectUseCase.execute(PROJECT_FDN, VALIDATION_REQUIRED, projectInfo);

        verify(orderNodeUseCase).execute(NODE1_FDN, VALIDATION_REQUIRED, nodeInfo1);
        verify(orderNodeUseCase).execute(NODE2_FDN, VALIDATION_REQUIRED, nodeInfo2);
        verify(orderNodeUseCase).execute(NODE3_FDN, VALIDATION_REQUIRED, nodeInfo3);
    }
}
