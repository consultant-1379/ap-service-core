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

package com.ericsson.oss.services.ap.core.usecase.importproject

import static com.ericsson.oss.services.ap.common.model.ControllingNodesAttribute.CONTROLLING_BSC
import static com.ericsson.oss.services.ap.common.model.ControllingNodesAttribute.CONTROLLING_RNC
import static com.ericsson.oss.services.ap.common.model.MoType.CONTROLLING_NODES
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NodeDescriptorBuilder.createDefaultNode
import static com.ericsson.oss.services.ap.model.NodeType.RadioNode
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull

import org.mockito.internal.util.reflection.Whitebox;

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.services.ap.api.model.ModelData
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper
import com.ericsson.oss.services.ap.common.model.access.ModelReader
import com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor
import com.ericsson.oss.services.ap.common.test.stubs.dps.StubbedDpsGenerator

class ControllingNodesMoCreatorSpec extends CdiSpecification {

    private static final String NODE_TYPE = "RadioNode";
    private static final String AP_NAMESPACE = "ap_ecim";
    private static final String VERSION = "1.0.0";

    @ObjectUnderTest
    private ControllingNodesMoCreator controllingNodesMoCreator

    @MockedImplementation
    private ModelReader modelReader;

    @MockedImplementation
    private NodeTypeMapper nodeTypeMapper;

    @MockedImplementation
    private ModelData controllingNodesModelData;

    final StubbedDpsGenerator dpsGenerator = new StubbedDpsGenerator()
    final DataPersistenceService stubbedDps = dpsGenerator.getStubbedDps()

    private NodeDescriptor nodeDescriptor

    def setup() {
        Whitebox.setInternalState(controllingNodesMoCreator, "dps", stubbedDps)
        nodeDescriptor = createDefaultNode(RadioNode).build();

        nodeTypeMapper.getNamespace(NODE_TYPE) >> AP_NAMESPACE
        modelReader.getLatestPrimaryTypeModel(AP_NAMESPACE, CONTROLLING_NODES.toString()) >> controllingNodesModelData

        controllingNodesModelData.getVersion() >> VERSION
        controllingNodesModelData.getNameSpace() >> AP_NAMESPACE
    }

    def "when ControllingNodes Mo created successfully THEN the Mo should exist in system" () {
        given: "a new created node Mo"
        final ManagedObject nodeMo = dpsGenerator.generate(nodeDescriptor)

        and: "a NodeInfo with ControllingNodes attributes"
        final Map<String, Object> controllingNodesAttributes = new HashMap<>()
        controllingNodesAttributes.put(CONTROLLING_BSC.getAttributeName(), "NetworkElement=bsc")
        controllingNodesAttributes.put(CONTROLLING_RNC.getAttributeName(), "NetworkElement=rnc")
        final NodeInfo nodeInfo = createNodeInfo(controllingNodesAttributes)

        when: "create ControllingNodes Mo for the node"
        final ManagedObject createdMo = controllingNodesMoCreator.create(nodeMo, nodeInfo)

        then: "the ControllingNodes Mo should exist in the system"
        assertNotNull(createdMo)
        assertNotNull(getControllingNodesFdn(nodeMo.getFdn()), createdMo.getFdn())
    }

    def "when controllingNodes attributes is NOT set in nodeInfo THEN ControllingNodes Mo will be not created" () {
        given: "a new created node Mo"
        final ManagedObject nodeMo = dpsGenerator.generate(nodeDescriptor)

        and: "a NodeInfo with empty ControllingNodes attributes"
        final Map<String, Object> controllingNodesAttributes = new HashMap<>()
        final NodeInfo nodeInfo = createNodeInfo(controllingNodesAttributes)

        when: "create ControllingNodes Mo for the node"
        final ManagedObject createdMo = controllingNodesMoCreator.create(nodeMo, nodeInfo)

        then: "the ControllingNodes Mo should NOT exist in the system"
        assertNull(createdMo)
    }

    private NodeInfo createNodeInfo(final Map<String, Object> controllingNodesAttributes) {
        final NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setNodeType(RadioNode.toString());
        nodeInfo.setControllingNodesAttributes(controllingNodesAttributes);
        return nodeInfo;
    }

    private String getControllingNodesFdn(final String nodeFdn){
        return nodeFdn + "," + CONTROLLING_NODES.toString() + "=1"
    }
}