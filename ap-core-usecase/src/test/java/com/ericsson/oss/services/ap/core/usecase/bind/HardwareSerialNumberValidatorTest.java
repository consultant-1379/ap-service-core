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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link HardwareSerialNumberValidator}.
 */
public class HardwareSerialNumberValidatorTest {

    private static final String VALID_HARDWARE_SERIAL_NUMBER_10 = "ABC1234567";
    private static final String VALID_HARDWARE_SERIAL_NUMBER_13 = "DEFABC1234567";
    private static final String HARDWARE_SERIAL_NUMBER_9_CHARS = "ABC123456";
    private static final String HARDWARE_SERIAL_NUMBER_14_CHARS = "ABCDABC1234567";
    private static final String HARDWARE_SERIAL_NUMBER_WITH_I = "ABC123I567";
    private static final String HARDWARE_SERIAL_NUMBER_WITH_O = "ABC1O34567";
    private static final String HARDWARE_SERIAL_NUMBER_WITH_SPACES = "ABC1 34567";
    private static final String VALID_HARDWARE_SERIAL_NUMBER_BRACKETS = "(B)1234567";
    private static final String HARDWARE_SERIAL_NUMBER_LOWERCASE_LETTER = "abc1234567";

    @Test
    public void whenHwSerialNumberIsCorrectFormatThenValidationSucceeds() {
        boolean validateResult = HardwareSerialNumberValidator.isValidSerialNumber(VALID_HARDWARE_SERIAL_NUMBER_10);
        assertTrue(validateResult);
        validateResult = HardwareSerialNumberValidator.isValidSerialNumber(VALID_HARDWARE_SERIAL_NUMBER_13);
        assertTrue(validateResult);
    }

    @Test
    public void whenHwSerialContainsBracketsAndIsCorrectLengthThenValidationSucceeds() {
        final boolean validateResult = HardwareSerialNumberValidator.isValidSerialNumber(VALID_HARDWARE_SERIAL_NUMBER_BRACKETS);
        assertTrue(validateResult);
    }

    @Test
    public void whenHwSerialNumberContainsIThenValidationFails() {
        final boolean validateResult = HardwareSerialNumberValidator.isValidSerialNumber(HARDWARE_SERIAL_NUMBER_WITH_I);
        assertFalse(validateResult);
    }

    @Test
    public void whenHwSerialNumberContainsOThenValidationFails() {
        final boolean validateResult = HardwareSerialNumberValidator.isValidSerialNumber(HARDWARE_SERIAL_NUMBER_WITH_O);
        assertFalse(validateResult);
    }

    @Test
    public void whenHwSerialNumberContainsSpacesThenValidationFails() {
        final boolean validateResult = HardwareSerialNumberValidator.isValidSerialNumber(HARDWARE_SERIAL_NUMBER_WITH_SPACES);
        assertFalse(validateResult);
    }

    @Test
    public void whenHwSerialNumberIsLessThanTenCharactersThenValidationFails() {
        final boolean validateResult = HardwareSerialNumberValidator.isValidSerialNumber(HARDWARE_SERIAL_NUMBER_9_CHARS);
        assertFalse(validateResult);
    }

    @Test
    public void whenHwSerialNumberIsGreaterThanThirteenCharactersThenValidationFails() {
        final boolean validateResult = HardwareSerialNumberValidator.isValidSerialNumber(HARDWARE_SERIAL_NUMBER_14_CHARS);
        assertFalse(validateResult);
    }

    @Test
    public void whenHwSerialNumberContainsLowercaseLetterThenValidationFails() {
        final boolean validateResult = HardwareSerialNumberValidator.isValidSerialNumber(HARDWARE_SERIAL_NUMBER_LOWERCASE_LETTER);
        assertFalse(validateResult);
    }
}