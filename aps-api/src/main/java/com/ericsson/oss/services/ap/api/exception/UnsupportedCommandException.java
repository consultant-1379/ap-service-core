/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
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
 * Thrown to indicate the node does not support the command.
 */
public class UnsupportedCommandException extends ApApplicationException {

    private static final long serialVersionUID = -5265855944175643418L;

    /**
     * Exception with message information only.
     *
     * @param message
     *            description of the exception
     */
    public UnsupportedCommandException(final String message) {
        super(message);
    }
}
