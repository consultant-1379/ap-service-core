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
import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Represents the Body payload used in the delete nodes endpoint
 */
public class DeleteNodesRequest {

    /**
     * Indicates if the Network Elements should be ignored in the deletion
     */
    private boolean ignoreNetworkElement = false;

    /**
     * List of node IDs to be deleted
     */
    @NotNull
    @Size(min = 1, message = "You need to provide at least one node ID.")
    private Set<String> nodeIds = new LinkedHashSet<>();

    public boolean isIgnoreNetworkElement() {
        return ignoreNetworkElement;
    }

    public void setIgnoreNetworkElement(final boolean ignoreNetworkElement) {
        this.ignoreNetworkElement = ignoreNetworkElement;
    }

    /**
     * Gets a Set of unique node IDs in a maintained order.
     * @return The Set of unique node IDs in a maintained order
     */
    public Set<String> getNodeIds() {
        return nodeIds;
    }

    public void setNodeIds(final Set<String> nodeIds) {
        this.nodeIds = nodeIds;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DeleteNodesRequest that = (DeleteNodesRequest) o;
        return ignoreNetworkElement == that.ignoreNetworkElement &&
            Objects.equals(nodeIds, that.nodeIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ignoreNetworkElement, nodeIds);
    }

}
