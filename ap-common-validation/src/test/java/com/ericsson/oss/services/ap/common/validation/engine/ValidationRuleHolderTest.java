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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.ericsson.oss.services.ap.api.validation.rules.ValidationRule;

/**
 * Unit tests for {@link ValidationRuleHolder}.
 */
public class ValidationRuleHolderTest {

    @Mock
    private ValidationRule aRule;

    private static final int PRIORTIY_ORDER = 0;
    private static final boolean ABORT_ON_FAIL = true;
    private static final String RULE_NAME = "test";
    private static final String GROUP_NAME = "group";

    @InjectMocks
    private final ValidationRuleHolder validationRuleHolder = new ValidationRuleHolder(RULE_NAME, GROUP_NAME, aRule, PRIORTIY_ORDER, ABORT_ON_FAIL);

    @Test
    public void verifyGettersAndSetters() {
        assertTrue(validationRuleHolder.getAbortOnFail());
        assertEquals(validationRuleHolder.getRuleName(), RULE_NAME);
        assertEquals(validationRuleHolder.getGroupName(), GROUP_NAME);
        assertEquals(validationRuleHolder.getRulePriority(), 0);
        assertEquals(validationRuleHolder.getRule(), aRule);
    }
}
