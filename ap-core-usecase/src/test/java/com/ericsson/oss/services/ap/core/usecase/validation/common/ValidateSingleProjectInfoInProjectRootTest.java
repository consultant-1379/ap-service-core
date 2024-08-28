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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;

/**
 * Unit tests for {@link ValidateSingleProjectInfoInProjectRoot}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateSingleProjectInfoInProjectRootTest {

    private static final String VALID_PROJECT_FILE_NAME = "test.zip";
    private static final String TEST_FOLDER_NAME = "test";
    private static final String PROJECT_INFO_XML = "projectInfo.xml";
    private static final String TESTCONTENT = "testcontent";
    private static final String PROJECT_INFO1_XML = "projectInfo1.xml";
    private static final String PROJECT_FILE_VALIDATION_GROUP = "validate_project_content";

    private static final String NO_FILE_IN_ROOT_EXPECTED_MESSAGE_FORMAT = "No projectInfo.xml file found in project file";
    private static final String MULTIPLE_FILES_EXPECTED_MESSAGE_FORMAT = "Multiple projectInfo.xml files found in project file";
    private static final String NOT_IN_ROOT_EXPECTED_MESSAGE_FORMAT = "projectInfo.xml is not in root of project file";

    @InjectMocks
    private ValidateSingleProjectInfoInProjectRoot validateSingleProjectInfoInZipRoot;

    private ValidationContext context;

    @Test
    public void testSingleProjectInfoInZipRootSuccessful() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(PROJECT_INFO_XML, TESTCONTENT);
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, zcg.getZipData(VALID_PROJECT_FILE_NAME));

        final boolean result = validateSingleProjectInfoInZipRoot.execute(context);

        assertTrue(result);
    }

    @Test
    public void testNoProjectInfoInZipRootSuccessful() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(PROJECT_INFO1_XML, TESTCONTENT);
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, zcg.getZipData(VALID_PROJECT_FILE_NAME));

        validateSingleProjectInfoInZipRoot.execute(context);

        assertEquals(NO_FILE_IN_ROOT_EXPECTED_MESSAGE_FORMAT, context.getValidationErrors().get(0));
    }

    @Test
    public void testMultipleProjectInfoInZipFailure() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(PROJECT_INFO_XML, TESTCONTENT);
        zcg.createFileInZip(TEST_FOLDER_NAME, PROJECT_INFO_XML, TESTCONTENT);
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, zcg.getZipData(VALID_PROJECT_FILE_NAME));

        validateSingleProjectInfoInZipRoot.execute(context);

        assertEquals(MULTIPLE_FILES_EXPECTED_MESSAGE_FORMAT, context.getValidationErrors().get(0));
    }

    @Test
    public void testProjectFileNotInRoot() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(TEST_FOLDER_NAME, PROJECT_INFO_XML, TESTCONTENT);
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, zcg.getZipData(VALID_PROJECT_FILE_NAME));

        validateSingleProjectInfoInZipRoot.execute(context);

        assertEquals(NOT_IN_ROOT_EXPECTED_MESSAGE_FORMAT, context.getValidationErrors().get(0));
    }
}
