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
package com.ericsson.oss.services.ap.core.usecase.hardwarereplace

import com.ericsson.oss.services.ap.common.test.util.assertions.CommonAssertionsSpec

import javax.inject.Inject

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService
import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.HierarchicalPrimaryTypeSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants

import com.ericsson.oss.services.ap.api.model.NodeTypeMapper
import com.ericsson.oss.services.ap.common.model.access.ModelReader
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo

class HardwareReplaceUtilSpec extends CdiSpecification {

    private static final String NODE_NAME = "HardwareReplaceNode"
    private static final String RADIO_NODE = "RadioNode"
    private static final String PICO_NODE = "MSRBS_V1"
    private static final String NODE_IDENTIFIER = "1998-184-092"
    private static final String HARDWARE_PROJECT_FDN = "Project=HardwareReplace"
    private static final String NODE_FDN = "Project=HardwareReplace,Node=HardwareReplaceNode"
    private static final String ECIM = "ecim"
    private static final String SECURITY = "Security"
    private static final String VERSION = "1.0.0"
    private static final String AP_ = "ap_"
    private static final String IP_ADDRESS = "ipAddress"
    private static final String CI_REF = "ciRef"
    private static final String SUBJECT_NAME = "SubjectName"
    private static final String SUBJECT_ALT_NAME = "SubjectAltName"
    private static final String SUBJECT_NAME_VALUE = "DNS:123456789X.Tmobile.US"
    private static final String SUBJECTALTNAME_VALUE = "123456789X.Tmobile.US"

    final Collection<PersistenceObject> ciRefAssociations = new ArrayList<>()

    @Inject
    private PersistenceObject persistenceObject

    @Inject
    private NodeTypeMapper nodeTypeMapper

    @ObjectUnderTest
    HardwareReplaceUtil hardwareReplaceUtil

    @MockedImplementation
    private ModelReader modelReader

    @MockedImplementation
    private HierarchicalPrimaryTypeSpecification primaryTypeSpecification

    @Inject
    private DataPersistenceService dps

    private RuntimeConfigurableDps runtimeConfigurableDps

    def setup() {
        runtimeConfigurableDps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        dps = runtimeConfigurableDps.build()
        MoCreatorSpec.setDps(runtimeConfigurableDps)
        MoCreatorSpec.createManagedElementMo(NODE_NAME, RADIO_NODE)
        MoCreatorSpec.createProjectMo(HARDWARE_PROJECT_FDN)
        final ManagedObject networkElement = MoCreatorSpec.createNetworkElementMo(NODE_NAME, RADIO_NODE, persistenceObject)
        final ManagedObject securityFunctionMo = MoCreatorSpec.createSecurityFunctionMo(NODE_NAME, networkElement)
        MoCreatorSpec.createNetworkElementSecurityMo(NODE_NAME, securityFunctionMo)
        retrieveConnectivityInformationIpAddress(networkElement)
        modelReader.getLatestPrimaryTypeSpecification(_ as String, SECURITY) >> primaryTypeSpecification
        primaryTypeSpecification.getModelInfo() >> (new ModelInfo(SchemaConstants.DPS_PRIMARYTYPE, AP_ + ECIM, SECURITY, VERSION))
    }

    def "when hardware replace radio node create is executed with security attributes then the AP Node MO and child MOs are created"() {
        given: "The Required MOs exist in the system to create the AP Node MO for a Radio Node"
            nodeTypeMapper.getNamespace(RADIO_NODE) >> ECIM
            NodeInfo nodeInfo = new NodeInfo()
            nodeInfo.setName(NODE_NAME)
            nodeInfo.setNodeType(RADIO_NODE)
            nodeInfo.setNodeIdentifier(NODE_IDENTIFIER)
            final Map<String, Object> nodeSecurityAttributes = new HashMap<>()
            nodeSecurityAttributes.put(SUBJECT_NAME, SUBJECT_NAME_VALUE)
            nodeSecurityAttributes.put(SUBJECT_ALT_NAME, SUBJECTALTNAME_VALUE)
            nodeInfo.setSecurityAttributes(nodeSecurityAttributes)

        when: "Creating the AP Node MO and child MOs as part of Hardware Replace for a Radio Node"
            hardwareReplaceUtil.create(HARDWARE_PROJECT_FDN, nodeInfo)

        then: "Assert that the Required MO and Security Attributes are created for Radio Node "
            CommonAssertionsSpec.assertMoCreated(runtimeConfigurableDps, NODE_FDN +",Security=1")
            CommonAssertionsSpec.assertAttributeForMo(runtimeConfigurableDps, NODE_FDN +",Security=1", SUBJECT_NAME, SUBJECT_NAME_VALUE)
            CommonAssertionsSpec.assertAttributeForMo(runtimeConfigurableDps, NODE_FDN +",Security=1", SUBJECT_ALT_NAME, SUBJECTALTNAME_VALUE)
    }

    def "when hardware replace pico create is executed with no security options then the AP Node MO and child MOs are created"() {
        given: "The Required MOs exist in the system to create the AP Node MO for a Pico Node"
            nodeTypeMapper.getNamespace(PICO_NODE) >> ECIM
            NodeInfo nodeInfo = new NodeInfo()
            nodeInfo.setName(NODE_NAME)
            nodeInfo.setNodeType(PICO_NODE)
            nodeInfo.setNodeIdentifier(NODE_IDENTIFIER)
            nodeInfo.setSecurityAttributes(Collections.emptyMap())

        when: "Creating the AP Node MO and child MOs as part of Hardware Replace for a Pico Node"
            hardwareReplaceUtil.create(HARDWARE_PROJECT_FDN, nodeInfo)

        then: "Assert that the Required MO and No Security Attributes are created for Radio Node"
            CommonAssertionsSpec.assertMoCreated(runtimeConfigurableDps, NODE_FDN +",Security=1")
            CommonAssertionsSpec.assertAttributeForMo(runtimeConfigurableDps, NODE_FDN +",Security=1", SUBJECT_NAME, null)
    }

    private void retrieveConnectivityInformationIpAddress(final ManagedObject networkElement){
        ciRefAssociations.add(persistenceObject)
        networkElement.getTarget().getAssociations(CI_REF) >> ciRefAssociations
        ciRefAssociations.iterator().next() >> persistenceObject
        persistenceObject.getAttribute(IP_ADDRESS) >> "1.1.1.1"
    }
}
