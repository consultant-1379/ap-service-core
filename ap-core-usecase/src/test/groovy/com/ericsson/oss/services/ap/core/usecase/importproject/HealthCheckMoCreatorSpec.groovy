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
package com.ericsson.oss.services.ap.core.usecase.importproject

import static com.ericsson.oss.services.ap.common.model.HealthCheckAttribute.HEALTH_CHECK_PROFILE_NAME
import static com.ericsson.oss.services.ap.common.model.HealthCheckAttribute.PRE_REPORT_IDS
import static com.ericsson.oss.services.ap.common.model.HealthCheckAttribute.POST_REPORT_IDS
import static com.ericsson.oss.services.ap.common.model.MoType.HEALTH_CHECK
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NodeDescriptorBuilder.createDefaultNode
import static com.ericsson.oss.services.ap.model.NodeType.RadioNode
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertNull

import org.mockito.internal.util.reflection.Whitebox

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
import com.ericsson.oss.services.ap.common.util.log.MRDefinition
import com.ericsson.oss.services.ap.common.util.log.MRExecutionRecorder

class HealthCheckMoCreatorSpec extends CdiSpecification {

    private static final String NODE_TYPE = "RadioNode"
    private static final String AP_NAMESPACE = "ap_ecim"
    private static final String VERSION = "1.0.0"

    @ObjectUnderTest
    private HealthCheckMoCreator healthCheckMoCreator

    @MockedImplementation
    private ModelReader modelReader

    @MockedImplementation
    private NodeTypeMapper nodeTypeMapper

    @MockedImplementation
    private ModelData healthCheckModelData

    @MockedImplementation
    private MRExecutionRecorder recorder

    final StubbedDpsGenerator dpsGenerator = new StubbedDpsGenerator()
    final DataPersistenceService stubbedDps = dpsGenerator.getStubbedDps()

    private NodeDescriptor nodeDescriptor

    def setup() {
        Whitebox.setInternalState(healthCheckMoCreator, "dps", stubbedDps)
        nodeDescriptor = createDefaultNode(RadioNode).build()

        nodeTypeMapper.getNamespace(NODE_TYPE) >> AP_NAMESPACE
        modelReader.getLatestPrimaryTypeModel(AP_NAMESPACE, HEALTH_CHECK.toString()) >> healthCheckModelData

        healthCheckModelData.getVersion() >> VERSION
        healthCheckModelData.getNameSpace() >> AP_NAMESPACE
    }

    def "when HealthCheck Mo created successfully THEN the Mo should exist in system" () {
        given: "a new MO is created"
            final ManagedObject nodeMo = dpsGenerator.generate(nodeDescriptor)

        and: "a NodeInfo with a HealthCheck profile is provided"
            final Map<String, Object> healthCheckAttributes = new HashMap<>()
            healthCheckAttributes.put(HEALTH_CHECK_PROFILE_NAME.getAttributeName(), "HealthCheckProfile1")
            healthCheckAttributes.put(PRE_REPORT_IDS.getAttributeName(), Collections.emptyList())
            healthCheckAttributes.put(POST_REPORT_IDS.getAttributeName(), Collections.emptyList())
            final NodeInfo nodeInfo = createNodeInfo(healthCheckAttributes)

        when: "create HealthCheck Mo for the node"
            final ManagedObject createdMo = healthCheckMoCreator.create(nodeMo, nodeInfo)

        then: "the HealthCheck Mo should exist in the system"
            assertNotNull(createdMo)
            assertNotNull(getHealthCheckFdn(nodeMo.getFdn()), createdMo.getFdn())

        and: "the MRid is logged to DDP"
            1 * recorder.recordMRExecution(MRDefinition.AP_EXPANSION_HEALTHCHECK)
    }

    def "when healthCheck attributes is NOT set in nodeInfo THEN HealthCheck Mo will be not created" () {
        given: "a new created node Mo"
            final ManagedObject nodeMo = dpsGenerator.generate(nodeDescriptor)

        and: "a NodeInfo with empty HealthCheck attributes"
            final Map<String, Object> healthCheckAttributes = new HashMap<>()
            final NodeInfo nodeInfo = createNodeInfo(healthCheckAttributes)

        when: "create HealthCheck Mo for the node"
            final ManagedObject createdMo = healthCheckMoCreator.create(nodeMo, nodeInfo)

        then: "the HealthCheck Mo should NOT exist in the system"
            assertNull(createdMo)

        and: "the MRid is NOT logged to DDP"
            0 * recorder.recordMRExecution(MRDefinition.AP_EXPANSION_HEALTHCHECK)
    }

    private NodeInfo createNodeInfo(final Map<String, Object> healthCheckAttributes) {
        final NodeInfo nodeInfo = new NodeInfo()
        nodeInfo.setNodeType(RadioNode.toString())
        nodeInfo.setHealthCheckAttributes(healthCheckAttributes)
        return nodeInfo
    }

    private String getHealthCheckFdn(final String nodeFdn){
        return nodeFdn + "," + HEALTH_CHECK.toString() + "=1"
    }
}
