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
package com.ericsson.oss.service.ap.core

import static com.ericsson.oss.services.ap.api.status.State.EXPANSION_CANCELLED
import static com.ericsson.oss.services.ap.api.status.State.EXPANSION_COMPLETED
import static com.ericsson.oss.services.ap.api.status.State.HARDWARE_REPLACE_COMPLETED
import static com.ericsson.oss.services.ap.api.status.State.INTEGRATION_CANCELLED
import static com.ericsson.oss.services.ap.api.status.State.INTEGRATION_COMPLETED
import static com.ericsson.oss.services.ap.api.status.State.INTEGRATION_COMPLETED_WITH_WARNING
import static com.ericsson.oss.services.ap.api.status.State.INTEGRATION_FAILED
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_CANCELLED
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_COMPLETED
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_COMPLETED_WITH_WARNING
import static com.ericsson.oss.services.ap.api.status.State.MIGRATION_FAILED
import static com.ericsson.oss.services.ap.api.status.State.ORDER_CANCELLED
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_CANCELLED
import static com.ericsson.oss.services.ap.api.status.State.PRE_MIGRATION_FAILED

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.service.ap.core.common.test.AbstractNodeStatusSpec
import com.ericsson.oss.services.ap.api.cluster.APServiceClusterMember
import com.ericsson.oss.services.ap.api.status.State
import com.ericsson.oss.services.ap.common.util.log.MRDefinition
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.ap.common.usecase.UseCaseName
import com.ericsson.oss.services.ap.common.util.log.MRExecutionRecorder
import com.ericsson.oss.services.ap.core.EndStateNodeCleanupEjb
import com.ericsson.oss.services.ap.core.usecase.DeleteNodeUseCase
import com.ericsson.oss.services.ap.core.usecase.UseCaseFactory

import java.time.LocalDate

class EndStateNodeCleanupEjbSpec extends AbstractNodeStatusSpec {

    private static final NODE_FDN2 = PROJECT_FDN + ",Node=Node2"

    @ObjectUnderTest
    private EndStateNodeCleanupEjb ejb

    @MockedImplementation
    private MRExecutionRecorder recorder

    @MockedImplementation
    private UseCaseFactory useCaseFactory

    @MockedImplementation
    private APServiceClusterMember serviceClusterMember;

    @MockedImplementation
    private DeleteNodeUseCase usecase

    private ManagedObject nodeMo

    def setup () {
        serviceClusterMember.isMasterNode() >> true

        useCaseFactory.getNamedUsecase(UseCaseName.DELETE_NODE) >> usecase
        usecase.execute(NODE_FDN, true) >> {}
    }

    def "Nodes with attributes that make the node eligible for deletion through cleanup are removed when cleanup is kicked off"  () {
        given: "Node created with valid state and date for cleanup"
            buildNodeStatusWithAttrs(NODE_FDN, validState, validDate)
        when: "Cleanup is called"
            ejb.start()
        then: "Cleanup calls delete on eligible node"
            1 * usecase.execute(NODE_FDN, true)
            1 * recorder.recordMRExecution(MRDefinition.AP_AUTOMATIC_HOUSEKEEPING)
        where:
            validState                         | validDate
            INTEGRATION_COMPLETED              | LocalDate.now().minusDays(7).toString()
            EXPANSION_COMPLETED                | LocalDate.now().minusDays(7).toString()
            HARDWARE_REPLACE_COMPLETED         | LocalDate.now().minusDays(7).toString()
            INTEGRATION_COMPLETED_WITH_WARNING | LocalDate.now().minusDays(7).toString()
            INTEGRATION_CANCELLED              | LocalDate.now().minusDays(7).toString()
            ORDER_CANCELLED                    | LocalDate.now().minusDays(7).toString()
            EXPANSION_CANCELLED                | LocalDate.now().minusDays(7).toString()
            MIGRATION_COMPLETED                | LocalDate.now().minusDays(7).toString()
            MIGRATION_COMPLETED_WITH_WARNING   | LocalDate.now().minusDays(7).toString()
            MIGRATION_CANCELLED                | LocalDate.now().minusDays(7).toString()
            PRE_MIGRATION_CANCELLED            | LocalDate.now().minusDays(7).toString()
            MIGRATION_FAILED                   | LocalDate.now().minusDays(7).toString()
            PRE_MIGRATION_FAILED               | LocalDate.now().minusDays(7).toString()
    }

    def "Nodes with attributes that make them ineligible for deletion by cleanup are left alone" () {
        given: "Node created with invalid state or date for cleanup"
            buildNodeStatusWithAttrs(NODE_FDN, state, date)
        when: "Cleanup is called"
            ejb.start()
        then: "Cleanup does not call delete"
            0 * usecase.execute(NODE_FDN, true)
            0 * recorder.recordMRExecution(MRDefinition.AP_AUTOMATIC_HOUSEKEEPING)
        where: "ineligible state, ineligible date, and both ineligible state and null date (endStateDate not set)"
            state                     | date
            INTEGRATION_FAILED        | null
            ORDER_CANCELLED           | null
    }

    def "MR Logger only executes once when multiple nodes deleted" () {
        given: "Node created with valid state and valid date"
            buildNodeStatusWithAttrs(NODE_FDN, validState, validDate)
            buildNodeStatusWithAttrs(NODE_FDN2, validState, validDate)
        when: "Cleanup is called"
            ejb.start()
        then: "Cleanup calls delete on eligible node"
            1 * usecase.execute(NODE_FDN, true)
            1 * usecase.execute(NODE_FDN2, true)
            1 * recorder.recordMRExecution(MRDefinition.AP_AUTOMATIC_HOUSEKEEPING)
        where: "Valid states and a valid date of 7 days before the date of test run"
            validState                         | validDate
            INTEGRATION_COMPLETED              | LocalDate.now().minusDays(7).toString()
    }

    def buildNodeStatusWithAttrs(final String nodeFdn, final State state, final String endStateDate) {
        final Map<String, String> attrs = new HashMap<>()
        attrs.put("state", state.toString())
        attrs.put("endStateDate", endStateDate)
        attrs.put("statusEntries","[Hello]")

        nodeMo = MoCreatorSpec.createNodeMo(nodeFdn, projectMo)
        MoCreatorSpec.createNodeStatusMo(nodeFdn, nodeMo, attrs)
    }
}
