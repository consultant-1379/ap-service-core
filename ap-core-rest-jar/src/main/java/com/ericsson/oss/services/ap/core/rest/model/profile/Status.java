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
package com.ericsson.oss.services.ap.core.rest.model.profile;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * POJO model for Status object in {@link Profile}.
 */
public class Status {

    private boolean isValid;
    private List<String> details;

    public Status(final boolean isValid, final List<String> details) {
        this.isValid = isValid;
        this.details = details;
    }

    public Status() {
    }

    @JsonProperty("isValid")
    public boolean getIsValid() {
        return isValid;
    }


    public void setIsValid(final boolean isValid) {
        this.isValid = isValid;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(final List<String> details) {
        this.details = details;
    }
}
