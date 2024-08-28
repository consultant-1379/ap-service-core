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

import com.ericsson.oss.services.ap.api.validation.rules.ValidationRule;

/**
 * {@link ValidationRule} container. Supports in context processing of validation rule e.g. priority ordering.
 */
class ValidationRuleHolder {

    private final ValidationRule rule;
    private final int priorityOrder;
    private final boolean abortOnFail;
    private final String ruleName;
    private final String groupName;

    public ValidationRuleHolder(final String ruleName, final String groupName, final ValidationRule rule, final int priorityOrder,
            final boolean abortOnFail) {
        this.ruleName = ruleName;
        this.groupName = groupName;
        this.rule = rule;
        this.priorityOrder = priorityOrder;
        this.abortOnFail = abortOnFail;
    }

    public int getRulePriority() {
        return priorityOrder;
    }

    public boolean getAbortOnFail() {
        return abortOnFail;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getGroupName() {
        return groupName;
    }

    public ValidationRule getRule() {
        return rule;
    }
}
