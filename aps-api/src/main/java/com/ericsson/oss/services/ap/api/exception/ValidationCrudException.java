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

/**
 * Thrown to indicate that node or project validation was terminated unexpectedly.
 */
public class ValidationCrudException extends ApApplicationException {

    private static final long serialVersionUID = -6095164755626275156L;

    /**
     * Exception with message information only.
     *
     * @param message
     *            description of the exception
     */
    public ValidationCrudException(final String message) {
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
    public ValidationCrudException(final String message, final Throwable exception) {
        super(message, exception);
    }
}
