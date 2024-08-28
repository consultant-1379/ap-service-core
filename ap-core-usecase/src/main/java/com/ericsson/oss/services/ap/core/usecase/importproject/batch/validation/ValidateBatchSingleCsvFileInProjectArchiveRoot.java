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

import java.util.List;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;

/**
 * Validate that the {@literal <}batch_project{@literal >}.zip file contains a single .csv file, which:
 * <ul>
 * <li>Is located in the only directory present <code>/root</code></li>
 * <li>Contains substitution data values required by the <code>%substitution_variables%</code> in the node artifact template xml files</li>
 * </ul>
 */
@Group(name = ValidationRuleGroups.ORDER_BATCH, priority = 4, abortOnFail = true)
@Rule(name = "ValidateBatchSingleCsvFileInProjectArchiveRoot")
public class ValidateBatchSingleCsvFileInProjectArchiveRoot extends AbstractValidateBatchImport {

    private static final String CSV_FILE_EXTENSION_PATTERN = "^.+.(?i)\\.csv";

    @Override
    protected boolean validate(final ValidationContext context, final String fileName, final List<String> directoryNames) {
        final List<ArchiveArtifact> csvFilesInProject = getFilesInBatchArchiveByPattern(context, CSV_FILE_EXTENSION_PATTERN);

        if (csvFilesInProject.isEmpty()) {
            recordValidationError(context, "validation.batch.csv.missing", fileName);
            return false;
        }

        if (csvFilesInProject.size() > 1) {
            recordValidationError(context, "validation.batch.csv.multiple.files.found", fileName);
            return false;
        }

        return true;
    }
}