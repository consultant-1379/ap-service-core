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
package com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class containing the error response for the Node Plugin validation.
 */
public class ErrorResponse {

    @JsonProperty("error")
    private ErrorDetails details;

    public ErrorDetails getDetails() {
        return details;
    }

    public void setDetails(ErrorDetails details) {
        this.details = details;
    }
}
