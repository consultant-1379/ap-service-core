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
package com.ericsson.oss.services.ap.core.usecase.hardwarereplace;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.HardwareReplaceException;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.NetworkElementAttribute;
import com.ericsson.oss.services.ap.common.model.NodeDhcpAttribute;
import com.ericsson.oss.services.ap.common.util.capability.NodeCapabilityModel;
import com.ericsson.oss.services.ap.common.util.log.MRDefinition;
import com.ericsson.oss.services.ap.common.util.log.MRExecutionRecorder;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;

/**
 * Create the DHCP attributes in the node info
 */
public class DhcpInfoCreator {

    private static final String ACCESS_POINT_ATTRIBUTE = "OamAccessPointAtrribute";
    private static final String ADDRESS_IP_ATTRIBUTE = "addressIpAttribute";
    private static final String ACCESS_POINT_FDN = "OamAccessPointFdn";
    private static final String ACCESS_POINT_ROUTER_FDN = "OamAccessPointRouterFdn";
    private static final String REPLACE_WITH_DHCP_USECASE = "REPLACE_WITH_DHCP";
    private static final String IS_SUPPORTED = "isSupported";
    private static final String ROUTER_MO_NAME = "Router";
    private static final String DNS_CLIENT_MO_NAME = "DnsClient";
    private static final String USED_DNS_SERVER_MO_NAME = "usedServerAddress";
    private static final String NTP_SERVER_MO_NAME = "NtpServer";
    private static final String SERVER_ADDRESS_ATTRIBUTE = "serverAddress";
    private static final String SYSM_FDN = "sysMFdn";
    private static final String NTP_FDN = "ntpFdn";
    private static final String ROUTE_TABLE_IPV4_STATIC_FDN = "RouteTableIPv4StaticFdn";
    private static final String ROUTE_TABLE_IPV6_STATIC_FDN = "RouteTableIPv6StaticFdn";
    private static final String DST_MO_NAME = "Dst";
    private static final String NEXT_HOP_MO_NAME = "NextHop";
    private static final String ROUTER_ADDRESS_ATTRIBUTE = "address";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private DpsOperations dps;

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private MRExecutionRecorder recorder;

    /**
     * Create the DHCP attributes in node info
     *
     * @param nodeInfo
     *            the node info which will be updated with DHCP attributes
     * @param nodeType
     *            the node type for checking
     * @param networkElementMo
     *            the Network Element MO
     */
    public void create(final NodeInfo nodeInfo, final String nodeType, final ManagedObject networkElementMo) {
        final String nodeName = nodeInfo.getName();
        final Map<String, Object> dhcpAttributes = new HashMap<>();
        nodeInfo.setDhcpAttributes(dhcpAttributes);

        try {
            if (!isDhcpSupported(nodeType)) {
                return;
            }

            final DataBucket dataBucket = dps.getDataPersistenceService().getLiveBucket();
            final String managedElementFdn = getManagedElementFdn(nodeName, networkElementMo);
            final String oamAccessPoint = getAttributeFromMO(dataBucket,
                String.format(getStringAttribute(nodeType, ACCESS_POINT_FDN), managedElementFdn),
                getStringAttribute(nodeType, ACCESS_POINT_ATTRIBUTE));
            if (StringUtils.isBlank(oamAccessPoint)) {
                return;
            }

            final String oamIpAddress = getAttributeFromMO(dataBucket, oamAccessPoint, getStringAttribute(nodeType, ADDRESS_IP_ATTRIBUTE));
            if (StringUtils.isBlank(oamIpAddress)) {
                return;
            }
            recorder.recordMRExecution(MRDefinition.AP_HARDWAREREPLACE_DHCP);
            dhcpAttributes.put(NodeDhcpAttribute.INITIAL_IP_ADDRESS.toString(), oamIpAddress);

            final String routerFdn = String.format(getStringAttribute(nodeType, ACCESS_POINT_ROUTER_FDN), managedElementFdn,
                FDN.get(oamAccessPoint).getRdnValueOfType(ROUTER_MO_NAME));

            final List<String> dNSAddress = getDNSAddress(routerFdn);
            if (CollectionUtils.isNotEmpty(dNSAddress)) {
                dhcpAttributes.put(NodeDhcpAttribute.DNS_SERVER.toString(), dNSAddress.stream().distinct().collect(Collectors.toList()));
            }

            final List<String> ntpServerAddresses = getNtpServerAddresses(nodeType, managedElementFdn);
            if (CollectionUtils.isNotEmpty(ntpServerAddresses)) {
                dhcpAttributes.put(NodeDhcpAttribute.NTP_SERVER.toString(), ntpServerAddresses.stream().distinct().collect(Collectors.toList()));
            }

            final String dhcpDefaultRouter = getDefaultRouterAddress(nodeType, routerFdn, isV4Network(oamIpAddress), nodeInfo);
            if (StringUtils.isNotBlank(dhcpDefaultRouter)) {
                dhcpAttributes.put(NodeDhcpAttribute.DEFAULT_ROUTER.toString(), dhcpDefaultRouter);
            }

        } catch (final HardwareReplaceException hre) {
            throw hre;
        } catch (final Exception e) {
            logger.error("Failed to set DHCP info to Node: {}, NodeType: {} - {}", nodeName, nodeType, e.getMessage());
        }
    }

    private boolean isDhcpSupported(final String nodeType) {
        return getBooleanAttribute(nodeType, IS_SUPPORTED);
    }

    private String getManagedElementFdn(final String nodeName, final ManagedObject networkElementMo) {
        final String ossPrefix = networkElementMo.getAttribute(NetworkElementAttribute.OSS_PREFIX.toString());
        String managedElementFdn = String.format("ManagedElement=%s", nodeName);
        if (StringUtils.isNotBlank(ossPrefix)) {
            managedElementFdn = String.format("%s,%s", ossPrefix, managedElementFdn);
        }
        return managedElementFdn;
    }

    private List<String> getDNSAddress(final String routerFdn) {
        List<String> dNSAddress = Collections.emptyList();
        try {
            final Iterator<ManagedObject> dNSMos = dpsQueries.findChildMosOfTypes(routerFdn, DNS_CLIENT_MO_NAME).execute();
            if (dNSMos.hasNext()) {
                final ManagedObject dNSMo = dNSMos.next();
                dNSAddress = getAttributeFromMO(dNSMo, USED_DNS_SERVER_MO_NAME);
            }
        } catch (final Exception e) {
            logger.error("Failed to retrieve DNS address from FDN: {} - {}", routerFdn, e.getMessage());
        }
        return dNSAddress;
    }

    private List<String> getNtpServerAddresses(final String nodeType, final String managedElementFdn) {
        final List<String> ntpServerAddresses = new ArrayList<>();

        final String ntpFdn = String.format(getStringAttribute(nodeType, NTP_FDN), managedElementFdn);
        getNtpServerAddresses(ntpFdn, ntpServerAddresses);

        if (CollectionUtils.isEmpty(ntpServerAddresses)) {
            final String sysmFdn = String.format(getStringAttribute(nodeType, SYSM_FDN), managedElementFdn);
            getNtpServerAddresses(sysmFdn, ntpServerAddresses);
        }

        return ntpServerAddresses;
    }

    private void getNtpServerAddresses(final String parentFdn, final List<String> serverAddresses) {
        try {
            final Iterator<ManagedObject> ntpServerMos = dpsQueries.findChildMosOfTypes(parentFdn, NTP_SERVER_MO_NAME).execute();
            while (ntpServerMos.hasNext()) {
                final ManagedObject ntpServerMo = ntpServerMos.next();
                final String serverAddress = getAttributeFromMO(ntpServerMo, SERVER_ADDRESS_ATTRIBUTE);
                serverAddresses.add(serverAddress);
            }
        } catch (final Exception e) {
            logger.error("Failed to get serverAddress of NtpServer, parent FDN {} - {}", parentFdn, e.getMessage());
        }
    }

    private boolean isV4Network(final String oamIpAddress) throws UnknownHostException {
        final String ipAddress = StringUtils.substringBefore(oamIpAddress, "/");
        final InetAddress oamIP = InetAddress.getByName(ipAddress);
        return oamIP instanceof Inet4Address;
    }

    private String getDefaultRouterAddress(final String nodeType, final String routerFdn, final boolean isV4Network, final NodeInfo nodeData) {
        final String defaultRouterAddress = nodeData.getDefaultRouterAddress();
        if (StringUtils.isBlank(defaultRouterAddress)) {
            return getDefaultRouterAddress(nodeType, routerFdn, isV4Network);
        } else {
            return defaultRouterAddress;
        }
    }

    private String getDefaultRouterAddress(final String nodeType, final String routerFdn, final boolean isV4Network) {
        final List<String> routerAddresses = new ArrayList<>();
        String routeTableFdn = null;
        if (isV4Network) {
            routeTableFdn = String.format(getStringAttribute(nodeType, ROUTE_TABLE_IPV4_STATIC_FDN), routerFdn);
        } else {
            routeTableFdn = String.format(getStringAttribute(nodeType, ROUTE_TABLE_IPV6_STATIC_FDN), routerFdn);
        }
        final List<String> dstFdns = getDstFdns(routeTableFdn);
        if (CollectionUtils.isNotEmpty(dstFdns)) {
            dstFdns.stream().forEach(s -> {
                final Iterator<ManagedObject> nextHopMos = dpsQueries.findChildMosOfTypes(s, NEXT_HOP_MO_NAME).execute();
                if (nextHopMos != null) {
                    while (nextHopMos.hasNext()) {
                        final ManagedObject nextHopMo = nextHopMos.next();
                        routerAddresses.add(getAttributeFromMO(nextHopMo, ROUTER_ADDRESS_ATTRIBUTE));
                    }
                }
            });
        }

        return getUniqueRouterAddress(routerAddresses);
    }

    private List<String> getDstFdns(final String routeTableFdn) {
        final List<String> dstFdns = new ArrayList<>();
        final Iterator<ManagedObject> dstMos = dpsQueries.findChildMosOfTypes(routeTableFdn, DST_MO_NAME).execute();
        if (dstMos != null) {
            while (dstMos.hasNext()) {
                final ManagedObject dstMo = dstMos.next();
                dstFdns.add(dstMo.getFdn());
            }
        }
        return dstFdns;
    }

    private String getUniqueRouterAddress(final List<String> routerAddressList) {
        final List<String> addresses = new ArrayList<>();
        addresses.addAll(routerAddressList.stream().distinct().collect(Collectors.toList()));
        if (addresses.size() > 1) {
            throw new HardwareReplaceException(String.format(
                "Cannot determine the default router: multiple routers %s found, Please specify it and try again.", addresses.toString()));
        }

        return CollectionUtils.isNotEmpty(addresses) ? addresses.get(0) : null;
    }

    private Boolean getBooleanAttribute(final String nodeType, final String attributeName) {
        try {
            return NodeCapabilityModel.INSTANCE.getAttributeAsBoolean(nodeType, REPLACE_WITH_DHCP_USECASE, attributeName);
        } catch (final Exception e) {
            logger.error("Failed to query {}.{}.{} in node capacities file - {}", nodeType, REPLACE_WITH_DHCP_USECASE, attributeName, e.getMessage());
        }
        return false;
    }

    private String getStringAttribute(final String nodeType, final String attributeName) {
        try {
            return NodeCapabilityModel.INSTANCE.getAttributeAsString(nodeType, REPLACE_WITH_DHCP_USECASE, attributeName);
        } catch (final Exception e) {
            logger.error("Failed to query {}.{}.{} in node capacities file - {}", nodeType, REPLACE_WITH_DHCP_USECASE, attributeName, e.getMessage());
        }
        return null;
    }

    private <T> T getAttributeFromMO(final DataBucket dataBucket, final String fdn, final String attribute) {
        try {
            final ManagedObject mo = dataBucket.findMoByFdn(fdn);
            return getAttributeFromMO(mo, attribute);
        } catch (final Exception e) {
            logger.error("Failed to get attribute: {} from FDN: {} - {}", attribute, fdn, e.getMessage());
        }
        return null;
    }

    private <T> T getAttributeFromMO(final ManagedObject mo, final String attribute) {
        try {
            return mo.getAttribute(attribute);
        } catch (final Exception e) {
            logger.error("Failed to get attribute: {} from FDN: {} - {}", attribute, mo.getFdn(), e.getMessage());
        }
        return null;
    }
}
