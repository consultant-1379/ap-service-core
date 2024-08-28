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
package com.ericsson.oss.services.ap.core.test.validation;

import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.assertj.core.api.AbstractThrowableAssert;

import com.ericsson.oss.services.ap.api.exception.CsvFileNotFoundException;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;

/**
 * This class extends the ThrowableAssert class from AssertJ to allow a more readable way of asserting on exception messages.
 */

public abstract class CsvValidationAssert extends AbstractThrowableAssert<CsvValidationAssert, CsvFileNotFoundException> {

    public static void assertCSVValidationException(final Throwable actual, final String fileName,final ResourceService resourceService, final String message) {
        final CsvFileNotFoundException cause = extractCSVValidationException(actual);

        if (cause == null) {
            final String stackTrace = getStackTraceAsString(actual);
            fail("No CsvFileNotFoundException found in stack. Original exception was: " + stackTrace);
        }

        validateCSV(fileName,cause,resourceService, message);
    }

    private static String getStackTraceAsString(final Throwable actual) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        actual.printStackTrace(pw);
        return sw.toString();
    }

    private static CsvFileNotFoundException extractCSVValidationException(final Throwable actual) {
        if (actual == null) {
            return null;
        } else if (actual instanceof CsvFileNotFoundException) {
            return (CsvFileNotFoundException) actual;
        }
        else {
            return extractCSVValidationException(actual.getCause());
        }

    }

    public CsvValidationAssert(final CsvFileNotFoundException actual) {
        super(actual, CsvValidationAssert.class);
    }


    private static void validateCSV(final String projectName, final CsvFileNotFoundException cause,final ResourceService resourceService, final String message)
    {
        List<String> variableList=new ArrayList<>();
        String patternValue= StringUtils.EMPTY;
        Matcher matcher;
        final String csvName=cause.getGeneratedCsvName();

        if (csvName==null) {
            final Throwable internalCause=cause.getCause();
            if(internalCause !=null && !message.equalsIgnoreCase(internalCause.getMessage())) {
                fail("Unknown Exception thrown while generating the CSV.Original exception was: " + internalCause);
            }
        }
        else{
            variableList = Arrays
                .asList(resourceService.getAsText(DirectoryConfiguration.getDownloadDirectory() + File.separator + cause
                    .getGeneratedCsvName()).split(","));
        }

        if(projectName.contains("unAccepted") && !variableList.isEmpty()) {
            fail("Unaccepted substitution variables are included in the generated CSV");
        }
        if(projectName.contains("[")) {
            matcher = Pattern.compile("(?<=\\[).+?(?=\\])").matcher(projectName);
            if (matcher.find ()) {
                patternValue = matcher.group();
            }
            if (csvName!=null && !csvName.contains(patternValue)) {
                fail("CSV not matched with project name");
            }
        }
        if(projectName.matches("\\d+")) {
            matcher = Pattern.compile ("\\d+").matcher(projectName);
            if (matcher.find ()) {
                patternValue = matcher.group();
            }
            if (patternValue!=null && !(variableList.size()==Integer.parseInt(patternValue))) {
                fail("All the substitution variables are not in the generated CSV");
            }
        }
        if(projectName.contains("no_subs") && !variableList.isEmpty()) {
            fail("No substitution variables are included in the project");
        }
    }
}
