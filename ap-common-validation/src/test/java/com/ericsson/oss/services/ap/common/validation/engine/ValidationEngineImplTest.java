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
package com.ericsson.oss.services.ap.common.validation.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.ValidationEngine;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Groups;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.api.validation.rules.ValidationRule;

/**
 * Unit tests for {@link ValidationEngineImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidationEngineImplTest {

    private final ValidationRuleImpl1 rule1Group1Priority1 = new ValidationRuleImpl1();
    private final ValidationRuleImpl2 rule1Group2Priority1 = new ValidationRuleImpl2();
    private final ValidationRuleImpl3 rule2Group1Priority2AbortOnFail = new ValidationRuleImpl3();
    private final ValidationRuleImpl4 rule3Group1Group2Priority3 = new ValidationRuleImpl4();

    @Mock
    private Instance<ValidationRule> validationRuleInstance;

    @Mock
    private Logger logger; // NOPMD

    @InjectMocks
    private final ValidationEngine validationEngineImpl = new ValidationEngineImpl();

    @Before
    public void setup() {
        final List<ValidationRule> validationRules = new ArrayList<>();
        validationRules.add(rule1Group1Priority1);
        validationRules.add(rule1Group2Priority1);
        validationRules.add(rule2Group1Priority2AbortOnFail);
        validationRules.add(rule3Group1Group2Priority3);

        when(validationRuleInstance.iterator()).thenReturn(validationRules.iterator()).thenReturn(validationRules.iterator());
    }

    @Test
    public void testAllRulesInGroupExecuted() {
        final ValidationContext ctx = new ValidationContext("Group1", null);
        final boolean validated = validationEngineImpl.validate(ctx);

        assertTrue(rule1Group1Priority1.executed);
        assertTrue(rule2Group1Priority2AbortOnFail.executed);
        assertTrue(rule3Group1Group2Priority3.executed);
        assertFalse(rule1Group2Priority1.executed);
        assertTrue(validated);
    }

    @Test
    public void testRulesInSpecifiedGroupAreExecutedInOrderOfPriority() {
        final ValidationContext ctx = new ValidationContext("Group1", null);
        final boolean validated = validationEngineImpl.validate(ctx);

        assertTrue(rule1Group1Priority1.executionStartTime < rule2Group1Priority2AbortOnFail.executionStartTime);
        assertTrue(rule1Group2Priority1.executionStartTime < rule2Group1Priority2AbortOnFail.executionStartTime);
        assertTrue(rule2Group1Priority2AbortOnFail.executionStartTime < rule3Group1Group2Priority3.executionStartTime);

        assertTrue(validated);
    }

    @Test
    public void testNoFurtherRulesExecutedWhenAbortOnFailureRuleReturnsFalse() {
        rule2Group1Priority2AbortOnFail.validateSuccess = false;
        final ValidationContext ctx = new ValidationContext("Group1", null);
        final boolean validated = validationEngineImpl.validate(ctx);

        assertFalse(rule3Group1Group2Priority3.executed);
        assertFalse(validated);
    }

    @Test
    public void testSameRuleIsExecutedInDifferentGroups() {
        ValidationContext ctx = new ValidationContext("Group1", null);
        validationEngineImpl.validate(ctx);

        ctx = new ValidationContext("Group2", null);
        validationEngineImpl.validate(ctx);

        assertEquals(2, rule3Group1Group2Priority3.executionCount);
    }

    @Group(name = "Group1", priority = 1)
    @Rule(name = "Rule1")
    private static class ValidationRuleImpl1 implements ValidationRule {

        private boolean executed = false;
        private long executionStartTime;

        @Override
        public boolean execute(final ValidationContext context) {
            executionStartTime = System.nanoTime();
            executed = true;
            return true;
        }
    }

    @Group(name = "Group2", priority = 1)
    @Rule(name = "Rule1")
    private static class ValidationRuleImpl2 implements ValidationRule {

        private boolean executed = false;
        private long executionStartTime;

        @Override
        public boolean execute(final ValidationContext context) {
            executionStartTime = System.nanoTime();
            executed = true;
            return true;
        }
    }

    @Group(name = "Group1", priority = 2, abortOnFail = true)
    @Rule(name = "Rule2")
    private static class ValidationRuleImpl3 implements ValidationRule {

        private boolean executed = false;
        private boolean validateSuccess = true;
        private long executionStartTime;

        @Override
        public boolean execute(final ValidationContext context) {
            executionStartTime = System.nanoTime();
            executed = true;
            return validateSuccess;
        }
    }

    @Groups(value = { @Group(name = "Group1", priority = 3), @Group(name = "Group2", priority = 3) })
    @Rule(name = "Rule3")
    private static class ValidationRuleImpl4 implements ValidationRule {

        private boolean executed = false;
        private int executionCount = 0;
        private long executionStartTime;

        @Override
        public boolean execute(final ValidationContext context) {
            executionStartTime = System.nanoTime();
            executed = true;
            executionCount++;
            return true;
        }
    }
}
