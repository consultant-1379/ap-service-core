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
package com.ericsson.oss.services.ap.core.test.statements;

import static com.ericsson.oss.services.ap.core.test.validation.CsvValidationAssert.assertCSVValidationException;
import static com.ericsson.oss.services.ap.core.test.validation.ValidationAssert.assertValidationException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.CatchException.resetCaughtException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.googlecode.catchexception.CatchException;

import cucumber.api.java.en.Then;

public class ErrorStatements extends ServiceCoreTestStatements {

    @EServiceRef
    private ResourceService resourceService;

    @Then("^(an|no) error will occur")
    public void check_for_error(final String error) {
        if ("an".equals(error)) {
            assertThat(CatchException.<Exception>caughtException()).as("Error should occur").isNotNull();
        } else if (caughtException() != null) {
            fail("No error was expected at this point", caughtException());
        }
    }

    @Then("^the error will have type '(.+)'$")
    public void check_error_type(final String errorType) {
        final String name = caughtException()
                .getClass()
                .getSimpleName();

        assertThat(name).as("Error Type").isEqualTo(errorType);
    }

    @Then("^the error message will contain '(.+)'$")
    public void validate_error_message_containing(final String expectedError) {
        assertThat(CatchException.<Exception>caughtException()).as("Current error message").hasMessageContaining(expectedError);
    }

    @Then("^the validation error message will be '(.+)'")
    public void validate_error_message(final String expectedException) {
        assertValidationException(caughtException()).has(expectedException);
    }

    @Then("^the validation error message will contain '(.+)'")
    public void validate_validation_error_message_contains(final String expectedException) {
        assertValidationException(caughtException()).hasMessageContaining(expectedException);
    }

    @Override
    public void clear() {
        resetCaughtException();
    }

    @Then("^validate the CSV for the filename '(.+)', validation message '(.+)'$")
    public void validate_csv_error_message(final String fileName, final String message) {
        assertCSVValidationException(caughtException(), fileName, resourceService, message);
    }
}
