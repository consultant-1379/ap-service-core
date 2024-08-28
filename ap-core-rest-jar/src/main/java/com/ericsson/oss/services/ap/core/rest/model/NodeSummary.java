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
 * POJO model for representing node summary of a project.
 */
public class NodeSummary {

    private final String id;
    private final String status;
    private final String state;

    public NodeSummary(final String id, final String status, final String state) {
        this.id = id;
        this.status = status;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getState() {
        return state;
    }
}
