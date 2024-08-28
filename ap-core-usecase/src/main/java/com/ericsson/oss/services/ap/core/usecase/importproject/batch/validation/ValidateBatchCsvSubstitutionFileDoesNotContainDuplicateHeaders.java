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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.csv.CsvData;
import com.ericsson.oss.services.ap.core.usecase.csv.CsvReader;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;

/**
 * Validate that the substitution .csv substitution does not contain duplicate headers.
 * <p>
 * This validation only applies to <i><b>ap order file:project_file.zip</b></i> of a batch project archive.
 */
@Group(name = ValidationRuleGroups.ORDER_BATCH, priority = 6, abortOnFail = true)
@Rule(name = "ValidateBatchCsvSubstitutionFileDoesNotContainDuplicateHeadersTest")
public class ValidateBatchCsvSubstitutionFileDoesNotContainDuplicateHeaders extends AbstractValidateBatchImport {

    @Override
    protected boolean validate(final ValidationContext context, final String fileName, final List<String> directoryNames) {
        final Archive batchArchiveFile = getArchive(context);
        final CsvData csvBatchData = new CsvReader(batchArchiveFile).readCsv();

        final String duplicateHeader = getDuplicateHeaderPresentInCsvfile(csvBatchData);
        if (duplicateHeader != null) {
            recordValidationError(context, "validation.batch.csv.data.header.duplicates", duplicateHeader);
        }
        return isValidatedWithoutError(context);
    }

    private static String getDuplicateHeaderPresentInCsvfile(final CsvData data) {
        final int numberOfHeaders = data.getHeaderCount();
        final Set<String> csvUniqueHeaders = new HashSet<>();
        final StringBuilder csvDuplicateHeaders = buildErrorMessage(data, numberOfHeaders, csvUniqueHeaders);

        if (csvDuplicateHeaders.length() == 0) {
            return null;
        }

        return csvDuplicateHeaders.toString().substring(2); // Remove the ", " we append earlier
    }

    private static StringBuilder buildErrorMessage(final CsvData data, final int numberOfHeaders, final Set<String> csvUniqueHeaders) {
        final StringBuilder csvDuplicateHeaders = new StringBuilder();

        for (int i = 0; i < numberOfHeaders; i++) {
            final String csvHeaderName = data.getHeader(i).trim();
            if (!(csvUniqueHeaders.add(csvHeaderName))) {
                csvDuplicateHeaders.append(", ").append(csvHeaderName);
            }
        }
        return csvDuplicateHeaders;
    }
}
