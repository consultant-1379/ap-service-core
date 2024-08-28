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
package com.ericsson.oss.services.ap.core.workflow.correlation;

import static com.ericsson.oss.services.ap.api.status.State.INTEGRATION_STARTED;
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_STARTED;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder;
import com.ericsson.oss.mediation.nodediscovery.event.NodeDiscoveryNodeUpEvent;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal;
import com.ericsson.oss.services.ap.api.status.StatusEntryNames;
import com.ericsson.oss.services.ap.api.status.StatusEntryProgress;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.core.status.NodeStatusMoUpdater;
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;

/**
 * Unit tests for {@link NodeUpEventConsumer}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeUpEventConsumerTest {

    private static final String BUSINESS_KEY = "AP_Node=" + NODE_NAME;
    private static final String NODE_UP_MESSAGE = "NODE_UP";
    private static final String NODE_UP_NOTIFICATION = "NodeUpNotification";

    private final Map<String, Object> nodeUpVariable = new HashMap<>();

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private NodeStatusMoUpdater nodeStatusMoUpdater;

    @Mock
    private DpsQueryExecutor<ManagedObject> dpsQueryExecutor;

    @Mock
    private ManagedObject nodeMo;

    @Mock
    private ManagedObject nodeStatusMo;

    @Mock
    private WorkflowInstanceServiceLocal wfsInstanceService;

    @Mock
    private SystemRecorder recorder; // NOPMD

    @Mock
    private StatusEntryManagerLocal statusEntryManager;

    @InjectMocks
    private NodeUpEventConsumer nodeDiscoveryNotificationConsumer;

    private NodeDiscoveryNodeUpEvent nodeDiscoveryNodeUpEvent;
    private StatusEntry nodeUpStatusEntry;

    @Before
    public void setUp() {
        nodeDiscoveryNodeUpEvent = new NodeDiscoveryNodeUpEvent();
        nodeDiscoveryNodeUpEvent.setNodeName(NODE_NAME);
        when(dpsQueries.findMoByName(NODE_NAME, MoType.NODE.toString(), AP.toString())).thenReturn(dpsQueryExecutor);
        nodeUpVariable.put(NODE_UP_NOTIFICATION, StatusEntryProgress.RECEIVED.toString());
        when(nodeMo.getName()).thenReturn(NODE_NAME);
        when(nodeMo.getChild("NodeStatus=1")).thenReturn(nodeStatusMo);
        when(nodeMo.getFdn()).thenReturn(NODE_FDN);
        when(nodeMo.getAttribute("isHardwareReplaceNode")).thenReturn(false);
        when(nodeMo.getAttribute("isNodeMigration")).thenReturn(false);
    }

    @Test
    public void whenNodeUpReceivedAndNodeExistsInApNamespaceAndIntegrationIsNotStartedThenWorkflowIsResumed()
            throws WorkflowMessageCorrelationException {
        final List<ManagedObject> apNodeMos = new ArrayList<>();
        apNodeMos.add(nodeMo);
        nodeUpStatusEntry = new StatusEntry(NODE_UP_NOTIFICATION, StatusEntryProgress.WAITING.toString(), "12:00", "");
        when(statusEntryManager.getStatusEntryByName(NODE_FDN, StatusEntryNames.NODE_UP.toString())).thenReturn(nodeUpStatusEntry);

        when(dpsQueryExecutor.execute()).thenReturn(apNodeMos.iterator());
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.ORDER_COMPLETED.toString());

        nodeDiscoveryNotificationConsumer.listenForNodeUpEvents(nodeDiscoveryNodeUpEvent);

        verify(wfsInstanceService).correlateMessage(NODE_UP_MESSAGE, BUSINESS_KEY, nodeUpVariable);
        verify(nodeStatusMoUpdater).setState(NODE_FDN, NodeStatusAttribute.STATE.toString(), INTEGRATION_STARTED.name());
    }

    @Test
    public void whenNodeUpIsReceivedAndNodeDoesNotExistInApNamespaceThenWorkflowIsNotResumed()
            throws WorkflowMessageCorrelationException {
        nodeUpStatusEntry = new StatusEntry(NODE_UP_NOTIFICATION, StatusEntryProgress.WAITING.toString(), "12:00", "");
        when(statusEntryManager.getStatusEntryByName(NODE_FDN, StatusEntryNames.NODE_UP.toString())).thenReturn(nodeUpStatusEntry);

        when(dpsQueryExecutor.execute()).thenReturn(Collections.<ManagedObject> emptyIterator());
        nodeDiscoveryNotificationConsumer.listenForNodeUpEvents(nodeDiscoveryNodeUpEvent);
        verify(nodeStatusMoUpdater, never()).setState(NODE_FDN, NodeStatusAttribute.STATE.toString(), INTEGRATION_STARTED.name());
        verify(wfsInstanceService, never()).correlateMessage(NODE_UP_MESSAGE, BUSINESS_KEY);
    }

    @Test
    public void whenNodeUpIsReceivedAndNodeIsInStateIntegrationStartedAndNodeUpIsNotAlreadyReceivedThenWorkflowIsResumed()
            throws WorkflowMessageCorrelationException {
        final List<ManagedObject> apNodeMos = new ArrayList<>();
        apNodeMos.add(nodeMo);
        nodeUpStatusEntry = new StatusEntry(NODE_UP_NOTIFICATION, StatusEntryProgress.WAITING.toString(), "12:00", "");
        when(statusEntryManager.getStatusEntryByName(NODE_FDN, StatusEntryNames.NODE_UP.toString())).thenReturn(nodeUpStatusEntry);

        when(dpsQueryExecutor.execute()).thenReturn(apNodeMos.iterator());
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.INTEGRATION_STARTED.toString());

        nodeDiscoveryNotificationConsumer.listenForNodeUpEvents(nodeDiscoveryNodeUpEvent);
        verify(nodeStatusMoUpdater, never()).setState(NODE_FDN, NodeStatusAttribute.STATE.toString(), INTEGRATION_STARTED.name());
        verify(wfsInstanceService).correlateMessage(NODE_UP_MESSAGE, BUSINESS_KEY, nodeUpVariable);
    }

    @Test
    public void whenNodeUpIsReceivedAndNodeIsInStateIntegrationStartedAndNodeUpHasAlreadyBeenReceivedThenWorkflowIsNotResumed()
            throws WorkflowMessageCorrelationException {
        nodeUpStatusEntry = new StatusEntry(NODE_UP_NOTIFICATION, StatusEntryProgress.RECEIVED.toString(), "12:00", "");
        when(statusEntryManager.getStatusEntryByName(NODE_FDN, StatusEntryNames.NODE_UP.toString())).thenReturn(nodeUpStatusEntry);

        final List<ManagedObject> apNodeMos = new ArrayList<>();
        apNodeMos.add(nodeMo);
        when(dpsQueryExecutor.execute()).thenReturn(apNodeMos.iterator());
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(INTEGRATION_STARTED.toString());

        nodeDiscoveryNotificationConsumer.listenForNodeUpEvents(nodeDiscoveryNodeUpEvent);
        verify(nodeStatusMoUpdater, never()).setState(NODE_FDN, NodeStatusAttribute.STATE.toString(), INTEGRATION_STARTED.name());
        verify(wfsInstanceService, never()).correlateMessage(NODE_UP_MESSAGE, BUSINESS_KEY, nodeUpVariable);
        verify(nodeStatusMoUpdater, never()).addOrUpdateEntry(nodeMo.getFdn(), StatusEntryNames.NODE_SENDING_NODE_UP.toString(),
                StatusEntryProgress.COMPLETED, "");
    }

    @Test
    public void whenNodeUpIsReceivedAndSendNodeUpTaskIsPresentThenNodeSendNodeUpTaskIsSetToCompleted() throws WorkflowMessageCorrelationException {
        final StatusEntry sendNodeUpStatusEntry = new StatusEntry("Node Sending Node Up", StatusEntryProgress.STARTED.toString(), "12:00", "");
        final List<ManagedObject> apNodeMos = new ArrayList<>();
        apNodeMos.add(nodeMo);
        when(dpsQueryExecutor.execute()).thenReturn(apNodeMos.iterator());
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.INTEGRATION_STARTED.toString());

        nodeUpStatusEntry = new StatusEntry(NODE_UP_NOTIFICATION, StatusEntryProgress.WAITING.toString(), "12:00", "");
        when(statusEntryManager.getStatusEntryByName(NODE_FDN, StatusEntryNames.NODE_UP.toString())).thenReturn(nodeUpStatusEntry);
        when(statusEntryManager.getStatusEntryByName(NODE_FDN, StatusEntryNames.NODE_SENDING_NODE_UP.toString()))
                .thenReturn(sendNodeUpStatusEntry);

        nodeDiscoveryNotificationConsumer.listenForNodeUpEvents(nodeDiscoveryNodeUpEvent);
        verify(nodeStatusMoUpdater).addOrUpdateEntry(nodeMo.getFdn(), StatusEntryNames.NODE_SENDING_NODE_UP.toString(), StatusEntryProgress.COMPLETED,
                "");
        verify(wfsInstanceService).correlateMessage(NODE_UP_MESSAGE, BUSINESS_KEY, nodeUpVariable);
    }

    @Test
    public void whenNodeUpIsReceivedAndSendNodeUpTaskIsNotPresentThenUpdateIsNeverCalled() throws WorkflowMessageCorrelationException {
        final List<ManagedObject> apNodeMos = new ArrayList<>();
        apNodeMos.add(nodeMo);
        when(dpsQueryExecutor.execute()).thenReturn(apNodeMos.iterator());
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.INTEGRATION_STARTED.toString());

        nodeUpStatusEntry = new StatusEntry(NODE_UP_NOTIFICATION, StatusEntryProgress.WAITING.toString(), "12:00", "");
        when(statusEntryManager.getStatusEntryByName(NODE_FDN, StatusEntryNames.NODE_UP.toString())).thenReturn(nodeUpStatusEntry);

        nodeDiscoveryNotificationConsumer.listenForNodeUpEvents(nodeDiscoveryNodeUpEvent);
        verify(nodeStatusMoUpdater, never()).addOrUpdateEntry(nodeMo.getFdn(), StatusEntryNames.NODE_SENDING_NODE_UP.toString(),
                StatusEntryProgress.COMPLETED, "");
        verify(wfsInstanceService).correlateMessage(NODE_UP_MESSAGE, BUSINESS_KEY, nodeUpVariable);
    }

    @Test
    public void whenNodeUpIsReceivedAndNodeIsInStateOrderStartedThenIntegrationStartedIsNotSet()
            throws WorkflowMessageCorrelationException {
        final List<ManagedObject> apNodeMos = new ArrayList<>();
        apNodeMos.add(nodeMo);
        nodeUpStatusEntry = new StatusEntry(NODE_UP_NOTIFICATION, StatusEntryProgress.WAITING.toString(), "12:00", "");
        when(statusEntryManager.getStatusEntryByName(NODE_FDN, StatusEntryNames.NODE_UP.toString())).thenReturn(nodeUpStatusEntry);

        when(dpsQueryExecutor.execute()).thenReturn(apNodeMos.iterator());
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.ORDER_STARTED.toString());

        nodeDiscoveryNotificationConsumer.listenForNodeUpEvents(nodeDiscoveryNodeUpEvent);
        verify(nodeStatusMoUpdater, never()).setState(NODE_FDN, NodeStatusAttribute.STATE.toString(), INTEGRATION_STARTED.name());
        verify(wfsInstanceService).correlateMessage(NODE_UP_MESSAGE, BUSINESS_KEY, nodeUpVariable);
    }

    @Test
    public void whenNodeUpIsReceivedAndNodeApplyingConfigurationsTaskIsNotInStateCompletedThenTaskIsUpdatedToCompleted()
            throws WorkflowMessageCorrelationException {
        final List<ManagedObject> apNodeMos = new ArrayList<>();
        apNodeMos.add(nodeMo);
        nodeUpStatusEntry = new StatusEntry(NODE_UP_NOTIFICATION, StatusEntryProgress.WAITING.toString(), "12:00", "");
        when(statusEntryManager.getStatusEntryByName(NODE_FDN, StatusEntryNames.NODE_UP.toString())).thenReturn(nodeUpStatusEntry);

        StatusEntry nodeApplyingConfigurationStatusEntry = new StatusEntry(StatusEntryNames.NODE_APPLYING_CONFIGURATION.toString(), StatusEntryProgress.STARTED.toString(), "12:00", "");
        when(statusEntryManager.getStatusEntryByName(NODE_FDN, StatusEntryNames.NODE_APPLYING_CONFIGURATION.toString())).thenReturn(nodeApplyingConfigurationStatusEntry);

        when(dpsQueryExecutor.execute()).thenReturn(apNodeMos.iterator());
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.ORDER_STARTED.toString());

        nodeDiscoveryNotificationConsumer.listenForNodeUpEvents(nodeDiscoveryNodeUpEvent);
        verify(statusEntryManager).taskCompleted(nodeMo.getFdn(),
                StatusEntryNames.NODE_APPLYING_CONFIGURATION.toString());
    }

    @Test
    public void whenNodeUpIsReceivedAndNodeIsHardwareReplaceThenIntegrationStartedIsNotSet()
        throws WorkflowMessageCorrelationException {
        final List<ManagedObject> apNodeMos = new ArrayList<>();
        when(nodeMo.getAttribute("isHardwareReplaceNode")).thenReturn(true);
        apNodeMos.add(nodeMo);
        nodeUpStatusEntry = new StatusEntry(NODE_UP_NOTIFICATION, StatusEntryProgress.WAITING.toString(), "12:00", "");
        when(statusEntryManager.getStatusEntryByName(NODE_FDN, StatusEntryNames.NODE_UP.toString())).thenReturn(nodeUpStatusEntry);

        when(dpsQueryExecutor.execute()).thenReturn(apNodeMos.iterator());
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.HARDWARE_REPLACE_BIND_COMPLETED.toString());

        nodeDiscoveryNotificationConsumer.listenForNodeUpEvents(nodeDiscoveryNodeUpEvent);
        verify(nodeStatusMoUpdater, never()).setState(NODE_FDN, NodeStatusAttribute.STATE.toString(), INTEGRATION_STARTED.name());
        verify(wfsInstanceService).correlateMessage(NODE_UP_MESSAGE, BUSINESS_KEY, nodeUpVariable);
    }

    @Test
    public void whenNodeUpIsReceivedAndNodeIsMigrationThenIntegrationStartedIsNotSet()
        throws WorkflowMessageCorrelationException {
        final List<ManagedObject> apNodeMos = new ArrayList<>();
        when(nodeMo.getAttribute("isNodeMigration")).thenReturn(true);
        when(nodeMo.getAttribute("isRollback")).thenReturn(false);
        apNodeMos.add(nodeMo);
        nodeUpStatusEntry = new StatusEntry(NODE_UP_NOTIFICATION, StatusEntryProgress.WAITING.toString(), "12:00", "");
        when(statusEntryManager.getStatusEntryByName(NODE_FDN, StatusEntryNames.NODE_UP.toString())).thenReturn(nodeUpStatusEntry);

        when(dpsQueryExecutor.execute()).thenReturn(apNodeMos.iterator());
        when(nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString())).thenReturn(State.MIGRATION_STARTED.toString());

        nodeDiscoveryNotificationConsumer.listenForNodeUpEvents(nodeDiscoveryNodeUpEvent);
        verify(nodeStatusMoUpdater, never()).setState(NODE_FDN, NodeStatusAttribute.STATE.toString(), INTEGRATION_STARTED.name());
        verify(wfsInstanceService).correlateMessage(NODE_UP_MESSAGE, BUSINESS_KEY, nodeUpVariable);
    }

    @Test
    public void whenNodeUpIsReceivedAndNodeIsMigrationThenMigrationStartedIsSet()
        throws WorkflowMessageCorrelationException {
        final List<ManagedObject> apNodeMos = new ArrayList<>();
        when(nodeMo.getAttribute("isNodeMigration")).thenReturn(true);
        when(nodeMo.getAttribute("isRollback")).thenReturn(null);
        apNodeMos.add(nodeMo);
        nodeUpStatusEntry = new StatusEntry(NODE_UP_NOTIFICATION, StatusEntryProgress.WAITING.toString(), "12:00", "");
        when(statusEntryManager.getStatusEntryByName(NODE_FDN, StatusEntryNames.NODE_UP.toString())).thenReturn(nodeUpStatusEntry);

        when(dpsQueryExecutor.execute()).thenReturn(apNodeMos.iterator());

        nodeDiscoveryNotificationConsumer.listenForNodeUpEvents(nodeDiscoveryNodeUpEvent);
        verify(nodeStatusMoUpdater).setState(NODE_FDN, NodeStatusAttribute.STATE.toString(), MIGRATION_STARTED.name());
        verify(wfsInstanceService).correlateMessage(NODE_UP_MESSAGE, BUSINESS_KEY, nodeUpVariable);
    }

    @Test
    public void whenNodeUpIsReceivedAndNodeIsMigrationRollbackThenMigrationStartedIsNotSet()
        throws WorkflowMessageCorrelationException {
        final List<ManagedObject> apNodeMos = new ArrayList<>();
        when(nodeMo.getAttribute("isNodeMigration")).thenReturn(true);
        when(nodeMo.getAttribute("isRollback")).thenReturn(true);
        apNodeMos.add(nodeMo);
        nodeUpStatusEntry = new StatusEntry(NODE_UP_NOTIFICATION, StatusEntryProgress.WAITING.toString(), "12:00", "");
        when(statusEntryManager.getStatusEntryByName(NODE_FDN, StatusEntryNames.NODE_UP.toString())).thenReturn(nodeUpStatusEntry);

        when(dpsQueryExecutor.execute()).thenReturn(apNodeMos.iterator());

        nodeDiscoveryNotificationConsumer.listenForNodeUpEvents(nodeDiscoveryNodeUpEvent);
        verify(nodeStatusMoUpdater, never()).setState(NODE_FDN, NodeStatusAttribute.STATE.toString(), MIGRATION_STARTED.name());
    }
}
