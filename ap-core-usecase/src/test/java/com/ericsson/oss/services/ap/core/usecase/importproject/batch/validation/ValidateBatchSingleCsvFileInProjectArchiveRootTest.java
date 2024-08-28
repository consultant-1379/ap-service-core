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
 * Unit tests for {@link ValidateBatchSingleCsvFileInProjectArchiveRoot}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateBatchSingleCsvFileInProjectArchiveRootTest {

    private static final String VALID_PROJECT_ZIP_FILE_NAME = "projectTest.zip";
    private static final String TEST_FOLDER_NAME = "test";
    private static final String SUBSTITUTION_1_CSV = "substitution.csv";
    private static final String SUBSTITUTION_2_CSV = "Substitutions.CSV";
    private static final String INVALID_FILE_TXT = "invalidFile.txt";
    private static final String TEST_TXT_CONTENT = "heading1, heading2, heading3";
    private static final String TEST_CSV_CONTENT = "heading-a, heading-b, heading-c";
    private static final String PROJECT_BATCH_VALIDATION_GROUP = "IMPORT_BATCH";
    private static final String EXPECTED_VALIDATION_MSG_FILE_NOT_FOUND = "csv substitution file is not found in " + VALID_PROJECT_ZIP_FILE_NAME;
    private static final String EXPECTED_VALIDATION_MSG_MULTIPLE_FILES_FOUND = "Multiple csv substitution files found in "
            + VALID_PROJECT_ZIP_FILE_NAME;

    @InjectMocks
    private ValidateBatchSingleCsvFileInProjectArchiveRoot validateBatchSingleCsvFileInProjectArchiveRoot;

    @Test
    public void when_single_csv_is_present_in_archive_then_ap_batch_validation_returns_true() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(SUBSTITUTION_1_CSV, TEST_CSV_CONTENT);
        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_PROJECT_ZIP_FILE_NAME));

        final boolean result = validateBatchSingleCsvFileInProjectArchiveRoot.execute(context);

        assertTrue(result);
    }

    @Test
    public void when_no_csv_is_present_in_archive_then_ap_batch_validation_returns_false() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(INVALID_FILE_TXT, TEST_TXT_CONTENT);
        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_PROJECT_ZIP_FILE_NAME));

        validateBatchSingleCsvFileInProjectArchiveRoot.execute(context);

        assertEquals(EXPECTED_VALIDATION_MSG_FILE_NOT_FOUND, context.getValidationErrors().get(0));
    }

    @Test
    public void when_multiple_csv_are_present_in_archive_then_ap_batch_validation_returns_false() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(SUBSTITUTION_1_CSV, TEST_CSV_CONTENT);
        zcg.createFileInZip(TEST_FOLDER_NAME, SUBSTITUTION_2_CSV, TEST_CSV_CONTENT);
        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_PROJECT_ZIP_FILE_NAME));

        validateBatchSingleCsvFileInProjectArchiveRoot.execute(context);

        assertEquals(EXPECTED_VALIDATION_MSG_MULTIPLE_FILES_FOUND, context.getValidationErrors().get(0));
    }
}