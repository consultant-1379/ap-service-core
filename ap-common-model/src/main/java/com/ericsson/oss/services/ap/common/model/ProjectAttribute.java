/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.model;

/**
 * An attribute in the AP <code>Project</code> model.
 */
public enum ProjectAttribute {

    CREATION_DATE("creationDate"),
    CREATOR("creator"),
    DESCRIPTION("description"),
    GENERATED_BY("generatedby"),
    NAME("name");

    private String attributeName;

    private ProjectAttribute(final String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public String toString() {
        return attributeName;
    }
}
