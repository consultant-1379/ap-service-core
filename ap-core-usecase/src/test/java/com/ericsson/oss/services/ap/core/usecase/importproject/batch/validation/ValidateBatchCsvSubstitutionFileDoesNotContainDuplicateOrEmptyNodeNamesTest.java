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
 * Unit tests for {@link ValidateBatchCsvSubstitutionFileDoesNotContainDuplicateOrEmptyNodeNames}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateBatchCsvSubstitutionFileDoesNotContainDuplicateOrEmptyNodeNamesTest {

    private static final String VALID_BATCH_ARCHIVE_FILENAME = "projectTest.zip";
    private static final String SUBSTITUTION_CSV = "Substitution.csv";
    private static final String NODEINFO_XML = "NODeInfo.xml";
    private static final String CSV_VALID_HEADER_ROW = "first_header, node_name, second_header\r\n";
    private static final String CSV_INVALID_HEADER_ROW = "first_header,what_name,second_header \r\n";
    private static final String CSV_DATA_ROW1 = "first_data,node-1,second_data\r\n";
    private static final String CSV_DATA_ROW2 = "first_data,node-2,second_data\r\n";
    private static final String CSV_DATA_ROW3 = "first_data,, second_data\r\n";
    private static final String NODEINFO_VALID_CONTENT = "<nodeInfo> <name>%node_name%</name> </nodeInfo>";

    private static final String NODEINFO_INVALID_CONTENT = "<nodeInfo> <name>node_ERbs</name> </nodeInfo>";
    private static final String MANDATORY_ELEMENT_MISSING_MESSAGE_FORMAT = "Mandatory variable substitution of element <%s> in nodeInfo.xml is not present";
    private static final String EXPECTED_DUPLICATE_NODE_NAME_MESSAGE = "Duplicate node name(s) [node-1] found in csv substitution file";
    private static final String EXPECTED_EMPTY_NODE_NAME_MESSAGE_FORMAT = "Empty node name found in csv substitution file at row(s) [%s]";
    private static final String EXPECTED_NO_NODE_NAME_SUB_VALUE_MESSAGE_FORMAT = "Cannot substitute element tag(s) [%s] from file nodeInfo.xml. Expected matching substitution header(s) not in csv file";
    private static final String PROJECT_BATCH_VALIDATION_GROUP = "IMPORT_BATCH";

    @InjectMocks
    private ValidateBatchCsvSubstitutionFileDoesNotContainDuplicateOrEmptyNodeNames validateDuplicateNodeNames;

    @Test
    public void when_no_duplicate_node_names_found_then_validation_returns_true() {
        final StringBuilder csvContent = new StringBuilder(CSV_VALID_HEADER_ROW);
        csvContent.append(CSV_DATA_ROW1);
        csvContent.append(CSV_DATA_ROW2);

        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(SUBSTITUTION_CSV, csvContent.toString());
        zcg.createFileInZip(NODEINFO_XML, NODEINFO_VALID_CONTENT);
        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_BATCH_ARCHIVE_FILENAME));

        final boolean result = validateDuplicateNodeNames.execute(context);

        assertTrue(result);
    }

    @Test
    public void when_duplicate_node_name_found_then_validation_returns_false() {
        final StringBuilder csvContent = new StringBuilder(CSV_VALID_HEADER_ROW);
        csvContent.append(CSV_DATA_ROW1);
        csvContent.append(CSV_DATA_ROW1);

        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(SUBSTITUTION_CSV, csvContent.toString());
        zcg.createFileInZip(NODEINFO_XML, NODEINFO_VALID_CONTENT);
        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_BATCH_ARCHIVE_FILENAME));

        validateDuplicateNodeNames.execute(context);

        assertEquals("Validation errors are recorded when duplicate node names exist", EXPECTED_DUPLICATE_NODE_NAME_MESSAGE,
                context.getValidationErrors().get(0));
    }

    @Test
    public void when_empty_node_name_found_then_validation_returns_false() {
        final StringBuilder csvContent = new StringBuilder(CSV_VALID_HEADER_ROW);
        csvContent.append(CSV_DATA_ROW1);
        csvContent.append(CSV_DATA_ROW3);

        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(SUBSTITUTION_CSV, csvContent.toString());
        zcg.createFileInZip(NODEINFO_XML, NODEINFO_VALID_CONTENT);
        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_BATCH_ARCHIVE_FILENAME));

        validateDuplicateNodeNames.execute(context);

        assertEquals("Validation errors are recorded when empty node names exist", String.format(EXPECTED_EMPTY_NODE_NAME_MESSAGE_FORMAT, "2"),
                context.getValidationErrors().get(0));
    }

    @Test
    public void when_node_name_tag_cannot_be_found_then_validation_returns_false() {
        final StringBuilder csvContent = new StringBuilder(CSV_VALID_HEADER_ROW);
        csvContent.append(CSV_DATA_ROW1);
        csvContent.append(CSV_DATA_ROW2);

        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(SUBSTITUTION_CSV, csvContent.toString());
        zcg.createFileInZip(NODEINFO_XML, NODEINFO_INVALID_CONTENT);
        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_BATCH_ARCHIVE_FILENAME));

        validateDuplicateNodeNames.execute(context);

        assertEquals(String.format(MANDATORY_ELEMENT_MISSING_MESSAGE_FORMAT, "name"), context.getValidationErrors().get(0));
    }

    @Test
    public void when_node_header_not_found_in_substitution_file_then_validation_returns_false() {
        final StringBuilder csvContent = new StringBuilder(CSV_INVALID_HEADER_ROW);
        csvContent.append(CSV_DATA_ROW1);
        csvContent.append(CSV_DATA_ROW2);

        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(SUBSTITUTION_CSV, csvContent.toString());
        zcg.createFileInZip(NODEINFO_XML, NODEINFO_VALID_CONTENT);
        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_BATCH_ARCHIVE_FILENAME));

        validateDuplicateNodeNames.execute(context);

        assertEquals(String.format(EXPECTED_NO_NODE_NAME_SUB_VALUE_MESSAGE_FORMAT, "node_name"), context.getValidationErrors().get(0));
    }
}
