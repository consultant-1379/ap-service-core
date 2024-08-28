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
 * Unit tests for {@link ValidateBatchCsvSubstitutionFileDoesNotContainDuplicateHeaders}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateBatchCsvSubstitutionFileDoesNotContainDuplicateHeadersTest {

    private static final String VALID_BATCH_ARCHIVE_FILENAME = "projectTest.zip";
    private static final String SUBSTITUTION_CSV = "Substitution.csv";
    private static final String CSV_VALID_HEADER_ROW = "node_name, node_ip, oam_ip \r\n";
    private static final String CSV_SINGLE_DUPLICATE_HEADER_ROW = "node_name, node_ip, node_ip\r\n";
    private static final String CSV_MULTIPLE_DUPLICATE_HEADER_ROW = "node_name, node_name, node_ip,node_ip\r\n";
    private static final String CSV_DATA_ROW = "node-1, 1.10.10.11, 1.2.3.4 \r\n";
    private static final String CSV_MULTIPLE_DATA_ROW = "node-1, 1.10.10.11, 1.2.3.4, 1.2.3.9 \r\n";
    private static final String CSV_HEADER_SINGLE_DUPLICATE = "node_ip";
    private static final String PROJECT_BATCH_VALIDATION_GROUP = "IMPORT_BATCH";
    private static final String EXPECTED_VALIDATION_MSG_HEADER_SINGLE_DUPLICATE = "Duplicate header(s) [" + CSV_HEADER_SINGLE_DUPLICATE
            + "] found in csv substitution file";

    @InjectMocks
    private ValidateBatchCsvSubstitutionFileDoesNotContainDuplicateHeaders validateBatchCsvSubstitutionFileDoesNotContainDuplicateHeaders;

    @Test
    public void when_csv_file_with_no_duplicate_headers_is_parsed_then_ap_batch_validation_returns_true() {
        final StringBuilder csvContent = new StringBuilder(CSV_VALID_HEADER_ROW);
        csvContent.append(CSV_DATA_ROW);

        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(SUBSTITUTION_CSV, csvContent.toString());
        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_BATCH_ARCHIVE_FILENAME));

        final boolean result = validateBatchCsvSubstitutionFileDoesNotContainDuplicateHeaders.execute(context);

        assertTrue(result);
    }

    @Test
    public void when_csv_file_with_single_duplicate_header_is_parsed_then_ap_batch_validation_returns_false() {
        final StringBuilder csvContent = new StringBuilder(CSV_SINGLE_DUPLICATE_HEADER_ROW);
        csvContent.append(CSV_DATA_ROW);

        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(SUBSTITUTION_CSV, csvContent.toString());
        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_BATCH_ARCHIVE_FILENAME));

        validateBatchCsvSubstitutionFileDoesNotContainDuplicateHeaders.execute(context);

        assertEquals(EXPECTED_VALIDATION_MSG_HEADER_SINGLE_DUPLICATE, context.getValidationErrors().get(0));
    }

    @Test
    public void when_csv_file_with_multiple_duplicate_header_is_parsed_then_ap_batch_validation_returns_false() {

        final StringBuilder csvContent = new StringBuilder(CSV_MULTIPLE_DUPLICATE_HEADER_ROW);
        csvContent.append(CSV_MULTIPLE_DATA_ROW);

        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(SUBSTITUTION_CSV, csvContent.toString());
        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_BATCH_ARCHIVE_FILENAME));

        validateBatchCsvSubstitutionFileDoesNotContainDuplicateHeaders.execute(context);
    }
}
