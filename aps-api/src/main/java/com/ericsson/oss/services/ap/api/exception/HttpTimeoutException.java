/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.exception;

/**
 * Thrown to indicate RESTful response timeout occurs.
 */
public class HttpTimeoutException extends ApServiceException {

    private static final long serialVersionUID = 1L;

    /**
     * Exception with message information only.
     *
     * @param message
     *            description of the exception
     */
    public HttpTimeoutException(final String message) {
        super(message);
    }
}