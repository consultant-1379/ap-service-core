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
 * Thrown to indicate that an AP node MO could not be found.
 */
public class NodeNotFoundException extends ApApplicationException {

    private static final long serialVersionUID = 7785708731400727206L;

    /**
     * Exception with message information only.
     *
     * @param message
     *            description of the exception
     */
    public NodeNotFoundException(final String message) {
        super(message);
    }
}
