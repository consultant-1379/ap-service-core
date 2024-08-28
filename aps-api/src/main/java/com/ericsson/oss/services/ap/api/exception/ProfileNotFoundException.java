/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.exception;

import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

/**
 * Thrown to indicate that an AP profile MO could not be found.
 */
public class ProfileNotFoundException extends ApApplicationException {

    private static final long serialVersionUID = -7801408861160774847L;

    private static final String PROFILE_NOT_FOUND_PROPERTY = "profile.not.found.";

    /**
     * Exception with message information only.
     *
     * @param message
     *         description of the exception
     */
    public ProfileNotFoundException(final String message) {
        super(message);
    }

    @Override
    public int getHttpCode() {
        return HTTP_NOT_FOUND;
    }

    @Override
    public String getErrorPropertyName() {
        return PROFILE_NOT_FOUND_PROPERTY;
    }
}
