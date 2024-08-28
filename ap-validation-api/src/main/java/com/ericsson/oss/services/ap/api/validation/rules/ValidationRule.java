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
package com.ericsson.oss.services.ap.api.validation.rules;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;

/**
 * Shared rules interface. Interface to rules for ValidationEngine.
 *
 * @see com.ericsson.oss.services.ap.api.validation.ValidationEngine
 */
public interface ValidationRule {

    /**
     * Executes a rule when used in conjunction with ValidationEngine.
     *
     * @param context
     *            the validation context
     * @return true if the rule passes validation
     */
    boolean execute(final ValidationContext context);
}
