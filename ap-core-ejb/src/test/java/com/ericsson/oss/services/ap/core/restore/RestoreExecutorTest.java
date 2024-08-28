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
package com.ericsson.oss.services.ap.core.restore;

import static com.ericsson.oss.services.ap.common.model.MoType.NODE;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.restore.RestoredNodeStateResolver;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor;

/**
 * Unit tests for {@link RestoreExecutor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RestoreExecutorTest {

    private static final String NODE_TYPE_INTERNAL = VALID_NODE_TYPE.toLowerCase();
    private static final String WORKFLOW_ID = "wfId";

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private DpsQueryExecutor<ManagedObject> dpsQueryExecutor;

    @Mock
    private ServiceFinderBean serviceFinder;

    @Mock
    private WorkflowRestoreCriteria workflowRestoreCriteria;

    @Mock
    private WorkflowActions workflowRestoreAction; // NOPMD

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private RestoredNodeStateResolver restoredNodeStateResolver;

    @Mock
    private StateTransitionManagerLocal stateTransitionManagerLocal; // NOPMD

    @InjectMocks
    private RestoreExecutor restoreService;

    @Test
    public void when_suspended_workflow_is_cancelled_by_restore_then_result_reports_cancelled() {
        final List<ManagedObject> erbsMos = new ArrayList<>();
        erbsMos.add(createNodeMoMock(NODE_FDN, VALID_NODE_TYPE));

        final List<String> suspendedWorkflows = new ArrayList<>();
        suspendedWorkflows.add(WORKFLOW_ID);

        when(dpsQueries.findMosWithAttributeValue("activeWorkflowInstanceId", WORKFLOW_ID, AP.toString(), NODE.toString()))
                .thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(erbsMos.iterator());
        when(workflowRestoreCriteria.isWorkflowCancellable(NODE_FDN, false)).thenReturn(true);
        when(serviceFinder.find(RestoredNodeStateResolver.class, NODE_TYPE_INTERNAL)).thenReturn(restoredNodeStateResolver);
        when(restoredNodeStateResolver.resolveNodeState(NODE_FDN)).thenReturn(State.UNKNOWN);
        when(nodeTypeMapper.getInternalEjbQualifier(VALID_NODE_TYPE)).thenReturn(NODE_TYPE_INTERNAL);

        final List<WorkflowRestoreResult> actualResults = restoreService.execute(suspendedWorkflows, false);

        assertEquals("Result did not report cancelled as expected", RestoreResult.CANCELLED, actualResults.get(0).getResult());
    }

    @Test
    public void when_suspended_workflow_is_resumed_by_restore_then_result_reports_resumed() {
        final List<ManagedObject> erbsMos = new ArrayList<>();
        erbsMos.add(createNodeMoMock(NODE_FDN, VALID_NODE_TYPE));

        final List<String> suspendedWorkflows = new ArrayList<>();
        suspendedWorkflows.add(WORKFLOW_ID);

        when(dpsQueries.findMosWithAttributeValue("activeWorkflowInstanceId", WORKFLOW_ID, AP.toString(), NODE.toString()))
                .thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(erbsMos.iterator());
        when(workflowRestoreCriteria.isWorkflowResumable(NODE_FDN, false)).thenReturn(true);

        final List<WorkflowRestoreResult> actualResults = restoreService.execute(suspendedWorkflows, false);

        assertEquals("Result did not report resumed as expected", RestoreResult.RESUMED, actualResults.get(0).getResult());
    }

    @Test
    public void when_suspended_workflow_is_not_resumed_or_cancelled_by_restore_then_result_reports_pending() {
        final List<ManagedObject> erbsMos = new ArrayList<>();
        erbsMos.add(createNodeMoMock(NODE_FDN, VALID_NODE_TYPE));

        final List<String> suspendedWorkflows = new ArrayList<>();
        suspendedWorkflows.add(WORKFLOW_ID);

        when(dpsQueries.findMosWithAttributeValue("activeWorkflowInstanceId", WORKFLOW_ID, AP.toString(), NODE.toString()))
                .thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(erbsMos.iterator());
        when(workflowRestoreCriteria.isWorkflowResumable(NODE_FDN, false)).thenReturn(false);
        when(workflowRestoreCriteria.isWorkflowCancellable(NODE_FDN, false)).thenReturn(false);

        final List<WorkflowRestoreResult> actualResults = restoreService.execute(suspendedWorkflows, false);

        assertEquals("Result did not report pending as expected", RestoreResult.PENDING, actualResults.get(0).getResult());
    }

    @Test
    public void when_no_mo_found_with_matching_wfInstanceId_then_return_no_result() {
        final List<String> suspendedWorkflows = new ArrayList<>();
        suspendedWorkflows.add(WORKFLOW_ID);
        when(dpsQueries.findMosWithAttributeValue("activeWorkflowInstanceId", WORKFLOW_ID, AP.toString(), NODE.toString()))
                .thenReturn(dpsQueryExecutor);
        doReturn(Collections.emptyListIterator()).when(dpsQueryExecutor).execute();

        final List<WorkflowRestoreResult> actualResponses = restoreService.execute(suspendedWorkflows, false);

        assertTrue("Results found for non-matching MO", actualResponses.isEmpty());
    }

    private ManagedObject createNodeMoMock(final String fdn, final String nodeType) {
        final ManagedObject nodeMo = Mockito.mock(ManagedObject.class);
        when(nodeMo.getFdn()).thenReturn(fdn);
        when(nodeMo.getNamespace()).thenReturn("ap");
        when(nodeMo.getVersion()).thenReturn("1.0.0");
        final Map<String, Object> nodeAttributes = new HashMap<>();
        nodeAttributes.put("nodeType", nodeType);
        when(nodeMo.getAllAttributes()).thenReturn(nodeAttributes);
        return nodeMo;
    }
}
