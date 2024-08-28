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
 -----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.status

import spock.lang.Shared
import spock.lang.Specification

/**
 * Unit tests for {@link ApNodeGroupStatus}.
 */
class ApNodeGroupStatusSpec extends Specification {

    private static final String PROJECT_NAME = "Project1"
    private static final List<StatusEntry> emptyList = []

    @Shared List<NodeStatus> inputNodeStatus

    def setup() {
        inputNodeStatus = new ArrayList()
    }

    def "When getting node statuses, nodes should be ordered alphabetically"() {
        setup:
            final NodeStatus nodeStatus2 = new NodeStatus("Node2", PROJECT_NAME, emptyList, "READY_FOR_ORDER")
            final NodeStatus nodeStatus1 = new NodeStatus("Node1", PROJECT_NAME, emptyList, "READY_FOR_ORDER")
        and: "Node2 is added to the list before Node1"
            inputNodeStatus << nodeStatus2 << nodeStatus1

        when:
            final ApNodeGroupStatus projectStatus = ApNodeGroupStatus.getProjectApNodeGroupStatus(PROJECT_NAME,inputNodeStatus)
            final List<NodeStatus> nodeStatuses = projectStatus.getNodesStatus()

        then: "Node1 is before Node2 in the list"
            nodeStatuses == [nodeStatus1, nodeStatus2]
    }

    def 'when get integration phase summary then correct number of nodes should be in each phase'() {
        setup:
            addNodesToNodeStatus("READY_FOR_ORDER", "ORDER_COMPLETED", "ORDER_FAILED", "INTEGRATION_COMPLETED", "INTEGRATION_COMPLETED")

        when:
            final ApNodeGroupStatus projectStatus = ApNodeGroupStatus.getProjectApNodeGroupStatus(PROJECT_NAME,inputNodeStatus)
            final Map<IntegrationPhase, Integer> integrationSummary = projectStatus.getIntegrationPhaseSummary()

        then:
            integrationSummary.get(phase).intValue() == result

        where:
            phase                           | result
            IntegrationPhase.IN_PROGRESS    | 2
            IntegrationPhase.SUCCESSFUL     | 2
            IntegrationPhase.FAILED         | 1
            IntegrationPhase.CANCELLED      | 0
    }

    def 'when get node quantity then correct quantity is returned'() {
        given:
            final NodeStatus defaultNodeStatus = new NodeStatus('Node1',PROJECT_NAME,emptyList,'READY_FOR_ORDER')

        when:
            inputNodeStatus << defaultNodeStatus << defaultNodeStatus
            final ApNodeGroupStatus projectStatus=ApNodeGroupStatus.getProjectApNodeGroupStatus(PROJECT_NAME,inputNodeStatus)

        then:
            projectStatus.getNumberOfNodes() == 2
    }

    def 'When getting project name, then project name is returned'() {
        given:
            final NodeStatus defaultNodeStatus = new NodeStatus('Node1',PROJECT_NAME,emptyList,'READY_FOR_ORDER')

        when:
            inputNodeStatus.add(defaultNodeStatus)
            final ApNodeGroupStatus projectStatus = ApNodeGroupStatus.getProjectApNodeGroupStatus(PROJECT_NAME,inputNodeStatus)

        then:
            projectStatus.getApNodeGroupName() == PROJECT_NAME
    }

    def addNodesToNodeStatus(String[] states) {
        for (int num = 0; num < states.length; num++) {
            inputNodeStatus << new NodeStatus("Node${num}", PROJECT_NAME, Collections.<StatusEntry> emptyList(), "${states[num]}")
        }
    }
}