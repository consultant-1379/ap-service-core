/*------------------------------------------------------------------------------
 ********************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.service.ap.core.aiws

import static com.ericsson.oss.services.ap.api.status.State.BIND_COMPLETED
import static com.ericsson.oss.services.ap.api.status.State.EXPANSION_SUSPENDED
import static com.ericsson.oss.services.ap.api.status.State.HARDWARE_REPLACE_BIND_COMPLETED
import static com.ericsson.oss.services.ap.api.status.State.HARDWARE_REPLACE_COMPLETED
import static com.ericsson.oss.services.ap.api.status.State.HARDWARE_REPLACE_FAILED
import static com.ericsson.oss.services.ap.api.status.State.HARDWARE_REPLACE_STARTED
import static com.ericsson.oss.services.ap.api.status.State.INTEGRATION_FAILED
import static com.ericsson.oss.services.ap.api.status.State.INTEGRATION_STARTED
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_CANCELLED
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_COMPLETED
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_FAILED
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_STARTED
import static com.ericsson.oss.services.ap.api.status.State.ORDER_COMPLETED
import static com.ericsson.oss.services.ap.api.status.State.ORDER_STARTED
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_COMPLETED
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_STARTED
import static com.ericsson.oss.services.ap.api.status.StatusEntryNames.AIWS_NOTIFICATION
import static com.ericsson.oss.services.ap.api.status.StatusEntryNames.EXPANSION_NOTIFICATION
import static com.ericsson.oss.services.ap.api.status.StatusEntryNames.NODE_APPLYING_CONFIGURATION
import static com.ericsson.oss.services.ap.api.status.StatusEntryNames.NODE_DOWNLOADING_CONFIGURATIONS
import static com.ericsson.oss.services.ap.api.status.StatusEntryNames.NODE_ESTABLISHING_CONTACT
import static com.ericsson.oss.services.ap.api.status.StatusEntryNames.NODE_INSTALLING_SOFTWARE
import static com.ericsson.oss.services.ap.api.status.StatusEntryNames.NODE_STARTING_SOFTWARE
import static com.ericsson.oss.services.ap.api.status.StatusEntryNames.NODE_SENDING_NODE_UP
import static com.ericsson.oss.services.ap.api.status.StatusEntryNames.NODE_UP
import static com.ericsson.oss.services.ap.api.status.StatusEntryProgress.COMPLETED
import static com.ericsson.oss.services.ap.api.status.StatusEntryProgress.FAILED
import static com.ericsson.oss.services.ap.api.status.StatusEntryProgress.STARTED
import static com.ericsson.oss.services.ap.api.status.StatusEntryProgress.WAITING
import static com.ericsson.oss.services.model.autoprovisioning.Phase.APPLYING_CONFIGURATION
import static com.ericsson.oss.services.model.autoprovisioning.Phase.DOWNLOADING_SOFTWARE
import static com.ericsson.oss.services.model.autoprovisioning.Phase.ESTABLISHING_CONTACT_MANAGEMENTSYSTEM
import static com.ericsson.oss.services.model.autoprovisioning.Phase.INSTALLING_SOFTWARE
import static com.ericsson.oss.services.model.autoprovisioning.Phase.SENDING_NODE_UP
import static com.ericsson.oss.services.model.autoprovisioning.Phase.STARTING_SOFTWARE

import javax.inject.Inject

import com.ericsson.cds.cdi.support.rule.ImplementationClasses
import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.service.ap.core.common.test.AbstractNodeStatusSpec
import com.ericsson.oss.services.ap.api.status.StatusEntry
import com.ericsson.oss.services.ap.api.status.StatusEntryManagerLocal
import com.ericsson.oss.services.ap.common.model.NodeAttribute
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute
import com.ericsson.oss.services.ap.common.test.util.assertions.CommonAssertionsSpec
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.ap.common.util.log.MRDefinition
import com.ericsson.oss.services.ap.common.util.log.MRExecutionRecorder
import com.ericsson.oss.services.ap.core.aiws.NodeIntegrationStatusEventProcessor
import com.ericsson.oss.services.ap.core.status.StatusEntryManagerEjb
import com.ericsson.oss.services.model.autoprovisioning.NodeIdentifier
import com.ericsson.oss.services.model.autoprovisioning.NodeIntegrationStatusEvent
import com.ericsson.oss.services.model.autoprovisioning.State

class NodeIntegrationStatusEventProcessorSpec extends AbstractNodeStatusSpec {

    @ImplementationClasses
    private static final def definedClasses = [StatusEntryManagerEjb]

    @Inject
    private StatusEntryManagerLocal statusEntryManagerLocal

    @ObjectUnderTest
    private NodeIntegrationStatusEventProcessor nodeIntegrationStatusEventProcessor

    @MockedImplementation
    private MRExecutionRecorder recorder

    private Map<String, Object> statusAttributes = new HashMap<String, Object>()
    private List<String> statusEntries = new ArrayList<String>()

    NodeIdentifier nodeIdentifier = new NodeIdentifier()
    private static final String WAITING_FOR_RESUME = "Waiting for Resume";

    def setup() {
        statusAttributes.clear()
        statusEntries.clear()
        nodeIdentifier.setName(NODE_NAME)
        definedClasses != null // work around for sonar
    }

    def "NodeIntegrationStatusEvent phase is received then StatusEntry is processed correctly"() {
        given: "that an AP node"
            final ManagedObject apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)

        and: " there exists statusEntries that simulates pre existing tasks in AP"
            statusEntries.add(createStatusEntry(previousTaskName.toString(), previousTaskProgress.toString(), additionalInfo))
            if (existingTaskName != null){
                statusEntries.add(createStatusEntry(existingTaskName.toString(), existingTaskProgress.toString(), existingAdditionalInfo))
            }

            buildNodeStatusMoWithEntries(INTEGRATION_STARTED.toString(), apNodeMo)

        when: "NodeIntegrationStatusEvent is received"
            NodeIntegrationStatusEvent nodeIntegrationStatusEvent = setNodeIntegrationStatusEvent(nodeIdentifier, phase, state, progress)
            nodeIntegrationStatusEventProcessor.processNodeIntegrationStatusEvent(apNodeMo, nodeIntegrationStatusEvent)

        then: "AP StatusEntries are updated correctly based on previous states"
            StatusEntry actualStatusEntry = statusEntryManagerLocal.getStatusEntryByName(NODE_FDN, expectedTaskName.toString())
            actualStatusEntry.getTaskProgress() == expectedState.toString()
            actualStatusEntry.getTaskName() == expectedTaskName.toString()

        where: "New Events are received and existing StatusEntry tasks exist"
            previousTaskName               | previousTaskProgress | additionalInfo || existingTaskName               | existingTaskProgress | existingAdditionalInfo || phase                                 | state          | progress || expectedTaskName                | expectedState
            NODE_UP                        | WAITING              | null           || null                           | null                 | null                   || ESTABLISHING_CONTACT_MANAGEMENTSYSTEM | State.STARTED  | null     || NODE_ESTABLISHING_CONTACT       | STARTED
            NODE_UP                        | WAITING              | null           || null                           | null                 | null                   || ESTABLISHING_CONTACT_MANAGEMENTSYSTEM | State.FINISHED | null     || NODE_ESTABLISHING_CONTACT       | COMPLETED
            NODE_UP                        | WAITING              | null           || null                           | null                 | null                   || ESTABLISHING_CONTACT_MANAGEMENTSYSTEM | State.FAILED   | null     || NODE_ESTABLISHING_CONTACT       | FAILED
            NODE_UP                        | WAITING              | null           || null                           | null                 | null                   || DOWNLOADING_SOFTWARE                  | State.STARTED  | 0        || NODE_DOWNLOADING_CONFIGURATIONS | STARTED
            NODE_UP                        | WAITING              | null           || null                           | null                 | null                   || DOWNLOADING_SOFTWARE                  | State.RUNNING  | 10       || NODE_DOWNLOADING_CONFIGURATIONS | STARTED
            NODE_UP                        | WAITING              | null           || null                           | null                 | null                   || DOWNLOADING_SOFTWARE                  | State.FAILED   | null     || NODE_DOWNLOADING_CONFIGURATIONS | FAILED
            NODE_UP                        | WAITING              | null           || NODE_ESTABLISHING_CONTACT      | STARTED              | null                   || ESTABLISHING_CONTACT_MANAGEMENTSYSTEM | State.FAILED   | null     || NODE_ESTABLISHING_CONTACT       | FAILED
            NODE_UP                        | WAITING              | null           || NODE_ESTABLISHING_CONTACT      | STARTED              | null                   || ESTABLISHING_CONTACT_MANAGEMENTSYSTEM | State.FINISHED | null     || NODE_ESTABLISHING_CONTACT       | COMPLETED
            NODE_UP                        | WAITING              | null           || NODE_ESTABLISHING_CONTACT      | COMPLETED            | null                   || ESTABLISHING_CONTACT_MANAGEMENTSYSTEM | State.STARTED  | 0        || NODE_ESTABLISHING_CONTACT       | COMPLETED
            NODE_UP                        | WAITING              | null           || NODE_ESTABLISHING_CONTACT      | STARTED              | null                   || DOWNLOADING_SOFTWARE                  | State.STARTED  | 0        || NODE_ESTABLISHING_CONTACT       | STARTED
            NODE_UP                        | WAITING              | null           || NODE_ESTABLISHING_CONTACT      | FAILED               | null                   || DOWNLOADING_SOFTWARE                  | State.STARTED  | 0        || NODE_ESTABLISHING_CONTACT       | FAILED
            NODE_UP                        | WAITING              | null           || NODE_DOWNLOADING_CONFIGURATIONS| FAILED               | null                   || DOWNLOADING_SOFTWARE                  | State.RUNNING  | 1        || NODE_DOWNLOADING_CONFIGURATIONS | STARTED
            NODE_UP                        | WAITING              | null           || NODE_DOWNLOADING_CONFIGURATIONS| FAILED               | null                   || DOWNLOADING_SOFTWARE                  | State.STARTED  | 0        || NODE_DOWNLOADING_CONFIGURATIONS | FAILED
            NODE_UP                        | WAITING              | null           || NODE_DOWNLOADING_CONFIGURATIONS| STARTED              | "10% test"             || DOWNLOADING_SOFTWARE                  | State.RUNNING  | 20       || NODE_DOWNLOADING_CONFIGURATIONS | STARTED
            NODE_UP                        | WAITING              | null           || NODE_DOWNLOADING_CONFIGURATIONS| COMPLETED            | null                   || DOWNLOADING_SOFTWARE                  | State.RUNNING  | 10       || NODE_DOWNLOADING_CONFIGURATIONS | COMPLETED
            NODE_ESTABLISHING_CONTACT      | COMPLETED            | null           || null                           | null                 | null                   || DOWNLOADING_SOFTWARE                  | State.STARTED  | 0        || NODE_DOWNLOADING_CONFIGURATIONS | STARTED
            NODE_ESTABLISHING_CONTACT      | COMPLETED            | null           || null                           | null                 | null                   || DOWNLOADING_SOFTWARE                  | State.RUNNING  | 10       || NODE_DOWNLOADING_CONFIGURATIONS | STARTED
            NODE_ESTABLISHING_CONTACT      | COMPLETED            | null           || null                           | null                 | null                   || DOWNLOADING_SOFTWARE                  | State.FAILED   | null     || NODE_DOWNLOADING_CONFIGURATIONS | FAILED
            NODE_ESTABLISHING_CONTACT      | COMPLETED            | null           || null                           | null                 | null                   || DOWNLOADING_SOFTWARE                  | State.FINISHED | null     || NODE_DOWNLOADING_CONFIGURATIONS | COMPLETED
            NODE_ESTABLISHING_CONTACT      | COMPLETED            | null           || NODE_DOWNLOADING_CONFIGURATIONS| STARTED              | "3% test"              || DOWNLOADING_SOFTWARE                  | State.RUNNING  | 5        || NODE_DOWNLOADING_CONFIGURATIONS | STARTED
            NODE_ESTABLISHING_CONTACT      | COMPLETED            | null           || NODE_DOWNLOADING_CONFIGURATIONS| STARTED              | "90% test"             || DOWNLOADING_SOFTWARE                  | State.FINISHED | null     || NODE_DOWNLOADING_CONFIGURATIONS | COMPLETED
            NODE_ESTABLISHING_CONTACT      | COMPLETED            | null           || NODE_DOWNLOADING_CONFIGURATIONS| STARTED              | "5% test"              || DOWNLOADING_SOFTWARE                  | State.FAILED   | null     || NODE_DOWNLOADING_CONFIGURATIONS | FAILED
            NODE_ESTABLISHING_CONTACT      | COMPLETED            | null           || NODE_DOWNLOADING_CONFIGURATIONS| FAILED               | null                   || DOWNLOADING_SOFTWARE                  | State.RUNNING  | 10       || NODE_DOWNLOADING_CONFIGURATIONS | STARTED
            NODE_ESTABLISHING_CONTACT      | COMPLETED            | null           || NODE_DOWNLOADING_CONFIGURATIONS| FAILED               | null                   || DOWNLOADING_SOFTWARE                  | State.FINISHED | null     || NODE_DOWNLOADING_CONFIGURATIONS | COMPLETED
            NODE_ESTABLISHING_CONTACT      | COMPLETED            | null           || NODE_DOWNLOADING_CONFIGURATIONS| COMPLETED            | null                   || DOWNLOADING_SOFTWARE                  | State.RUNNING  | 10       || NODE_DOWNLOADING_CONFIGURATIONS | COMPLETED
            NODE_DOWNLOADING_CONFIGURATIONS| COMPLETED            | null           || null                           | null                 | null                   || INSTALLING_SOFTWARE                   | State.STARTED  | 0        || NODE_INSTALLING_SOFTWARE        | STARTED
            NODE_DOWNLOADING_CONFIGURATIONS| COMPLETED            | null           || null                           | null                 | null                   || INSTALLING_SOFTWARE                   | State.RUNNING  | 10       || NODE_INSTALLING_SOFTWARE        | STARTED
            NODE_DOWNLOADING_CONFIGURATIONS| COMPLETED            | null           || null                           | null                 | null                   || INSTALLING_SOFTWARE                   | State.FAILED   | null     || NODE_INSTALLING_SOFTWARE        | FAILED
            NODE_DOWNLOADING_CONFIGURATIONS| COMPLETED            | null           || NODE_INSTALLING_SOFTWARE       | STARTED              | "10% test"             || INSTALLING_SOFTWARE                   | State.STARTED  | 20       || NODE_INSTALLING_SOFTWARE        | STARTED
            NODE_DOWNLOADING_CONFIGURATIONS| COMPLETED            | null           || NODE_INSTALLING_SOFTWARE       | STARTED              | "90% test"             || INSTALLING_SOFTWARE                   | State.FINISHED | null     || NODE_INSTALLING_SOFTWARE        | COMPLETED
            NODE_DOWNLOADING_CONFIGURATIONS| COMPLETED            | null           || NODE_INSTALLING_SOFTWARE       | STARTED              | "90% test"             || INSTALLING_SOFTWARE                   | State.FAILED   | null     || NODE_INSTALLING_SOFTWARE        | FAILED
            NODE_DOWNLOADING_CONFIGURATIONS| COMPLETED            | null           || NODE_INSTALLING_SOFTWARE       | FAILED               | null                   || INSTALLING_SOFTWARE                   | State.FINISHED | null     || NODE_INSTALLING_SOFTWARE        | COMPLETED
            NODE_DOWNLOADING_CONFIGURATIONS| COMPLETED            | null           || NODE_INSTALLING_SOFTWARE       | FAILED               | null                   || INSTALLING_SOFTWARE                   | State.RUNNING  | 20       || NODE_INSTALLING_SOFTWARE        | STARTED
            NODE_INSTALLING_SOFTWARE       | COMPLETED            | null           || null                           | null                 | null                   || STARTING_SOFTWARE                     | State.STARTED  | 0        || NODE_STARTING_SOFTWARE          | STARTED
            NODE_INSTALLING_SOFTWARE       | COMPLETED            | null           || null                           | null                 | null                   || STARTING_SOFTWARE                     | State.RUNNING  | 10       || NODE_STARTING_SOFTWARE          | STARTED
            NODE_INSTALLING_SOFTWARE       | COMPLETED            | null           || null                           | null                 | null                   || STARTING_SOFTWARE                     | State.FAILED   | null     || NODE_STARTING_SOFTWARE          | FAILED
            NODE_INSTALLING_SOFTWARE       | COMPLETED            | null           || NODE_STARTING_SOFTWARE         | STARTED              | "10% test"             || STARTING_SOFTWARE                     | State.RUNNING  | 20       || NODE_STARTING_SOFTWARE          | STARTED
            NODE_INSTALLING_SOFTWARE       | COMPLETED            | null           || NODE_STARTING_SOFTWARE         | STARTED              | "10% test"             || STARTING_SOFTWARE                     | State.RUNNING  | 5        || NODE_STARTING_SOFTWARE          | STARTED
            NODE_INSTALLING_SOFTWARE       | COMPLETED            | null           || NODE_STARTING_SOFTWARE         | COMPLETED            | null                   || STARTING_SOFTWARE                     | State.RUNNING  | 50       || NODE_STARTING_SOFTWARE          | COMPLETED
            NODE_INSTALLING_SOFTWARE       | COMPLETED            | null           || NODE_STARTING_SOFTWARE         | FAILED               | null                   || STARTING_SOFTWARE                     | State.STARTED  | 50       || NODE_STARTING_SOFTWARE          | FAILED
            NODE_INSTALLING_SOFTWARE       | COMPLETED            | null           || NODE_STARTING_SOFTWARE         | FAILED               | null                   || STARTING_SOFTWARE                     | State.RUNNING  | 50       || NODE_STARTING_SOFTWARE          | STARTED
            NODE_STARTING_SOFTWARE         | COMPLETED            | null           || null                           | null                 | null                   || APPLYING_CONFIGURATION                | State.STARTED  | 0        || NODE_APPLYING_CONFIGURATION     | STARTED
            NODE_STARTING_SOFTWARE         | COMPLETED            | null           || null                           | null                 | null                   || APPLYING_CONFIGURATION                | State.RUNNING  | 50       || NODE_APPLYING_CONFIGURATION     | STARTED
            NODE_STARTING_SOFTWARE         | COMPLETED            | null           || null                           | null                 | null                   || APPLYING_CONFIGURATION                | State.FAILED   | null     || NODE_APPLYING_CONFIGURATION     | FAILED
            NODE_STARTING_SOFTWARE         | COMPLETED            | null           || NODE_APPLYING_CONFIGURATION    | STARTED              | 0                      || APPLYING_CONFIGURATION                | State.STARTED  | 0        || NODE_APPLYING_CONFIGURATION     | STARTED
            NODE_STARTING_SOFTWARE         | COMPLETED            | null           || NODE_APPLYING_CONFIGURATION    | COMPLETED            | null                   || APPLYING_CONFIGURATION                | State.RUNNING  | 50       || NODE_APPLYING_CONFIGURATION     | COMPLETED
            NODE_STARTING_SOFTWARE         | COMPLETED            | null           || NODE_APPLYING_CONFIGURATION    | STARTED              | 0                      || APPLYING_CONFIGURATION                | State.RUNNING  | 50       || NODE_APPLYING_CONFIGURATION     | STARTED
            NODE_ESTABLISHING_CONTACT      | STARTED              | null           || null                           | null                 | null                   || DOWNLOADING_SOFTWARE                  | State.FINISHED | 100      || NODE_DOWNLOADING_CONFIGURATIONS | COMPLETED
            NODE_DOWNLOADING_CONFIGURATIONS| STARTED              | null           || null                           | null                 | null                   || INSTALLING_SOFTWARE                   | State.FINISHED | 100      || NODE_INSTALLING_SOFTWARE        | COMPLETED
            NODE_INSTALLING_SOFTWARE       | STARTED              | null           || null                           | null                 | null                   || STARTING_SOFTWARE                     | State.FINISHED | 100      || NODE_STARTING_SOFTWARE          | COMPLETED
            NODE_STARTING_SOFTWARE         | STARTED              | null           || null                           | null                 | null                   || APPLYING_CONFIGURATION                | State.FINISHED | 100      || NODE_APPLYING_CONFIGURATION     | COMPLETED
            NODE_ESTABLISHING_CONTACT      | COMPLETED            | null           || null                           | null                 | null                   || DOWNLOADING_SOFTWARE                  | State.STARTED  | 100      || NODE_DOWNLOADING_CONFIGURATIONS | STARTED
            NODE_APPLYING_CONFIGURATION    | COMPLETED            | null           || null                           | null                 | null                   || SENDING_NODE_UP                       | State.STARTED  | 0        || NODE_SENDING_NODE_UP            | STARTED
    }

    def "NodeIntegrationStatusEvent should cause correct node state updates"() {
        given: "that an AP node exists with existing statusEntries"
            final ManagedObject apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            apNodeMo.setAttribute(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString(), isHwRepaceNode)
            apNodeMo.setAttribute(NodeAttribute.IS_NODE_MIGRATION.toString(), isMigrationNode)
            apNodeMo.setAttribute(NodeAttribute.IS_ROLLBACK.toString(), null)

        and: "There exists statusEntries that simulates pre existing tasks in AP"
            statusEntries.add(createStatusEntry(previousTaskName.toString(), WAITING, null))
            buildNodeStatusMoWithEntries(previousNodeState.toString(), apNodeMo)

        when: "NodeIntegrationStatusEvent is received"
            NodeIntegrationStatusEvent nodeIntegrationStatusEvent = setNodeIntegrationStatusEvent(nodeIdentifier, eventPhase, eventState, null)
            nodeIntegrationStatusEventProcessor.processNodeIntegrationStatusEvent(apNodeMo, nodeIntegrationStatusEvent)

        then: "AP StatusEntries are updated correctly based on previous states"
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, expectedNodeState.name())
            eventInvocations * recorder.recordMRExecution(MRDefinition.AP_AI_STATUS_IMPROVEMENT)
            hwEventInvocations * recorder.recordMRExecution(MRDefinition.AP_HARDWAREREPLACE_STATUS_IMPROVEMENT)

        where: "New Events are received and existing StatusEntry tasks exist"
            previousTaskName                | previousNodeState               ||  eventPhase                           | eventState     | expectedNodeState          || eventInvocations || hwEventInvocations  || isHwRepaceNode || isMigrationNode
            NODE_UP                         | ORDER_COMPLETED                 || ESTABLISHING_CONTACT_MANAGEMENTSYSTEM | State.STARTED  | INTEGRATION_STARTED        || 1                || 0                   || false          || null
            NODE_UP                         | ORDER_COMPLETED                 || DOWNLOADING_SOFTWARE                  | State.RUNNING  | INTEGRATION_STARTED        || 1                || 0                   || false          || null
            NODE_UP                         | ORDER_STARTED                   || DOWNLOADING_SOFTWARE                  | State.STARTED  | ORDER_STARTED              || 0                || 0                   || null           || false
            NODE_UP                         | ORDER_COMPLETED                 || DOWNLOADING_SOFTWARE                  | State.FAILED   | INTEGRATION_FAILED         || 1                || 0                   || null           || false
            NODE_UP                         | ORDER_STARTED                   || DOWNLOADING_SOFTWARE                  | State.FINISHED | ORDER_STARTED              || 0                || 0                   || null           || false
            NODE_ESTABLISHING_CONTACT       | INTEGRATION_STARTED             || DOWNLOADING_SOFTWARE                  | State.FAILED   | INTEGRATION_FAILED         || 0                || 0                   || null           || false
            NODE_DOWNLOADING_CONFIGURATIONS | INTEGRATION_FAILED              || ESTABLISHING_CONTACT_MANAGEMENTSYSTEM | State.STARTED  | INTEGRATION_STARTED        || 0                || 0                   || null           || false
            NODE_UP                         | PRE_MIGRATION_STARTED           || DOWNLOADING_SOFTWARE                  | State.STARTED  | PRE_MIGRATION_STARTED      || 0                || 0                   || false          || true
            NODE_UP                         | PRE_MIGRATION_COMPLETED         || DOWNLOADING_SOFTWARE                  | State.FAILED   | MIGRATION_FAILED           || 0                || 0                   || false          || true
            NODE_ESTABLISHING_CONTACT       | MIGRATION_STARTED               || DOWNLOADING_SOFTWARE                  | State.FAILED   | MIGRATION_FAILED           || 0                || 0                   || false          || true
            NODE_DOWNLOADING_CONFIGURATIONS | MIGRATION_FAILED                || ESTABLISHING_CONTACT_MANAGEMENTSYSTEM | State.RUNNING  | MIGRATION_STARTED          || 0                || 0                   || false          || true
            NODE_INSTALLING_SOFTWARE        | MIGRATION_COMPLETED             || STARTING_SOFTWARE                     | State.STARTED  | MIGRATION_COMPLETED        || 0                || 0                   || false          || true
            NODE_UP                         | MIGRATION_CANCELLED             || DOWNLOADING_SOFTWARE                  | State.STARTED  | MIGRATION_CANCELLED        || 0                || 0                   || false          || true
            NODE_ESTABLISHING_CONTACT       | MIGRATION_CANCELLED             || APPLYING_CONFIGURATION                | State.FAILED   | MIGRATION_CANCELLED        || 0                || 0                   || false          || true
            AIWS_NOTIFICATION               | HARDWARE_REPLACE_BIND_COMPLETED || DOWNLOADING_SOFTWARE                  | State.STARTED  | HARDWARE_REPLACE_STARTED   || 0                || 1                   || true           || false
            AIWS_NOTIFICATION               | HARDWARE_REPLACE_BIND_COMPLETED || ESTABLISHING_CONTACT_MANAGEMENTSYSTEM | State.FAILED   | HARDWARE_REPLACE_FAILED    || 0                || 1                   || true           || false
            AIWS_NOTIFICATION               | HARDWARE_REPLACE_FAILED         || DOWNLOADING_SOFTWARE                  | State.FINISHED | HARDWARE_REPLACE_STARTED   || 0                || 1                   || true           || false
            AIWS_NOTIFICATION               | BIND_COMPLETED                  || ESTABLISHING_CONTACT_MANAGEMENTSYSTEM | State.FAILED   | BIND_COMPLETED             || 0                || 1                   || true           || null
            NODE_ESTABLISHING_CONTACT       | HARDWARE_REPLACE_STARTED        || DOWNLOADING_SOFTWARE                  | State.FINISHED | HARDWARE_REPLACE_STARTED   || 0                || 0                   || true           || false
            NODE_ESTABLISHING_CONTACT       | HARDWARE_REPLACE_COMPLETED      || DOWNLOADING_SOFTWARE                  | State.FAILED   | HARDWARE_REPLACE_COMPLETED || 0                || 0                   || true           || false
            NODE_DOWNLOADING_CONFIGURATIONS | BIND_COMPLETED                  || ESTABLISHING_CONTACT_MANAGEMENTSYSTEM | State.FAILED   | BIND_COMPLETED             || 0                || 0                   || true           || false
            EXPANSION_NOTIFICATION          | EXPANSION_SUSPENDED             || ESTABLISHING_CONTACT_MANAGEMENTSYSTEM | State.STARTED  | EXPANSION_SUSPENDED        || 0                || 0                   || null           || false
    }

    def "NodeIntegrationStatusEvent does not update node state for migration rollback"() {
        given: "that an AP node exists with existing statusEntries"
            final ManagedObject apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            apNodeMo.setAttribute(NodeAttribute.IS_ROLLBACK.toString(), true)
            apNodeMo.setAttribute(NodeAttribute.IS_NODE_MIGRATION.toString(), true)

        and: "There exists statusEntries that simulates pre existing tasks in AP"
            statusEntries.add(createStatusEntry(previousTaskName.toString(), WAITING, null))
            buildNodeStatusMoWithEntries(previousNodeState.toString(), apNodeMo)

        when: "NodeIntegrationStatusEvent is received"
            NodeIntegrationStatusEvent nodeIntegrationStatusEvent = setNodeIntegrationStatusEvent(nodeIdentifier, eventPhase, eventState, null)
            nodeIntegrationStatusEventProcessor.processNodeIntegrationStatusEvent(apNodeMo, nodeIntegrationStatusEvent)

        then: "AP StatusEntries are updated correctly based on previous states"
            CommonAssertionsSpec.assertIntegrationState(dps, NODE_FDN, expectedNodeState.name())

        where: "New Events are received and existing StatusEntry tasks exist"
            previousTaskName                | previousNodeState       || eventPhase                            | eventState    | expectedNodeState
            NODE_UP                         | PRE_MIGRATION_STARTED   || DOWNLOADING_SOFTWARE                  | State.STARTED | PRE_MIGRATION_STARTED
            NODE_UP                         | PRE_MIGRATION_STARTED   || DOWNLOADING_SOFTWARE                  | State.FAILED  | PRE_MIGRATION_STARTED
            NODE_ESTABLISHING_CONTACT       | MIGRATION_STARTED       || DOWNLOADING_SOFTWARE                  | State.FAILED  | MIGRATION_STARTED
            NODE_DOWNLOADING_CONFIGURATIONS | MIGRATION_FAILED        || ESTABLISHING_CONTACT_MANAGEMENTSYSTEM | State.STARTED | MIGRATION_FAILED
    }

    def "Events coming in with progress not in order should be ignored"() {
        given: "that an AP node exists with existing statusEntry"
            final ManagedObject apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)

        and: "There exists statusEntries that simulates pre existing tasks in AP"
            statusEntries.add(createStatusEntry(NODE_DOWNLOADING_CONFIGURATIONS, STARTED, previousAdditionalInfo))
            buildNodeStatusMoWithEntries(INTEGRATION_STARTED.toString(), apNodeMo)

        when: "NodeIntegrationStatusEvent is received"
            NodeIntegrationStatusEvent nodeIntegrationStatusEvent = setNodeIntegrationStatusEvent(nodeIdentifier, DOWNLOADING_SOFTWARE, State.RUNNING, eventProgress, eventMessage)
            nodeIntegrationStatusEventProcessor.processNodeIntegrationStatusEvent(apNodeMo, nodeIntegrationStatusEvent)

        then: "AP additional information is updated correctly based on previous entries"
            StatusEntry actualStatusEntry = statusEntryManagerLocal.getStatusEntryByName(NODE_FDN, NODE_DOWNLOADING_CONFIGURATIONS.toString())
            actualStatusEntry.getAdditionalInfo() == expectedAdditionalInfo.toString()

        where: "New Events are received and existing StatusEntry tasks exist"
            previousAdditionalInfo || eventProgress | eventMessage         || expectedAdditionalInfo
            "90% test"             || 10            | "event test message" || "90% test"
            "80% test"             || 90            | "event test message" || "90% event test message"
            "seliiacie00025_6610"  || 15            | "event test message" || "15% event test message"
            "10%"                  || 15            | "event test message" || "15% event test message"
            "30%"                  || 15            | "event test message" || "30%"
            "10% 30%"              || 15            | "event test message" || "15% event test message"
    }

    def "NodeIntegrationStatusEvent should cause correct node state updates in Hardware Replace LMT scenario"() {
        given: "that an AP node exists with existing statusEntries"
            final ManagedObject apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
            apNodeMo.setAttribute(NodeAttribute.IS_HARDWARE_REPLACE_NODE.toString(), true)

        and: "There exists statusEntries that simulates LMT in AP"
            statusEntries.add(createStatusEntry(NODE_DOWNLOADING_CONFIGURATIONS.toString(), COMPLETED, null))
            statusEntries.add(createStatusEntry(NODE_INSTALLING_SOFTWARE.toString(), COMPLETED, null))
            statusEntries.add(createStatusEntry(NODE_STARTING_SOFTWARE.toString(), STARTED, null))
            if (waitForResumeExist){
                statusEntries.add(createStatusEntry(WAITING_FOR_RESUME, waitForResumeStatus, null))
            }

            buildNodeStatusMoWithEntries(HARDWARE_REPLACE_STARTED.toString(), apNodeMo)

        when: "NodeIntegrationStatusEvent is received"
            NodeIntegrationStatusEvent nodeIntegrationStatusEvent = setNodeIntegrationStatusEvent(nodeIdentifier, STARTING_SOFTWARE, State.RUNNING, eventProgress, eventMessage)
            nodeIntegrationStatusEventProcessor.processNodeIntegrationStatusEvent(apNodeMo, nodeIntegrationStatusEvent)

        then: "AP task progress is updated correctly based on event"
            StatusEntry actualStatusEntry = statusEntryManagerLocal.getStatusEntryByName(NODE_FDN, NODE_STARTING_SOFTWARE.toString())
            actualStatusEntry.getTaskProgress() == expectedTaskProgress.toString()

        where: "New Events are received and existing StatusEntry tasks exist"
            eventProgress | eventMessage            | waitForResumeStatus | waitForResumeExist  || expectedTaskProgress
            10            | "event test message"    | STARTED             | true                || STARTED
            20            | "Node reboot initiated" | STARTED             | true                || COMPLETED
            20            | "Node reboot initiated" | COMPLETED           | true                || STARTED
            20            | "Node reboot initiated" | null                | false               || STARTED
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
