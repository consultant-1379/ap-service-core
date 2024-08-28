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
package com.ericsson.oss.services.ap.api.model.node;

import java.io.Serializable;

/**
 * POJO model for representing a Node.
 */
public class Node implements Serializable {

    private static final long serialVersionUID = -8847952436161721628L;

    private String id;
    private String label;
    private String nodeType;
    private String nodeIdentifier;
    private String ipAddress;
    private String hardwareSerialNumber;
    private String parent;
    private String workOrderId;

    public Node(final String id, final String nodeType, final String nodeIdentifier, final String ipAddress,
        final String hardwareSerialNumber, final String workOrderId, final String parent) {
        this.id = id;
        this.label = id;
        this.nodeType = nodeType;
        this.nodeIdentifier = nodeIdentifier;
        this.ipAddress = ipAddress;
        this.hardwareSerialNumber = hardwareSerialNumber;
        this.parent = parent;
        this.workOrderId = workOrderId;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getNodeIdentifier() {
        return nodeIdentifier;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getHardwareSerialNumber() {
        return hardwareSerialNumber;
    }

    public String getParent() {
        return parent;
    }

    public String getworkOrderId() {
        return workOrderId;
    }
}