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
package com.ericsson.oss.services.ap.common.cm.snmp

import java.util.concurrent.Callable

import javax.inject.Inject

import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.nms.security.nscs.api.enums.SnmpSecurityLevel
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.services.ap.common.cm.TransactionalExecutor
import com.ericsson.oss.services.ap.common.cm.UserOperations
import com.ericsson.oss.services.ap.common.model.NodeAttribute
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec

/**
 * SnmpUserUpdaterSpec is a test class for {@link SnmpUserUpdater}
 */
class SnmpUserUpdaterSpec extends CdiSpecification{

    private static final String PROJECT_FDN = "Project=Project1"
    private static final String NODE_FDN = "Project=Project1,Node=Node1"
    private static final String SNMP_USER_VALUE = "user"
    private static final String UNDEFINED = "UNDEFINED"
    private static final String ECIMUSER = "ECIMUser"

    @ObjectUnderTest
    private SnmpUserUpdater userUpdater

    @Inject
    private RuntimeConfigurableDps dps

    @Inject
    private DataPersistenceService dataPersistenceService

    @Inject
    private DpsOperations dpsOperations

    @MockedImplementation
    private UserOperations userOperations

    @MockedImplementation
    private SnmpSecurityData snmpSecurityData

    @MockedImplementation
    private TransactionalExecutor executor

    private ManagedObject projectMo

    private Map<String, Object> nodeAttributes = new HashMap<String, Object>()

    def setup() {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        dataPersistenceService = dps.build()
        dpsOperations.getDataPersistenceService() >> dataPersistenceService
        executor.execute(_ as Callable) >> { Callable call -> call.call() }
        Whitebox.setInternalState(dpsOperations, "dps", dps.build())
        Whitebox.setInternalState(dpsOperations, "executor", executor)
        MoCreatorSpec.setDps(dps)
        projectMo = MoCreatorSpec.createProjectMo(PROJECT_FDN)
        nodeAttributes.clear()
        userOperations.getNodeSnmpInitSecurityData() >> snmpSecurityData
        snmpSecurityData.getUser() >> SNMP_USER_VALUE
    }

    def "When setNodeSnmpUserToUndefined is called the nodes snmpUser is set to UNDEFINED" () {
        given: "A node with a snmpUser value of 'user'"
            nodeAttributes.put(NodeAttribute.SNMP_USER.toString(), SNMP_USER_VALUE)
            MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, nodeAttributes)
        when: "The setNodeSnmpUserToUndefined method is called"
            userUpdater.setNodeSnmpUserToUndefined(NODE_FDN)
        then: "The nodes snmpUser is updated"
            assertUser(UNDEFINED)
    }

    def "When setNodeSnmpUserToNodeSnmpInitSecurity is called and the security level is AUTH_PRIV the nodes snmpUser is set to SNMP_USER_VALUE" () {
        given: "A node with a snmpUser value of UNDEFINED"
            nodeAttributes.put(NodeAttribute.SNMP_USER.toString(), UNDEFINED)
            MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, nodeAttributes)
            snmpSecurityData.getSecurityLevel() >> SnmpSecurityLevel.AUTH_PRIV
        when: "The setNodeSnmpUserToNodeSnmpInitSecurity method is called"
            userUpdater.setNodeSnmpUserToNodeSnmpInitSecurity(NODE_FDN, userOperations.getNodeSnmpInitSecurityData())
        then: "The nodes snmpUser is updated"
            SnmpSecurityData snmpData = userOperations.getNodeSnmpInitSecurityData()
            assertUser(snmpData.getUser())
    }

    def "When setNodeSnmpUserToNodeSnmpInitSecurity is called and the security level is NO_AUTH_NO_PRIV the nodes snmpUser is set to ECIMUser" () {
        given: "A node with a snmpUser value of UNDEFINED"
            nodeAttributes.put(NodeAttribute.SNMP_USER.toString(), UNDEFINED)
            MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, nodeAttributes)
            snmpSecurityData.getSecurityLevel() >> SnmpSecurityLevel.NO_AUTH_NO_PRIV
        when: "The setNodeSnmpUserToNodeSnmpInitSecurity method is called"
            userUpdater.setNodeSnmpUserToNodeSnmpInitSecurity(NODE_FDN, userOperations.getNodeSnmpInitSecurityData())
        then: "The nodes snmpUser is updated"
            assertUser(ECIMUSER)
    }

    def "When an invalid node fdn is given no snmpUser attribute is updated" () {
        given: "A node with the default snmpUser"
            nodeAttributes.put(NodeAttribute.SNMP_USER.toString(), UNDEFINED)
            MoCreatorSpec.createNodeMo(NODE_FDN, projectMo, nodeAttributes)
        when: "The setNodeSnmpUserToNodeSnmpInitSecurity method is called with an invalid fdn"
            userUpdater.setNodeSnmpUserToNodeSnmpInitSecurity("Project=Project1,Node=Node2", userOperations.getNodeSnmpInitSecurityData())
        then: "The nodes snmpUser is not updated"
            assertUser(UNDEFINED)
    }

    def assertUser(final String expectedUser) {
        final ManagedObject nodeMo = dataPersistenceService.getLiveBucket().findMoByFdn(NODE_FDN)
        nodeMo.getAttribute("snmpUser").toString() == expectedUser
    }
}
