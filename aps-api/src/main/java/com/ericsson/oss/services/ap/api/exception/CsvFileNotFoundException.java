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
package com.ericsson.oss.services.ap.api.exception;

import javax.ejb.ApplicationException;

/**
 * Exception thrown when the Archive missing the required CSV.
 */
@ApplicationException(rollback = false)
public class CsvFileNotFoundException extends ApServiceException {
    private final String generatedCsvName;


    public CsvFileNotFoundException(final String message, final String generatedCsvName) {
        super(message);
        this.generatedCsvName = generatedCsvName;
    }

    public CsvFileNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
        this.generatedCsvName = null;
    }

    public String getGeneratedCsvName() {
        return generatedCsvName;
    }

}
