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
 * The node type is not unsupported for the given operation.
 */
public class UnsupportedNodeTypeException extends ApApplicationException {

    private static final long serialVersionUID = -7737885960842782652L;

    /**
     * Exception with message information only.
     *
     * @param message
     *            description of the exception
     */
    public UnsupportedNodeTypeException(final String message) {
        super(message);
    }
}
