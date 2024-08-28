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

import org.xml.sax.SAXException;

/**
 * Thrown when an artifact fails to validate against a schema.
 */
public class SchemaValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private static final String SCHEMA_ERROR_DELIMITER = ":";
    private final String validationError;

    public SchemaValidationException(final String xsdErrorMessage) {
        super(xsdErrorMessage);
        validationError = constructXsdValidationError(xsdErrorMessage);
    }

    /**
     * Handle the XSD Schema SAXException and construct valid output error message
     *
     * @param message
     *            thrown SAXException message
     * @param xsdException
     *            exception from XSD Schema validation
     */
    public SchemaValidationException(final String message, final SAXException xsdException) {
        super(message, xsdException);
        validationError = constructXsdValidationError(message);
    }

    private static String constructXsdValidationError(final String message) {
        // Validation errors can come in the form"cvc-complex-type.2.4.a: <error>"
        // Just output the <error>
        return message.contains(SCHEMA_ERROR_DELIMITER) ? message.split(SCHEMA_ERROR_DELIMITER, 2)[1].trim() : message;
    }

    public String getValidationError() {
        return validationError;
    }
}