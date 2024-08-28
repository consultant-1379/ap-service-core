/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.validation.eoi;

import com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.ValidationRule;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.ericsson.oss.services.ap.core.usecase.validation.eoi.EoiProjectTargetKey.REQUEST_CONTENT;

public abstract class EoiBasedValidation implements ValidationRule {

    protected final Logger logger = LoggerFactory.getLogger(getClass());


    protected final ApMessages apMessages = new ApMessages();

    @Override
    public final boolean execute(final ValidationContext context) {

        return validate(context, getNetworkElementsList(context));
    }

    protected abstract boolean validate(final ValidationContext context, final List<Map<String, Object>> networkElements);

    protected boolean isValidatedWithoutError(final ValidationContext context) {
        return context.getValidationErrors().isEmpty();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getNetworkElementsList(final ValidationContext context) {
        final Map<String, Map<String, Object>> contextTarget = (Map<String, Map<String, Object>>) context.getTarget();
        final Map<String, Object> projectRequestJsonData = contextTarget.get(REQUEST_CONTENT.toString());
        final List<Map<String, Object>> networkElements = (List<Map<String, Object>>) projectRequestJsonData.get((ProjectRequestAttributes.EOI_NETWORK_ELEMENTS.toString()));
        if (networkElements == null) {
            return Collections.emptyList();
        }
        return networkElements;
    }
}
