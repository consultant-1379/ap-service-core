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
package com.ericsson.oss.services.ap.core.usecase

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN

import java.util.concurrent.Callable

import javax.inject.Inject

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderSPI
import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal
import com.ericsson.oss.services.ap.common.artifacts.generated.GeneratedArtifactHandler
import com.ericsson.oss.services.ap.common.artifacts.raw.RawArtifactHandler
import com.ericsson.oss.services.ap.common.cm.TransactionalExecutor
import com.ericsson.oss.services.ap.common.model.NodeAttribute
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables
import com.ericsson.oss.services.ap.core.rest.client.healthcheck.HealthCheckRestExecutor
import com.ericsson.oss.services.ap.core.usecase.delete.DeleteNodeWorkflowHelper
import com.ericsson.oss.services.ap.core.usecase.testutils.SimpleTestMosFactory
import com.ericsson.oss.services.ap.core.usecase.testutils.TestPersistenceService

class DeleteNodeUseCaseSpec extends CdiSpecification {

    @ObjectUnderTest
    private DeleteNodeUseCase deleteNodeUseCase

    @MockedImplementation
    private NodeTypeMapper nodeTypeMapper

    @MockedImplementation
    private DeleteNodeWorkflowHelper workflowHelper

    @MockedImplementation
    private StateTransitionManagerLocal nodeStateTransitionManager

    @MockedImplementation
    private RawArtifactHandler rawArtifactHandler

    @MockedImplementation
    private GeneratedArtifactHandler generatedArtifactHandler

    @MockedImplementation
    private TransactionalExecutor executor

    @MockedImplementation
    private HealthCheckRestExecutor healthCheckExecutor;

    @Inject
    private TestPersistenceService testPersistenceService

    @Inject
    private SimpleTestMosFactory simpleTestMosFactory

    ManagedObject projectManagedObject = null
    ManagedObject nodeManagedObject = null

    @MockedImplementation
    ServiceFinderSPI serviceFinderSPI

    def setup() {
        ServiceFinderBean.serviceFinder = this.serviceFinderSPI
        serviceFinderSPI.find(StateTransitionManagerLocal.class, null) >> nodeStateTransitionManager
        deleteNodeUseCase.init()
        executor.execute(_) >> {
            args -> ((Callable) args[0]).call()
        }

        testPersistenceService.setupPersistence(cdiInjectorRule.getService(RuntimeConfigurableDps), executor)
        projectManagedObject = simpleTestMosFactory.newProject()
    }

    def "when delete skips delete NetworkElement, AP node will be deleted but delete workflow won't be executed"() {

        given: "a node is in skip delete workflow state"
            nodeManagedObject = simpleTestMosFactory.newTestNode(projectManagedObject, Collections.emptyMap())
            testPersistenceService.createNodeStatus(nodeManagedObject, state)
            testPersistenceService.createHealthCheck(nodeManagedObject)

        when: "when delete node usecase is executed"
            deleteNodeUseCase.execute(NODE_FDN, false)

        then: "ap node Mo's are deleted but delete workflow is not executed"
            0 * workflowHelper.cancelOrderWorkflowWithRetries(NODE_FDN)
            0 * workflowHelper.executeDeleteWorkflow(NODE_FDN, false, _ as String)
            1 * healthCheckExecutor.deleteGeneratedReports(_ as String)
            1 * generatedArtifactHandler.deleteAllForProjectWithNoModelUpdate(_ as String);
            1 * rawArtifactHandler.deleteAllForProjectWithNoModelUpdate(_ as String);
            testPersistenceService.findByFdn(NODE_FDN) == null

        where:
            state                                      | _
            "EXPANSION_CANCELLED"                      | _
            "EXPANSION_COMPLETED"                      | _
            "EXPANSION_FAILED"                         | _
            "EXPANSION_STARTED"                        | _
            "EXPANSION_SUSPENDED"                      | _
            "EXPANSION_IMPORT_CONFIGURATION_SUSPENDED" | _
            "HARDWARE_REPLACE_COMPLETED"               | _
            "HARDWARE_REPLACE_FAILED"                  | _
            "INTEGRATION_COMPLETED"                    | _
            "INTEGRATION_CANCELLED"                    | _
            "INTEGRATION_COMPLETED_WITH_WARNING"       | _
            "ORDER_FAILED"                             | _
            "ORDER_CANCELLED"                          | _
            "READY_FOR_EXPANSION"                      | _
    }

    def "when non-vnf node is NOT in skip delete workflow state, AP node will be deleted and delete workflow will be executed"() {

        given: "a RadioNode node"
            def nodeProps = new HashMap()
            nodeProps.put(NodeAttribute.NODE_TYPE.toString(), "RadioNode")
            nodeManagedObject = simpleTestMosFactory.newTestNode(projectManagedObject, nodeProps)
            nodeTypeMapper.getInternalEjbQualifier("RadioNode") >> "ecim"

        and: "the node is not in delete workflow state"
            testPersistenceService.createNodeStatus(nodeManagedObject, state)

        and: "get dhcpClientIdToRemove variable from active workflow"
            workflowHelper.getWorkflowVariable(NODE_FDN, AbstractWorkflowVariables.DHCP_CLIENT_ID_TO_REMOVE_KEY) >> dhcpClientId

        when: "when delete node usecase is executed"
            deleteNodeUseCase.execute(NODE_FDN, false)

        then: "ap node is deleted and delete workflow is also executed"
            1 * workflowHelper.cancelOrderWorkflowWithRetries(NODE_FDN)
            1 * workflowHelper.executeDeleteWorkflow(NODE_FDN, false, dhcpClientId)
            testPersistenceService.findByFdn(NODE_FDN) == null

        where:
            state                   | dhcpClientId
            "ORDER_COMPLETED"       | null
            "ORDER_ROLLBACK_FAILED" | null
            "ORDER_STARTED"         | null
            "ORDER_SUSPENDED"       | null
            "READY_FOR_ORDER"       | null
            "ORDER_COMPLETED"       | "ABC1234567"
            "ORDER_ROLLBACK_FAILED" | "ABC1234567"
            "ORDER_STARTED"         | "ABC1234567"
            "ORDER_SUSPENDED"       | "ABC1234567"
            "READY_FOR_ORDER"       | "ABC1234567"
    }

    def "when vnf node is NOT in skip delete workflow state, AP node will NOT be deleted and delete workflow will be executed"() {

        given: "a vPP node"
            def nodeProps = new HashMap()
            nodeProps.put(NodeAttribute.NODE_TYPE.toString(), "vPP")
            nodeManagedObject = simpleTestMosFactory.newTestNode(projectManagedObject, nodeProps)

            nodeTypeMapper.getInternalEjbQualifier("vPP") >> "vnf"

        and: "the node is not in delete workflow state"
            testPersistenceService.createNodeStatus(nodeManagedObject, state)

        and: "get dhcpClientIdToRemove variable from active workflow"
            workflowHelper.getWorkflowVariable(NODE_FDN, AbstractWorkflowVariables.DHCP_CLIENT_ID_TO_REMOVE_KEY) >> dhcpClientId

        when: "when delete node usecase is executed"
            deleteNodeUseCase.execute(NODE_FDN, false)

        then: "ap node is not deleted but delete workflow is executed"
            1 * workflowHelper.cancelOrderWorkflowWithRetries(NODE_FDN)
            1 * workflowHelper.executeDeleteWorkflow(NODE_FDN, false, dhcpClientId)
            testPersistenceService.findByFdn(NODE_FDN) != null

        where:
            state                   | dhcpClientId
            "ORDER_COMPLETED"       | null
            "ORDER_ROLLBACK_FAILED" | null
            "ORDER_STARTED"         | null
            "ORDER_SUSPENDED"       | null
            "READY_FOR_ORDER"       | null
            "ORDER_COMPLETED"       | "ABC1234567"
            "ORDER_ROLLBACK_FAILED" | "ABC1234567"
            "ORDER_STARTED"         | "ABC1234567"
            "ORDER_SUSPENDED"       | "ABC1234567"
            "READY_FOR_ORDER"       | "ABC1234567"
    }

    def "when deleting a node which is not exists then NodeNotFoundException exception is thrown"() {

        given: "node not exist"

        when: "when delete node usecase is executed"
            deleteNodeUseCase.execute("BAD_NODE_FDN", false)

        then: "NodeNotFoundException is thrown"
            thrown(NodeNotFoundException)
    }

    def "when node is not in valid state then delete fails with exception"() {
        given: "setup a invalid node state"
            nodeManagedObject = simpleTestMosFactory.newTestNode(projectManagedObject, Collections.emptyMap())

        and: "an invalid node state for delete use case"
            testPersistenceService.createNodeStatus(nodeManagedObject, "BadStatus")
            nodeStateTransitionManager.validateAndSetNextState(NODE_FDN, StateTransitionEvent.DELETE_STARTED) >> {
                throw new InvalidNodeStateException("Invalid State", "DummyState")
            }

        when: "when delete node usecase is executed"
            deleteNodeUseCase.execute(NODE_FDN, false)

        then: "InvalidNodeStateException is thrown"
            thrown(InvalidNodeStateException)
    }
}
