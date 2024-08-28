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
package com.ericsson.oss.services.ap.common.util.capability

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ApApplicationException

class NodeCapabilityModelSpec extends CdiSpecification  {

    def "Get Boolean attribute value from nodetype, usecase and attributeName query successfully"() {
        given:

        when:
        def attribute = NodeCapabilityModel.INSTANCE.getAttributeAsBoolean(nodeType, useCase, attributeName)

        then:
        attribute == expected

        where:
        nodeType         | useCase                                    | attributeName               | expected
        "RadioNode"      | "REPLACE_WITH_DHCP"                        | "isSupported"               | true
        "RadioNode"      | "BIND_WITH_NODE_NAME"                      | "isSupported"               | true
        "MSRBS_V1"       | "REPLACE_WITH_DHCP"                        | "isSupported"               | false
        "RadioNode"      | "CONFIGURE_SNMP_SECURITY_WITH_NODE_NAME"   | "isSupported"               | true
        "vTIF"           | "CONFIGURE_SNMP_SECURITY_WITH_NODE_NAME"   | "isSupported"               | true
        "Router6x71"     | "CONFIGURE_SNMP_SECURITY_WITH_NODE_NAME"   | "isSupported"               | false
        "ERBS"           | "CONFIGURE_SNMP_SECURITY_WITH_NODE_NAME"   | "isSupported"               | false
        "MSRBS_V1"       | "CONFIGURE_SNMP_SECURITY_WITH_NODE_NAME"   | "isSupported"               | false
        "RadioNode"      | "APPLY_NETCONF_POST_SYNC"                  | "isSupported"               | true
        "Controller6610" | "NETCONF_NODE_PLUGIN_VALIDATION"           | "isSupported"               | true
        "Controller6610" | "APPLY_BASELINES"                          | "isSupported"               | true
    }

    def "Get String attribute value from nodetype, usecase and attributeName query successfully"() {
        given:

        when:
        def attribute = NodeCapabilityModel.INSTANCE.getAttributeAsString(nodeType, useCase, attributeName)

        then:
        attribute == expected

        where:
        nodeType      | useCase             | attributeName             | expected
        "RadioNode"   | "REPLACE_WITH_DHCP" | "OamAccessPointFdn"       | "%s,SystemFunctions=1,SysM=1,OamAccessPoint=1"
        "RadioNode"   | "REPLACE_WITH_DHCP" | "OamAccessPointAtrribute" | "accessPoint"
        "RadioNode"   | "REPLACE_WITH_DHCP" | "OamAccessPointRouterFdn" | "%s,Transport=1,Router=%s"
        "RadioNode"   | "REPLACE_WITH_DHCP" | "addressIpAttribute"      | "address"
        "RadioNode"   | "REPLACE_WITH_DHCP" | "sysMFdn"                 | "%s,SystemFunctions=1,SysM=1"
        "RadioNode"   | "REPLACE_WITH_DHCP" | "ntpFdn"                  | "%s,SystemFunctions=1,SysM=1,TimeM=1,Ntp=1"
        "RadioNode"   | "REPLACE_WITH_DHCP" | "RouteTableIPv4StaticFdn" | "%s,RouteTableIPv4Static=1"
        "RadioNode"   | "REPLACE_WITH_DHCP" | "RouteTableIPv6StaticFdn" | "%s,RouteTableIPv6Static=1"
    }

    def "Get default attribute value from nodetype, usecase and attributeName query successfully"() {
        given:

        when:
        def attribute = NodeCapabilityModel.INSTANCE.getAttributeAsBoolean(nodeType, useCase, attributeName)

        then:
        attribute == expected

        where:
        nodeType      | useCase                                  | attributeName     | expected
        "DUMMY_NODE"  | "REPLACE_WITH_DHCP"                      | "isSupported"     | false
        "DUMMY_NODE"  | "CONFIGURE_SNMP_SECURITY_WITH_NODE_NAME" | "isSupported"     | false
        "DUMMY_NODE"  | "APPLY_NETCONF_POST_SYNC"                | "isSupported"     | false
    }

    def "Get exception for undefined element query"() {
        given:

        when:
        NodeCapabilityModel.INSTANCE.getAttributeValue(nodeType, useCase, attributeName)

        then:
        ApApplicationException exception = thrown()
        exception.getMessage().toString().contains(expected)

        where:
        nodeType     | useCase               | attributeName       | expected
        "RadioNode"  | "DUMMY_USE_CASE"      | "isSupported"       | "RadioNode, DUMMY_USE_CASE"
        "RadioNode"  | "REPLACE_WITH_DHCP"   | "DUMMY_ATTRIBUTE"   | "RadioNode, REPLACE_WITH_DHCP, DUMMY_ATTRIBUTE"
        "DUMMY_NODE" | "DUMMY_USE_CASE"      | "isSupported"       | "DUMMY_NODE, DUMMY_USE_CASE"
    }
}
