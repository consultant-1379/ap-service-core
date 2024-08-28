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
package com.ericsson.oss.services.ap.core.usecase.validation.greenfield;

import java.util.List;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.usecase.validation.common.AbstractValidateSchemasRule;

/**
 * Rule to validate all artifacts against their corresponding schemas for all nodes within a project.
 */
@Group(name = ValidationRuleGroups.ORDER, priority = 12, abortOnFail = true)
@Rule(name = "ValidateNodeArtifactsAgainstSchema")
public class ValidateNodeArtifactsAgainstSchema extends AbstractValidateSchemasRule {

    /**
     * Validate all node artifacts listed in the nodeInfo file against their corresponding schema, for each node folder in a project.
     *
     * @param context
     *            context of rule execution
     * @param directoryNames
     *            node artifacts
     * @return true if all artifacts validate against schema, false otherwise
     */
    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        boolean result = true;

        for (final String directory : directoryNames) {
            result &= validateArtifactsForSingleNode(context, directory);
        }
        return result;
    }

}
