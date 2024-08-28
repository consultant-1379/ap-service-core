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
package com.ericsson.oss.services.ap.core.rest.war.resource

import javax.inject.Inject

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef
import com.ericsson.oss.services.ap.api.AutoProvisioningService
import com.ericsson.oss.services.ap.api.exception.InvalidArgumentsException
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException
import com.ericsson.oss.services.ap.api.model.MoData
import com.ericsson.oss.services.ap.api.model.node.Node
import com.ericsson.oss.services.ap.api.status.NodeStatus
import com.ericsson.oss.services.ap.api.status.State
import com.ericsson.oss.services.ap.api.status.StatusEntry
import com.ericsson.oss.services.ap.api.status.StatusEntryNames
import com.ericsson.oss.services.ap.api.status.StatusEntryProgress
import com.ericsson.oss.services.ap.core.rest.builder.NodePropertiesBuilder
import com.ericsson.oss.services.ap.core.rest.builder.NodeStatusDataBuilder
import com.ericsson.oss.services.ap.core.rest.handlers.ArgumentResolver
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse
import com.ericsson.oss.services.ap.core.rest.model.NodeStatusData
import com.ericsson.oss.services.ap.core.rest.model.StatusEntryData
import com.ericsson.oss.services.ap.core.rest.model.nodeproperty.NodeProperties
import com.ericsson.oss.services.ap.core.rest.model.request.DeleteNodesRequest
import com.ericsson.oss.services.ap.core.rest.model.request.OrderNodesRequest
import com.ericsson.oss.services.ap.core.rest.war.response.ApResponseBuilder
import com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers.ExceptionMapperFactory
import com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers.NodeNotFoundExceptionMapper

class NodeResourceSpec extends CdiSpecification {

    private static final String NODE_FDN = "Project=TestProject,Node=TestNode"
    private static final String PROJECT_FDN = "Project=TestProject"
    private static final String PROJECT_NAME = "TestProject"
    private static final String NODE_NAME = "TestNode"
    private static final String IP_ADDRESS = "192.168.102.100"
    private static final String NODE_TYPE = "RadioNode"


    private static MoData nodeMoData
    private static List<Object> nodeAttributes
    private static List<StatusEntry> nodeStatusEntries
    private static StatusEntryData nodeStatusDataEntry
    private static NodeProperties nodeProperties = new NodeProperties()
    private static NodeStatus nodeStatus

    @ObjectUnderTest
    NodeResource nodeResource

    @Inject
    @EServiceRef(qualifier = "apcore")
    private AutoProvisioningService service

    @Inject
    private ApResponseBuilder apResponseBuilder

    @MockedImplementation
    private ExceptionMapperFactory exceptionMapperFactory

    @MockedImplementation
    private ArgumentResolver argumentResolver

    @MockedImplementation
    private NodePropertiesBuilder nodePropertiesBuilder

    def setupSpec() {
        final Map<String, Object> attributes = ["projectName" : PROJECT_NAME,
             "NodeId" : NODE_NAME,
             "ipAddress": IP_ADDRESS,
             "nodeType" : NODE_TYPE]

        nodeMoData = new MoData(NODE_FDN,
                attributes, NODE_TYPE, null)

        nodeAttributes = new ArrayList<>()
        nodeAttributes.add(nodeMoData)

        nodeProperties.attributes = nodeAttributes

        nodeStatusDataEntry = new StatusEntryData(StatusEntryNames.NODE_UP.toString(), StatusEntryProgress.WAITING.toString(), null, null)
        nodeStatusEntries = new ArrayList<StatusEntry>()
        nodeStatusEntries.add(new StatusEntry(StatusEntryNames.NODE_UP.toString(), StatusEntryProgress.WAITING.toString(), null, null))

        nodeStatus = new NodeStatus(NODE_NAME, PROJECT_NAME, nodeStatusEntries, State.ORDER_COMPLETED.toString())
    }

    def "Get a nodes properties successfully using the properties filter"() {

        given: "the node properties builder returns the requested node"
            nodePropertiesBuilder.buildNodeProperties(*_) >> nodeProperties
            nodeResource.nodePropertiesBuilder = nodePropertiesBuilder

        when: "the query node endpoint is called with the properties filter"
            def response = nodeResource.queryNode(PROJECT_NAME, NODE_NAME, "properties")

        then: "the status code should be 200"
            response.status == 200

        and: "the response entity should contain the node mo data"
            def node = ((MoData)response.entity.attributes[0])
            node == nodeMoData
    }

    def "Get a node status successfully using the status filter"() {

        given: "the node properties builder returns the requested node"
            nodeResource.nodeStatusDataBuilder = new NodeStatusDataBuilder()
            service.statusNode(NODE_FDN) >> nodeStatus

        when: "the query node endpoint is called with the properties filter"
            def response = nodeResource.queryNode(PROJECT_NAME, NODE_NAME, "status")

        then: "the status code should be 200"
            response.status == 200

        and: "the response entity should contain the node mo data"
            def nodeStatus = ((NodeStatusData)response.entity)
            nodeStatus.id == NODE_NAME
            nodeStatus.projectId == PROJECT_NAME
            StatusEntryData statusEntry = nodeStatus.statusEntries[0]
            statusEntry.task == nodeStatusDataEntry.task
            statusEntry.progress == nodeStatusDataEntry.progress
    }

    def "View a projects nodes successfully"() {

        given: "the view project usecase will return the correct mo data"
            nodeAttributes.add(0, new MoData(PROJECT_FDN, new HashMap<String, Object>(), null, null))
            service.viewProject(PROJECT_FDN) >> nodeAttributes

        when: "the view project nodes endpoint is called"
            def response = nodeResource.viewProjectNodes(PROJECT_NAME, PROJECT_FDN)

        then: "the status code should be 200"
            response.status == 200

        and: "the response entity should contain the node mo data"
            def expectedNode = new Node(NODE_NAME, NODE_TYPE, null, IP_ADDRESS, null, null, PROJECT_NAME)
            Node node = ((Node)response.entity.nodes[0])
            node.id == expectedNode.id
            node.nodeType == expectedNode.nodeType
            node.ipAddress == expectedNode.ipAddress
            node.parent == expectedNode.parent
    }

    def "Invalid arguments exception when an invalid query parameter is given"() {

        when: "the query node endpoint is called with an invalid value"
            nodeResource.queryNode(PROJECT_NAME, NODE_NAME, "status10")

        then: "Invalid arguments exception is thrown"
            thrown(InvalidArgumentsException.class)
    }

    def "Delete a single node successfully"() {

        when: "calling the delete rest endpoint to delete a single node"
            def response = nodeResource.deleteNodes("myProject",new DeleteNodesRequest(ignoreNetworkElement: false, nodeIds: ["myNode"]))

        then: "the corresponding service method should be called once"
            1 * service.deleteNode("Project=myProject,Node=myNode", false)

        and: "the status code should be 204"
            response.status == 204
    }

    def "Delete multiple nodes successfully"() {

        when: "calling delete rest endpoint to delete 3 nodes"
            def response = nodeResource.deleteNodes("myProject",new DeleteNodesRequest(ignoreNetworkElement: false, nodeIds: ["myID-1", "myID-2", "myID-3"]))

        then: "the corresponding service method is called once for each ID"
            1 * service.deleteNode("Project=myProject,Node=myID-1", false)
            1 * service.deleteNode("Project=myProject,Node=myID-2", false)
            1 * service.deleteNode("Project=myProject,Node=myID-3", false)

        and: "the status code should be 204"
            response.status == 204
    }

    def "Delete using duplicate node IDs"() {

        when: "calling delete rest endpoint to delete 2 nodes with the same IDs"
            def response = nodeResource.deleteNodes("myProject",new DeleteNodesRequest(ignoreNetworkElement: false, nodeIds: ["myID", "myID"]))

        then: "the corresponding service method is only called once"
            1 * service.deleteNode("Project=myProject,Node=myID", false)

        and: "the status code should be 204"
            response.status == 204
    }

    def "Delete using invalid IDs"() {

        given: "the exception mapper is mocked as @Any injection points are not supported by the test framework"
            exceptionMapperFactory.find(*_) >> new NodeNotFoundExceptionMapper()

        and: "the delete node service throws an exception when called"
            service.deleteNode(*_) >> { throw new NodeNotFoundException("Node not found.") }

        when: "the rest endpoint is called to delete a node"
            def response = nodeResource.deleteNodes("myProject", new DeleteNodesRequest(ignoreNetworkElement: false, nodeIds: ["invalid"]))

        then: "the status code should be 500"
            response.status == 500

        and: "the node ID and error message should be reported in the payload"
            response.entity?.size() == 1
            response.entity[0].id == "invalid"
            response.entity[0].errorMessage == "Node does not exist."
    }

    def "Delete valid and invalid nodes"() {

        given: "the exception mapper is mocked as @Any injection points are not supported by the test framework"
            exceptionMapperFactory.find(*_) >> new NodeNotFoundExceptionMapper()

        when: "the rest endpoint is called to delete valid and invalid nodes"
            def response = nodeResource.deleteNodes("myProject", new DeleteNodesRequest(ignoreNetworkElement: false, nodeIds: ["myID", "invalid"]))

        then: "the node with \"myID\" ID should be deleted successfully"
            1 * service.deleteNode("Project=myProject,Node=myID", false)

        and: "the node with \"invalid\" ID should fail"
            1 * service.deleteNode("Project=myProject,Node=invalid", false) >> { throw new NodeNotFoundException("Node not found.") }

        and: "the status code should be 500"
            response.status == 500

        and: "only the id \"invalid\" should be reported with error"
            response.entity?.size() == 1
            response.entity[0].id == "invalid"
            response.entity[0].errorMessage == "Node does not exist."
    }

    def "Ordering nodes successfully"() {

        when: "calling the order endpoint to order four nodes"
            def response = nodeResource.orderNodes(PROJECT_NAME, new OrderNodesRequest(nodeIds: ["node1", "node1", "node2", "node3"]))

        then: "the order node service use case should only be called three times, ignoring the duplicate node ID"
            1 * service.orderNode("Project=TestProject,Node=node1")
            1 * service.orderNode("Project=TestProject,Node=node2")
            1 * service.orderNode("Project=TestProject,Node=node3")

        and: "the HTTP Status should be 202 Accepted"
            response.status == 202

        and: "the response entity should not be returned"
            response.entity == null
    }

    def "Order valid and invalid nodes"() {

        given: "the service throws node not found Exception for the invalid node"
            ErrorResponse errorResponse = new ErrorResponse()
            errorResponse.setErrorTitle("Node does not exist.")
            apResponseBuilder.buildServiceError(_ as String, _ as Exception) >> errorResponse
            exceptionMapperFactory.find(_ as NodeNotFoundException) >> new NodeNotFoundExceptionMapper()
            argumentResolver.resolveFdn("Project=TestProject,Node=nodeNotFound", "ORDER_NODE") >> { throw new NodeNotFoundException("Node not found.") }

        when: "calling the order endpoint to order multiple valid and invalid nodes"
            def response = nodeResource.orderNodes(PROJECT_NAME, new OrderNodesRequest(nodeIds: ["validNode1", "nodeNotFound", "validNode2"]))

        then: "the valid nodes are ordered successfully"
            1 * service.orderNode("Project=TestProject,Node=validNode1")
            1 * service.orderNode("Project=TestProject,Node=validNode2")

        and: "the invalid node is reported in the response entity"
            response.entity?.size() == 1
            response.entity[0].nodeId == "nodeNotFound"
            response.entity[0].errorMessage == "Node does not exist."

        and: "the HTTP Status code is 207 Multi-Status"
            response.status == 207
    }

    def "Delete a node successfully using the singular endpoint"() {

        when: "the delete is called with a valid node"
            def response = nodeResource.deleteNode(PROJECT_NAME, NODE_NAME)

        then: "the corresponding service method should be called once"
            1 * service.deleteNode("Project=TestProject,Node=TestNode", false)

        and: "the status code should be 204"
            response.status == 204
    }

    def "Delete non existing node using singular endpoint"() {

         given: "the exception mapper is mocked as @Any injection points are not supported by the test framework"
             exceptionMapperFactory.find(*_) >> new NodeNotFoundExceptionMapper()

         and: "the delete node service throws an exception when called"
             service.deleteNode("Project=myProject,Node=NoNode", false) >> { throw new NodeNotFoundException("Node not found.") }

         when: "the rest endpoint is called to delete a node"
             def response = nodeResource.deleteNode("myProject", "NoNode")

         then: "the status code should be 404"
             response.status == 404

         and: "the error title and error message should be reported in the payload"
            response.entity.errorTitle == "Node does not exist."
            response.entity.errorBody == "Suggested Solution : Perform action with a valid node name."
      }
}
