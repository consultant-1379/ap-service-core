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
 * Thrown to indicate executing the download artifact command at this point is illegal.
 */
public class IllegalDownloadArtifactException extends ApApplicationException {

    private static final long serialVersionUID = -2567092972761719178L;

    /**
     * Exception with message information only.
     *
     * @param message
     *            description of the exception
     */
    public IllegalDownloadArtifactException(final String message) {
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
    public IllegalDownloadArtifactException(final String message, final Throwable exception) {
        super(message, exception);
    }
}
