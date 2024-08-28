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

import com.ericsson.oss.services.ap.api.exception.ValidationCrudException;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Groups;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
/**
 * Validate that each node folder only contains artifact files are which detailed as artifacts in each nodeInfo file.
 * <p>
 * So each node folder must:
 * <ol>
 * <li>Contain files specified per node in nodeInfo</li>
 * <li>Not contain any files not specified in nodeInfo</li>
 * </ol>
 */
@Groups(value = { @Group(name = ValidationRuleGroups.EXPANSION, priority = 7, abortOnFail = true),
            @Group(name = ValidationRuleGroups.MIGRATION, priority = 7, abortOnFail = true)})
@Rule(name = "ValidateNodeArtifactsExist")
public class ValidateNodeArtifactsExist extends AbstractValidateRule {

    private static final String VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE = "failure.general";
    private static final String VALIDATION_FAIL_NODEINFO_FILE_NOT_LISTED = "validation.nodeinfo.file.artifact.listed.not.in.node.folder";
    private static final String BASELINE_ARTIFACT_TAG = "baseline";

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {

        final LocalContext localContext = createLocalContext(context);

        for (final String dirName : directoryNames) {
            final String nodeInfoContent = getContentAsString(getArchive(context), ProjectArtifact.NODEINFO.toString(), dirName);
            final String fileNodeName = new DocumentReader(nodeInfoContent).getElementValue("name");
            final List<String> baselineArtifactFilesInNodeInfo = readBaselineArtifactFileNamesInNodeInfo(context, dirName);
            try {
                final List<String> artifactFilesInNodeInfo = readArtifactFileNamesInNodeInfo(context, dirName);
                final List<String> artifactFilesInDir = readArtifactFileNamesInDir(context, dirName);

                allFilesInDirPresentInNodeInfo(artifactFilesInNodeInfo, artifactFilesInDir, localContext, dirName);
                allFilesInNodeInfoPresentInDir(artifactFilesInNodeInfo, baselineArtifactFilesInNodeInfo, artifactFilesInDir, localContext, dirName);

            } catch (final Exception e) {
                logger.error("Unexpected error while validating node artifacts for the node {}", fileNodeName, e);
                throw new ValidationCrudException(apMessages.get(VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE), e);
            }
        }
        return localContext.isValid();
    }

    private static void allFilesInNodeInfoPresentInDir(final List<String> artifactFilesInNodeInfo, final List<String> baselineArtifactFilesInNodeInfo,
                                                       final List<String> artifactFilesInDir,
                                                       final LocalContext localContext, final String dirName) {

        if (!artifactFilesInDir.containsAll(artifactFilesInNodeInfo)) {
            for (final String requiredFileName : artifactFilesInNodeInfo) {
                if (!artifactFilesInDir.contains(requiredFileName) && !baselineArtifactFilesInNodeInfo.contains(requiredFileName)) {
                    localContext.recordNodeValidationError(VALIDATION_FAIL_NODEINFO_FILE_NOT_LISTED, dirName, requiredFileName);
                }
            }
        }
    }

    private List<String> readBaselineArtifactFileNamesInNodeInfo(final ValidationContext context, final String dirName) {
        final NodeInfo nodeInfo = getNodeInfo(context, dirName);
        final List<String> baselineArtifactNamesInNodeInfo = new CaseInsensitiveStringList();
        final List<String> artifactNames = nodeInfo.getNodeArtifacts().get(BASELINE_ARTIFACT_TAG);
        if (artifactNames != null) {
            baselineArtifactNamesInNodeInfo.addAll(artifactNames);
        }
        return baselineArtifactNamesInNodeInfo;
    }
}
