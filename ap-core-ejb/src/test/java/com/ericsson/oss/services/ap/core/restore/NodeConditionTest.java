/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.restore;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.api.status.StatusEntryProgress;

/**
 * Unit tests for {@link NodeCondition}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeConditionTest {

    @Mock
    private StatusEntryManagerLocal statusEntryManagerLocal;

    @InjectMocks
    private NodeCondition nodeCondition;

    @Test
    public void whenCheckingForNodeUp_andNodeUpNotificationStatusEntryExists_andProgressIsWaiting_thenTrueIsReturned() {
        final List<StatusEntry> statusEntries = new ArrayList<>();
        statusEntries.add(new StatusEntry("Node Up Notification", StatusEntryProgress.WAITING.toString(), null, null));
        when(statusEntryManagerLocal.getAllStatusEntries(NODE_FDN)).thenReturn(statusEntries);

        final boolean waitingForNodeup = nodeCondition.isWaitingForNodeUp(NODE_FDN);

        assertTrue(waitingForNodeup);
    }

    @Test
    public void whenCheckingForNodeUp_andNodeUpNotificationStatusEntryExists_andProgressIsRecieved_thenFalseIsReturned() {
        final List<StatusEntry> statusEntries = new ArrayList<>();
        statusEntries.add(new StatusEntry("Node Up Notification", StatusEntryProgress.RECEIVED.toString(), null, null));
        when(statusEntryManagerLocal.getAllStatusEntries(NODE_FDN)).thenReturn(statusEntries);

        final boolean waitingForNodeup = nodeCondition.isWaitingForNodeUp(NODE_FDN);

        assertFalse(waitingForNodeup);
    }

    @Test
    public void whenCheckingForNodeUp_andNodeUpNotificationStatusEntryDoesNotExist_thenFalseIsReturned() {
        when(statusEntryManagerLocal.getAllStatusEntries(NODE_FDN)).thenReturn(Collections.<StatusEntry> emptyList());
        final boolean waitingForNodeup = nodeCondition.isWaitingForNodeUp(NODE_FDN);
        assertFalse(waitingForNodeup);
    }

    @Test
    public void whenCheckingForNodeUp_andNodeUpNotificationStatusEntryExists_andProgressIsWaiting_andStatusEntryIsNotLastEntry_thenTrueIsReturned() {
        final List<StatusEntry> statusEntries = new ArrayList<>();
        statusEntries.add(new StatusEntry("Node Up Notification", StatusEntryProgress.WAITING.toString(), null, null));
        statusEntries.add(new StatusEntry("Another Notification", StatusEntryProgress.WAITING.toString(), null, null));
        when(statusEntryManagerLocal.getAllStatusEntries(NODE_FDN)).thenReturn(statusEntries);

        final boolean waitingForNodeup = nodeCondition.isWaitingForNodeUp(NODE_FDN);

        assertTrue(waitingForNodeup);
    }
}