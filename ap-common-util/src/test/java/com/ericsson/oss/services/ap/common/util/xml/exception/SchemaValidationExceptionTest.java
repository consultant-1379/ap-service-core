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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link SchemaValidationException}.
 */
public class SchemaValidationExceptionTest {

    private static final String SAMPLE_SCHEMA_ERROR_MESSAGE = "cvc-elt.1: Cannot find the declaration of element 'RbsSiteInstallationFile'.";
    private static final String SAMPLE_SCHEMA_ERROR_OUTPUT_MESSAGE = "Cannot find the declaration of element 'RbsSiteInstallationFile'.";
    private static final String SAMPLE_NO_COLON_ERROR_MESSAGE = "[Fatal Error] XML document structures must start and end within the same entity.";
    private static final String SCHEMA_ERROR_PREFIX = "csv-complex-type.2.4.a: ";
    private static final String SCHEMA_ERROR = "<tag> was not found";

    private SchemaValidationException schemaValidationException;

    @Test
    public void schemaValidationExceptionSchemaString() {
        schemaValidationException = new SchemaValidationException(SAMPLE_SCHEMA_ERROR_MESSAGE);
        assertEquals(SAMPLE_SCHEMA_ERROR_OUTPUT_MESSAGE, schemaValidationException.getValidationError());
    }

    @Test
    public void schemaValidationExceptionNoColonString() {
        schemaValidationException = new SchemaValidationException(SAMPLE_NO_COLON_ERROR_MESSAGE);
        assertEquals(SAMPLE_NO_COLON_ERROR_MESSAGE, schemaValidationException.getValidationError());
    }

    @Test
    public void whenExceptionIsCreatedAndErrorMessageHasPrefixToBeRemovedThenExceptionReturnsMessageWithoutPrefix() {
        final SchemaValidationException exception = new SchemaValidationException(SCHEMA_ERROR_PREFIX + SCHEMA_ERROR);
        assertEquals(SCHEMA_ERROR, exception.getValidationError());
    }

    @Test
    public void whenExceptionIsCreatedAndErrorMessageHasNoPrefixThenExceptionReturnsSameMessage() {
        final SchemaValidationException exception = new SchemaValidationException(SCHEMA_ERROR);
        assertEquals(SCHEMA_ERROR, exception.getValidationError());
    }
}
