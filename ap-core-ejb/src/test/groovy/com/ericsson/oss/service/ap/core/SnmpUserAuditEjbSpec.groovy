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
package com.ericsson.oss.service.ap.core

import org.slf4j.Logger

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.nms.security.nscs.api.enums.SnmpSecurityLevel
import com.ericsson.oss.service.ap.core.common.test.AbstractNodeStatusSpec
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpDataManager
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.ap.core.SnmpUserAuditEjb
import com.ericsson.oss.services.ap.core.rest.client.snmp.SnmpRestClient
/**
 * SnmpUserAuditEjbSpec is a test class for {@link SnmpUserAuditEjb}
 */
class SnmpUserAuditEjbSpec extends AbstractNodeStatusSpec {

    private final String SNMP_TARGET_V3_FDN= "SubNetwork=LTELI,ManagedElement=%s,SystemFunctions=1,SysM=1,Snmp=1,SnmpTargetV3=1";
    private final String[] NODE_FDNS = [
            NODE_FDN,
            PROJECT_FDN + ",Node=Node2",
            PROJECT_FDN + ",Node=Node3",
            PROJECT_FDN + ",Node=Node4",
            PROJECT_FDN + ",Node=Node5"
    ]
    private final String[] NODE_USERS = [
            "snmpUser1",
            "UNDEFINED",
            "ECIMUser",
            "snmpUser2",
            "snmpUser3"
    ]
    private final String[] SNMPV3_USERS = [
            "snmpUser4",
            "snmpUser5",
            "snmpUser6",
            "snmpUser7",
            "snmpUser8"
    ]

    private final List<String> ACTIVE_USERS = Arrays.asList(
            "SnmpTestUser1",
            "SnmpTestUser1",
            "snmpUser1",
            "ECIMUser",
            "snmpUser2",
            "snmpUser3",
            "snmpUser4",
            "snmpUser5",
            "snmpUser6",
            "snmpUser7",
            "snmpUser8"
    )

    private final String SNMP_USER = "SnmpTestUser1"

    @ObjectUnderTest
    private final SnmpUserAuditEjb userAuditEjb = new SnmpUserAuditEjb()

    @MockedImplementation
    private SnmpRestClient restClient

    @MockedImplementation
    private Logger logger

    @MockedImplementation
    private SnmpDataManager snmpDataManager

    @MockedImplementation
    private SnmpSecurityData snmpSecurityData

    def setup() {
        userAuditEjb.logger = logger
    }

    def "When Node users are retrieved only valid user names are added to the array to be sent to node discovery" () {
        given: "Node Mos with valid and invalid users"
            buildNodeMosWithUsers()
        when: "Node Mo users are retrieved"
            userAuditEjb.users = new ArrayList<>()
            userAuditEjb.getNodeUsers()
            final String[] moUsers = userAuditEjb.users
        then: "Only valid users are returned"
            moUsers.every() {
                it != "UNDEFINED"
            }
    }

    def "When SnmpTargetV3 users are retrieved all user names are added to the array to be sent to node discovery" () {
        given: "SnmpTargetV3 Mos with valid users"
            buildSnmpTargetV3MosWithUsers()
        when: "SnmpTargetV3 Mo users are retrieved"
            userAuditEjb.users = new ArrayList<>()
            userAuditEjb.getSnmpTargetV3Users()
            final String[] moUsers = userAuditEjb.users
        then: "All users are returned"
            moUsers.length == SNMPV3_USERS.length
    }

    def "When the timer service times out the array of users is created and sent to node discovery" () {
        given: "Node and SnmpTargetV3 Mos with assigned users"
            buildNodeMosWithUsers()
            buildSnmpTargetV3MosWithUsers()
            userAuditEjb.restClient = restClient
            userAuditEjb.snmpDataManager = snmpDataManager
            snmpSecurityData.getSecurityLevel() >> SnmpSecurityLevel.AUTH_PRIV.getSnmpSecurityLevel()
            snmpSecurityData.getUser() >> SNMP_USER
            snmpDataManager._(*_) >> snmpSecurityData
        when: "Timeout occurs and start is called"
            userAuditEjb.start()
        then: "The rest client is called with the correct array of users"
            1 * restClient.sendActiveConnectionIdsToNodeDiscovery(ACTIVE_USERS)
    }

    def buildNodeMosWithUsers() {
        for (int index = 0; index < NODE_FDNS.length; index++) {
            final Map<String, String> attrs = new HashMap<>()
            attrs.put("snmpUser", NODE_USERS[index])
            MoCreatorSpec.createNodeMo(NODE_FDNS[index], projectMo, attrs)
            attrs.clear()
        }
    }

    def buildSnmpTargetV3MosWithUsers() {
        for (int index = 0; index < SNMPV3_USERS.length; index++) {
            String snmpTargetFdn = String.format(SNMP_TARGET_V3_FDN, "Node" + (index + 1))
            createSnmpTargetMo(snmpTargetFdn, SNMPV3_USERS[index])
        }
    }

    def createSnmpTargetMo(String fdn, String user) {
        final Map<String, String> attrs = new HashMap<>()
        attrs.clear()
        attrs.put("user", user)
        return  dps.addManagedObject()
                .withFdn(fdn)
                .type("SnmpTargetV3")
                .namespace("RcsSnmp")
                .version("10.13.2")
                .addAttributes(attrs)
                .build()
    }
}
