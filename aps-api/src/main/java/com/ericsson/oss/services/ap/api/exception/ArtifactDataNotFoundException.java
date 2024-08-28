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

import javax.ejb.ApplicationException;

/**
 * Schema-related data does not exist exception.
 */
@ApplicationException(rollback = false)
public class ArtifactDataNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -95241606411561579L;

    /**
     * Exception with message information only.
     *
     * @param message
     *            description of the exception
     */
    public ArtifactDataNotFoundException(final String message) {
        super(message);
    }

    /**
     * Exception taking a Throwable.
     *
     * @param error
     *            the cause exception
     */
    public ArtifactDataNotFoundException(final Throwable error) {
        super(error);
    }

    /**
     * Exception with message information and caused Throwable.
     *
     * @param message
     *            description of the exception
     * @param error
     *            the cause exception
     */
    public ArtifactDataNotFoundException(final String message, final Throwable error) {
        super(message, error);
    }
}
