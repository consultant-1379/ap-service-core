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

import java.util.List;

/**
 * POJO model for representing project status.
 */
public class ProjectStatus {

    private final String id;
    private final List<NodeSummary> nodeSummary;

    public ProjectStatus(final String id, final List<NodeSummary> nodeSummary) {
        this.id = id;
        this.nodeSummary = nodeSummary;
    }

    public String getId() {
        return id;
    }

    public List<NodeSummary> getNodeSummary() {
        return nodeSummary;
    }
}
