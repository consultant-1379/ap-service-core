/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
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
 * Exception thrown when the Health Check Profile not found in NHC.
 */
public class HealthCheckProfileNotFoundException extends ApServiceException {

    private static final long serialVersionUID = 925620226283942154L;

    /**
     * Exception with message information only.
     *
     * @param message
     *            description of the exception
     */
    public HealthCheckProfileNotFoundException(final String message) {
        super(message);
    }
}
