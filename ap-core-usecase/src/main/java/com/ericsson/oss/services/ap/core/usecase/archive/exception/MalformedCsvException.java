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
package com.ericsson.oss.services.ap.core.usecase.archive.exception;

/**
 * Thrown when the csv file found in the project archive is malformed.
 */
public class MalformedCsvException extends RuntimeException {

    private static final long serialVersionUID = 2483793259172300616L;

    public MalformedCsvException(final Throwable t) {
        super(t);
    }

    public MalformedCsvException(final String message) {
        super(message);
    }
}