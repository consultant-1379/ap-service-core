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
package com.ericsson.oss.services.ap.core.rest.model;

/**
 * POJO model for representing a Project Node.
 */
public class ProjectNode {

    private final String id;
    private final String type;
    private final String identifier;
    private final String ipAddress;

    public ProjectNode(final String id, final String type, final String identifier, final String ipAddress) {
        this.id = id;
        this.type = type;
        this.identifier = identifier;
        this.ipAddress = ipAddress;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
