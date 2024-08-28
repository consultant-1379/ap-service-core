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
 * Indicates that hardware bind failed because the specified serial number is not in the correct format.
 */
public class HwIdInvalidFormatException extends ApApplicationException {

    private static final long serialVersionUID = 1L;

    /**
     * Exception with message information only.
     *
     * @param message
     *            description of the exception
     */
    public HwIdInvalidFormatException(final String message) {
        super(message);
    }
}
