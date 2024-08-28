/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
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

import com.ericsson.oss.services.ap.api.exception.ValidationCrudException;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.usecase.validation.common.AbstractValidateRule;
import com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact;

/**
 * Validate that each node folder only contains artifact files are which detailed as artifacts in each nodeInfo file.
 * <p>
 * So each node folder must:
 * <ol>
 * <li>Contain files specified per node in nodeInfo</li>
 * <li>Not contain any files not specified in nodeInfo</li>
 * </ol>
 */
@Group(name = ValidationRuleGroups.HARDWARE_REPLACE, priority = 7, abortOnFail = true)
@Rule(name = "ValidateHardwareReplaceNodeArtifactsExist")
public class ValidateHardwareReplaceNodeArtifactsExist extends AbstractValidateRule {

    private static final String VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE = "failure.general";
    private static final String VALIDATION_FAIL_NODEINFO_FILE_NOT_LISTED = "validation.nodeinfo.file.artifact.listed.not.in.node.folder";

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {

        final LocalContext localContext = createLocalContext(context);

        for (final String dirName : directoryNames) {
            final String nodeInfoContent = getContentAsString(getArchive(context), ProjectArtifact.NODEINFO.toString(), dirName);
            final String fileNodeName = new DocumentReader(nodeInfoContent).getElementValue("name");
            try {
                final List<String> artifactFilesInNodeInfo = readArtifactFileNamesInNodeInfo(context, dirName);
                final List<String> artifactFilesInDir = readArtifactFileNamesInDir(context, dirName);

                allFilesInDirPresentInNodeInfo(artifactFilesInNodeInfo, artifactFilesInDir, localContext, dirName);
                allFilesInNodeInfoPresentInDir(artifactFilesInNodeInfo, artifactFilesInDir, localContext, dirName);

            } catch (final Exception e) {
                logger.error("Unexpected error while validating node artifacts for the node {}", fileNodeName, e);
                throw new ValidationCrudException(apMessages.get(VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE), e);
            }
        }
        return localContext.isValid();
    }

    private static void allFilesInNodeInfoPresentInDir(final List<String> artifactFilesInNodeInfo,
                                                       final List<String> artifactFilesInDir,
                                                       final LocalContext localContext, final String dirName) {

        if (!artifactFilesInDir.containsAll(artifactFilesInNodeInfo)) {
            for (final String requiredFileName : artifactFilesInNodeInfo) {
                if (!artifactFilesInDir.contains(requiredFileName)) {
                    localContext.recordNodeValidationError(VALIDATION_FAIL_NODEINFO_FILE_NOT_LISTED, dirName, requiredFileName);
                }
            }
        }
    }
}
