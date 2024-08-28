/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
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
 * Thrown to indicate that an error has occurred in the execution of the deletion of a hardware replace node (or project containing hardware replace
 * nodes).
 */
public class HardwareReplaceDeleteException extends ApApplicationException {

    private static final long serialVersionUID = -4743017867301452032L;

    /**
     * Exception with exception name only.
     */
    public HardwareReplaceDeleteException() {

    }

    /**
     * Exception with message information only.
     *
     * @param message
     *            description of the exception
     */
    public HardwareReplaceDeleteException(final String message) {
        super(message);
    }

    /**
     * Exception taking a Throwable.
     *
     * @param exception
     *            the cause exception
     */
    public HardwareReplaceDeleteException(final Throwable exception) {
        super(exception);
    }

    /**
     * Exception with message information and caused Throwable.
     *
     * @param message
     *            description of the exception
     * @param exception
     *            the cause exception
     */
    public HardwareReplaceDeleteException(final String message, final Throwable exception) {
        super(message, exception);
    }
}
