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

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Class holds HTTP error response details.
 * Contextual details must be annotated as ignored or they will appear in the response body.
 */
public class ErrorResponse {

    @JsonIgnore
    private int httpResponseStatus;

    private String errorTitle;
    private String errorBody;
    private List<String> errorDetails;

    public int getHttpResponseStatus() {
        return httpResponseStatus;
    }

    public void setHttpResponseStatus(final int httpResponseStatus) {
        this.httpResponseStatus = httpResponseStatus;
    }

    public String getErrorTitle() {
        return errorTitle;
    }

    public void setErrorTitle(final String errorTitle) {
        this.errorTitle = errorTitle;
    }

    public List<String> getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(final List<String> errorDetails) {
        this.errorDetails = errorDetails;
    }

    public String getErrorBody() {
        return errorBody;
    }

    public void setErrorBody(final String errorBody) {
        this.errorBody = errorBody;
    }

    public static ErrorResponseBuilder builder() {
        return new ErrorResponseBuilder();
    }

    /**
     * Used to build {@link ErrorResponse} instances.
     */
    public static class ErrorResponseBuilder {

        private final ErrorResponse errorResponse;

        private ErrorResponseBuilder() {
            errorResponse = new ErrorResponse();
        }

        public ErrorResponseBuilder withErrorTitle(final String errorTitle) {
            errorResponse.setErrorTitle(errorTitle);
            return this;
        }

        public ErrorResponseBuilder withErrorDetails(final List<String> errorDetails) {
            errorResponse.setErrorDetails(errorDetails);
            return this;
        }

        public ErrorResponseBuilder withErrorBody(final String errorBody) {
            errorResponse.setErrorBody(errorBody);
            return this;
        }

        public ErrorResponseBuilder withHttpResponseStatus(final int status) {
            errorResponse.setHttpResponseStatus(status);
            return this;
        }

        public ErrorResponse build() {
            return errorResponse;
        }
    }
}
