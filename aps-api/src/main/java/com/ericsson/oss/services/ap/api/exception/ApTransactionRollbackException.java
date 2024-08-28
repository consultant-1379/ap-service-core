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
 * Base class for AutoProvisioningService exceptions which are considered fatal to the ongoing transaction. The throwing of such an exception will
 * result in the current transaction being marked as rollback only.
 */
@ApplicationException(rollback = true)
public class ApTransactionRollbackException extends ApServiceException {

    private static final long serialVersionUID = -8181147209206563156L;

    /**
     * Exception with message information only.
     *
     * @param message
     *            description of the exception
     */
    public ApTransactionRollbackException(final String message) {
        super(message);
    }

    /**
     * Exception taking a Throwable.
     *
     * @param exception
     *            the cause exception
     */
    public ApTransactionRollbackException(final Throwable exception) {
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
    public ApTransactionRollbackException(final String message, final Throwable exception) {
        super(message, exception);
    }
}
