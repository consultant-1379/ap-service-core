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
 * Unit tests for {@link ValidateBatchSingleNodeInfoXmlFileInProjectArchiveRoot}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateBatchSingleNodeInfoXmlFileInProjectArchiveRootTest {

    private static final String VALID_PROJECT_ZIP_FILE_NAME = "projectTest.zip";
    private static final String NODE_INFO_XML = "nodeInfo.xml";
    private static final String INVALID_FILE_TXT = "invalidFile.txt";
    private static final String TEST_CONTENT = "<!-- All or nothing, test content --> <sample> <tag> </tag> </sample>";

    private static final String PROJECT_BATCH_VALIDATION_GROUP = "IMPORT_BATCH";
    private static final String EXPECTED_VALIDATION_MSG_FILE_NOT_FOUND = "nodeInfo.xml is not found in " + VALID_PROJECT_ZIP_FILE_NAME;

    @InjectMocks
    private ValidateBatchSingleNodeInfoXmlFileInProjectArchiveRoot validateBatchSingleNodeInfoXmlFileInProjectArchiveRoot;

    @Test
    public void when_nodeInfo_is_present_in_archive_then_ap_test_batch_validation_returns_true() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(NODE_INFO_XML, TEST_CONTENT);
        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_PROJECT_ZIP_FILE_NAME));

        final boolean result = validateBatchSingleNodeInfoXmlFileInProjectArchiveRoot.execute(context);

        assertTrue(result);
    }

    @Test
    public void when_nodeInfo_is_not_present_in_archive_then_ap_batch_validation_returns_false() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(INVALID_FILE_TXT, TEST_CONTENT);

        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_PROJECT_ZIP_FILE_NAME));

        validateBatchSingleNodeInfoXmlFileInProjectArchiveRoot.execute(context);
        assertEquals(EXPECTED_VALIDATION_MSG_FILE_NOT_FOUND, context.getValidationErrors().get(0));
    }
}