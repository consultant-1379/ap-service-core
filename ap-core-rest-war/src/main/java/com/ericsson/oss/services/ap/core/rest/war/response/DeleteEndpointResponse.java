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
package com.ericsson.oss.services.ap.core.rest.war.response;

import java.util.Objects;

/**
 * Response payload for the delete endpoint
 */
public class DeleteEndpointResponse {

    /**
     * id being deleted
     */
    private String id;

    /**
     * Error message
     */
    private String errorMessage;

    public DeleteEndpointResponse(final String id, final String errorMessage) {
        this.id = id;
        this.errorMessage = errorMessage;
    }

    public DeleteEndpointResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DeleteEndpointResponse that = (DeleteEndpointResponse) o;
        return Objects.equals(id, that.id) &&
            Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, errorMessage);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DeleteEndpointResponse{");
        sb.append("id='").append(id).append('\'');
        sb.append(", errorMessage='").append(errorMessage).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
