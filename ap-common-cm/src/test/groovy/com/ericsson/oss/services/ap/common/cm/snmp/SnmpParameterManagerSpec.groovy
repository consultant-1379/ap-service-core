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
package com.ericsson.oss.services.ap.common.cm.snmp

import static com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData.NODE_SNMP_INIT_SECURITY;
import static com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData.NODE_SNMP_SECURITY;

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.config.ConfigurationEnvironment
import com.ericsson.oss.itpf.sdk.config.ConfigurationPropertyNotFoundException

/**
 * SnmpParameterManagerSpec is a test class for {@link SnmpParameterManager}
 */
class SnmpParameterManagerSpec extends CdiSpecification{

    private static final String[] pibValuesSnmpSecurity = new String[6]
    private static final String[] pibValuesSnmpInitSecurity = new String[6]

    @ObjectUnderTest
    private SnmpParameterManager parameterManager

    @MockedImplementation
    private ConfigurationEnvironment configurationEnvironment

    def "When NODE_SNMP_SECURITY is given the configuration environment fetch the PIB values" () {
        given: "A NODE_SNMP_SECURITY parameter"
        pibValuesSnmpSecurity[0] = "securityLevel:AUTH_PRIV"
        pibValuesSnmpSecurity[1] = "authProtocol:SHA1"
        pibValuesSnmpSecurity[2] = "authPassword:v3{V}Ri{Z}z0{W}n"
        pibValuesSnmpSecurity[3] = "privProtocol:AES128"
        pibValuesSnmpSecurity[4] = "privPassword:v3{V}Ri{Z}z0{W}n"
        pibValuesSnmpSecurity[5] = "user:mediation"

        when: "The configuration environment calls getValue"
        configurationEnvironment.getValue(NODE_SNMP_SECURITY) >> pibValuesSnmpSecurity

        then: "The node snmp security values are returned"
        parameterManager.getNodeSnmpSecurity() == pibValuesSnmpSecurity
    }

    def "When NODE_SNMP_INIT_SECURITY is given the configuration environment fetch the PIB values" () {
        given: "A NODE_SNMP_INIT_SECURITY parameter"
        pibValuesSnmpInitSecurity[0] = "securityLevel:AUTH_PRIV"
        pibValuesSnmpInitSecurity[1] = "authProtocol:SHA1"
        pibValuesSnmpInitSecurity[2] = "authPassword:v3{V}Ri{Z}z0{W}n"
        pibValuesSnmpInitSecurity[3] = "privProtocol:AES128"
        pibValuesSnmpInitSecurity[4] = "privPassword:v3{V}Ri{Z}z0{W}n"
        pibValuesSnmpInitSecurity[5] = "user:mediation"

        when: "The configuration environment calls getValue"
        configurationEnvironment.getValue(NODE_SNMP_INIT_SECURITY) >> pibValuesSnmpInitSecurity

        then: "The node init snmp security values are returned"
        parameterManager.getNodeSnmpInitSecurity() == pibValuesSnmpInitSecurity
    }

    def "Throws exception when the given property is not found"() {
        given: "A NODE_SNMP_SECURITY parameter"
        configurationEnvironment.getValue(NODE_SNMP_SECURITY) >> {throw new ConfigurationPropertyNotFoundException(NODE_SNMP_SECURITY)}

        when: "unable to retrieve snmp parameter"
        parameterManager.getNodeSnmpSecurity()

        then: "exception is thrown with proper message"
        ConfigurationPropertyNotFoundException exception = thrown()
        exception.message == "Was not able to find configuration property with name 'NODE_SNMP_SECURITY'. Did you model it? Are all the models deployed correctly?"
    }
}
