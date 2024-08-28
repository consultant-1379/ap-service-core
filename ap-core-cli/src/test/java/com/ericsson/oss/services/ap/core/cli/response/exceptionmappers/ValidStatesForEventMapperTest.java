/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.response.exceptionmappers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link ValidStatesForEventMapper}.
 */
public class ValidStatesForEventMapperTest {

    private final ValidStatesForEventMapper validStatesForEventMapper = new ValidStatesForEventMapper();

    @Test
    public void whenGetValidStates_thenStatesAreReturned_andInternalStatesAreNotDisplayed() {
        final String result = validStatesForEventMapper.getValidStates("order");
        assertEquals("Order Failed, Order Cancelled", result);
    }

    @Test
    public void whenGetValidStates_then_command_name_is_case_insensitive() {
        final String result = validStatesForEventMapper.getValidStates("BIND");
        assertEquals("Bind Completed, Order Completed, Hardware Replace Bind Completed, Pre Migration Bind Completed, Pre Migration Completed", result);
    }

    @Test
    public void whenGetValidStates_andInputCommandHasNoTransitionEvent_thenEmptyStringIsReturned() {
        final String result = validStatesForEventMapper.getValidStates("resume");
        assertTrue(result.isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void whenGetValidStates_command_name_is_null_then_throw_NullPointerException() {
        validStatesForEventMapper.getValidStates(null);

    }
}
