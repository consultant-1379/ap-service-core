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
package com.ericsson.oss.services.ap.core.status;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;

/**
 * Unit tests for {@link StatusEntryManagerEjb}.
 */
@RunWith(MockitoJUnitRunner.class)
public class StatusEntryManagerEjbTest {

    private static final String NODE_STATUS_FDN = NODE_FDN + ",NodeStatus=1";
    private static final String STATUS_ENTRY = "{\"taskName\":\"TestTask\",\"taskProgress\":\"Started\",\"timeStamp\":\"TimeStamp\",\"additionalInfo\":\"Additional Info\"}";

    @Mock
    private DpsOperations dps;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private DataPersistenceService dpsService;

    @Mock
    private ManagedObject nodeStatusMo;

    @Spy
    private StatusEntryFormatter statusEntryFormatter; // NOPMD

    @InjectMocks
    private StatusEntryManagerEjb statusEntryManagerEjb;

    @Before
    public void setUp() {
        when(dps.getDataPersistenceService()).thenReturn(dpsService);
        when(dps.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findMoByFdn(NODE_STATUS_FDN)).thenReturn(nodeStatusMo);
    }

    @Test
    public void when_getAllStatusEntries_is_called_then_all_StatusEntries_are_returned() {
        final List<String> statusEntries = new ArrayList<>();
        statusEntries.add(STATUS_ENTRY);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATUS_ENTRIES.toString())).thenReturn(statusEntries);

        final List<StatusEntry> actualStatusEntries = statusEntryManagerEjb.getAllStatusEntries(NODE_FDN);

        assertEquals(1, actualStatusEntries.size());
        final StatusEntry expectedStatusEntry = new StatusEntry("TestTask", "Started", "TimeStamp", "Additional Info");
        assertStatusEntryObjectsAreEqual(expectedStatusEntry, actualStatusEntries.get(0));
    }

    @Test
    public void whenGetAllStatusEntriesInNewTxIsCalledThenAllStatusEntriesAreReturned() {
        final List<String> statusEntries = new ArrayList<>();
        statusEntries.add(STATUS_ENTRY);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATUS_ENTRIES.toString())).thenReturn(statusEntries);

        final List<StatusEntry> actualStatusEntries = statusEntryManagerEjb.getAllStatusEntriesInNewTx(NODE_FDN);

        assertEquals(1, actualStatusEntries.size());
        final StatusEntry expectedStatusEntry = new StatusEntry("TestTask", "Started", "TimeStamp", "Additional Info");
        assertStatusEntryObjectsAreEqual(expectedStatusEntry, actualStatusEntries.get(0));
    }

    @Test
    public void whenGetStatusEntryByName_andEntryExists_thenEntryIsReturned() {
        final List<String> statusEntries = new ArrayList<>();
        statusEntries.add(STATUS_ENTRY);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATUS_ENTRIES.toString())).thenReturn(statusEntries);

        final StatusEntry result = statusEntryManagerEjb.getStatusEntryByName(NODE_FDN, "TestTask");

        final StatusEntry expectedStatusEntry = new StatusEntry("TestTask", "Started", "TimeStamp", "Additional Info");
        assertStatusEntryObjectsAreEqual(expectedStatusEntry, result);
    }

    @Test
    public void whenGetStatusEntryByNameInNewTxAndEntryExistsThenEntryIsReturned() {
        final List<String> statusEntries = new ArrayList<>();
        statusEntries.add(STATUS_ENTRY);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATUS_ENTRIES.toString())).thenReturn(statusEntries);

        final StatusEntry result = statusEntryManagerEjb.getStatusEntryByNameInNewTx(NODE_FDN, "TestTask");

        final StatusEntry expectedStatusEntry = new StatusEntry("TestTask", "Started", "TimeStamp", "Additional Info");
        assertStatusEntryObjectsAreEqual(expectedStatusEntry, result);
    }

    @Test
    public void whenGetStatusEntryByName_andEntryDoesNotExist_thenNullIsReturned() {
        final List<String> statusEntries = new ArrayList<>();
        statusEntries.add(STATUS_ENTRY);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATUS_ENTRIES.toString())).thenReturn(statusEntries);

        final StatusEntry result = statusEntryManagerEjb.getStatusEntryByName(NODE_FDN, "fakeTaskName");

        assertNull(result);
    }

    @Test
    public void whenGetStatusEntryByNameInNewTxAndEntryDoesNotExistThenNullIsReturned() {
        final List<String> statusEntries = new ArrayList<>();
        statusEntries.add(STATUS_ENTRY);
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATUS_ENTRIES.toString())).thenReturn(statusEntries);

        final StatusEntry result = statusEntryManagerEjb.getStatusEntryByNameInNewTx(NODE_FDN, "fakeTaskName");

        assertNull(result);
    }

    private void assertStatusEntryObjectsAreEqual(final StatusEntry first, final StatusEntry second) {
        final boolean result = first.getTaskName().equals(second.getTaskName())
                && first.getTaskProgress().equals(second.getTaskProgress())
                && first.getTimeStamp().equals(second.getTimeStamp())
                && first.getAdditionalInfo().equals(second.getAdditionalInfo());
        assertTrue(result);
    }
}
