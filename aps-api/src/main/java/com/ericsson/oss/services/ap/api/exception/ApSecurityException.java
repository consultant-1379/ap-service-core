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

import javax.ejb.ApplicationException;

/**
 * Base class for all AutoProvisioningService exceptions which are not considered fatal. The throwing of such an exception will not result in the
 * current transaction being marked as rollback only.
 */
@ApplicationException(rollback = false)
public class ApSecurityException extends ApServiceException {

    private static final long serialVersionUID = 925620225360759154L;

    /**
     * Exception with exception name only.
     */
    ApSecurityException() {

    }

    /**
     * Exception with message information only.
     *
     * @param message
     *            description of the exception
     */
    public ApSecurityException(final String message) {
        super(message);
    }

    /**
     * Exception taking a Throwable.
     *
     * @param exception
     *            the cause exception
     */
    public ApSecurityException(final Throwable exception) {
        super(exception);
    }

    /**
     * Exception with message information and caused Throwable.
     *
     * @param message
     *            description of the exception
     * @param exception
     *            the cause exception
     */
    public ApSecurityException(final String message, final Throwable exception) {
        super(message, exception);
    }
}
