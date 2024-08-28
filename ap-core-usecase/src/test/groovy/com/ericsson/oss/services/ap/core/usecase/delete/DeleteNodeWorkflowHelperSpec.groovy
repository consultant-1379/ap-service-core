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
package com.ericsson.oss.services.ap.core.usecase.delete

import static com.ericsson.oss.services.ap.common.model.MoType.NODE
import static com.ericsson.oss.services.ap.common.model.MoType.PROJECT
import static com.ericsson.oss.services.ap.common.model.Namespace.AP
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN

import org.mockito.internal.util.reflection.Whitebox

import javax.inject.Inject

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.services.ap.core.usecase.workflow.WorkflowCleanUpOperations
import com.ericsson.oss.services.ap.core.usecase.workflow.WorkflowOperations

/**
 * Unit tests of {@link DeleteNodeWorkflowHelper}.
 */
class DeleteNodeWorkflowHelperSpec extends CdiSpecification {

    @ObjectUnderTest
    private DeleteNodeWorkflowHelper deleteNodeWorkflowHelper

    @Inject
    private PersistenceObject persistanceObject

    RuntimeConfigurableDps dps

    @Inject
    private DpsOperations dpsOperations

    @MockedImplementation
    WorkflowOperations workflowOperations

    @MockedImplementation
    private WorkflowCleanUpOperations workflowCleanUpOperations;


    void setup() {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        Whitebox.setInternalState(dpsOperations, "dps", dps.build())
    }

    def "when ap delete executed for a node for which CmNodeHeartbeatSupervision.active is true, then executeDeleteWorkflow is executed with ignoreNetworkElement as true"() {
        given: "NetworkElement exists with CmNodeHeartbeatSupervision active as true"
        final ManagedObject apProjectMo = addProjectMo(PROJECT_FDN)
        addNodeMo(NODE_FDN, apProjectMo)
        final ManagedObject networkElement = createNetworkElementMo(NODE_NAME)
        createCmNodeHeartbeatSupervisionMo(NODE_NAME, networkElement, true)

        when: "DeleteNodeWorkflowHelper.executeDeleteWorkflow is called"
        deleteNodeWorkflowHelper.executeDeleteWorkflow(NODE_FDN, false, null)

        then: "workflowOperations.executeDeleteWorkflow will be called with ignoreNetworkElement as true"
        1 * workflowOperations.executeDeleteWorkflow(NODE_FDN, true, null) >> true
    }

    def "when ap delete executed for a node for which CmNodeHeartbeatSupervision.active is false, then executeDeleteWorkflow is executed with ignoreNetworkElement as false"() {
        given: "NetworkElement exists with CmNodeHeartbeatSupervision active as false"
        final ManagedObject apProjectMo = addProjectMo(PROJECT_FDN)
        addNodeMo(NODE_FDN, apProjectMo)
        final ManagedObject networkElement = createNetworkElementMo(NODE_NAME)
        createCmNodeHeartbeatSupervisionMo(NODE_NAME, networkElement, false)

        when: "DeleteNodeWorkflowHelper.executeDeleteWorkflow is called"
        deleteNodeWorkflowHelper.executeDeleteWorkflow(NODE_FDN, false, null)

        then: "workflowOperations.executeDeleteWorkflow will be called with ignoreNetworkElement as false"
        1 * workflowOperations.executeDeleteWorkflow(NODE_FDN, false, null) >> true
    }

    def "when ap delete executed for a node for which CmNodeHeartbeatSupervision null, then executeDeleteWorkflow is executed with ignoreNetworkElement as false"() {
        given: "NetworkElement exists but not CmNodeHeartbeatSupervision"
        final ManagedObject apProjectMo = addProjectMo(PROJECT_FDN)
        addNodeMo(NODE_FDN, apProjectMo)
        createNetworkElementMo(NODE_NAME)

        when: "DeleteNodeWorkflowHelper.executeDeleteWorkflow is called"
        deleteNodeWorkflowHelper.executeDeleteWorkflow(NODE_FDN, false, null)

        then: "workflowOperations.executeDeleteWorkflow will be called with ignoreNetworkElement as false"
        1 * workflowOperations.executeDeleteWorkflow(NODE_FDN, false, null) >> true
    }

    def "when ap delete executed for a node for which NetworkElement does not exists, then executeDeleteWorkflow is executed with ignoreNetworkElement as false"() {
        given: "AP node exists but no NetworkElement"
        final ManagedObject apProjectMo = addProjectMo(PROJECT_FDN)
        addNodeMo(NODE_FDN, apProjectMo)

        when: "DeleteNodeWorkflowHelper.executeDeleteWorkflow is called"
        deleteNodeWorkflowHelper.executeDeleteWorkflow(NODE_FDN, false, null)

        then: "workflowOperations.executeDeleteWorkflow will be called with ignoreNetworkElement as false"
        1 * workflowOperations.executeDeleteWorkflow(NODE_FDN, false, null) >> true
    }

    private ManagedObject createNetworkElementMo(final String nodeName) {
        final Map<String, Object> networkElementAttributes = new HashMap<>()
        networkElementAttributes.put("networkElementId", nodeName)
        networkElementAttributes.put("neType", "RadioNode")
        networkElementAttributes.put("ossPrefix", "")
        networkElementAttributes.put("ossModelIdentity", "1998-184-092")

        return dps.addManagedObject()
                .withFdn(getNetworkElementFdn(nodeName))
                .type("NetworkElement")
                .namespace("OSS_NE_DEF")
                .version("2.0.0")
                .target(persistanceObject)
                .name(nodeName)
                .addAttributes(networkElementAttributes)
                .build()
    }

    private ManagedObject createCmNodeHeartbeatSupervisionMo(final String nodeName, final ManagedObject parentMo, final boolean active) {
        final Map<String, Object> attributes = new HashMap<>()
        attributes.put("active", active)

        return dps.addManagedObject()
                .withFdn("NetworkElement=" + nodeName + ",CmNodeHeartbeatSupervision=1")
                .type("CmNodeHeartbeatSupervision")
                .version("3.0.0")
                .parent(parentMo)
                .name("1")
                .addAttributes(attributes)
                .build()
    }

    private ManagedObject addProjectMo(final String fdn) {
        final Map<String, Object> projectAttributes = new HashMap<String, Object>()
        return dps.addManagedObject()
                .withFdn(fdn)
                .type(PROJECT.toString())
                .namespace(AP.toString())
                .version("1.0.0")
                .addAttributes(projectAttributes)
                .build()
    }

    private ManagedObject addNodeMo(final String fdn, final ManagedObject parentMo) {
        return dps.addManagedObject()
                .withFdn(fdn)
                .type(NODE.toString())
                .namespace(AP.toString())
                .version("2.0.0")
                .parent(parentMo)
                .build()
    }

    private static String getNetworkElementFdn(final String nodeName) {
        return "NetworkElement=" + nodeName
    }
}
