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
 * Defines the data object for a Project with their required properties.
 */
public class ProjectProperty {

    private final String id;
    private final String description;
    private final String creator;
    private final String creationDate;
    private final String generatedby;
    private final List<ProjectNode> nodes;

    public ProjectProperty(final String id, final String description, final String generatedby, final String creator, final String creationDate, final List<ProjectNode> nodes) {
        this.id = id;
        this.description = description;
        this.creator = creator;
        this.generatedby = generatedby;
        this.creationDate = creationDate;
        this.nodes = nodes;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getCreator() {
        return creator;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getGeneratedby() {
        return generatedby;
    }

    public List<ProjectNode> getNodes() {
        return nodes;
    }
}
