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
package com.ericsson.oss.services.ap.core.test.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.assertj.core.api.AbstractThrowableAssert;

import com.ericsson.oss.services.ap.api.exception.ValidationException;

/**
 * This class extends the ThrowableAssert class from AssertJ to allow a more readable way of asserting on exception messages.
 */
public class ValidationAssert extends AbstractThrowableAssert<ValidationAssert, ValidationException> {

    public static ValidationAssert assertValidationException(final Throwable actual) {
        final ValidationException cause = extractValidationException(actual);
        if (cause == null) {
            final String stackTrace = getStackTraceAsString(actual);
            fail("No ValidationException found in stack. Original exception was: " + stackTrace);
        }

        return new ValidationAssert(cause);
    }

    private static String getStackTraceAsString(final Throwable actual) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        actual.printStackTrace(pw);
        return sw.toString();
    }

    private static ValidationException extractValidationException(final Throwable actual) {
        if (actual == null) {
            return null;
        } else if (actual instanceof ValidationException) {
            return (ValidationException) actual;
        } else {
            return extractValidationException(actual.getCause());
        }
    }

    private ValidationAssert(final ValidationException actual) {
        super(actual, ValidationAssert.class);
    }

    /**
     * Validates if the exception messages are all contained within the messages in the exception
     *
     * @param messages
     *            the messages to be validated
     */
    public void has(final String... messages) {
        assertThat(actual.getValidationFailures()).contains(messages);
    }

    /**
     * Validates if the exception has this message, and this message only.
     *
     * @param message
     *            the message to be validated
     */
    public void is(final String message) {
        are(message);
    }

    /**
     * Validates if the exception has these messages, and these messages only.
     *
     * @param messages
     *            the messages to be validated
     */
    public void are(final String... messages) {
        assertThat(actual.getValidationFailures()).containsOnly(messages);
    }

}
