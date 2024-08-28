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
package com.ericsson.oss.services.ap.common.validation.rules;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.ValidationRule;

/**
 * Base class for any validation that is node based.
 */
public abstract class NodeConfigurationsValidation implements ValidationRule {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /*
     * (non-Javadoc)
     *
     * @see com.ericsson.oss.services.ap.api.validation.rules.ValidationRule#execute(com.ericsson.oss.services.ap.api.validation.ValidationContext)
     */
    @Override
    public boolean execute(final ValidationContext context) {
        return validate(context);
    }

    @SuppressWarnings("unchecked")
    public String getNodeFdn(final ValidationContext context) {
        final Map<String, Object> contextTarget = (Map<String, Object>) context.getTarget();
        return (String) contextTarget.get("nodeFdn");
    }

    @SuppressWarnings("unchecked")
    public List<String> getConfigFilesLocation(final ValidationContext context) {
        final Map<String, Object> contextTarget = (Map<String, Object>) context.getTarget();
        return (List<String>) contextTarget.get("configFiles");
    }

    protected boolean isValidatedWithoutError(final ValidationContext context) {
        return context.getValidationErrors().isEmpty();
    }

    protected abstract boolean validate(final ValidationContext context);
}