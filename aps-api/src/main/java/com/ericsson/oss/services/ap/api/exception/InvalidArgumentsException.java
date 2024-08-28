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
package com.ericsson.oss.services.ap.api.exception;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * Thrown to indicate that arguments passed in REST request are invalid.
 */
public class InvalidArgumentsException extends ApApplicationException {

    private static final long serialVersionUID = -7301453851160774847L;

    private static final String INVALID_ARGUMENTS_PROPERTY = "invalid.arguments.";

    /**
     * Exception with message information only.
     *
     * @param message
     *            description of the exception
     */
    public InvalidArgumentsException(final String message) {
        super(message);
    }

    @Override
    public int getHttpCode() {
        return HTTP_BAD_REQUEST;
    }

    @Override
    public String getErrorPropertyName() {
        return INVALID_ARGUMENTS_PROPERTY;
    }
}
