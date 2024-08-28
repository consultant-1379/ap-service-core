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
package com.ericsson.oss.services.ap.core.usecase.importproject;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeDhcpAttribute;

/**
 * Creates the <code>NodeDhcp</code> MO from the input {@link NodeInfo}.
 */
public class NodeDhcpMoCreator {

    @Inject
    private DpsOperations dpsOperations;

    /**
     * Creates an {@link MoType#NODE_DHCP} MO for greenfield node in AP model.
     *
     * @param nodeMo
     *            the node MO
     * @param nodeInfo
     *            the node information from nodeInfo.xml
     * @return the created Node DHCP MO
     */
    public ManagedObject create(final ManagedObject nodeMo, final NodeInfo nodeInfo) {
        if (nodeInfo.getDhcpAttributes().isEmpty()) {
            return null;
        }
        return createDhcpMo(nodeMo, nodeInfo);
    }

    private ManagedObject createDhcpMo(final ManagedObject nodeMo, final NodeInfo nodeInfo) {
        final String initialIpAddress = NodeDhcpAttribute.INITIAL_IP_ADDRESS.toString();
        final String defaultRouter = NodeDhcpAttribute.DEFAULT_ROUTER.toString();
        final String ntpServer = NodeDhcpAttribute.NTP_SERVER.toString();
        final String dnsServer = NodeDhcpAttribute.DNS_SERVER.toString();

        final Map<String, Object> nodeDhcpAttributes = new HashMap<>();
        nodeDhcpAttributes.put(initialIpAddress, nodeInfo.getDhcpAttributes().get(initialIpAddress));
        nodeDhcpAttributes.put(defaultRouter, nodeInfo.getDhcpAttributes().get(defaultRouter));
        nodeDhcpAttributes.put(ntpServer, nodeInfo.getDhcpAttributes().get(ntpServer));
        nodeDhcpAttributes.put(dnsServer, nodeInfo.getDhcpAttributes().get(dnsServer));

        return dpsOperations.getDataPersistenceService().getLiveBucket()
            .getManagedObjectBuilder()
            .type(MoType.NODE_DHCP.toString())
            .parent(nodeMo)
            .addAttributes(nodeDhcpAttributes)
            .create();
    }
}
