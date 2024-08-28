/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.util.xml.exception;

/**
 * Thrown when an artifact schema can't be accessed.
 */
public class SchemaAccessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SchemaAccessException(final String message) {
        super(message);
    }

    public SchemaAccessException(final String message, final Throwable exception) {
        super(message, exception);
    }
}