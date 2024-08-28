/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.csv.generator.exception;

/**
 * Thrown when the csv is failed to generate.
 */
public class GenerateCsvFailedException extends Exception {

    private static final long serialVersionUID = 1L;

    public GenerateCsvFailedException(final String message) {
        super(message);
    }

    public GenerateCsvFailedException(final String message, final Throwable exception) {
        super(message, exception);
    }
}
