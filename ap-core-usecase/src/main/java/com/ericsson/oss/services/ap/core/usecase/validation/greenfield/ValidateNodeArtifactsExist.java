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
package com.ericsson.oss.services.ap.core.usecase.validation.greenfield;

import java.util.List;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
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
@Group(name = ValidationRuleGroups.ORDER, priority = 11, abortOnFail = true)
@Rule(name = "ValidateNodeArtifactsExist")
public class ValidateNodeArtifactsExist extends ZipBasedValidation {

    private static final String BASELINE_ARTIFACT_TAG = "baseline";

    private static final String DIAGRAM_FILE = "diagram.svg";

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        boolean isValid = true;
        for (final String dirName : directoryNames) {
            final List<String> artifactFilesInNodeInfo = readArtifactFileNamesInNodeInfo(context, dirName);
            final List<String> baselineArtifactFilesInNodeInfo = readBaselineArtifactFileNamesInNodeInfo(context, dirName);
            final List<String> artifactFilesInDir = readArtifactFileNamesInDir(context, dirName);

            if (!allFilesInDirPresentInNodeInfo(artifactFilesInNodeInfo, artifactFilesInDir, context, dirName)) {
                isValid = false;
            }

            if (!allFilesInNodeInfoPresentInDir(artifactFilesInNodeInfo, baselineArtifactFilesInNodeInfo, artifactFilesInDir, context, dirName)) {
                isValid = false;
            }
        }
        return isValid;
    }

    private boolean allFilesInDirPresentInNodeInfo(final List<String> artifactFilesInNodeInfo, final List<String> artifactFilesInDir,
            final ValidationContext context, final String dirName) {
        boolean validationSuccess = true;

        if(artifactFilesInDir.contains(DIAGRAM_FILE)) {
            artifactFilesInNodeInfo.add(DIAGRAM_FILE);
        }
        if (!artifactFilesInNodeInfo.containsAll(artifactFilesInDir)) {
            logger.info("Inside order validation :{}",dirName);
            recordNodeValidationError(context, "validation.project.file.content.not.listed", dirName);
            validationSuccess = false;
        }
        return validationSuccess;
    }

    private boolean allFilesInNodeInfoPresentInDir(final List<String> artifactFilesInNodeInfo, final List<String> baselineArtifactFilesInNodeInfo,
            final List<String> artifactFilesInDir, final ValidationContext context, final String dirName) {
        boolean validationSuccess = true;

        if (!artifactFilesInDir.containsAll(artifactFilesInNodeInfo)) {
            for (final String requiredFileName : artifactFilesInNodeInfo) {
                if (!artifactFilesInDir.contains(requiredFileName) && !baselineArtifactFilesInNodeInfo.contains(requiredFileName)) {
                    recordNodeValidationError(context, "validation.nodeinfo.file.artifact.listed.not.in.node.folder", dirName, requiredFileName);
                    validationSuccess = false;
                }
            }
        }
        return validationSuccess;
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
