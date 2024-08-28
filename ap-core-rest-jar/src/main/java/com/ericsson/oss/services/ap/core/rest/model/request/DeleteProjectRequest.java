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
package com.ericsson.oss.services.ap.core.rest.model.request;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Represents the Body payload used in the delete project endpoint
 */
public class DeleteProjectRequest {

    /**
     * Indicates if the Network Elements should be ignored in the deletion
     */
    private boolean ignoreNetworkElement = false;

    @NotNull
    @Size(min = 1, message = "You need to provide at least one project ID.")
    private Set<String> projectIds = new LinkedHashSet<>();

    public boolean isIgnoreNetworkElement() {
        return ignoreNetworkElement;
    }

    public void setIgnoreNetworkElement(final boolean ignoreNetworkElement) {
        this.ignoreNetworkElement = ignoreNetworkElement;
    }

    /**
     * Gets a Set of unique project IDs in a maintained order.
     * @return The Set of unique project IDs in a maintained order
     */
    public Set<String> getProjectIds() {
        return projectIds;
    }

    public void setProjectIds(final Set<String> projectIds) {
        this.projectIds = projectIds;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DeleteProjectRequest that = (DeleteProjectRequest) o;
        return ignoreNetworkElement == that.ignoreNetworkElement &&
            Objects.equals(projectIds, that.projectIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ignoreNetworkElement, projectIds);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeleteProjectRequest{");
        sb.append("ignoreNetworkElement=").append(ignoreNetworkElement);
        sb.append(", projectIds=").append(projectIds);
        sb.append('}');
        return sb.toString();
    }
}

