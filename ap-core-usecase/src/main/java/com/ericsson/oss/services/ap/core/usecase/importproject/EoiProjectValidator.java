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

package com.ericsson.oss.services.ap.core.usecase.importproject;

import com.ericsson.oss.services.ap.api.exception.ValidationException;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.ValidationEngine;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.usecase.validation.eoi.EoiProjectTargetKey;

import javax.inject.Inject;
import java.util.*;

public class EoiProjectValidator {

    @Inject
    private ValidationEngine validationEngine;

    private final ApMessages apMessages = new ApMessages();


    public void validateStandardProject(final Map<String, Object> networkElements) {

        final List<String> validationErrors = new ArrayList<>();
        validationErrors.addAll(getErrorsOnValidationIfAny(networkElements, ValidationRuleGroups.EOI));

        if (!validationErrors.isEmpty()) {
            final String allValidationErrors = mergeAllValidationErrors(validationErrors);
            throw new ValidationException(validationErrors, apMessages.get("validation.project.error") + ": " + allValidationErrors);
        }
    }

    private List<String> getErrorsOnValidationIfAny(final Map<String, Object> project, final String ruleGroup) {
        final Map<String, Map<String, Object>> validationTarget = new HashMap<>();
        validationTarget.put(EoiProjectTargetKey.REQUEST_CONTENT.toString(), project);

        final ValidationContext context = new ValidationContext(ruleGroup, validationTarget);

        final boolean validProject = validationEngine.validate(context);
        if (!validProject) {
            return context.getValidationErrors();
        } else {
            return Collections.<String>emptyList();
        }
    }

    private static String mergeAllValidationErrors(final List<String> validationErrors) {
        final StringBuilder fullValidationErrorMessage = new StringBuilder();
        for (final String validationError : validationErrors) {
            if (fullValidationErrorMessage.length() != 0) {
                fullValidationErrorMessage.append(" ,");
            }
            fullValidationErrorMessage.append(validationError);
        }
        return fullValidationErrorMessage.toString();
    }
}
