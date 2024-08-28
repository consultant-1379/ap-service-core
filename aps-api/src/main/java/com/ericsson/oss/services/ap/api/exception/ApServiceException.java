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

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;

/**
 * Superclass exception which covers all exceptions potentially thrown by the AutoProvisioningService. Thrown to indicate that some error occurred in
 * AutoProvisioningService.
 */
public class ApServiceException extends RuntimeException {

    private static final long serialVersionUID = -3755360799655629127L;

    private static final String INTERNAL_ERROR_PROPERTY = "internal.server.error.";

    /**
     * Exception with exception name only.
     */
    ApServiceException() {

    }

    /**
     * Exception with message information only.
     *
     * @param message
     *            description of the exception
     */
    public ApServiceException(final String message) {
        super(message);
    }

    /**
     * Exception taking a Throwable.
     *
     * @param exception
     *            the cause exception
     */
    public ApServiceException(final Throwable exception) {
        super(exception);
    }

    /**
     * Exception with message information only.
     *
     * @param message
     *            description of the exception
     * @param exception
     *            the cause exception
     */
    public ApServiceException(final String message, final Throwable exception) {
        super(message, exception);
    }

    /**
     * Used to retrieve appropriate http response code to exception.
     *
     * @return http status code
     */
    public int getHttpCode() {
        return HTTP_INTERNAL_ERROR;
    }

    /**
     * Used to retrieve appropriate error property name to exception.
     *
     * @return property name
     */
    public String getErrorPropertyName() {
        return INTERNAL_ERROR_PROPERTY;
    }
}
