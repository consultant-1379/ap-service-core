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
package com.ericsson.oss.services.ap.core.usecase.validation.common;

import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.NODEINFO;

import java.util.List;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ZipBasedValidation;

/**
 * Validate project file contains a single nodeInfo.xml file per node directory. So:
 * <ol>
 * <li>Each node directory contains and only contains one nodeInfo.xml</li>
 * <li>nodeInfo.xml only stored in node directory</li>
 * </ol>
 */
@Group(name = ValidationRuleGroups.ORDER, priority = 5, abortOnFail = true)
@Rule(name = "ValidateSingleNodeInfoPerNode")
public class ValidateSingleNodeInfoPerNode extends ZipBasedValidation {

    private static final String FILE_PATH_SEPERATOR = "/";

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        boolean isValid = true;

        if (nodeInfoInArchiveRoot(context)) {
            recordValidationError(context, "validation.nodeinfo.file.found.not.in.node.folder");
            isValid = false;
        }

        for (final String dirName : directoryNames) {
            if (isNodeArtifactEmpty(context, dirName)) {
                recordNodeValidationError(context, "validation.nodeinfo.missing", dirName);
                isValid = false;
            }
        }

        return isValid;
    }

    private boolean isNodeArtifactEmpty(final ValidationContext context, final String dirName) {
        final Archive projectArchive = getArchive(context);
        final ArchiveArtifact nodeInfoArtifact = projectArchive.getArtifactOfNameInDir(dirName, NODEINFO.toString());
        return nodeInfoArtifact == null;
    }

    private boolean nodeInfoInArchiveRoot(final ValidationContext context) {
        boolean hasNodeInfoInRoot = false;

        final List<ArchiveArtifact> nodeInfoList = getArtifactsOfName(context, NODEINFO.toString());
        for (final ArchiveArtifact nodeInfoArtifact : nodeInfoList) {
            hasNodeInfoInRoot |= !nodeInfoArtifact.getAbsoluteName().contains(FILE_PATH_SEPERATOR);
        }

        return hasNodeInfoInRoot;
    }
}