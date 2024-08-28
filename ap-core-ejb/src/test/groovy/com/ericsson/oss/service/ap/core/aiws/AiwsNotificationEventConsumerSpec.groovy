/*------------------------------------------------------------------------------
 *******************************************************************************
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

import com.ericsson.cds.cdi.support.rule.ImplementationClasses
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.service.ap.core.common.test.AbstractNodeStatusSpec
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.ap.common.test.util.assertions.CommonAssertionsSpec
import com.ericsson.oss.services.ap.api.status.State
import com.ericsson.oss.services.ap.common.model.NodeAttribute
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute
import com.ericsson.oss.services.ap.core.aiws.AiwsNotificationEventConsumer
import com.ericsson.oss.services.ap.core.status.StatusEntryManagerEjb
import com.ericsson.oss.services.model.autoprovisioning.AiwsNotification
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal
import com.ericsson.oss.services.ap.common.workflow.ActivityType
import org.slf4j.Logger

import spock.lang.Unroll
import javax.inject.Inject

class AiwsNotificationEventConsumerSpec extends AbstractNodeStatusSpec {

    private static final String ANOTHER_NODE_FDN = "Project=Project1,Node=Node2"
    private static final String NODE_FDN_3 = "Project=Project1,Node=Node3"
    private static final String BIND_TYPE = "Bind Type: "
    private static final String SERIAL_NUMBER = "Hardware Serial Number"

    private static
    final EnumSet<State> VALID_STATES = EnumSet.of(State.ORDER_COMPLETED,
            State.INTEGRATION_FAILED, State.BIND_STARTED, State.BIND_COMPLETED, State.HARDWARE_REPLACE_BIND_COMPLETED, State.HARDWARE_REPLACE_STARTED,
            State.PRE_MIGRATION_BIND_STARTED, State.PRE_MIGRATION_BIND_COMPLETED, State.PRE_MIGRATION_COMPLETED, State.MIGRATION_FAILED)

    @ImplementationClasses
    private static final def definedClasses = [StatusEntryManagerEjb]

    @Inject
    private Logger logger

    @Inject
    private WorkflowInstanceServiceLocal wfsInstanceService

    @ObjectUnderTest
    private AiwsNotificationEventConsumer aiwsNotificationEventConsumer

    private Map<String, Object> statusAttributes = new HashMap<String, Object>()
    private Map<String, Object> nodeAttributes = new HashMap<String, Object>()

    private final String taskName = "Node Connection to AIWS Notification"
    private final String taskProgress = "Received"
    private final String serialNumber = "SCB4567890"

    @Override
    def setup() {
        statusAttributes.clear()
        nodeAttributes.clear()
        nodeAttributes.put(NodeAttribute.HARDWARE_SERIAL_NUMBER.toString(), serialNumber)
        assert definedClasses != null // work around for sonar
    }

    @Unroll
    def "Status entry is updated for AIWS notification when state is #nodeState and bind type is hardware serial number"(State nodeState) {

        given: "An AP project with a status of #nodeState"
            final ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, nodeAttributes)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), nodeState.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, nodeMo, statusAttributes)

        when: "A notification from AIWS is received"
            AiwsNotification aiwsNotification = new AiwsNotification()
            aiwsNotification.setHardwareSerialNumber(serialNumber)
            aiwsNotification.setBindType(SERIAL_NUMBER)
            aiwsNotificationEventConsumer.listenToAiwsNotifications(aiwsNotification)

        then: "the node status has the AIWS notification entry"
            assertStatusEntries(statusEntryManager.getAllStatusEntries(nodeMo.getFdn()), taskName, taskProgress, BIND_TYPE + SERIAL_NUMBER)
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, State.INTEGRATION_STARTED.name())

        where:
            nodeState << VALID_STATES
    }

    @Unroll
    def "Status entry is NOT updated for AIWS notification when state is #nodeState"(State nodeState) {

        given: "An AP project with a status of #nodeState"
            final ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, nodeAttributes)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), nodeState.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, nodeMo, statusAttributes)

        when: "A notification from AIWS is received"
            AiwsNotification aiwsNotification = new AiwsNotification()
            aiwsNotification.setHardwareSerialNumber(serialNumber)
            aiwsNotification.setBindType(SERIAL_NUMBER)
            aiwsNotificationEventConsumer.listenToAiwsNotifications(aiwsNotification)

        then: "The status entry is NOT updated"
            assertNoOfStatusEntries(statusEntryManager.getAllStatusEntries(nodeMo.getFdn()), 0)
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, nodeState.name())

        where:
            nodeState << EnumSet.complementOf(VALID_STATES)
    }

    def "An AIWS notification is ignored if the an AP node is not found the serial number provided"() {

        given: "Given two AP Nodes with different serial numbers"
            final ManagedObject node1 = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, nodeAttributes)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), State.ORDER_COMPLETED.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, node1, statusAttributes)
            Map<String, Object> node2Attributes = new HashMap<String, Object>()
            node2Attributes.put(NodeAttribute.HARDWARE_SERIAL_NUMBER.toString(), "SCB1234567")
            final ManagedObject node2 = MoCreatorSpec.createNodeMo(ANOTHER_NODE_FDN, projectMo, node2Attributes)
            MoCreatorSpec.createNodeStatusMo(ANOTHER_NODE_FDN, node2, statusAttributes)

        when: "An AIWS Notification is received with an invalid serial number"
            AiwsNotification aiwsNotification = new AiwsNotification()
            aiwsNotification.setHardwareSerialNumber("doesNotExist")
            aiwsNotification.setBindType(SERIAL_NUMBER)
            aiwsNotificationEventConsumer.listenToAiwsNotifications(aiwsNotification)

        then: "No notificationReceived request is processed"
            assertNoOfStatusEntries(statusEntryManager.getAllStatusEntries(NODE_FDN), 0)
            assertNoOfStatusEntries(statusEntryManager.getAllStatusEntries(ANOTHER_NODE_FDN), 0)
            1 * logger.info("AIWS notification received for hardware serial number: {} and bind type: {}", "doesNotExist", SERIAL_NUMBER)
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, State.ORDER_COMPLETED.name())
    }

    def "AIWS Notification is correlated when the node is executing hardware replace" () {

        given: "AP MOs are created and node is in state BIND_COMPLETED"
            nodeAttributes.put(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString(), true)
            final ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, nodeAttributes)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), State.BIND_COMPLETED.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, nodeMo, statusAttributes)

        when: "A notification from AIWS is received"
            AiwsNotification aiwsNotification = new AiwsNotification()
            aiwsNotification.setHardwareSerialNumber(serialNumber)
            aiwsNotification.setBindType(SERIAL_NUMBER)
            aiwsNotificationEventConsumer.listenToAiwsNotifications(aiwsNotification)

        then: "correlate message is called"
            1 * wfsInstanceService.correlateMessage(_ as String, _ as String)
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, State.BIND_COMPLETED.name())
    }

    def "AIWS Notification is not correlated when the node is not executing hardware replace" (){

        given: "AP MOs are created and node is in state ORDER_COMPLETED"
            final ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, nodeAttributes)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), State.ORDER_COMPLETED.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, nodeMo, statusAttributes)

        when: "A notification from AIWS is received"
            AiwsNotification aiwsNotification = new AiwsNotification()
            aiwsNotification.setHardwareSerialNumber(serialNumber)
            aiwsNotification.setBindType(SERIAL_NUMBER)
            aiwsNotificationEventConsumer.listenToAiwsNotifications(aiwsNotification)

        then: "correlate message is not called"
            0 * wfsInstanceService.correlateMessage(_ as String, _ as String)
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, State.INTEGRATION_STARTED.name())
    }

    def "AIWS Notification is not correlated when the node is executing migration use case" (){

        given: "AP MOs are created and node is in state PRE_MIGRATION_COMPLETED"
        final ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, nodeAttributes)
        statusAttributes.put(NodeStatusAttribute.STATE.toString(), State.PRE_MIGRATION_COMPLETED.toString())
        nodeMo.setAttribute(NodeAttribute.IS_NODE_MIGRATION.toString(), true)
        MoCreatorSpec.createNodeStatusMo(NODE_FDN, nodeMo, statusAttributes)

        when: "Notification from AIWS is received"
        AiwsNotification aiwsNotification = new AiwsNotification()
        aiwsNotification.setHardwareSerialNumber(serialNumber)
        aiwsNotification.setBindType(SERIAL_NUMBER)
        aiwsNotificationEventConsumer.listenToAiwsNotifications(aiwsNotification)

        then: "correlate message is not called"
        0 * wfsInstanceService.correlateMessage(_ as String, _ as String)
        CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, State.MIGRATION_STARTED.name())
    }

    @Unroll
    def "Status entry is updated for AIWS notification when state is #nodeState and bind type is node name"(State nodeState) {

        given: "An AP project with a status of #nodeState"
            final String nodeName = "RBS1234567890"
            Map<String, Object> attributes = new HashMap<String, Object>()
            attributes.put(NodeAttribute.NAME.toString(), nodeName)
            final ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, attributes)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), nodeState.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, nodeMo, statusAttributes)


        when: "A notification from AIWS is received"
            AiwsNotification aiwsNotification = new AiwsNotification()
            aiwsNotification.setNodeName(nodeName)
            aiwsNotification.setBindType(NODE_NAME)
            aiwsNotificationEventConsumer.listenToAiwsNotifications(aiwsNotification)

        then: "the node status has the AIWS notification entry"
            assertStatusEntries(statusEntryManager.getAllStatusEntries(nodeMo.getFdn()), taskName, taskProgress, BIND_TYPE + NODE_NAME)

        where:
            nodeState << VALID_STATES
    }

//    def "Correct activity types are read from node Mo and all activities are covered" () {
//
//        given: "AP MOs are created for all activityTypes"
//            final List<String> activityTypesWithoutAiws = Arrays.asList("expansion")
//            final List<String> activityUnderTest = new ArrayList<String>(activityTypesWithoutAiws);
//
//            nodeAttributes.clear()
//            nodeAttributes.put(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString(), true)
//            nodeAttributes.put(NodeAttribute.IS_NODE_MIGRATION.toString(), false)
//            final ManagedObject hwReplaceNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, nodeAttributes)
//
//            nodeAttributes.clear()
//            nodeAttributes.put(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString(), false)
//            nodeAttributes.put(NodeAttribute.IS_NODE_MIGRATION.toString(), true)
//            final ManagedObject migrationNodeMo = MoCreatorSpec.createNodeMo(ANOTHER_NODE_FDN, projectMo, nodeAttributes)
//
//            nodeAttributes.clear()
//            nodeAttributes.put(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString(), false)
//            nodeAttributes.put(NodeAttribute.IS_NODE_MIGRATION.toString(), false)
//            final ManagedObject greenfieldNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN_3, projectMo, nodeAttributes)
//
//        when: "Call getAcivityName for all activityTypes"
//            String activityNameHwReplace = aiwsNotificationEventConsumer.getAcivityName(hwReplaceNodeMo)
//            activityUnderTest.add(activityNameHwReplace)
//
//            String activityNameMigration = aiwsNotificationEventConsumer.getAcivityName(migrationNodeMo)
//            activityUnderTest.add(activityNameMigration)
//
//            String activityNameGreenfield = aiwsNotificationEventConsumer.getAcivityName(greenfieldNodeMo)
//            activityUnderTest.add(activityNameGreenfield)
//
//        then: "validate all activityTypes are under test"
//            ActivityType.HARDWARE_REPLACE_ACTIVITY.getActivityName() == activityNameHwReplace
//            ActivityType.MIGRATION_ACTIVITY.getActivityName() == activityNameMigration
//            ActivityType.GREENFIELD_ACTIVITY.getActivityName() == activityNameGreenfield
//            true == containsAllActivityTypes(activityUnderTest)
//    }
//
    private boolean containsAllActivityTypes(List<String> activities) {
        for (ActivityType activityType : ActivityType.values()) {
            if (!activities.contains(activityType.getActivityName())) {
                return false
            }
        }
        return true
    }
}
