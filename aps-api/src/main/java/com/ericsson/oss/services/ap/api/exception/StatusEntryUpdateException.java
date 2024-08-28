/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
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
 * Indicates that an error that has occurred when updating a <code>NodeStatus</code> MO's <i>statusEntry</i> attribute.
 */
public class StatusEntryUpdateException extends ApApplicationException {

    private static final long serialVersionUID = 1L;

    /**
     * Exception with message information only.
     *
     * @param message
     *            description of the exception
     */
    public StatusEntryUpdateException(final String message) {
        super(message);
    }
}
