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

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.ericsson.oss.services.ap.core.rest.validation.ValidName;

/**
 * Represents the Body payload used in the create project endpoint
 */
public class ProjectRequest {

    @ValidName
    private String name;

    private String description = "";

    @Size(min = 1, message = "Creator is mandatory and must be a meaningful username greater than or equal to 1 character(s)")
    @NotNull
    private String creator;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(final String creator) {
        this.creator = creator;
    }

}
