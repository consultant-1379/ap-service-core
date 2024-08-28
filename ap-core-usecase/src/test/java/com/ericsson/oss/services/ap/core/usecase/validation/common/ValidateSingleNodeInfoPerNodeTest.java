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

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;

/**
 * Unit tests for {@link ValidateSingleNodeInfoPerNode}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateSingleNodeInfoPerNodeTest {

    private static final String NODE_INFO_XML = "nodeInfo.xml";
    private static final String TEST_FOLDER = "test";
    private static final String VALID_PROJECT_FILE_NAME = "test.zip";
    private static final String PROJECT_INFO_XML = "projectInfo.xml";
    private static final String TESTCONTENT = "testcontent";
    private static final String INVALID_NODE_INFO = "nodeInfo1.xml";

    private static final String PROJECT_FILE_VALIDATION_GROUP = "validate_project_content";
    private static final String NO_FILE_NODE_FOLDER_MESSAGE_FORMAT = "nodeInfo.xml is not found";
    private static final String NODE_INFO_OUTSIDE_NODE_FOLDERS_MESSAGE_FORMAT = "nodeInfo.xml found outside node folders";

    @InjectMocks
    private ValidateSingleNodeInfoPerNode validateSingleNodeInfoPerNode;

    @Mock
    private ValidationContext context;

    @Test
    public void testSingleNodeInfoInSingleNodeFolderSuccessful() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(PROJECT_INFO_XML, TESTCONTENT);
        zcg.createFileInZip(TEST_FOLDER, NODE_INFO_XML, TESTCONTENT);
        final Map<String, Object> target = zcg.getZipData(VALID_PROJECT_FILE_NAME);
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, target);

        final boolean result = validateSingleNodeInfoPerNode.execute(context);

        assertTrue(result);
    }

    @Test
    public void testNoNodeInfoInSingleNodeFolderFailure() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(PROJECT_INFO_XML, TESTCONTENT);
        zcg.createFileInZip(TEST_FOLDER, INVALID_NODE_INFO, TESTCONTENT);

        final Map<String, Object> target = zcg.getZipData(VALID_PROJECT_FILE_NAME);
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, target);
        final String expectedMessage = String.format(NO_FILE_NODE_FOLDER_MESSAGE_FORMAT);

        validateSingleNodeInfoPerNode.execute(context);

        assertEquals(String.format("%s - %s", TEST_FOLDER, expectedMessage), context.getValidationErrors().get(0));
    }

    @Test
    public void testNodeInfoInRootFolderFailure() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(NODE_INFO_XML, TESTCONTENT);
        zcg.createFileInZip(TEST_FOLDER, NODE_INFO_XML, TESTCONTENT);

        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, zcg.getZipData(VALID_PROJECT_FILE_NAME));
        final String expectedMessage = String.format(NODE_INFO_OUTSIDE_NODE_FOLDERS_MESSAGE_FORMAT);

        validateSingleNodeInfoPerNode.execute(context);

        assertEquals(expectedMessage, context.getValidationErrors().get(0));
    }
}
