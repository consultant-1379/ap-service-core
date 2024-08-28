/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.response

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.status.ApNodeGroupStatus
import com.ericsson.oss.services.ap.api.status.NodeStatus
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseStatus

class StatusResponseDtoBuilderSpec extends CdiSpecification {

    ApNodeGroupStatus deploymentStatus
    List<NodeStatus> nodeStatusList

    @ObjectUnderTest
    private StatusResponseDtoBuilder statusResponseDtoBuilder

    def "succesful response returned when deployment status contains at least one node"() {

        given: "deployment status with at least one node"
            nodeStatusList = new ArrayList<>();
            NodeStatus nodeStatus = new NodeStatus("Node1", "Project1", null, "ORDER_COMPLETED")
            nodeStatusList.add(nodeStatus)
            deploymentStatus = ApNodeGroupStatus.getDeploymentApNodeGroupStatus("deployment1", nodeStatusList)
        when: "command response is created"
            CommandResponseDto response = statusResponseDtoBuilder.buildViewDeploymentStatusCommandResponseDto(deploymentStatus, "command")
        then: "response is successful"
            ResponseStatus.SUCCESS == response.getStatusCode()
    }

    def "error response returned when deployment status contains zero nodes"() {

        given: "deployment status with zero nodes"
            deploymentStatus = ApNodeGroupStatus.getDeploymentApNodeGroupStatus("deployment1", Collections.EMPTY_LIST)
        when: "command response is created"
            CommandResponseDto response = statusResponseDtoBuilder.buildViewDeploymentStatusCommandResponseDto(deploymentStatus, "command")
        then: "response is error"
            ResponseStatus.COMMAND_EXECUTION_ERROR == response.getStatusCode()
    }

    def "successful response returned with one node in suspened state when project status contains one node in state ORDER_SUSPENDED"() {

        given: "deployment status with at least one node"
            nodeStatusList = new ArrayList<>();
            NodeStatus nodeStatus = new NodeStatus("Node1", "Project1", null, STATE)
            nodeStatusList.add(nodeStatus)
            deploymentStatus = ApNodeGroupStatus.getDeploymentApNodeGroupStatus("Project1", nodeStatusList)
        when: "command response is created"
            CommandResponseDto response = statusResponseDtoBuilder.buildViewDeploymentStatusCommandResponseDto(deploymentStatus, "command")
            String suspendedColumnName = response.getResponseDto().getElements().get(0).getElements().get(3).getValue()
            String suspendedNumber = response.getResponseDto().getElements().get(1).getElements().get(3).getValue()
        then: "response is successful"
            ResponseStatus.SUCCESS == response.getStatusCode()
            suspendedColumnName == "Suspended"
            suspendedNumber == SUSPENDED_NUMBER
        where:
            STATE                        || SUSPENDED_NUMBER
            "ORDER_SUSPENDED"            || "1"
            "HARDWARE_REPLACE_SUSPENDED" || "1"
            "INTEGRATION_SUSPENDED"      || "1"
            "EXPANSION_SUSPENDED"        || "1"
            "INTEGRATION_COMPLETED"      || "0"
    }
}
