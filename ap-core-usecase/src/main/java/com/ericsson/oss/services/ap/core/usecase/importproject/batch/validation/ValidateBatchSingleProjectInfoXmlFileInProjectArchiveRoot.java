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
package com.ericsson.oss.services.ap.core.usecase.importproject.batch.validation;

import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.PROJECTINFO;

import java.util.List;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;

/**
 * Validate that the {@literal <}batch_project{@literal >}.zip file contains a single projectInfo.xml file, located in the only directory present
 * <i><b>/root</b></i>.
 */
@Group(name = ValidationRuleGroups.ORDER_BATCH, priority = 2, abortOnFail = true)
@Rule(name = "ValidateBatchSingleProjectInfoXmlFileInProjectArchiveRoot")
public class ValidateBatchSingleProjectInfoXmlFileInProjectArchiveRoot extends AbstractValidateBatchImport {

    @Override
    protected boolean validate(final ValidationContext context, final String fileName, final List<String> directoryNames) {
        boolean isSingleProjectInfoFileInBatchProjectArchiveRoot = true;
        final List<ArchiveArtifact> projectInfoList = getFilesInBatchArchive(context, PROJECTINFO.artifactName());

        if (projectInfoList.isEmpty()) {
            isSingleProjectInfoFileInBatchProjectArchiveRoot = false;
            recordValidationError(context, "validation.projectinfo.missing");
        }

        return isSingleProjectInfoFileInBatchProjectArchiveRoot;
    }
}
