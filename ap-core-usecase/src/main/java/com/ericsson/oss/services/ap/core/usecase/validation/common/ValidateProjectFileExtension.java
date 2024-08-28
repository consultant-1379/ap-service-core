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

import static com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey.FILENAME;

import java.util.Locale;
import java.util.Map;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.api.validation.rules.ValidationRule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;

/**
 * Validate project file extension is <code>.zip</code>.
 */
@Group(name = ValidationRuleGroups.ORDER, priority = 1, abortOnFail = true)
@Rule(name = "ValidateProjectFileExtension")
public class ValidateProjectFileExtension implements ValidationRule {

    private final ApMessages apMessages = new ApMessages();

    /**
     * Executes the rule
     *
     * @param context
     *            context of rule execution
     * @return true if rule passes, false otherwise
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean execute(final ValidationContext context) {

        final Map<String, Object> contextTarget = (Map<String, Object>) context.getTarget();
        final String fileName = (String) contextTarget.get(FILENAME.toString());

        if (isProjectExtensionValid(fileName)) {
            return true;
        } else {
            final String errorMessage = apMessages.format("validation.project.zip.file.extension", fileName);
            context.addValidationError(errorMessage);
            return false;
        }
    }

    private static boolean isProjectExtensionValid(final String fileName) {
        return fileName.toLowerCase(Locale.US).endsWith(".zip");
    }
}
