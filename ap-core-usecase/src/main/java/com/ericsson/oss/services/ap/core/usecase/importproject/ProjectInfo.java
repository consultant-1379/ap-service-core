/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
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

/**
 * Data read from projectInfo.xml and nodeInfo.xml.
 */
public class ProjectInfo {

    private String name;
    private String type;
    private int nodeQuantity;
    private Map<String, Object> projectAttributes;
    private final Map<String, NodeInfo> nodeInfos = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    void setType(final String type) {
        this.type = type;
    }

    public Map<String, Object> getProjectAttributes() {
        return projectAttributes;
    }

    public void setProjectAttributes(final Map<String, Object> attributes) {
        this.projectAttributes = attributes;
    }

    public int getNodeQuantity() {
        return nodeQuantity;
    }

    void setNodeQuantity(final int nodeQuantity) {
        this.nodeQuantity = nodeQuantity;
    }

    public Map<String, NodeInfo> getNodeInfos() {
        return nodeInfos;
    }

    public NodeInfo getNodeInfoFromName(final String nodeName) {
        return nodeInfos.get(nodeName);
    }

    public void addNodeInfo(final NodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        nodeInfos.put(nodeInfo.getName(), nodeInfo);
    }
}