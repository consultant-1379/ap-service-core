/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.validation.expansion;

import java.util.List;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Groups;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.usecase.validation.common.AbstractValidateSchemasRule;

/**
 * Rule to validate the expansionNodeInfo artifact against its schema.
 */
@Groups(value = { @Group(name = ValidationRuleGroups.EXPANSION, priority = 7, abortOnFail = true) })
@Rule(name = "ValidateExpansionNodeInfoArtifactAgainstSchema")
public class ValidateExpansionNodeInfoArtifactAgainstSchema extends AbstractValidateSchemasRule {

    private static final String EXPANSION_NODE_INFO_SCHEMA_TYPE = "ExpansionNodeInfo";

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        boolean allNodeInfoArtifactsValid = true;
        for (final String directoryName : directoryNames) {
            if (!isValidNodeInfoArtifact(context, directoryName,EXPANSION_NODE_INFO_SCHEMA_TYPE)) {
                allNodeInfoArtifactsValid = false;
            }
        }

        return allNodeInfoArtifactsValid;
    }
}
