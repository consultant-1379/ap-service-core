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
 * Contains the status information for an AP node, as well as some basic node information.
 */
public class NodeStatusData {

    private final String id;
    private final String projectId;
    private final String state;
    private final List<StatusEntryData> statusEntries;

    /**
     * Constructs an instance of {@link NodeStatusData}.
     *
     * @param id            the name of the node
     * @param projectId     the name of the project
     * @param statusEntries a list of the node status entries
     * @param state         the current node state
     */
    public NodeStatusData(final String id, final String projectId, final String state, final List<StatusEntryData> statusEntries) {
        this.id = id;
        this.projectId = projectId;
        this.state = state;
        this.statusEntries = statusEntries;
    }

    /**
     * The AP node's logical name.
     *
     * @return the node name
     */
    public String getId() {
        return id;
    }

    /**
     * The node's parent project name.
     *
     * @return the project name
     */
    public String getProjectId() {
        return projectId;
    }

    /**
     * The AP node's <code>state</code> attribute value.
     *
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * The {@link StatusEntryData}s for an AP node.
     *
     * @return a list of status entries for the node
     */
    public List<StatusEntryData> getStatusEntries() {
        return statusEntries;
    }
}
