/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.constant;

import com.ericsson.oss.services.ap.api.exception.InvalidArgumentsException;

/**
 * Enum representing valid query parameters for AP REST endpoints.
 */
public enum ApQueryParameter {

    PROPERTIES("properties"),
    STATUS("status");

    private String parameterName;

    ApQueryParameter(final String parameterName) {
        this.parameterName = parameterName;
    }

    /**
     * Matches a {@link ApQueryParameter} for a {@link String} representing a {@link ApQueryParameter}.
     *
     * @param parameterName
     *            the {@link String} representation of the {@link ApQueryParameter}
     * @return the matching {@link ApQueryParameter}
     * @throws IllegalArgumentException if no match is found or {@link String} is null.
     */
    public static ApQueryParameter getEnumParameterName(final String parameterName) {
        for(final ApQueryParameter query : ApQueryParameter.values()) {
            if(query.parameterName.equalsIgnoreCase(parameterName)) {
                return query;
            }
        }
        throw new InvalidArgumentsException (String.format("Invalid query parameter %s specified. ", parameterName));
    }

    /**
    * Gets the query parameter name in {@link String} form.
    * @return {@link String} the parameter name
    */
    public String getParameterName() {
        return this.parameterName;
    }
}