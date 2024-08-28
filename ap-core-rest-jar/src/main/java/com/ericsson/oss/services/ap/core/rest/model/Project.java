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
import java.util.Map;

import com.ericsson.oss.services.ap.api.model.node.Node;

/**
 * POJO model for representing a Project.
 */
public class Project {

    private static final int ROOT_PARENT_ID_OF_PROJECT = 0;

    public static final String PROJECT_ID = "id";
    public static final String CREATION_DATE = "creationDate";
    public static final String PROJECT_CREATOR = "creator";
    public static final String GENERATED_BY = "generatedby";
    public static final String PROJECT_DESC = "description";
    public static final String PROJECT_NODE_QUANTITY = "children";
    public static final String NODES_LIST = "nodes";
    public static final String INTEGRATION_PROFILE = "integrationProfile";
    public static final String EXPANSION_PROFILE = "expansionProfile";

    private String id;
    private String label;
    private String creationDate;
    private String creator;
    private String generatedby;
    private String description;
    private int parent;
    private int children;
    private List<Node> nodes;
    private String integrationProfile;
    private String expansionProfile;

    public Project(final Map<String, Object> attributes) {
        this.id = (String) attributes.get(PROJECT_ID);
        this.label = (String) attributes.get(PROJECT_ID);
        this.creationDate = (String) attributes.get(CREATION_DATE);
        this.creator = (String) attributes.get(PROJECT_CREATOR);
        this.generatedby = (String) attributes.get(GENERATED_BY);
        this.description = (String) attributes.get(PROJECT_DESC);
        this.parent = ROOT_PARENT_ID_OF_PROJECT;
        this.nodes = (List<Node>) attributes.get(NODES_LIST);
        this.children = (int) attributes.get(PROJECT_NODE_QUANTITY);
        this.integrationProfile = (String) attributes.get(INTEGRATION_PROFILE);
        this.expansionProfile = (String) attributes.get(EXPANSION_PROFILE);
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public String getCreator() {
        return creator;
    }

    public String getGeneratedby() {
        return generatedby;
    }

    public String getDescription() {
        return description;
    }

    public int getParent() {
        return parent;
    }

    public int getChildren() {
        return children;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public String getExpansionProfile() {
        return expansionProfile;
    }

    public String getIntegrationProfile() {
        return integrationProfile;
    }

}
