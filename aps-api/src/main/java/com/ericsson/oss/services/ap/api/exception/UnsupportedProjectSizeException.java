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
 * Thrown to indicate that an AP project has unsupported size.
 */

public class UnsupportedProjectSizeException extends ApApplicationException { //NOSONAR

    private static final long serialVersionUID = 3221798368655775894L;

    private static final String PROJECT_UNSUPPORTED_SIZE = "validation.project.maximum.file.size.error";

    /**
     * Exception with message information only.
     *
     * @param message
     *         description of the exception
     */
    public UnsupportedProjectSizeException(final String message) {
        super(message);
    }

    @Override
    public String getErrorPropertyName() {
        return PROJECT_UNSUPPORTED_SIZE;
    }
}
