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
import static com.ericsson.oss.services.ap.api.status.State.READY_FOR_ORDER;
import static com.ericsson.oss.services.ap.common.model.MoType.NODE_STATUS;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.api.status.StatusEntryNames.ADD_NODE_TASK;
import static com.ericsson.oss.services.ap.api.status.StatusEntryProgress.COMPLETED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
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
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;

/**
 * Unit tests for {@link StatusNodeUseCase}.
 */
@RunWith(MockitoJUnitRunner.class)
public class StatusNodeUseCaseTest {

    private static final String STATE_ATTRIBUTE_NAME = "state";

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private DpsOperations dps;

    @Mock
    private DataPersistenceService dpsService;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private ManagedObject nodeMo;

    @Mock
    private ManagedObject nodeStatusMo;

    @Mock
    private StatusEntryManagerLocal statusEntryManager;

    @InjectMocks
    private StatusNodeUseCase viewNodeStatusUseCase;

    private static final List<StatusEntry> INTEGRATION_COMPLETED_STATUS_ENTRIES = new ArrayList<>();

    static {
        INTEGRATION_COMPLETED_STATUS_ENTRIES.add(new StatusEntry(ADD_NODE_TASK.toString(), COMPLETED.toString(), "2015-01-01 01:23:45.678", ""));
    }

    @Before
    public void setup() {
        when(dps.getDataPersistenceService()).thenReturn(dpsService);
        when(dps.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findMoByFdn(NODE_FDN)).thenReturn(nodeMo);
        when(nodeMo.getFdn()).thenReturn(NODE_FDN);
        when(nodeMo.getChild(NODE_STATUS.toString() + "=1")).thenReturn(nodeStatusMo);
    }

    @Test
    public void whenViewingNodeStatus_andNodeHasNotBeenOrdered_thenNodeStatusReturnedShouldHaveNoEntries() {
        when(nodeStatusMo.getAttribute(STATE_ATTRIBUTE_NAME)).thenReturn(READY_FOR_ORDER.toString());
        when(statusEntryManager.getAllStatusEntries(NODE_FDN)).thenReturn(Collections.<StatusEntry> emptyList());

        final NodeStatus result = viewNodeStatusUseCase.execute(NODE_FDN);

        assertTrue(result.getStatusEntries().isEmpty());
    }

    @Test
    public void whenViewingNodeStatus_andNodeHasBeenIntegrated_thenNodeStatusReturnedShouldHaveValidEntriesForIntegration() {
        when(nodeStatusMo.getAttribute(STATE_ATTRIBUTE_NAME)).thenReturn(INTEGRATION_COMPLETED.toString());
        when(statusEntryManager.getAllStatusEntries(NODE_FDN)).thenReturn(INTEGRATION_COMPLETED_STATUS_ENTRIES);

        final NodeStatus result = viewNodeStatusUseCase.execute(NODE_FDN);

        assertEquals(INTEGRATION_COMPLETED_STATUS_ENTRIES.size(), result.getStatusEntries().size());
    }
}
