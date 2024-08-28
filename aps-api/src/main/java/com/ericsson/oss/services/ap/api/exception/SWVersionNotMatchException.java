/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
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
 * Thrown to indicate that the backup SW version doesn't match the node's current software version.
 */
public class SWVersionNotMatchException extends ApServiceException {

    private static final long serialVersionUID = 92562024358577456L;

    /**
     * Exception with message information only.
     *
     * @param message
     *            description of the exception
     */
    public SWVersionNotMatchException(final String message) {
        super(message);
    }

    /**
     * Exception with message information and caused Throwable.
     *
     * @param message
     *            description of the exception
     * @param exception
     *            the cause exception
     */
    public SWVersionNotMatchException(final String message, final Throwable exception) {
        super(message, exception);
    }
}