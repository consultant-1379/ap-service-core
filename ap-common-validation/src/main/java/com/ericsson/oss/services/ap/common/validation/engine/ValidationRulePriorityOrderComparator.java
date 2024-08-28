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

import java.io.Serializable;
import java.util.Comparator;

/**
 * Compares validation rules (contained within {@link ValidationRuleHolder}) based on priority order.
 */
class ValidationRulePriorityOrderComparator implements Comparator<ValidationRuleHolder>, Serializable {

    private static final long serialVersionUID = 4555585470777741014L;

    @Override
    public int compare(final ValidationRuleHolder r1, final ValidationRuleHolder r2) {
        return Math.min(Math.max(r1.getRulePriority() - r2.getRulePriority(), -1), 1);
    }
}
