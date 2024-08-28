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
package com.ericsson.oss.services.ap.core.usecase.migration;

import java.util.Map;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.common.model.NetworkElementAttribute;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.util.AbstractApActivityUtil;

/**
 * Utility class used to create the MOs required to perform a Migration.
 */
public class MigrationUtil extends AbstractApActivityUtil {

    /**
     * Creates the AP Node MO and child MOs.
     * @param nodeInfo
     *            link to NodeInfo Object for elements supplied in nodeInfo.xml
     */
    public void create(final NodeInfo nodeInfo) {
        createApNodeMo(nodeInfo);
    }

    private void createApNodeMo(final NodeInfo nodeInfo) {
        final ManagedObject networkElementMo = getNetworkElementMo(nodeInfo);
        final Map<String, Object> nodeAttributes = nodeInfo.getNodeAttributes();
        nodeAttributes.put(NodeAttribute.IPADDRESS.toString(), getNodeIpAddress(networkElementMo));
        nodeAttributes.put(NodeAttribute.OSS_PREFIX.toString(), networkElementMo.getAttribute(NetworkElementAttribute.OSS_PREFIX.toString()));
        nodeAttributes.put(NodeAttribute.IS_NODE_MIGRATION.toString(), Boolean.TRUE);
        nodeInfo.setNodeAttributes(nodeAttributes);
    }

}
