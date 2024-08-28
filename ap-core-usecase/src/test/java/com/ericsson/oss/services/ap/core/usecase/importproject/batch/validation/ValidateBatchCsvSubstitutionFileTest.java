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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.core.usecase.validation.common.ZipContentGenerator;

/**
 * Unit tests for {@link ValidateBatchCsvSubstitutionFile}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateBatchCsvSubstitutionFileTest {

    private static final String VALID_BATCH_ARCHIVE_FILENAME = "projectTest.zip";
    private static final String SUBSTITUTION_CSV = "Substitution.csv";
    private static final String CSV_VALID_HEADER_ROW = "node_name, node_ip, oam_ip \r\n";
    private static final String CSV_VALID_DATA_ROW_ONE = "node-1, 1.10.10.11, 1.2.3.4 \r\n";
    private static final String CSV_VALID_DATA_ROW_TWO = "node-2,1.10.10.12,1.2.3.4 \r\n";
    private static final String CSV_MISMATCH_DATA_ROW_ONE = "node-1, 1.10.10.11\r\n";
    private static final String CSV_MISMATCH_DATA_ROW_TWO = "node-2, 1.10.10.12, 1.2.3.4, 5.6.7.8 \r\n";

    private static final String PROJECT_BATCH_VALIDATION_GROUP = "IMPORT_BATCH";
    private static final String EXPECTED_VALIDATION_MSG_FILE_EMPTY = "csv substitution file " + SUBSTITUTION_CSV
            + " is empty (both header and data row required)";
    private static final String EXPECTED_VALIDATION_MSG_FILE_NO_DATA = "csv substitution file " + SUBSTITUTION_CSV
            + " contains no data, only presumed header row";
    private static final String EXPECTED_VALIDATION_MSG_FILE_MISMATCH = "The number of data values do not match the number of headers in "
            + SUBSTITUTION_CSV;

    @InjectMocks
    private ValidateBatchCsvSubstitutionFile validateBatchCsvSubstitutionFile;

    @Test
    public void when_valid_csv_with_data_is_parsed_then_ap_batch_validation_returns_true() {
        final StringBuilder csvContent = new StringBuilder(CSV_VALID_HEADER_ROW);
        csvContent.append(CSV_VALID_DATA_ROW_ONE);
        csvContent.append(CSV_VALID_DATA_ROW_TWO);

        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(SUBSTITUTION_CSV, csvContent.toString());
        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_BATCH_ARCHIVE_FILENAME));

        final boolean csvValidationResult = validateBatchCsvSubstitutionFile.execute(context);

        assertTrue(csvValidationResult);
    }

    @Test
    public void when_invalid_csv_with_mismatched_data_rows_is_parsed_then_ap_batch_validation_returns_false() {
        final StringBuilder csvContent = new StringBuilder(CSV_VALID_HEADER_ROW);
        csvContent.append(CSV_MISMATCH_DATA_ROW_ONE);
        csvContent.append(CSV_MISMATCH_DATA_ROW_TWO);

        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(SUBSTITUTION_CSV, csvContent.toString());
        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_BATCH_ARCHIVE_FILENAME));

        validateBatchCsvSubstitutionFile.execute(context);

        assertEquals(EXPECTED_VALIDATION_MSG_FILE_MISMATCH, context.getValidationErrors().get(0));
    }

    @Test
    public void when_invalid_csv_with_no_first_row_is_parsed_then_ap_batch_validation_returns_false() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(SUBSTITUTION_CSV, "");
        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_BATCH_ARCHIVE_FILENAME));

        validateBatchCsvSubstitutionFile.execute(context);

        assertEquals(EXPECTED_VALIDATION_MSG_FILE_EMPTY, context.getValidationErrors().get(0));
    }

    @Test
    public void when_invalid_csv_with_no_data_rows_is_parsed_then_ap_batch_validation_returns_false() {
        final StringBuilder csvContent = new StringBuilder(CSV_VALID_HEADER_ROW);
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(SUBSTITUTION_CSV, csvContent.toString());
        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_BATCH_ARCHIVE_FILENAME));

        validateBatchCsvSubstitutionFile.execute(context);

        assertEquals(EXPECTED_VALIDATION_MSG_FILE_NO_DATA, context.getValidationErrors().get(0));
    }
}
