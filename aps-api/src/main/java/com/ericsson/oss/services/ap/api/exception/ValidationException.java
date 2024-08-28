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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * Exception contains the details of validation failures and is used to pass this back to the user.
 */
public class ValidationException extends ApApplicationException {

    private static final long serialVersionUID = 4642031532086184273L;

    private static final String VALIDATION_ERROR_PROPERTY = "validation.error.";

    private final List<String> validationFailures;

    /**
     * Exception with message information and validation failures.
     *
     * @param validationFailures
     *         a list of the validation failures to be shown to the user
     * @param message
     *         description of the exception
     */
    public ValidationException(final List<String> validationFailures, final String message) {
        super(message);
        this.validationFailures = validationFailures;
    }

    /**
     * Exception with message information. a list of the validation failures to be shown to the user
     *
     * @param message
     *         description of the exception
     */
    public ValidationException(final String message) {
        super(message);
        validationFailures = new ArrayList<>(0);
    }

    /**
     * Exception taking a Throwable.
     *
     * @param throwable
     *         the cause exception
     */
    public ValidationException(final Throwable throwable) {
        super(throwable);
        validationFailures = new ArrayList<>(0);
    }

    /**
     * Get all validation failure for this exception.
     *
     * @return a list of the validation failures.
     */
    public List<String> getValidationFailures() {
        return Collections.unmodifiableList(validationFailures);
    }

    @Override
    public int getHttpCode() {
        return HTTP_BAD_REQUEST;
    }

    @Override
    public String getErrorPropertyName() {
        return VALIDATION_ERROR_PROPERTY;
    }
}
