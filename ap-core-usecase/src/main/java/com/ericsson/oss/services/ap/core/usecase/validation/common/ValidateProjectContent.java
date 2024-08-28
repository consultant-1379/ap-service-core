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
package com.ericsson.oss.services.ap.core.usecase.validation.common;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.api.validation.rules.ValidationRule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey;

/**
 * Validate project file content can be accessed as zip format.
 */
@Group(name = ValidationRuleGroups.ORDER, priority = 2, abortOnFail = true)
@Rule(name = "ValidateProjectContent")
public class ValidateProjectContent implements ValidationRule {

    @Inject
    private Logger logger;

    private final ApMessages apMessages = new ApMessages();

    /**
     * Executes the rule
     *
     * @param context
     *            context of rule execution
     * @return true if rule passes
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean execute(final ValidationContext context) {
        final Map<String, Object> contextTarget = (Map<String, Object>) context.getTarget();

        if (isProjectAValidZip(contextTarget)) {
            return true;
        } else {
            recordValidationError(apMessages.get("validation.project.empty"), context);
            return false;
        }
    }

    private static boolean isProjectAValidZip(final Map<String, Object> contextTarget) {
        final Archive projectArchiveReader = (Archive) contextTarget.get(ImportProjectTargetKey.FILE_CONTENT.toString());
        return projectArchiveReader.getNumberOfArtifacts() > 0;
    }

    private void recordValidationError(final String validationErrorMessage, final ValidationContext context) {
        logger.error(validationErrorMessage);
        context.addValidationError(validationErrorMessage);
    }
}
