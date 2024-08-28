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

import javax.inject.Inject

import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.common.model.NodeDhcpAttribute
import com.ericsson.oss.services.ap.core.usecase.hardwarereplace.DhcpInfoCreator
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo
import com.ericsson.oss.services.ap.api.exception.HardwareReplaceException

class DhcpInfoCreatorSpec extends CdiSpecification {

    private static final String NODE_NAME = "HardwareReplaceNode"
    private static final String NODE_TYPE = "RadioNode"
    private static final String ACCESS_POINT = "accessPoint"
    private static final String ADDRESS = "address"
    private static final String USED_SERVER_ADDRESS = "usedServerAddress"

    private static final String NETWORK_ELEMENT_FDN = "NetworkElement=%s"
    private static final String ME_FDN = "SubNetwork=EnmSn,MeContext=%s"
    private static final String MANAMENT_ELEMENT_FDN = ME_FDN + ",ManagedElement=%s"
    private static final String OAM_ACCESS_POINT_FDN = MANAMENT_ELEMENT_FDN + ",SystemFunctions=1,SysM=1,OamAccessPoint=1"
    private static final String ROUTER_FDN =  MANAMENT_ELEMENT_FDN + ",Transport=1,Router=OAM"
    private static final String OAM_IP_ADDRESS_FDN = ROUTER_FDN + ",InterfaceIPv4=1,AddressIPv4=1"
    private static final String DNS_CLIENT_FDN = ROUTER_FDN + ",DnsClient=1"
    private static final String SYSTEM_FUNCTIONS_FDN = MANAMENT_ELEMENT_FDN + ",SystemFunctions=1"
    private static final String SYSM_FDN = SYSTEM_FUNCTIONS_FDN + ",SysM=1"
    private static final String NTP_SERVER_FDN_1 = SYSM_FDN + ",NtpServer=%s"
    private static final String TIMEM_FDN = SYSM_FDN + ",TimeM=1"
    private static final String NTP_FDN = TIMEM_FDN + ",Ntp=1"
    private static final String NTP_SERVER_FDN_2 = NTP_FDN + ",NtpServer=%s"
    private static final String ROUTE_TABLE_FDN = ROUTER_FDN + ",RouteTableIPv4Static=1"
    private static final String DST_1_FDN = ROUTE_TABLE_FDN + ",Dst=1"
    private static final String DST_2_FDN = ROUTE_TABLE_FDN + ",Dst=2"
    private static final String NEXT_HOP_1_FDN = DST_1_FDN + ",NextHop=1"
    private static final String NEXT_HOP_2_FDN = DST_2_FDN + ",NextHop=1"

    private static final String NETWORK_ELEMENT_TYPE = "NetworkElement"
    private static final String ME_TYPE = "MeContext"
    private static final String MANAMENT_ELEMENT_TYPE = "ManagedElement"
    private static final String OAM_ACCESS_POINT_TYPE = "OamAccessPoint"
    private static final String OAM_IP_ADDRESS_TYPE = "AddressIPv4"
    private static final String ROUTER_TYPE = "Router"
    private static final String DNS_CLIENT_TYPE = "DnsClient"
    private static final String SYSTEM_FUNCTIONS_TYPE = "SystemFunctions"
    private static final String SYSM_TYPE = "SysM"
    private static final String NTP_SERVER_TYPE = "NtpServer"
    private static final String TIMEM_TYPE = "TimeM"
    private static final String NTP_TYPE = "Ntp"
    private static final String ROUTE_TABLE_TYPE = "RouteTableIPv4Static"
    private static final String DST_TYPE = "Dst"
    private static final String NEXT_HOP_TYPE = "NextHop"

    private static final String NETWORK_ELEMENT_NAMESPACE = "OSS_NE_DEF"
    private static final String OAM_ACCESS_POINT_NAMESPACE = "RcsOamAccessPoint"
    private static final String ADDRESS_IPV4_NAMESPACE = "RtnL3AddressIPv4"
    private static final String DNS_CLIENT_NAMESPACE = "RtnDnsClient"
    private static final String ROUTER_NAMESPACE = "RtnL3Router";

    private static final String COM_TOP_NAMESPACE_VERSION = "10.10.1"
    private static final String NETWORK_ELEMENT_NAMESPACE_VERSION = "2.0.0"
    private static final String ME_NAMESPACE_VERSION = "3.0.0"
    private static final String ROUTER_NAMESPACE_VERSION = "1.25.0";
    private static final String DNS_CLIENT_NAMESPACE_VERSION = "2.11.0"
    private static final String OAM_ACCESS_POINT_NAMESPACE_VERSION = "1.2.0"
    private static final String ADDRESS_IPV4_NAMESPACE_VERSION = "7.29.0"

    private static final String OSS_MODEL_IDENTITY = "1998-184-092"
    private static final String ACCESS_POINT_ATTRIBUTE = ME_FDN + ",ManagedElement=%s,Transport=1,Router=OAM,InterfaceIPv4=1,AddressIPv4=1"
    private static final String ADDRESS_OF_INTEFACEIPV4 = "1.1.1.1/24"
    private static final String ROUTER_ATTRIBUTE = "OAM"

    private static final String SERVER_ADDRESS_1 = "10.10.10.0"
    private static final String SERVER_ADDRESS_2 = "10.10.10.1"

    private static final String SYSTEM_FUNCTIONS_NAMESPACE = "ComTop"
    private static final String SYSM_NAMESPACE = "ComSysM"
    private static final String TIMEM_NAMESPACE = "RcsTimeM"
    private static final String NTP_SERVER_NAMESPACE_1 = "ComSysM"
    private static final String NTP_SERVER_NAMESPACE_2 = "RcsSysM"

    private static final String SYSTEM_FUNCTIONS_NAMESPACE_VERSION = "10.22.0"
    private static final String SYSM_NAMESPACE_VERSION = "3.2.1002"
    private static final String TIMEM_NAMESPACE_VERSION = "1.4.2"
    private static final String NTP_SERVER_NAMESPACE_VERSION_1 = "3.2.1002"
    private static final String NTP_SERVER_NAMESPACE_VERSION_2 = "1.1.1"

    private static final String DEFAULT_ROUTER_ADDRESS = "192.168.0.31"
    private static final String SECOND_ROUTER_ADDRESS = "192.168.0.32"

    @Inject
    private DpsQueries dpsQueries

    @Inject
    private PersistenceObject persistanceObject

    @Inject
    private DhcpInfoCreator dhcpInfoCreator

    RuntimeConfigurableDps dps

    List<String> usedDNSServer

    def setup() {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        Whitebox.setInternalState(dpsQueries, "dps", dps.build())
        usedDNSServer = new ArrayList()
        String firstDNSServer = "1.2.3.4"
        usedDNSServer.add(firstDNSServer)
    }

    def "Check if attribute can be retrieved from JSON file" (){
        given:
            when:
            def ret = dhcpInfoCreator.getStringAttribute(nodeType, attributeName)

        then:
            ret == expected

        where:
            nodeType     | attributeName             | expected
            "RadioNode"  | "OamAccessPointFdn"       | "%s,SystemFunctions=1,SysM=1,OamAccessPoint=1"
            "RadioNode"  | "OamAccessPointAtrribute" | "accessPoint"
            "RadioNode"  | "addressIpAttribute"      | "address"
    }

    def "Check no exception when DHCP attributes is created if OAMAccessPoint is not set" (){
        given: "Required MOs exist"
            final ManagedObject networkElement = createNetworkElementMo(NODE_NAME)
            createNodeMos(networkElement)
            NodeInfo nodeInfo = new NodeInfo()

        when: "Try to create DHCP attributes"
            dhcpInfoCreator.create(nodeInfo, NODE_TYPE, networkElement)

        then:
            dhcpInfoCreator.isDhcpSupported(NODE_TYPE) == true
            notThrown(Exception)
    }

    def "Check no exception when DHCP attributes is created if OAMAddress IP is not set" (){
        given: "Required MOs exist"
            final ManagedObject networkElement = createNetworkElementMo(NODE_NAME)
            createNodeMos(networkElement)
            createOamAccessPointMo(NODE_NAME)
            NodeInfo nodeInfo = new NodeInfo()

        when: "Try to create DHCP attributes"
            dhcpInfoCreator.create(nodeInfo, NODE_TYPE, networkElement)

        then:
            dhcpInfoCreator.isDhcpSupported(NODE_TYPE) == true
            notThrown(Exception)
    }

    def "Check no exception when DHCP attributes is created if OAMAccessPoint is set to incorrect reference" (){
        given: "Required MOs exist"
            final ManagedObject networkElement = createNetworkElementMo(NODE_NAME)
            createNodeMos(networkElement)
            createOamAccessPointMo(NODE_NAME, "DUMMY_ATTRIBUTE")
            NodeInfo nodeInfo = new NodeInfo()

        when: "Try to create DHCP attributes"
            dhcpInfoCreator.create(nodeInfo, NODE_TYPE, networkElement)

        then:
            dhcpInfoCreator.isDhcpSupported(NODE_TYPE) == true
            notThrown(Exception)
    }

    def "Check DHCP attributes is created successfully if OAMAddress IP is set" (){
        given: "Required MOs exist"
            final ManagedObject networkElement = createNetworkElementMo(NODE_NAME)
            createNodeMos(networkElement)
            createOamAddressIpMo(NODE_NAME)
            createOamAccessPointMo(NODE_NAME)
            NodeInfo nodeInfo = new NodeInfo()
            nodeInfo.setName(NODE_NAME)

        when: "Try to create DHCP attributes"
            dhcpInfoCreator.create(nodeInfo, NODE_TYPE, networkElement)

        then:
            dhcpInfoCreator.isDhcpSupported(NODE_TYPE) == true
            nodeInfo.getDhcpAttributes().get(NodeDhcpAttribute.INITIAL_IP_ADDRESS.toString()).toString() == ADDRESS_OF_INTEFACEIPV4
            notThrown(Exception)
    }

    def "Check DHCP attributes is created successfully if OAMAddress IP and DNSClient and NtpServers are set" (){
        given: "Required MOs exist"
            final ManagedObject networkElement = createNetworkElementMo(NODE_NAME)
            createNodeMos(networkElement)
            createOamAddressIpMo(NODE_NAME)
            createOamAccessPointMo(NODE_NAME)

            final ManagedObject routerMO = createRouterMo(NODE_NAME)
            createDNSClientMo(NODE_NAME,routerMO,usedDNSServer)

            final ManagedObject systemFunctionsMo = createSystemFunctionsMo(NODE_NAME)
            final ManagedObject sysmMo = createSysMMo(NODE_NAME, systemFunctionsMo)
            createNtpServerMo(NODE_NAME, sysmMo, "1", SERVER_ADDRESS_1)
            createNtpServerMo(NODE_NAME, sysmMo, "2", SERVER_ADDRESS_2)
            createNtpServerMo(NODE_NAME, sysmMo, "3", SERVER_ADDRESS_1)

            NodeInfo nodeInfo = new NodeInfo()
            nodeInfo.setName(NODE_NAME)

        when: "Try to create DHCP attributes"
            dhcpInfoCreator.create(nodeInfo, NODE_TYPE, networkElement)

        then:
            dhcpInfoCreator.isDhcpSupported(NODE_TYPE) == true
            nodeInfo.getDhcpAttributes().get(NodeDhcpAttribute.INITIAL_IP_ADDRESS.toString()).toString() == ADDRESS_OF_INTEFACEIPV4
            nodeInfo.getDhcpAttributes().get(NodeDhcpAttribute.DNS_SERVER.toString()) == usedDNSServer
            nodeInfo.getDhcpAttributes().get(NodeDhcpAttribute.NTP_SERVER.toString()) == Arrays.asList(SERVER_ADDRESS_1,SERVER_ADDRESS_2)
            notThrown(Exception)
    }

    def "Check DHCP attributes is created successfully if OAMAddress IP and DNSClient and NtpServers v2 are set" (){
        given: "Required MOs exist"
            final ManagedObject networkElement = createNetworkElementMo(NODE_NAME)
            createNodeMos(networkElement)
            createOamAddressIpMo(NODE_NAME)
            createOamAccessPointMo(NODE_NAME)

            final ManagedObject routerMO = createRouterMo(NODE_NAME)
            createDNSClientMo(NODE_NAME,routerMO,usedDNSServer)

            final ManagedObject systemFunctionsMo = createSystemFunctionsMo(NODE_NAME)
            final ManagedObject sysmMo = createSysMMo(NODE_NAME, systemFunctionsMo)
            final ManagedObject timemMo = createTimeMMo(NODE_NAME, sysmMo)
            final ManagedObject ntpMo = createNtpMo(NODE_NAME, timemMo)
            createNtpServerMo_v2(NODE_NAME, ntpMo, "1", SERVER_ADDRESS_1)
            createNtpServerMo_v2(NODE_NAME, ntpMo, "2", SERVER_ADDRESS_2)
            createNtpServerMo_v2(NODE_NAME, ntpMo, "3", SERVER_ADDRESS_1)

            NodeInfo nodeInfo = new NodeInfo()
            nodeInfo.setName(NODE_NAME)

        when: "Try to create DHCP attributes"
            dhcpInfoCreator.create(nodeInfo, NODE_TYPE, networkElement)

        then:
            dhcpInfoCreator.isDhcpSupported(NODE_TYPE) == true
            nodeInfo.getDhcpAttributes().get(NodeDhcpAttribute.INITIAL_IP_ADDRESS.toString()).toString() == ADDRESS_OF_INTEFACEIPV4
            nodeInfo.getDhcpAttributes().get(NodeDhcpAttribute.DNS_SERVER.toString()) == usedDNSServer
            nodeInfo.getDhcpAttributes().get(NodeDhcpAttribute.NTP_SERVER.toString()) == Arrays.asList(SERVER_ADDRESS_1,SERVER_ADDRESS_2)
            notThrown(Exception)
    }

    def "Check no exception when DHCP attributes is created if NtpServer is not configured" (){
        given: "Required MOs exist"
            final ManagedObject networkElement = createNetworkElementMo(NODE_NAME)
            createNodeMos(networkElement)
            createOamAddressIpMo(NODE_NAME)
            createOamAccessPointMo(NODE_NAME)

            final ManagedObject systemFunctionsMo = createSystemFunctionsMo(NODE_NAME)
            final ManagedObject sysmMo = createSysMMo(NODE_NAME, systemFunctionsMo)
            final ManagedObject timemMo = createTimeMMo(NODE_NAME, sysmMo)
            createNtpMo(NODE_NAME, timemMo)

            final ManagedObject routerMO = createRouterMo(NODE_NAME)
            createDNSClientMo(NODE_NAME,routerMO,usedDNSServer)
            NodeInfo nodeInfo = new NodeInfo()

        when: "Try to create DHCP attributes"
            dhcpInfoCreator.create(nodeInfo, NODE_TYPE, networkElement)

        then:
            dhcpInfoCreator.isDhcpSupported(NODE_TYPE) == true
            notThrown(Exception)
    }

    def "Check DHCP attributes is created successfully if usedServerAddress is null" (){
        given: "Required MOs exist"
            final ManagedObject networkElement = createNetworkElementMo(NODE_NAME)
            createNodeMos(networkElement)
            createOamAddressIpMo(NODE_NAME)
            createOamAccessPointMo(NODE_NAME)
            final ManagedObject routerMO = createRouterMo(NODE_NAME)
            createDNSClientMo(NODE_NAME,routerMO)
            NodeInfo nodeInfo = new NodeInfo()
            nodeInfo.setName(NODE_NAME)

        when: "Try to create DHCP attributes"
            dhcpInfoCreator.create(nodeInfo, NODE_TYPE, networkElement)

        then:
            dhcpInfoCreator.isDhcpSupported(NODE_TYPE) == true
            nodeInfo.getDhcpAttributes().get(NodeDhcpAttribute.INITIAL_IP_ADDRESS.toString()).toString() == ADDRESS_OF_INTEFACEIPV4
            notThrown(Exception)
    }

    def "Check DHCP attributes with default router is created successfully without CLI input" (){
        given: "Required MOs exist"
            final ManagedObject networkElement = createNetworkElementMo(NODE_NAME)
            createNodeMos(networkElement)
            createOamAddressIpMo(NODE_NAME)
            createOamAccessPointMo(NODE_NAME)
            final ManagedObject routerMO = createRouterMo(NODE_NAME)
            final ManagedObject routeTableMO = createRouteTableMo(NODE_NAME,routerMO)
            final ManagedObject dstMO = createDstMo(NODE_NAME,routeTableMO,DST_1_FDN)
            createNextHopMoWithRouterIP(NODE_NAME,dstMO,NEXT_HOP_1_FDN,DEFAULT_ROUTER_ADDRESS)
            NodeInfo nodeInfo = new NodeInfo()
            nodeInfo.setName(NODE_NAME)

        when: "Try to create DHCP attributes"
            dhcpInfoCreator.create(nodeInfo, NODE_TYPE, networkElement)

        then:
            dhcpInfoCreator.isDhcpSupported(NODE_TYPE) == true
            nodeInfo.getDhcpAttributes().get(NodeDhcpAttribute.DEFAULT_ROUTER.toString()).toString() == DEFAULT_ROUTER_ADDRESS
            notThrown(Exception)
    }

    def "Check DHCP attributes with default router is created successfully with CLI input" (){
        given: "Required MOs exist"
            final ManagedObject networkElement = createNetworkElementMo(NODE_NAME)
            createNodeMos(networkElement)
            createOamAddressIpMo(NODE_NAME)
            createOamAccessPointMo(NODE_NAME)
            final ManagedObject routerMO = createRouterMo(NODE_NAME)
            final ManagedObject routeTableMO = createRouteTableMo(NODE_NAME,routerMO)
            final ManagedObject dstMO = createDstMo(NODE_NAME,routeTableMO,DST_1_FDN)
            createNextHopMoWithRouterIP(NODE_NAME,dstMO,NEXT_HOP_1_FDN,DEFAULT_ROUTER_ADDRESS)
            NodeInfo nodeInfo = new NodeInfo()
            nodeInfo.setName(NODE_NAME)
            nodeInfo.setDefaultRouterAddress(DEFAULT_ROUTER_ADDRESS)

        when: "Try to create DHCP attributes"
            dhcpInfoCreator.create(nodeInfo, NODE_TYPE, networkElement) //both

        then:
            dhcpInfoCreator.isDhcpSupported(NODE_TYPE) == true
            nodeInfo.getDhcpAttributes().get(NodeDhcpAttribute.DEFAULT_ROUTER.toString()).toString() == DEFAULT_ROUTER_ADDRESS
            notThrown(Exception)
    }

    def "Check DHCP attributes with default router is created successfully with nodeInfo input" (){
        given: "Required MOs exist"
            final ManagedObject networkElement = createNetworkElementMo(NODE_NAME)
            createNodeMos(networkElement)
            createOamAddressIpMo(NODE_NAME)
            createOamAccessPointMo(NODE_NAME)
            final ManagedObject routerMO = createRouterMo(NODE_NAME)
            final ManagedObject routeTableMO = createRouteTableMo(NODE_NAME,routerMO)
            final ManagedObject dstMO = createDstMo(NODE_NAME,routeTableMO,DST_1_FDN)
            createNextHopMoWithRouterIP(NODE_NAME,dstMO,NEXT_HOP_1_FDN,DEFAULT_ROUTER_ADDRESS)
            NodeInfo nodeInfo = new NodeInfo()
            nodeInfo.setDefaultRouterAddress(DEFAULT_ROUTER_ADDRESS)
            nodeInfo.setName(NODE_NAME)

        when: "Try to create DHCP attributes"
            dhcpInfoCreator.create(nodeInfo, NODE_TYPE, networkElement)

        then:
            dhcpInfoCreator.isDhcpSupported(NODE_TYPE) == true
            nodeInfo.getDhcpAttributes().get(NodeDhcpAttribute.DEFAULT_ROUTER.toString()).toString() == DEFAULT_ROUTER_ADDRESS
            notThrown(Exception)
    }

    def "Check DHCP attributes with default router is created successfully without CLI input and multiple routers are configured with the same IP address" (){
        given: "Required MOs exist"
            final ManagedObject networkElement = createNetworkElementMo(NODE_NAME)
            createNodeMos(networkElement)
            createOamAddressIpMo(NODE_NAME)
            createOamAccessPointMo(NODE_NAME)
            final ManagedObject routerMO = createRouterMo(NODE_NAME)
            final ManagedObject routeTableMO = createRouteTableMo(NODE_NAME,routerMO)
            final ManagedObject dst1MO = createDstMo(NODE_NAME,routeTableMO, DST_1_FDN)
            createNextHopMoWithRouterIP(NODE_NAME,dst1MO,NEXT_HOP_1_FDN,DEFAULT_ROUTER_ADDRESS)
            final ManagedObject dst2MO = createDstMo(NODE_NAME,routeTableMO, DST_2_FDN)
            createNextHopMoWithRouterIP(NODE_NAME,dst2MO,NEXT_HOP_2_FDN,DEFAULT_ROUTER_ADDRESS)
            NodeInfo nodeInfo = new NodeInfo()
            nodeInfo.setName(NODE_NAME)

        when: "Try to create DHCP attributes"
            dhcpInfoCreator.create(nodeInfo, NODE_TYPE, networkElement)

        then:
            dhcpInfoCreator.isDhcpSupported(NODE_TYPE) == true
            nodeInfo.getDhcpAttributes().get(NodeDhcpAttribute.DEFAULT_ROUTER.toString()).toString() == DEFAULT_ROUTER_ADDRESS
            notThrown(Exception)
    }

    def "Check DHCP attributes with default router is failed to created without CLI input and multiple routers are configured with different IPs" (){
        given: "Required MOs exist"
            final ManagedObject networkElement = createNetworkElementMo(NODE_NAME)
            createNodeMos(networkElement)
            createOamAddressIpMo(NODE_NAME)
            createOamAccessPointMo(NODE_NAME)
            final ManagedObject routerMO = createRouterMo(NODE_NAME)
            final ManagedObject routeTableMO = createRouteTableMo(NODE_NAME,routerMO)
            final ManagedObject dst1MO = createDstMo(NODE_NAME,routeTableMO, DST_1_FDN)
            createNextHopMoWithRouterIP(NODE_NAME,dst1MO,NEXT_HOP_1_FDN,DEFAULT_ROUTER_ADDRESS)
            final ManagedObject dst2MO = createDstMo(NODE_NAME,routeTableMO, DST_2_FDN)
            createNextHopMoWithRouterIP(NODE_NAME,dst2MO,NEXT_HOP_2_FDN,SECOND_ROUTER_ADDRESS)
            NodeInfo nodeInfo = new NodeInfo()
            nodeInfo.setName(NODE_NAME)

        when: "Try to create DHCP attributes"
            dhcpInfoCreator.create(nodeInfo, NODE_TYPE, networkElement)

        then:
            dhcpInfoCreator.isDhcpSupported(NODE_TYPE) == true
            thrown(HardwareReplaceException)
    }

    def "Check DHCP attributes without default router is created successfully without CLI input and no router is configured" (){
        given: "Required MOs exist"
            final ManagedObject networkElement = createNetworkElementMo(NODE_NAME)
            createNodeMos(networkElement)
            createOamAddressIpMo(NODE_NAME)
            createOamAccessPointMo(NODE_NAME)
            createRouterMo(NODE_NAME)
            NodeInfo nodeInfo = new NodeInfo()

        when: "Try to create DHCP attributes"
            dhcpInfoCreator.create(nodeInfo, NODE_TYPE, networkElement)

        then:
            dhcpInfoCreator.isDhcpSupported(NODE_TYPE) == true
            nodeInfo.getDhcpAttributes().get(NodeDhcpAttribute.DEFAULT_ROUTER.toString()) == null
            notThrown(Exception)
    }

    private void createNodeMos(final ManagedObject networkElement){
        final ManagedObject meContext = createMeContextMo(NODE_NAME, networkElement)
        createManagedElementMo(NODE_NAME, meContext)
    }

    private ManagedObject createNetworkElementMo(final String nodeName) {
        final Map<String, Object> networkElementAttributes = new HashMap<>()
        networkElementAttributes.put("networkElementId", nodeName)
        networkElementAttributes.put("neType", NODE_TYPE)
        networkElementAttributes.put("ossPrefix", String.format(ME_FDN, nodeName))
        networkElementAttributes.put("ossModelIdentity", OSS_MODEL_IDENTITY)

        return dps.addManagedObject()
                .withFdn(String.format(NETWORK_ELEMENT_FDN, nodeName))
                .type(NETWORK_ELEMENT_TYPE)
                .namespace(NETWORK_ELEMENT_NAMESPACE)
                .version(NETWORK_ELEMENT_NAMESPACE_VERSION)
                .target(persistanceObject)
                .name(nodeName)
                .addAttributes(networkElementAttributes)
                .build()
    }

    private ManagedObject createMeContextMo(final String nodeName, final ManagedObject parentMo) {
        return dps.addManagedObject()
                .withFdn(String.format(ME_FDN, nodeName))
                .type(ME_TYPE)
                .version(ME_NAMESPACE_VERSION)
                .parent(parentMo)
                .name(nodeName)
                .build()
    }

    private ManagedObject createManagedElementMo(final String nodeName, final ManagedObject parentMo) {
        final Map<String, Object> managedElementAttributes = new HashMap<>()
        managedElementAttributes.put("managedElementId", nodeName)
        managedElementAttributes.put("neType", NODE_TYPE)

        return dps.addManagedObject()
                .withFdn(String.format(MANAMENT_ELEMENT_FDN, nodeName, nodeName))
                .type(MANAMENT_ELEMENT_TYPE)
                .version(COM_TOP_NAMESPACE_VERSION)
                .name(nodeName)
                .parent(parentMo)
                .addAttributes(managedElementAttributes)
                .build()
    }

    private ManagedObject createOamAccessPointMo(final String nodeName){
        return dps.addManagedObject()
                .withFdn(String.format(OAM_ACCESS_POINT_FDN, nodeName, nodeName))
                .type(OAM_ACCESS_POINT_TYPE)
                .version(OAM_ACCESS_POINT_NAMESPACE_VERSION)
                .namespace(OAM_ACCESS_POINT_NAMESPACE)
                .addAttribute(ACCESS_POINT, String.format(ACCESS_POINT_ATTRIBUTE, nodeName, nodeName))
                .build()
    }

    private ManagedObject createOamAccessPointMo(final String nodeName, final String accessPointAttribute){
        return dps.addManagedObject()
                .withFdn(String.format(OAM_ACCESS_POINT_FDN, nodeName, nodeName))
                .type(OAM_ACCESS_POINT_TYPE)
                .version(OAM_ACCESS_POINT_NAMESPACE_VERSION)
                .namespace(OAM_ACCESS_POINT_NAMESPACE)
                .addAttribute(ACCESS_POINT, accessPointAttribute)
                .build()
    }

    private ManagedObject createRouterMo(final String nodeName) {
        return dps.addManagedObject()
                .withFdn(String.format(ROUTER_FDN, nodeName, nodeName))
                .namespace(ROUTER_NAMESPACE)
                .type(ROUTER_TYPE)
                .version(ROUTER_NAMESPACE_VERSION)
                .addAttribute(ROUTER_TYPE, ROUTER_ATTRIBUTE)
                .build();
    }

    private ManagedObject createOamAddressIpMo(final String nodeName){
        return dps.addManagedObject()
                .withFdn(String.format(OAM_IP_ADDRESS_FDN, nodeName, nodeName))
                .namespace(ADDRESS_IPV4_NAMESPACE)
                .type(OAM_IP_ADDRESS_TYPE)
                .version(ADDRESS_IPV4_NAMESPACE_VERSION)
                .addAttribute(ADDRESS, ADDRESS_OF_INTEFACEIPV4)
                .build()
    }

    private ManagedObject createDNSClientMo(final String nodeName, final ManagedObject parentMo, final List<String> usedDNSAddress){
        return dps.addManagedObject()
                .withFdn(String.format(DNS_CLIENT_FDN, nodeName, nodeName))
                .type(DNS_CLIENT_TYPE)
                .version(DNS_CLIENT_NAMESPACE_VERSION)
                .namespace(DNS_CLIENT_NAMESPACE)
                .addAttribute(USED_SERVER_ADDRESS, usedDNSAddress)
                .parent(parentMo)
                .build()
    }

    private ManagedObject createDNSClientMo(final String nodeName, final ManagedObject parentMo){
        return dps.addManagedObject()
                .withFdn(String.format(DNS_CLIENT_FDN, nodeName, nodeName))
                .type(DNS_CLIENT_TYPE)
                .version(DNS_CLIENT_NAMESPACE_VERSION)
                .namespace(DNS_CLIENT_NAMESPACE)
                .parent(parentMo)
                .build()
    }

    private ManagedObject createSystemFunctionsMo(final String nodeName) {
        return dps.addManagedObject()
                        .withFdn(String.format(SYSTEM_FUNCTIONS_FDN, nodeName, nodeName))
                        .type(SYSTEM_FUNCTIONS_TYPE)
                        .namespace(SYSTEM_FUNCTIONS_NAMESPACE)
                        .version(SYSTEM_FUNCTIONS_NAMESPACE_VERSION)
                        .name("1")
                        .build()
    }

    private ManagedObject createSysMMo(final String nodeName, final ManagedObject systemFunctionsMo) {
        return dps.addManagedObject()
                        .withFdn(String.format(SYSM_FDN, nodeName, nodeName))
                        .type(SYSM_TYPE)
                        .namespace(SYSM_NAMESPACE)
                        .version(SYSM_NAMESPACE_VERSION)
                        .name("1")
                        .parent(systemFunctionsMo)
                        .build()
    }

    private ManagedObject createNtpServerMo(final String nodeName, final ManagedObject sysmMo, final String ntpInstanceName, String serverAddress) {
        final Map<String, Object> ntpServerAttributes = new HashMap<>()
        ntpServerAttributes.put("serverAddress", serverAddress)
        ntpServerAttributes.put("ntpServerId", ntpInstanceName)

        return dps.addManagedObject()
                        .withFdn(String.format(NTP_SERVER_FDN_1, nodeName, nodeName, ntpInstanceName))
                        .type(NTP_SERVER_TYPE)
                        .namespace(NTP_SERVER_NAMESPACE_1)
                        .version(NTP_SERVER_NAMESPACE_VERSION_1)
                        .name(ntpInstanceName)
                        .parent(sysmMo)
                        .addAttributes(ntpServerAttributes)
                        .build()
    }

    private ManagedObject createTimeMMo(final String nodeName, final ManagedObject sysmMo) {
        return dps.addManagedObject()
                        .withFdn(String.format(TIMEM_FDN, nodeName, nodeName))
                        .type(TIMEM_TYPE)
                        .namespace(TIMEM_NAMESPACE)
                        .version(TIMEM_NAMESPACE_VERSION)
                        .name("1")
                        .parent(sysmMo)
                        .build()
    }

    private ManagedObject createNtpMo(final String nodeName, final ManagedObject timemMo) {
        return dps.addManagedObject()
                        .withFdn(String.format(NTP_FDN, nodeName, nodeName))
                        .type(NTP_TYPE)
                        .namespace(TIMEM_NAMESPACE)
                        .version(TIMEM_NAMESPACE_VERSION)
                        .name("1")
                        .parent(timemMo)
                        .build()
    }

    private ManagedObject createNtpServerMo_v2(final String nodeName, final ManagedObject ntpMo, final String ntpInstanceName, String serverAddress) {
        final Map<String, Object> ntpServerAttributes = new HashMap<>()
        ntpServerAttributes.put("serverAddress", serverAddress)
        ntpServerAttributes.put("ntpServerId", ntpInstanceName)

        return dps.addManagedObject()
                        .withFdn(String.format(NTP_SERVER_FDN_2, nodeName, nodeName, ntpInstanceName))
                        .type(NTP_SERVER_TYPE)
                        .namespace(NTP_SERVER_NAMESPACE_2)
                        .version(NTP_SERVER_NAMESPACE_VERSION_2)
                        .name(ntpInstanceName)
                        .parent(ntpMo)
                        .addAttributes(ntpServerAttributes)
                        .build()
    }

    private ManagedObject createRouteTableMo(final String nodeName, final ManagedObject parentMo){
        return dps.addManagedObject()
                .withFdn(String.format(ROUTE_TABLE_FDN, nodeName, nodeName))
                .type(ROUTE_TABLE_TYPE)
                .parent(parentMo)
                .build()
    }

    private ManagedObject createDstMo(final String nodeName, final ManagedObject parentMo, final String dstFdn){
        return dps.addManagedObject()
                .withFdn(String.format(dstFdn, nodeName, nodeName))
                .type(DST_TYPE)
                .parent(parentMo)
                .build()
    }

    private ManagedObject createNextHopMoWithRouterIP(final String nodeName, final ManagedObject parentMo, final String nextHopFdn, final String routerIP){
        return dps.addManagedObject()
                .withFdn(String.format(nextHopFdn, nodeName, nodeName))
                .type(NEXT_HOP_TYPE)
                .parent(parentMo)
                .addAttribute(ADDRESS, routerIP)
                .build()
    }
}
