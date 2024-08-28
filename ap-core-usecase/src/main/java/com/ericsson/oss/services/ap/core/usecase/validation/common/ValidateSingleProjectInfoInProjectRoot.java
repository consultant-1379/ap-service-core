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

import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.PROJECTINFO;

import java.util.List;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ZipBasedValidation;

/**
 * Validate project file contains a single projectInfo.xml file in the root.
 */
@Group(name = ValidationRuleGroups.ORDER, priority = 3, abortOnFail = true)
@Rule(name = "ValidateSingleProjectInfoInZipRoot")
public class ValidateSingleProjectInfoInProjectRoot extends ZipBasedValidation {

    public static final String FILE_PATH_SEPERATOR = "/";

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        final List<ArchiveArtifact> projectInfoList = getListOfProjectInfoInZip(context);

        final boolean result;
        if (projectInfoList.size() == 1) {
            result = isProjectInfoInArchiveRoot(projectInfoList);
            if (!result) {
                recordValidationError(context, "validation.projectinfo.file.invalid.location");
            }
        } else {
            result = false;
            if (projectInfoList.size() > 1) {
                recordValidationError(context, "validation.projectinfo.multiple.files.found");
            } else {
                recordValidationError(context, "validation.projectinfo.missing");
            }
        }

        return result;
    }

    private static boolean isProjectInfoInArchiveRoot(final List<ArchiveArtifact> projectList) {
        return !projectList.get(0).getAbsoluteName().contains(FILE_PATH_SEPERATOR);
    }

    private List<ArchiveArtifact> getListOfProjectInfoInZip(final ValidationContext context) {
        return getArtifactsOfName(context, PROJECTINFO.toString());
    }
}
