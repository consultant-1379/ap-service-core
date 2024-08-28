/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.builder;

import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.model.node.Node;
import com.ericsson.oss.services.ap.core.rest.constant.ApAttributes;
import com.ericsson.oss.services.ap.core.rest.model.NodeData;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to build a node data object.
 */
public class NodeDataBuilder {

    /**
     * Builds a list of {@link NodeData} objects.
     *
     * @param nodeData
     *             List of {@link MoData} objects
     * @param projectName
     *             Name of Project
     * @return {@link NodeData}
     *             as a list of {@link Node} objects which is unmarshalled as JSON
     */
    public NodeData buildNodeData(final List<MoData> nodeData, final String projectName) {
        final List<Node> nodeResponseData = new ArrayList<>();
        // Skipping first entry in the list as it's the Project MO's data
        for (int i = 1; i < nodeData.size(); i++) {
            final String nodeId = (String) nodeData.get(i).getAttribute(ApAttributes.NODE_ID.getAttributeName());
            final String nodeType = (String) nodeData.get(i).getAttribute(ApAttributes.NODE_TYPE.getAttributeName());
            final String nodeIdentifier = (String) nodeData.get(i).getAttribute(ApAttributes.NODE_IDENTIFIER.getAttributeName());
            final String ipAddress = (String) nodeData.get(i).getAttribute(ApAttributes.IP_ADDRESS.getAttributeName());
            final String hardwareSerialNumber = (String) nodeData.get(i).getAttribute(ApAttributes.HARDWARE_SERIAL_NUMBER.getAttributeName());
            final String workOrderId = (String) nodeData.get(i).getAttribute(ApAttributes.WORK_ORDER_ID.getAttributeName());

            final Node node = new Node(nodeId, nodeType, nodeIdentifier, ipAddress, hardwareSerialNumber, workOrderId, projectName);
            nodeResponseData.add(node);
        }
        return new NodeData(nodeResponseData);
    }
}