/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.status;

import java.io.Serializable;
import java.util.List;

/**
 * Contains the status information for an AP node, as well as some basic node information.
 */
public class NodeStatus implements Serializable {

    private static final long serialVersionUID = 8459922889074841214L;

    private final String nodeName;
    private final String projectName;
    private final List<StatusEntry> statusEntries;
    private final String state;
    private final IntegrationPhase integrationPhase;

    /**
     * Constructs an instance of {@link NodeStatus}.
     *
     * @param nodeName
     *            the name of the node
     * @param projectName
     *            the name of the project
     * @param statusEntries
     *            a list of the node status entries
     * @param state
     *            the current node state
     */
    public NodeStatus(final String nodeName, final String projectName, final List<StatusEntry> statusEntries, final String state) {
        this.nodeName = nodeName;
        this.projectName = projectName;
        this.statusEntries = statusEntries;
        this.state = state;
        integrationPhase = IntegrationPhase.getIntegrationPhase(state);
    }

    /**
     * The AP node's logical name.
     *
     * @return the node name
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * The node's parent project name.
     *
     * @return the project name
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * The {@link StatusEntry}s for an AP node.
     *
     * @return a list of status entries for the node
     */
    public List<StatusEntry> getStatusEntries() {
        return statusEntries;
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
     * The AP node's {@link IntegrationPhase}.
     *
     * @return the integrationPhase
     */
    public IntegrationPhase getIntegrationPhase() {
        return integrationPhase;
    }
}
