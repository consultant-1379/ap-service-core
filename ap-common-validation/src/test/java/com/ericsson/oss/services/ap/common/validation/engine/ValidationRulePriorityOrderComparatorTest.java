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
package com.ericsson.oss.services.ap.common.validation.engine;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link ValidationRulePriorityOrderComparator}.
 */
public class ValidationRulePriorityOrderComparatorTest {

    private final ValidationRulePriorityOrderComparator comparator = new ValidationRulePriorityOrderComparator();

    @Test
    public void testComparatorReturns0WhenPriorityIsTheSame() {
        final ValidationRuleHolder rule1 = new ValidationRuleHolder("", "", null, 0, false);
        final ValidationRuleHolder rule2 = new ValidationRuleHolder("", "", null, 0, false);

        final int result = comparator.compare(rule1, rule2);

        assertEquals(0, result);
    }

    @Test
    public void testComparatorReturnsGreaterThan1WhenPriorityIsHigher() {
        final ValidationRuleHolder rule1 = new ValidationRuleHolder("", "", null, 1, false);
        final ValidationRuleHolder rule2 = new ValidationRuleHolder("", "", null, 0, false);

        final int result = comparator.compare(rule1, rule2);

        assertEquals(1, result);
    }

    @Test
    public void testComparatorReturnsLessThan1WhenPriorityIsLower() {
        final ValidationRuleHolder rule1 = new ValidationRuleHolder("", "", null, 0, false);
        final ValidationRuleHolder rule2 = new ValidationRuleHolder("", "", null, 1, false);

        final int result = comparator.compare(rule1, rule2);

        assertEquals(-1, result);
    }
}
