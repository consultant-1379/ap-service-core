/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.migration

import com.ericsson.oss.services.ap.common.test.util.assertions.CommonAssertionsSpec

import javax.inject.Inject
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps

import com.ericsson.oss.services.ap.api.model.NodeTypeMapper
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo
class MigrationUtilSpec extends CdiSpecification {

    private static final String NODE_NAME = "MigrationNode"
    private static final String RADIO_NODE = "RadioNode"
    private static final String MIGRATION_PROJECT_FDN = "Project=Migration"
    private static final String ECIM = "ecim"
    private static final String IP_ADDRESS = "ipAddress"
    private static final String CI_REF = "ciRef"
    private static final String IS_NODE_MIGRATION = "isNodeMigration"

    final Collection<PersistenceObject> ciRefAssociations = new ArrayList<>()

    @Inject
    private PersistenceObject persistenceObject

    @Inject
    private NodeTypeMapper nodeTypeMapper

    @ObjectUnderTest
    MigrationUtil migrationUtil

    @Inject
    private DataPersistenceService dps

    private RuntimeConfigurableDps runtimeConfigurableDps

    def setup() {
        runtimeConfigurableDps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        dps = runtimeConfigurableDps.build()
        MoCreatorSpec.setDps(runtimeConfigurableDps)
        MoCreatorSpec.createManagedElementMo(NODE_NAME, RADIO_NODE)
        MoCreatorSpec.createProjectMo(MIGRATION_PROJECT_FDN)
        final ManagedObject networkElement = MoCreatorSpec.createNetworkElementMo(NODE_NAME, RADIO_NODE, persistenceObject)
        final ManagedObject securityFunctionMo = MoCreatorSpec.createSecurityFunctionMo(NODE_NAME, networkElement)
        MoCreatorSpec.createNetworkElementSecurityMo(NODE_NAME, securityFunctionMo)
        retrieveConnectivityInformationIpAddress(networkElement)
    }

    def "when migration radio node create is executed then the AP Node MO and child MOs are created"() {

        given: "The Required MOs exist in the system to create the AP Node MO for a Radio Node"

        nodeTypeMapper.getNamespace(RADIO_NODE) >> ECIM
        NodeInfo nodeInfo = new NodeInfo()
        final Map<String, Object> nodeAttributes = new HashMap<>()
        nodeAttributes.put(IP_ADDRESS, "12345678")
		nodeAttributes.put(IS_NODE_MIGRATION, Boolean.TRUE)
        nodeInfo.setNodeAttributes(nodeAttributes)
        nodeInfo.setName(NODE_NAME)
        nodeInfo.setNodeType(RADIO_NODE)

        when: "Creating the AP Node MO and child MOs as part of Migration for a Radio Node"

        migrationUtil.create(nodeInfo)

        then: "Assert that the Required MO and No Security Attributes are created for Radio Node"

        CommonAssertionsSpec.assertMoCreated(runtimeConfigurableDps, MIGRATION_PROJECT_FDN)
    }

    private void retrieveConnectivityInformationIpAddress(final ManagedObject networkElement){
        ciRefAssociations.add(persistenceObject)
        networkElement.getTarget().getAssociations(CI_REF) >> ciRefAssociations
        ciRefAssociations.iterator().next() >> persistenceObject
        persistenceObject.getAttribute(IP_ADDRESS) >> "1.1.1.1"
    }
}
