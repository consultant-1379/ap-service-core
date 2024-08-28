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
 * Thrown to indicate executing the skip command at this point is illegal.
 */
public class IllegalSkipOperationException extends ApApplicationException {

    private static final long serialVersionUID = 8612061234543726776L;

    /**
     * Exception taking a Throwable.
     *
     * @param exception
     *            the cause exception
     */
    public IllegalSkipOperationException(final Throwable exception) {
        super(exception);
    }
}
