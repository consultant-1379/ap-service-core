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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.ValidationEngine;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Groups;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.api.validation.rules.ValidationRule;

/**
 * Implementation of {@link ValidationEngine}, which executes applicable rules on a given {@link ValidationContext}.
 */
public class ValidationEngineImpl implements ValidationEngine {

    @Inject
    @Any
    private Instance<ValidationRule> validationRules;

    @Inject
    private Logger logger;

    /**
     * Validate based on the {@link ValidationContext}.
     *
     * @param context
     *            a context for the validation, providing a means for the {@link ValidationEngine} to determine the applicable rules from available
     *            rules
     * @return true if all selected rules execute successfully
     */
    @Override
    public boolean validate(final ValidationContext context) {
        final List<ValidationRuleHolder> rulesOfInterest = getRulesToExecute(context);
        boolean validated = true;

        for (final ValidationRuleHolder ruleHolder : rulesOfInterest) {
            logger.trace("Executing rule {} in group {}", ruleHolder.getRuleName(), ruleHolder.getGroupName());
            final boolean result = ruleHolder.getRule().execute(context);

            if (!result) {
                validated = false;
                final boolean isAbortOnFail = ruleHolder.getAbortOnFail();

                if (isAbortOnFail) {
                    logger.trace("Aborting further rule execution as {} abort on fail flag is {}", ruleHolder.getRuleName(), isAbortOnFail);
                    break;
                }
            }
        }
        return validated;
    }

    private List<ValidationRuleHolder> getRulesToExecute(final ValidationContext context) {
        final List<ValidationRuleHolder> rulesToExecute = new ArrayList<>();
        final String groupOfInterest = context.getGroup();

        for (final ValidationRule validationRule : validationRules) {
            addRuleIfAnnotationIsValid(rulesToExecute, groupOfInterest, validationRule);
        }

        Collections.sort(rulesToExecute, new ValidationRulePriorityOrderComparator());
        return rulesToExecute;
    }

    private static void addRuleIfAnnotationIsValid(final List<ValidationRuleHolder> rulesToExecute, final String groupOfInterest,
            final ValidationRule validationRule) {
        final Rule ruleAnnotation = validationRule.getClass().getAnnotation(Rule.class);

        if (ruleAnnotation != null) {
            addRuleIfInValidGroup(rulesToExecute, groupOfInterest, validationRule, ruleAnnotation);
        }
    }

    private static void addRuleIfInValidGroup(final List<ValidationRuleHolder> rulesToExecute, final String groupOfInterest,
            final ValidationRule validationRule, final Rule ruleAnnotation) {
        final ValidationRuleHolder ruleToExecute = checkRuleGroups(ruleAnnotation, validationRule, groupOfInterest);

        if (ruleToExecute != null) {
            rulesToExecute.add(ruleToExecute);
        }
    }

    private static ValidationRuleHolder checkRuleGroups(final Rule ruleAnnotation, final ValidationRule validationRule,
            final String groupOfInterest) {
        final Groups ruleGroups = validationRule.getClass().getAnnotation(Groups.class);
        final Group ruleGroup = validationRule.getClass().getAnnotation(Group.class);
        return getRuleFromSingleOrMultipleGroups(ruleAnnotation, validationRule, groupOfInterest, ruleGroups, ruleGroup);
    }

    private static ValidationRuleHolder getRuleFromSingleOrMultipleGroups(final Rule ruleAnnotation, final ValidationRule validationRule,
            final String groupOfInterest, final Groups multipleRuleGroups, final Group singleRuleGroup) {
        final String ruleName = ruleAnnotation.name();

        if (multipleRuleGroups != null) {
            return checkRuleInMultipleGroups(validationRule, ruleName, groupOfInterest, multipleRuleGroups);
        } else if (singleRuleGroup != null) {
            return checkRuleInSingleGroup(validationRule, ruleName, groupOfInterest, singleRuleGroup);
        }
        return null;
    }

    private static ValidationRuleHolder checkRuleInMultipleGroups(final ValidationRule validationRule, final String ruleName,
            final String groupOfInterest, final Groups multipleGroups) {
        final Group[] groups = multipleGroups.value();

        for (final Group group : groups) {
            if (group.name().equals(groupOfInterest)) {
                return new ValidationRuleHolder(ruleName, group.name(), validationRule, group.priority(), group.abortOnFail());
            }
        }
        return null;
    }

    private static ValidationRuleHolder checkRuleInSingleGroup(final ValidationRule validationRule, final String ruleName,
            final String groupOfInterest, final Group singleGroup) {
        if (singleGroup.name().equals(groupOfInterest)) {
            return new ValidationRuleHolder(ruleName, singleGroup.name(), validationRule, singleGroup.priority(), singleGroup.abortOnFail());
        }
        return null;
    }
}
