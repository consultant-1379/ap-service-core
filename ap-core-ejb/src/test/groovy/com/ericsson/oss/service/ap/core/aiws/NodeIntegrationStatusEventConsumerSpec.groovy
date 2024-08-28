/*------------------------------------------------------------------------------
 ********************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.service.ap.core.aiws

import static com.ericsson.oss.services.ap.api.status.State.ORDER_COMPLETED
import static com.ericsson.oss.services.ap.api.status.StatusEntryNames.NODE_UP
import static com.ericsson.oss.services.ap.api.status.StatusEntryProgress.WAITING
import static com.ericsson.oss.services.model.autoprovisioning.Phase.ESTABLISHING_CONTACT_MANAGEMENTSYSTEM

import com.ericsson.cds.cdi.support.rule.ImplementationClasses
import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.service.ap.core.common.test.AbstractNodeStatusSpec
import com.ericsson.oss.services.ap.common.model.NodeAttribute
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.ap.core.aiws.NodeIntegrationStatusEventConsumer
import com.ericsson.oss.services.ap.core.aiws.NodeIntegrationStatusEventProcessor
import com.ericsson.oss.services.ap.core.status.StatusEntryManagerEjb
import com.ericsson.oss.services.model.autoprovisioning.NodeIdentifier
import com.ericsson.oss.services.model.autoprovisioning.NodeIntegrationStatusEvent
import com.ericsson.oss.services.model.autoprovisioning.State

class NodeIntegrationStatusEventConsumerSpec extends AbstractNodeStatusSpec {

    @ImplementationClasses
    private static final def definedClasses = [StatusEntryManagerEjb]

    @MockedImplementation
    private NodeIntegrationStatusEventProcessor statusEventProcessor

    @ObjectUnderTest
    private NodeIntegrationStatusEventConsumer nodeIntegrationStatusEventEventConsumer

    private Map<String, Object> nodeAttributes = new HashMap<String, Object>()
    private Map<String, Object> statusAttributes = new HashMap<String, Object>()
    private List<String> statusEntries = new ArrayList<String>()

    NodeIdentifier nodeIdentifier = new NodeIdentifier()

    def setup() {
        nodeAttributes.clear()
        statusAttributes.clear()
        statusEntries.clear()
        assert definedClasses != null // work around for sonar
        nodeIdentifier.setName(NODE_NAME)
    }

    def "Node event is sent for processing"() {
        given: "that an AP node exists"
            ManagedObject apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)

        and: " there exists statusEntries that simulates pre existing tasks in AP"
            statusEntries.add(createStatusEntry(NODE_UP.toString(), WAITING.toString(), null))
            buildNodeStatusMoWithEntries(ORDER_COMPLETED.toString(), apNodeMo)

            NodeIntegrationStatusEvent nodeIntegrationStatusEvent = setNodeIntegrationStatusEvent(nodeIdentifier, ESTABLISHING_CONTACT_MANAGEMENTSYSTEM, State.STARTED, null)

        when: "A node integration status event is received"
            nodeIntegrationStatusEventEventConsumer.listenToAiwsNodeIntegrationNotifications(nodeIntegrationStatusEvent)

        then: "Event is sent to be processed for the node MO"
            1 * statusEventProcessor.processNodeIntegrationStatusEvent(apNodeMo, nodeIntegrationStatusEvent)
    }

    def "Node event is sent for processing given hardware serial number"() {
        given: "that an AP node exists"
            nodeAttributes.put(NodeAttribute.HARDWARE_SERIAL_NUMBER.toString(), "ABC10000000")
            final ManagedObject apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, nodeAttributes)

        and: " there exists statusEntries that simulates pre existing tasks in AP"
            statusEntries.add(createStatusEntry(NODE_UP.toString(), WAITING.toString(), null))
            buildNodeStatusMoWithEntries(ORDER_COMPLETED.toString(), apNodeMo)

        and: "Event created with hardware serial number"
            NodeIdentifier nodeIdentifier2 = new NodeIdentifier()
            nodeIdentifier2.setSerialNumber("ABC10000000")
            NodeIntegrationStatusEvent nodeIntegrationStatusEvent = setNodeIntegrationStatusEvent(nodeIdentifier2, ESTABLISHING_CONTACT_MANAGEMENTSYSTEM, State.STARTED, null)

        when: "A node integration status event is received"
            nodeIntegrationStatusEventEventConsumer.listenToAiwsNodeIntegrationNotifications(nodeIntegrationStatusEvent)

        then: "Event sent to be processed for the node MO"
            1 * statusEventProcessor.processNodeIntegrationStatusEvent(apNodeMo, nodeIntegrationStatusEvent)
    }

    def setNodeIntegrationStatusEvent(nodeIdentifier, phase, state, progress, message = "") {
        NodeIntegrationStatusEvent nodeIntegrationStatusEvent = new NodeIntegrationStatusEvent()
        nodeIntegrationStatusEvent.setNodeIdentifier(nodeIdentifier)
        nodeIntegrationStatusEvent.setPhase(phase)
        nodeIntegrationStatusEvent.setState(state)
        nodeIntegrationStatusEvent.setProgress(progress)
        nodeIntegrationStatusEvent.setMessage(message)
        return nodeIntegrationStatusEvent
    }

    def buildNodeStatusMoWithEntries(String state, ManagedObject apNodeMo) {
        statusAttributes.put(NodeStatusAttribute.STATUS_ENTRIES.toString(), statusEntries)
        statusAttributes.put(NodeStatusAttribute.STATE.toString(), state)
        MoCreatorSpec.createNodeStatusMo(NODE_FDN, apNodeMo, statusAttributes)
    }
}
