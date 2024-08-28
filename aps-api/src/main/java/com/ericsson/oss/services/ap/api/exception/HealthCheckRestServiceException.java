/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
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
 * Exception thrown when the Health Check Service response that cannot successfully finish request.
 */
public class HealthCheckRestServiceException extends ApApplicationException {

    private static final long serialVersionUID = 925620226283942154L;

    /**
     * Exception with message information only.
     *
     * @param message
     *            description of the exception
     */
    public HealthCheckRestServiceException(final String message) {
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
    public HealthCheckRestServiceException(final String message, final Throwable exception) {
        super(message, exception);
    }

    /**
     * Exception with default general message.
     */
    public HealthCheckRestServiceException() {
        super("General problem receiving response from Health Check Service");
    }

}
