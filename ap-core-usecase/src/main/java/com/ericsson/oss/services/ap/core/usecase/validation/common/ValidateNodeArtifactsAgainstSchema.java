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
package com.ericsson.oss.services.ap.core.usecase.validation.common;

import java.util.List;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Groups;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
/**
 * Rule to validate all artifacts files against their corresponding schemas for all nodes within a expansion and migration project.
 */
@Groups(value = { @Group(name = ValidationRuleGroups.EXPANSION, priority = 8, abortOnFail = true), 
             @Group(name = ValidationRuleGroups.MIGRATION, priority = 8, abortOnFail = true)})
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
            final String nodeInfoContent = getContentAsString(getArchive(context), ProjectArtifact.NODEINFO.toString(), directory);
            final String fileNodeName = new DocumentReader(nodeInfoContent).getElementValue("name");
            try {
                result &= validateArtifactsForSingleNode(context, directory);
            } catch (final Exception e) {
                logger.error("Unexpected error while validating the name uniqueness for node {}", fileNodeName, e);
                final String message = apMessages.format("validation.node.does.not.exist.failure", fileNodeName);
                addNodeValidationFailure(context, message, directory);
                result = false;
            }
        }
        return result;
    }
}
