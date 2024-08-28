/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.validation.replace;

import java.util.List;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Groups;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.usecase.validation.common.AbstractValidateSchemasRule;

/**
 * Rule to validate the hardwareReplaceNodeInfo artifact against its schema.
 */
@Groups(value = { @Group(name = ValidationRuleGroups.HARDWARE_REPLACE, priority = 7, abortOnFail = true) })
@Rule(name = "ValidateReplaceNodeInfoArtifactAgainstSchema")
public class ValidateHardwareReplaceNodeInfoArtifactAgainstSchema extends AbstractValidateSchemasRule {

    private static final String HARDWARE_REPLACE_NODE_INFO_SCHEMA_TYPE = "HardwareReplaceNodeInfo";

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        boolean allNodeInfoArtifactsValid = true;
        for (final String directoryName : directoryNames) {
            if (!isValidNodeInfoArtifact(context, directoryName,HARDWARE_REPLACE_NODE_INFO_SCHEMA_TYPE)) {
                allNodeInfoArtifactsValid = false;
            }
        }
        return allNodeInfoArtifactsValid;
    }
}
