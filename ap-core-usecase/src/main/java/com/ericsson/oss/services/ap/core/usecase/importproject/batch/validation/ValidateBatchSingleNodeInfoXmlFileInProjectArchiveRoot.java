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

import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.NODEINFO;

import java.util.List;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;

/**
 * Validate that the {@literal <}batch_project{@literal >}.zip file contains a single nodeInfo.xml file, located in the only directory present
 * <code>/root</code>.
 */
@Group(name = ValidationRuleGroups.ORDER_BATCH, priority = 3, abortOnFail = true)
@Rule(name = "ValidateBatchSingleNodeInfoXmlFileInProjectArchiveRoot")
public class ValidateBatchSingleNodeInfoXmlFileInProjectArchiveRoot extends AbstractValidateBatchImport {

    @Override
    protected boolean validate(final ValidationContext context, final String fileName, final List<String> directoryNames) {
        boolean isSingleNodeInfoFileInBatchProjectArchiveRoot = true;
        final List<ArchiveArtifact> nodeInfoList = getFilesInBatchArchive(context, NODEINFO.artifactName());

        if (nodeInfoList.isEmpty()) {
            isSingleNodeInfoFileInBatchProjectArchiveRoot = false;
            recordValidationError(context, "validation.batch.nodeinfo.missing", fileName);
        }

        return isSingleNodeInfoFileInBatchProjectArchiveRoot;
    }
}