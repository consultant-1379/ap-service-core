/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.context.ContextService;
import com.ericsson.oss.itpf.sdk.context.classic.ContextConstants;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.MoType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link EoiOrderProjectUseCase}.
 */
@RunWith(MockitoJUnitRunner.class)

public class EoiOrderProjectUseCaseTest {
    private static final String NODE_FDN_PREFIX = "Node=";
    private static final String NODE1_NAME = "Node1";
    private static final String NODE2_NAME = "Node2";
    private static final String NODE3_NAME = "Node3";

    private static final String NODE1_FDN = PROJECT_FDN + "," + NODE_FDN_PREFIX + NODE1_NAME;
    private static final String NODE2_FDN = PROJECT_FDN + "," + NODE_FDN_PREFIX + NODE2_NAME;
    private static final String NODE3_FDN = PROJECT_FDN + "," + NODE_FDN_PREFIX + NODE3_NAME;
    private static final String USER_ID = "userId";
    private static final String BASE_URL = "hppts://athtem.eei.ericsson.se";
    private static final String SESSION_ID = "hkdjlDKJ";

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private DpsQueries.DpsQueryExecutor<ManagedObject> dpsQueryExecutor;

    @Mock
    private EoiOrderNodeUseCase eoiOrderNodeUseCase;

    @Mock
    private ContextService contextService;

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private SystemRecorder systemRecorder; // NOPMD

    @InjectMocks
    private EoiOrderProjectUseCase eoiOrderProjectUseCase;

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
    }

    @Test
    public void testOrderWithThreeParmsSuccessfullyInitiatedForAllNodesInProject() {
        eoiOrderProjectUseCase.execute(PROJECT_FDN, BASE_URL, SESSION_ID);
        verify(eoiOrderNodeUseCase).execute(NODE1_FDN,BASE_URL,SESSION_ID);
        verify(eoiOrderNodeUseCase).execute(NODE2_FDN, BASE_URL, SESSION_ID);
        verify(eoiOrderNodeUseCase).execute(NODE3_FDN, BASE_URL, SESSION_ID);
    }

    @Test
    public void userContextIsSetAfterOrderingEachNode() {
        eoiOrderProjectUseCase.execute(PROJECT_FDN, BASE_URL, SESSION_ID);
        verify(contextService, times(nodeMos.size())).setContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY, USER_ID);
    }

    @Test
    public void testAttemptToOrderAllNodesInProjectDespiteIndividualNodeFailures() {
        doThrow(ApApplicationException.class).when(eoiOrderNodeUseCase).execute(NODE1_FDN);
        doThrow(InvalidNodeStateException.class).when(eoiOrderNodeUseCase).execute(NODE2_FDN);

        eoiOrderProjectUseCase.execute(PROJECT_FDN, BASE_URL, SESSION_ID);

        verify(eoiOrderNodeUseCase).execute(NODE1_FDN, BASE_URL, SESSION_ID);
        verify(eoiOrderNodeUseCase).execute(NODE2_FDN, BASE_URL, SESSION_ID);
        verify(eoiOrderNodeUseCase).execute(NODE3_FDN, BASE_URL, SESSION_ID);
    }
}
