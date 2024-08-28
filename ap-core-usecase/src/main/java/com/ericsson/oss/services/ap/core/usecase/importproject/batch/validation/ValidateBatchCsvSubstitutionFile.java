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
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.archive.exception.MalformedCsvException;
import com.ericsson.oss.services.ap.core.usecase.csv.CsvData;
import com.ericsson.oss.services.ap.core.usecase.csv.CsvReader;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;

/**
 * Validate that the substitution .csv file is valid:
 * <ul>
 * <li>File is a valid .csv format:<br>
 * <b><i>CSVFormat.EXCEL</i></b> - Comma separated format as defined by <a href="http://tools.ietf.org/html/rfc4180">RFC 4180</a>, which permits
 * missing column names
 * <li>If the file contains one row, this row is presumed to be header row and the file is not valid
 * <li>If the file contains no rows, then the file is not valid</li>
 * <li>If there is a mismatch between number of data values and headers, then the file is not valid
 * </ul>
 * This validation only applies to batch project archives.
 */
@Group(name = ValidationRuleGroups.ORDER_BATCH, priority = 5, abortOnFail = true)
@Rule(name = "ValidateBatchCsvSubstitutionFile")
public class ValidateBatchCsvSubstitutionFile extends AbstractValidateBatchImport {

    private static final String CSV_FILE_EXTENSION_PATTERN = "^.+.(?i)\\.csv";

    @Override
    protected boolean validate(final ValidationContext context, final String batchArchiveFilename, final List<String> directoryNames) {

        boolean isCsvFileInBatchProjectArchiveValid = true;
        final List<ArchiveArtifact> csvSubstitutionFileList = getFilesInBatchArchiveByPattern(context, CSV_FILE_EXTENSION_PATTERN);
        final String csvSubstitutionFilename = csvSubstitutionFileList.get(0).getName();

        try {
            final Archive batchArchiveFile = getArchive(context);
            final CsvData csvBatchData = new CsvReader(batchArchiveFile).readCsv();

            if (isHeaderRowMissingInCsvFile(csvBatchData)) {
                recordValidationError(context, "validation.batch.csv.file.empty", csvSubstitutionFilename);
                isCsvFileInBatchProjectArchiveValid = false;
            }
            if (isZeroRowCountInCsv(csvBatchData) && !isHeaderRowMissingInCsvFile(csvBatchData)) {
                recordValidationError(context, "validation.batch.csv.file.no.data", csvSubstitutionFilename);
                isCsvFileInBatchProjectArchiveValid = false;
            }
        } catch (final MalformedCsvException mce) {
            logger.warn("Error in CSV file for archive {}: {}", batchArchiveFilename, mce.getMessage(), mce);
            recordValidationError(context, "validation.batch.csv.data.header.mismatch", csvSubstitutionFilename);
            isCsvFileInBatchProjectArchiveValid = false;
        }
        return isCsvFileInBatchProjectArchiveValid;
    }

    private static boolean isHeaderRowMissingInCsvFile(final CsvData data) {
        return data.getHeaderCount() == 0;
    }

    private static boolean isZeroRowCountInCsv(final CsvData data) {
        return data.getRowCount() == 0;
    }
}
