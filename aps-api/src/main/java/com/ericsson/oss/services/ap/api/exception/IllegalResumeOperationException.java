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
 * Thrown to indicate executing the resume command at this point is illegal.
 */
public class IllegalResumeOperationException extends ApApplicationException {

    private static final long serialVersionUID = 8612064278843721317L;

    /**
     * Exception taking a Throwable.
     *
     * @param exception
     *            the cause exception
     */
    public IllegalResumeOperationException(final Throwable exception) {
        super(exception);
    }
}
