/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.validation.common;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.HARDWARE_SERIAL_NUMBER_VALUE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;

/**
 * Unit tests for {@link ValidateHwSerialNumberUniqueInProject}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateHwSerialNumberUniqueInProjectTest {

    private static final String VALID_PROJECT_FILE_NAME = "test.zip";
    private static final String ULSTER_NODE_FOLDER = "ulster";
    private static final String LEINSTER_NODE_FOLDER = "leinster";
    private static final String NODE_INFO_XML = "nodeInfo.xml";
    private static final String INVALID_CONTENT = "invalid_content";

    private static final String PROJECT_FILE_VALIDATION_GROUP = "validate_project_content";
    private static final String MESSAGE_NODE_DUPLICATE_HW_SERIAL_NUMBER = "Duplicate hardware serial number %s";

    private static final String VALID_NODE_INFO = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
        + "<nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"NodeInfo.xsd\">"
        + "<name>"
        + NODE_NAME
        + "</name>"
        + "<nodeIdentifier>D.1.44</nodeIdentifier>"
        + "<ipAddress>192.168.5.18</ipAddress>"
        + "<hardwareSerialNumber>"
        + HARDWARE_SERIAL_NUMBER_VALUE
        + "</hardwareSerialNumber>"
        + "<nodeType>ERBS</nodeType>"
        + "<!--autoIntegration>"
        + "<unlockCells>false</unlockCells>"
        + "</autoIntegration-->"
        + "<artifacts>"
        + "<siteBasic>SiteBasic.xml</siteBasic>"
        + "<siteInstallation>SiteInstallation.xml</siteInstallation>"
        + "<siteEquipment>SiteEquipment.xml</siteEquipment>"
        + "<configs><config>TN_Data.xml</config></configs>"
        + "</artifacts>"
        + "</nodeInfo>";

    @Mock
    private NodeInfo nodeInfo;

    @InjectMocks
    @Spy
    private final ValidateHwSerialNumberUniqueInProject validateSerialNumberUniqueInProject = new ValidateHwSerialNumberUniqueInProject();

    @Before
    public void setUp() {
        doReturn(nodeInfo).when(validateSerialNumberUniqueInProject).getNodeInfo(any(ValidationContext.class), anyString());
        when(nodeInfo.getName()).thenReturn(NODE_NAME);
    }

    @Test
    public void whenNodeInfoHwSerialNumberIsUniqueAndValidThenValidationIsSuccessful() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(ULSTER_NODE_FOLDER, NODE_INFO_XML, VALID_NODE_INFO);
        final ValidationContext context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, zcg.getZipData(VALID_PROJECT_FILE_NAME));

        when(nodeInfo.getHardwareSerialNumber()).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE);
        final boolean isValidationSuccessful = validateSerialNumberUniqueInProject.execute(context);

        assertTrue(isValidationSuccessful);
    }

    @Test
    public void whenNodeInfoHwSerialNumberIsNullThenValidationIsSuccessful() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(ULSTER_NODE_FOLDER, NODE_INFO_XML, INVALID_CONTENT);
        final ValidationContext context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, zcg.getZipData(VALID_PROJECT_FILE_NAME));

        when(nodeInfo.getHardwareSerialNumber()).thenReturn(null);
        final boolean isValidationSuccessful = validateSerialNumberUniqueInProject.execute(context);

        assertTrue(isValidationSuccessful);
    }

    @Test
    public void whenNodeInfoHwSerialNumberIsEmptyThenValidationIsSuccessful() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(ULSTER_NODE_FOLDER, NODE_INFO_XML, INVALID_CONTENT);
        final ValidationContext context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, zcg.getZipData(VALID_PROJECT_FILE_NAME));

        when(nodeInfo.getHardwareSerialNumber()).thenReturn("");
        final boolean isValidationSuccessful = validateSerialNumberUniqueInProject.execute(context);

        assertTrue(isValidationSuccessful);
    }

    @Test
    public void whenNodeInfoHwSerialNumberAlreadyExistsInProjectThenValidationFailsWithDuplicateIpAddressMessage() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(ULSTER_NODE_FOLDER, NODE_INFO_XML, VALID_NODE_INFO);
        zcg.createFileInZip(LEINSTER_NODE_FOLDER, NODE_INFO_XML, VALID_NODE_INFO);

        final ValidationContext context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, zcg.getZipData(VALID_PROJECT_FILE_NAME));
        when(nodeInfo.getHardwareSerialNumber()).thenReturn(HARDWARE_SERIAL_NUMBER_VALUE);

        final String expectedMessage = String.format(MESSAGE_NODE_DUPLICATE_HW_SERIAL_NUMBER, HARDWARE_SERIAL_NUMBER_VALUE);
        validateSerialNumberUniqueInProject.execute(context);

        assertEquals(String.format("%s - %s", LEINSTER_NODE_FOLDER, expectedMessage), context.getValidationErrors().get(0));
    }
}