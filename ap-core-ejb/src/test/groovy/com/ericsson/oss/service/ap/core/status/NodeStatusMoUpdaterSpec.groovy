/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.service.ap.core.status

import static com.ericsson.oss.services.ap.common.model.MoType.NODE_STATUS

import javax.inject.Inject

import com.ericsson.cds.cdi.support.rule.ImplementationClasses
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.sdk.recording.ErrorSeverity
import com.ericsson.oss.itpf.sdk.recording.EventLevel
import com.ericsson.oss.itpf.sdk.recording.SystemRecorder
import com.ericsson.oss.service.ap.core.common.test.AbstractNodeStatusSpec
import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException
import com.ericsson.oss.services.ap.api.status.State
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent
import com.ericsson.oss.services.ap.api.status.StatusEntryNames
import com.ericsson.oss.services.ap.api.status.StatusEntryProgress
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute
import com.ericsson.oss.services.ap.common.test.util.assertions.CommonAssertionsSpec
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.ap.common.util.log.TaskOutputLogger
import com.ericsson.oss.services.ap.core.status.NodeStatusMoUpdater
import com.ericsson.oss.services.ap.core.status.StatusEntryManagerEjb

class NodeStatusMoUpdaterSpec extends AbstractNodeStatusSpec {

    @ImplementationClasses
    private static final def definedClasses = [StatusEntryManagerEjb]

    private static final String LOGVIEWER_ID = "AUTO_PROVISIONING.TASK_OUTPUT"
    private static final String ADDITIONAL_INFO = "additional info"
    private static final String ADDITIONAL_INFO_EMPTY = ""
    private static final String ADDITIONAL_INFO_NEW = "new additional info"
    private static final String STATUS_ENTRY_NAME = "TestTask"
    private static final String STATUS_ENTRY_NODE_UP = "Node Up Notification"
    private static final String STATUS_ENTRY_NODE_DOWNLOADING_CONFIGURATIONS = "Node Downloading Configurations"

    @Inject
    private TaskOutputLogger taskOutputLogger

    @Inject
    private SystemRecorder recorder

    @ObjectUnderTest
    private NodeStatusMoUpdater nodeStatusMoUpdater

    private Map<String, Object> statusAttributes = new HashMap<String, Object>()

    @Override
    def setup() {
        statusAttributes.clear()
        taskOutputLogger.recorder = this.recorder
        definedClasses != null // work around for sonar
    }

    def "Verification of Status Entry and Log viewer updates completing successfully"() {

        given: "That an AP Node exists in a valid state"
            final ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), State.ORDER_COMPLETED.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, nodeMo, statusAttributes)

        when: "Status Entry is Updated"
            nodeStatusMoUpdater.addOrUpdateEntry(NODE_FDN, statusEntry, statusEntryProgress, additionalInfo)

        then: "NodeStatus MO is updated and LogViewer correctly updated"
            assertStatusEntry(statusEntryManager.getAllStatusEntries(NODE_FDN).last(), statusEntry.toString(), statusEntryProgress.toString(), additionalInfo.toString())
            recordErrorInvocations * recorder.recordError(LOGVIEWER_ID, ErrorSeverity.ERROR, NODE_NAME, NODE_FDN, _ as String)
            recordEventInvocations * recorder.recordEvent(LOGVIEWER_ID, EventLevel.COARSE, NODE_NAME, NODE_FDN, _ as String)

        where:
            statusEntry       | statusEntryProgress           | additionalInfo        | recordErrorInvocations | recordEventInvocations
            STATUS_ENTRY_NAME | StatusEntryProgress.STARTED   | ADDITIONAL_INFO       |      0                 |     0
            STATUS_ENTRY_NAME | StatusEntryProgress.FAILED    | ADDITIONAL_INFO       |      1                 |     0
            STATUS_ENTRY_NAME | StatusEntryProgress.COMPLETED | ADDITIONAL_INFO       |      0                 |     1
            STATUS_ENTRY_NAME | StatusEntryProgress.RECEIVED  | ADDITIONAL_INFO_EMPTY |      0                 |     1
    }

    def "NodeUp Notification is Updated from Waiting to Received and Status Entry is moved to last"() {

        given: "That an AP Node exists in a valid state"
            final ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), State.ORDER_COMPLETED.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, nodeMo, statusAttributes)

        when: "Status Entry is Updated"
            nodeStatusMoUpdater.addOrUpdateEntry(NODE_FDN, STATUS_ENTRY_NODE_UP, StatusEntryProgress.WAITING, ADDITIONAL_INFO_EMPTY)
            nodeStatusMoUpdater.addOrUpdateEntry(NODE_FDN, STATUS_ENTRY_NODE_UP, StatusEntryProgress.RECEIVED, ADDITIONAL_INFO_EMPTY)
            nodeStatusMoUpdater.addOrUpdateEntry(NODE_FDN, STATUS_ENTRY_NODE_UP, StatusEntryProgress.RECEIVED, ADDITIONAL_INFO)
            nodeStatusMoUpdater.addOrUpdateEntry(NODE_FDN, STATUS_ENTRY_NODE_UP, StatusEntryProgress.RECEIVED, ADDITIONAL_INFO_EMPTY)

        then: "NodeStatus MO is updated and LogViewer correctly updated"
            assertStatusEntry(statusEntryManager.getAllStatusEntries(NODE_FDN).last(), statusEntry.toString(), statusEntryProgress.toString(), additionalInfo.toString())
            recordEvent * recorder.recordEvent(LOGVIEWER_ID, EventLevel.COARSE, NODE_NAME, NODE_FDN, _ as String)

        where:
            statusEntry          | statusEntryProgress           | additionalInfo        | recordEvent
            STATUS_ENTRY_NODE_UP | StatusEntryProgress.RECEIVED  | ADDITIONAL_INFO_EMPTY |      3
    }

    def "Certain tasks should not duplicate when coming in as completed multiple times"() {

        given: "That an AP Node exists in a valid state"
            final ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            final List<String> statusEntries = [createStatusEntry(statusEntry.toString(), StatusEntryProgress.COMPLETED.toString())]
            statusAttributes.put(NodeStatusAttribute.STATUS_ENTRIES.toString(), statusEntries)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), State.ORDER_COMPLETED.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, nodeMo, statusAttributes)

        when: "Status Entry is Updated"
            nodeStatusMoUpdater.addOrUpdateEntry(NODE_FDN, statusEntry, StatusEntryProgress.COMPLETED, ADDITIONAL_INFO_EMPTY)
            nodeStatusMoUpdater.addOrUpdateEntry(NODE_FDN, statusEntry, StatusEntryProgress.COMPLETED, ADDITIONAL_INFO_EMPTY)

        then: "status entries are not duplicated for variable status entries"
            assertNoOfStatusEntries(statusEntryManager.getAllStatusEntries(NODE_FDN), numberOfStatusEntries)

        where:
            statusEntry                                        | numberOfStatusEntries
            StatusEntryNames.ENROLL_OAM_CERTIFICATE.toString() | 1
            StatusEntryNames.SYNC_NODE_NOTIFICATION.toString() | 1
            StatusEntryNames.CREATE_CV_TASK.toString()         | 3
    }

    def "Additional Info updates successfully for new entries"() {

        given: "That an AP Node exists in a valid state"
            final ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), State.ORDER_COMPLETED.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, nodeMo, statusAttributes)

        when: "Additional info update request is made"
            nodeStatusMoUpdater.addOrUpdateEntry(NODE_FDN, statusEntry, statusEntryProgress, ADDITIONAL_INFO)
            nodeStatusMoUpdater.updateAdditionalInformation(NODE_FDN, statusEntry, updatedAdditionalInfo)

        then: "Status entry is updated with the Additional info"
            assertStatusEntry(statusEntryManager.getAllStatusEntries(NODE_FDN).last(), statusEntry.toString(), statusEntryProgress.toString(), updatedAdditionalInfo.toString())

        where:
            statusEntry       | statusEntryProgress         | updatedAdditionalInfo
            STATUS_ENTRY_NAME | StatusEntryProgress.STARTED | ADDITIONAL_INFO_NEW
    }

    def "State is updated when valid transition to next state"() {

        given: "That an AP Node exists in a valid state"
            final ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), State.ORDER_STARTED.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, nodeMo, statusAttributes)

        when: "A valid transition is requested"
            nodeStatusMoUpdater.validateAndSetNextState(NODE_FDN, stateTransition)

        then: "NodeStatus MO is updated"
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, stateResult.name())

        where:
            stateTransition                       | stateResult
            StateTransitionEvent.ORDER_SUCCESSFUL | State.ORDER_COMPLETED
            StateTransitionEvent.DELETE_STARTED   | State.DELETE_STARTED
    }

    def "InvalidNodeStateException is thrown when invalid transition to next state"() {

        given: "That an AP Node exists in a valid state"
            final ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), State.INTEGRATION_CANCELLED.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, nodeMo, statusAttributes)

        when: "A valid transition is requested"
            nodeStatusMoUpdater.validateAndSetNextState(NODE_FDN, stateTransition)

        then: "NodeStatus MO is updated"
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, stateResult.name())
            thrown(InvalidNodeStateException)

        where:
            stateTransition                       | stateResult
            StateTransitionEvent.ORDER_SUCCESSFUL | State.INTEGRATION_CANCELLED
    }

    def "Verify updateEndState updates Node Status MO successfully and updates status entries"() {

        given: "That an AP Node exists in a valid end state"
            final ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), State.INTEGRATION_FAILED.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, nodeMo, statusAttributes)

        when: "A valid transition is requested"
            nodeStatusMoUpdater.updateExistingEntry(NODE_FDN, statusEntry, statusEntryProgress, additionalInfo)

        then: "Status entry is updated and node state remains the same"
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, State.INTEGRATION_FAILED.name())
            assertStatusEntry(statusEntryManager.getAllStatusEntries(NODE_FDN).last(), statusEntry.toString(), statusEntryProgress.toString(), additionalInfo.toString())
            1 * recorder.recordError(LOGVIEWER_ID, ErrorSeverity.ERROR, NODE_NAME, NODE_FDN, _ as String)

        where:
            statusEntry        | statusEntryProgress        | additionalInfo
            STATUS_ENTRY_NAME  | StatusEntryProgress.FAILED | ADDITIONAL_INFO_NEW
    }

    def "Verify setState updates Node Status MO State successfully"() {

        given: "That an AP Node exists in a valid state"
            final ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), State.INTEGRATION_CANCELLED.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, nodeMo, statusAttributes)

        when: "A state update is requested"
            nodeStatusMoUpdater.setState(NODE_FDN, NodeStatusAttribute.STATE.toString(), State.INTEGRATION_STARTED.name())

        then: "NodeStatus MO is updated"
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, State.INTEGRATION_STARTED.name())
    }

    def "Multiple notifications are updated successfully"() {

        given: "That an AP Node exists in a valid state"
            final ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), State.ORDER_COMPLETED.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, nodeMo, statusAttributes)

        when: "updates are sent for notifications"
            nodeStatusMoUpdater.updateExistingEntry(NODE_FDN, "Other task", StatusEntryProgress.COMPLETED, ADDITIONAL_INFO_EMPTY)
            nodeStatusMoUpdater.updateExistingEntry(NODE_FDN, statusEntry.toString(), StatusEntryProgress.WAITING, ADDITIONAL_INFO_EMPTY)
            nodeStatusMoUpdater.updateExistingEntry(NODE_FDN, statusEntry.toString(), statusEntryProgress, ADDITIONAL_INFO_EMPTY)
            nodeStatusMoUpdater.updateExistingEntry(NODE_FDN, statusEntry.toString(), statusEntryProgress, ADDITIONAL_INFO_EMPTY)

        then: "Status entries are not repeated when receiving mutiple updates to a notification, they update correctly"
            checkNoRepeatedStatusEntries(NODE_FDN) == noRepeatedEntries
            assertNoOfStatusEntries(statusEntryManager.getAllStatusEntries(NODE_FDN), noEntries)
            assertStatusEntry(statusEntryManager.getAllStatusEntries(NODE_FDN).last(), statusEntry.toString(), statusEntryProgress.toString(), ADDITIONAL_INFO_EMPTY)

        where:
            statusEntry                                  | statusEntryProgress           | noEntries | noRepeatedEntries
            StatusEntryNames.AIWS_NOTIFICATION           | StatusEntryProgress.RECEIVED  | 2         | true
            StatusEntryNames.EXPANSION_NOTIFICATION      | StatusEntryProgress.RECEIVED  | 3         | false
            StatusEntryNames.NODE_APPLYING_CONFIGURATION | StatusEntryProgress.COMPLETED | 2         | true
    }

    def "Multiple notifications are updated successfully after Node Applying Configuration"() {

       given: "That an AP Node exists in a valid state"
                    final ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
                    statusAttributes.put(NodeStatusAttribute.STATE.toString(), State.ORDER_COMPLETED.toString())
                    MoCreatorSpec.createNodeStatusMo(NODE_FDN, nodeMo, statusAttributes)

        when: "updates are sent for notifications"
                    nodeStatusMoUpdater.updateExistingEntry(NODE_FDN, "Other task", StatusEntryProgress.COMPLETED, ADDITIONAL_INFO_EMPTY)
                    nodeStatusMoUpdater.addOrUpdateEntry(NODE_FDN, statusEntry.toString(), originalProgress, ADDITIONAL_INFO_EMPTY)
                    nodeStatusMoUpdater.addOrUpdateEntry(NODE_FDN, statusEntry.toString(), statusEntryProgress, ADDITIONAL_INFO_EMPTY)
                    nodeStatusMoUpdater.addOrUpdateEntry(NODE_FDN, statusEntry.toString(), statusEntryProgress, ADDITIONAL_INFO_EMPTY)

        then: "Status entries are not repeated when receiving mutiple updates to a notification, they update correctly"
                    checkNoRepeatedStatusEntries(NODE_FDN) == noRepeatedEntries
                    assertNoOfStatusEntries(statusEntryManager.getAllStatusEntries(NODE_FDN), noEntries)
                    assertStatusEntry(statusEntryManager.getAllStatusEntries(NODE_FDN).last(), statusEntry.toString(), statusEntryProgress.toString(), ADDITIONAL_INFO_EMPTY)

        where:
            statusEntry                                  | originalProgress            | statusEntryProgress           | noEntries | noRepeatedEntries
            StatusEntryNames.NODE_APPLYING_CONFIGURATION | StatusEntryProgress.STARTED | StatusEntryProgress.COMPLETED | 2         | true
            StatusEntryNames.NODE_APPLYING_CONFIGURATION | StatusEntryProgress.FAILED  | StatusEntryProgress.COMPLETED | 2         | true
            }

    def "Verify no duplicated node status entries existing"() {

        given: "That an AP Node exists in a valid end state"
            final ManagedObject nodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            statusAttributes.put(NodeStatusAttribute.STATE.toString(), State.INTEGRATION_FAILED.toString())
            MoCreatorSpec.createNodeStatusMo(NODE_FDN, nodeMo, statusAttributes)

        when: "A Failed event after a Started event is requested"
            nodeStatusMoUpdater.updateExistingEntry(NODE_FDN, statusEntry, StatusEntryProgress.STARTED, additionalInfo)
            nodeStatusMoUpdater.updateExistingEntry(NODE_FDN, statusEntry, statusEntryProgress, additionalInfo)

        then: "Status entry is updated and no duplicated node state existing"
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, State.INTEGRATION_FAILED.name())
            assertStatusEntry(statusEntryManager.getAllStatusEntries(NODE_FDN).last(), statusEntry.toString(), statusEntryProgress.toString(), additionalInfo.toString())
            1 * recorder.recordError(LOGVIEWER_ID, ErrorSeverity.ERROR, NODE_NAME, NODE_FDN, _ as String)
            checkNoRepeatedStatusEntries(NODE_FDN) == noRepeatedEntries

        where:
            statusEntry                                   | statusEntryProgress        | additionalInfo           | noRepeatedEntries
            STATUS_ENTRY_NODE_DOWNLOADING_CONFIGURATIONS  | StatusEntryProgress.FAILED | ADDITIONAL_INFO_NEW      | true
    }

    def checkNoRepeatedStatusEntries(final String fdn) {
        final ManagedObject nodeStatusMo = dataPersistenceService.getLiveBucket().findMoByFdn(fdn + "," + NODE_STATUS.toString() + "=1")
        final List<String> immutableStatusEntries = nodeStatusMo.getAttribute(NodeStatusAttribute.STATUS_ENTRIES.toString())
        final List<String> noDuplicatesStatusEntries = new ArrayList<>()
        for (final String currentStatusEntry : immutableStatusEntries) {
            final String taskName = currentStatusEntry.split("\"")[3]
            if (noDuplicatesStatusEntries.contains(taskName)) {
                return false
            } else {
                noDuplicatesStatusEntries.add(taskName)
            }
        }
        return true
    }
}
