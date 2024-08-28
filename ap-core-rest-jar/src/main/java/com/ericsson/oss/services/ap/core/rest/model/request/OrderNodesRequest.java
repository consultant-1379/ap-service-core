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

package com.ericsson.oss.services.ap.core.rest.model.request;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;

/**
 * Represents the body payload used for the order nodes endpoint.
 */
public class OrderNodesRequest {

    @NotNull
    @Size(min = 1, message = "You need to provide at least one node ID.")
    private Set<String> nodeIds = new LinkedHashSet<>();

    /**
     * Gets a Set of unique node IDs in a maintained order.
     * @return The Set of unique node IDs in a maintained order
     */
    public Set<String> getNodeIds() {
        return nodeIds;
    }

    /**
     * Set the node IDs.
     * @param nodeIds the list of node IDs to set.
     */
    public void setNodeIds(final Set<String> nodeIds) {
        this.nodeIds = nodeIds;
    }

}
