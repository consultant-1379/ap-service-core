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
package com.ericsson.oss.services.ap.core.usecase.importproject.batch.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link ReservedSubstitutionTagChecker}.
 */
public class ReservedSubstitutionTagCheckerTest {

    @Test
    public void whenCheckingReservedPrefix_thenIsReserved() {
        final boolean result = ReservedSubstitutionTagChecker.isReserved("INTERNAL_test");
        assertTrue(result);
    }

    @Test
    public void whenCheckingReservedSubstitutionTag_thenIsReserved() {
        final boolean result = ReservedSubstitutionTagChecker.isReserved("logicalName");
        assertTrue(result);
    }

    @Test
    public void whenCheckingReservedPrefix_withIncorrectCase_thenIsNotReserved() {
        final boolean result = ReservedSubstitutionTagChecker.isReserved("internal_test");
        assertFalse(result);
    }

    @Test
    public void whenCheckingReservedSubstitutionTag_withIncorrectCase_thenIsNotReserved() {
        final boolean result = ReservedSubstitutionTagChecker.isReserved("LogicalName");
        assertFalse(result);
    }

    @Test
    public void whenCheckingNull_thenIsNotReserved() {
        final boolean result = ReservedSubstitutionTagChecker.isReserved(null);
        assertFalse(result);
    }

    @Test
    public void whenCheckingEmptyString_thenIsNotReserved() {
        final boolean result = ReservedSubstitutionTagChecker.isReserved("");
        assertFalse(result);
    }
}
