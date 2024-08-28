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
 * Unit tests for {@link ValidateBatchXmlAgainstCsvForAllSubstitutions}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateBatchXmlAgainstCsvForAllSubstitutionsTest {

    private static final String VALID_BATCH_ARCHIVE_FILENAME = "projectTest.zip";

    private static final String CSV_FILENAME = "substitutions.csv";
    private static final String CSV_CONTENT_HEADER = "node_name, node_ip,oam_ip, oam_router, rs \r\n";
    private static final String CSV_CONTENT_ROW_ONE = "node 1 , 1.2.3.1, 101.102.103.101,10.200.0.1, rs1 \r\n";
    private static final String CSV_CONTENT_ROW_TWO = "node 2 , 1.2.3.2, 101.102.103.102,  10.200.0.1, rs2 \r\n";

    private static final String CSV_MISSING_TAG_SINGLE = "missing";
    private static final String CSV_MISSING_TAG_MULTIPLE = "oam_subnet, missing";

    private static final String XML_NODEINFO_FILENAME = "nodeInfo.xml";
    private static final String XML_NODEINFO_CONTENT = "<!-- nodeInfo.xml - test --> <name>%node_name%</name> <ipAddress>%node_ip%</ipAddress>";
    private static final String XML_NODEINFO_CONTENT_MISSING = "<!-- nodeInfo.xml - missing --> <name>%node_name%</name> <ipAddress>%node_ip%</ipAddress>"
            + "<another>%missing%</another>";

    private static final String XML_SITEINSTALL_FILENAME = "SiteInstall.xml";
    private static final String XML_SITEINSTALL_CONTENT = "<!-- SiteInstall.xml - test --> <OamIpConfigurationData ipAddress=\"%oam_ip%\" subnetMask=\"255.255.240.0\" defaultRouter0=\"%oam_router%\">";
    private static final String XML_SITEINSTALL_CONTENT_MISSING = "<!-- SiteInstall.xml - missing --> <OamIpConfigurationData ipAddress=\"%oam_ip%\" subnetMask=\"%oam_subnet%\" defaultRouter0=\"%oam_router%\">"
            + "<another>%missing%</another>";

    private static final String XML_ICF_FILENAME = "Icf.xml";
    private static final String XML_ICF_CONTENT = "<!-- Icf.xml - test --> <dnPrefix =\"%INTERNAL_dnPrefix%\" nodeName =\"%logicalName%\" networkManagedElementId=\"%INTERNAL_logicalName%\" \"%INTERNAL_Ldap%\">";

    private static final String PROJECT_BATCH_VALIDATION_GROUP = "IMPORT_BATCH";
    private static final String EXPECTED_VALIDATION_SUBSTITUTION_MISSING_FORMAT = "Cannot substitute element tag(s) [%s] from file %s. Expected matching substitution header(s) not in csv file";

    @InjectMocks
    private ValidateBatchXmlAgainstCsvForAllSubstitutions validateBatchXmlAgainstCsvForAllSubstitutions;

    @Test
    public void when_substitution_variables_in_single_xml_are_in_header_row_of_substitution_csv_then_ap_test_batch_validation_returns_true() {
        final StringBuilder csvContent = new StringBuilder(CSV_CONTENT_HEADER);
        csvContent.append(CSV_CONTENT_ROW_ONE);
        csvContent.append(CSV_CONTENT_ROW_TWO);

        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(CSV_FILENAME, csvContent.toString());
        zcg.createFileInZip(XML_NODEINFO_FILENAME, XML_NODEINFO_CONTENT);

        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_BATCH_ARCHIVE_FILENAME));

        final boolean result = validateBatchXmlAgainstCsvForAllSubstitutions.execute(context);

        assertTrue(result);
    }

    @Test
    public void when_substitution_variables_missing_from_csv_is_reserved_substitution_tag_then_ap_test_batch_validation_returns_true() {

        final StringBuilder csvContent = new StringBuilder(CSV_CONTENT_HEADER);
        csvContent.append(CSV_CONTENT_ROW_ONE);
        csvContent.append(CSV_CONTENT_ROW_TWO);

        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(CSV_FILENAME, csvContent.toString());
        zcg.createFileInZip(XML_NODEINFO_FILENAME, XML_NODEINFO_CONTENT);
        zcg.createFileInZip(XML_ICF_FILENAME, XML_ICF_CONTENT);

        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_BATCH_ARCHIVE_FILENAME));

        final boolean result = validateBatchXmlAgainstCsvForAllSubstitutions.execute(context);

        assertTrue(result);
    }

    @Test
    public void when_substitution_variables_in_multiple_xml_are_in_header_row_of_substitution_csv_then_ap_test_batch_validation_returns_true() {

        final StringBuilder csvContent = new StringBuilder(CSV_CONTENT_HEADER);
        csvContent.append(CSV_CONTENT_ROW_ONE);
        csvContent.append(CSV_CONTENT_ROW_TWO);

        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(CSV_FILENAME, csvContent.toString());
        zcg.createFileInZip(XML_NODEINFO_FILENAME, XML_NODEINFO_CONTENT);
        zcg.createFileInZip(XML_SITEINSTALL_FILENAME, XML_SITEINSTALL_CONTENT);

        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_BATCH_ARCHIVE_FILENAME));

        final boolean result = validateBatchXmlAgainstCsvForAllSubstitutions.execute(context);

        assertTrue(result);
    }

    @Test
    public void when_one_substitution_variable_in_single_xml_is_not_in_header_row_of_substitution_csv_then_ap_test_batch_validation_returns_false() {
        final StringBuilder csvContent = new StringBuilder(CSV_CONTENT_HEADER);
        csvContent.append(CSV_CONTENT_ROW_ONE);
        csvContent.append(CSV_CONTENT_ROW_TWO);

        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(CSV_FILENAME, csvContent.toString());
        zcg.createFileInZip(XML_NODEINFO_FILENAME, XML_NODEINFO_CONTENT_MISSING);

        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_BATCH_ARCHIVE_FILENAME));

        validateBatchXmlAgainstCsvForAllSubstitutions.execute(context);

        assertEquals(String.format(EXPECTED_VALIDATION_SUBSTITUTION_MISSING_FORMAT, CSV_MISSING_TAG_SINGLE, XML_NODEINFO_FILENAME),
                context.getValidationErrors().get(0));
    }

    @Test
    public void when_many_substitution_variables_in_multiple_xml_are_not_in_header_row_of_substitution_csv_then_ap_test_batch_validation_returns_false() {
        final StringBuilder csvContent = new StringBuilder(CSV_CONTENT_HEADER);
        csvContent.append(CSV_CONTENT_ROW_ONE);
        csvContent.append(CSV_CONTENT_ROW_TWO);

        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(CSV_FILENAME, csvContent.toString());
        zcg.createFileInZip(XML_NODEINFO_FILENAME, XML_NODEINFO_CONTENT_MISSING);
        zcg.createFileInZip(XML_SITEINSTALL_FILENAME, XML_SITEINSTALL_CONTENT_MISSING);

        final ValidationContext context = new ValidationContext(PROJECT_BATCH_VALIDATION_GROUP, zcg.getZipData(VALID_BATCH_ARCHIVE_FILENAME));

        validateBatchXmlAgainstCsvForAllSubstitutions.execute(context);

        assertEquals(String.format(EXPECTED_VALIDATION_SUBSTITUTION_MISSING_FORMAT, CSV_MISSING_TAG_SINGLE, XML_NODEINFO_FILENAME),
                context.getValidationErrors().get(0));
        assertEquals(String.format(EXPECTED_VALIDATION_SUBSTITUTION_MISSING_FORMAT, CSV_MISSING_TAG_MULTIPLE, XML_SITEINSTALL_FILENAME),
                context.getValidationErrors().get(1));
    }
}