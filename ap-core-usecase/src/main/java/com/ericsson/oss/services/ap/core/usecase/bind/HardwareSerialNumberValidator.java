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
package com.ericsson.oss.services.ap.core.usecase.bind;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates hardware serial number using the follwing specifications:
 * <ul>
 * <li>Length of 10 to 13 characters</li>
 * <li>A-Z characters, except I and O</li>
 * <li>0-9 digits</li>
 * </ul>
 */
public final class HardwareSerialNumberValidator {

    private static final Pattern SERIAL_NUMBER_PATTERN = Pattern.compile("[A-HJ-NP-Z0-9()]{10,13}");

    private HardwareSerialNumberValidator() {

    }

    public static boolean isValidSerialNumber(final String hardwareSerialNumber) {
        final Matcher matcher = SERIAL_NUMBER_PATTERN.matcher(hardwareSerialNumber);
        return matcher.matches();
    }
}
