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
package com.ericsson.oss.services.ap.core.usecase.validation.greenfield;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_IDENTIFIER_VALUE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.schema.SchemaData;
import com.ericsson.oss.services.ap.api.schema.SchemaService;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.common.util.xml.XmlValidator;
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaAccessException;
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaValidationException;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;

/**
 * Unit tests for {@link ValidateNodeInfoArtifactAgainstSchema}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateNodeInfoArtifactAgainstSchemaTest {

    private static final String NODE_INFO_FILE = "nodeInfo.xml";
    private static final String NODE_INFO_ARTIFACT_TYPE = "NodeInfo";
    private static final String VALID_WORK_ORDER_ID = "ABC-12345";
    private static final String INVALID_WORK_ORDER_ID = " ZYX-54321";
    private static final String VALID_USER_LABEL = "User label";
    private static final String INVALID_USER_LABEL = "Invalid user label value exceeding the maximum allowed characters permitted by the validation schema";
    private static final String VALID_EMAIL = "abc@ex1.org,efg.123@example.net;ar-2b@mcc_rt.cn,111_wzt@164-bn.czt";
    private static final String INVALID_EMAIL = "abc@ex1org,efg.123example.net;ar@-2b@mcc_rt.cn, 111_wzt@164-bn.czt";

    private static final String NODEINFO_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"NodeInfo.xsd\">"
            + "<name>nodeName</name>"
            + "<workOrderId>" + VALID_WORK_ORDER_ID + "</workOrderId>"
            + "<notifications>"
            + "<email>" + VALID_EMAIL + "</email>"
            + "</notifications>"
            + "<nodeIdentifier>" + NODE_IDENTIFIER_VALUE + "</nodeIdentifier>"
            + "<ipAddress>192.168.5.18</ipAddress>"
            + "<nodeType>" + VALID_NODE_TYPE + "</nodeType>"
            + "<userLabel>" + VALID_USER_LABEL + "</userLabel>"
            + "<artifacts>"
            + "<siteBasic>SiteBasic.xml</siteBasic>"
            + "<siteInstallation>SiteInstallation.xml</siteInstallation>"
            + "<siteEquipment>SiteEquipment.xml</siteEquipment>"
            + "</artifacts>"
            + "</nodeInfo>";

    @Mock
    private Archive archiveReader;

    @Mock
    private SchemaService schemaService;

    @Mock
    private XmlValidator xmlValidator;

    @InjectMocks
    private ValidateNodeInfoArtifactAgainstSchema validateNodeInfoArtifactAgainstSchema;

    private final List<SchemaData> schemas = new ArrayList<>();
    private final Map<String, Object> projectDataContentTarget = new HashMap<>();

    private ValidationContext validationContext;

    @Before
    public void setup() {
        final List<String> directoryList = new ArrayList<>();
        directoryList.add("Node1");
        directoryList.add("Node2");
        directoryList.add("Node3");

        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), "testProject.zip");
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archiveReader);
        projectDataContentTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryList);
        validationContext = new ValidationContext("import", projectDataContentTarget);

        when(archiveReader.getAllDirectoryNames()).thenReturn(directoryList);

        final ArchiveArtifact archiveArtifact = new ArchiveArtifact("NodeInfo", NODEINFO_XML);
        when(archiveReader.getArtifactOfNameInDir("Node1", NODE_INFO_FILE)).thenReturn(archiveArtifact);
        when(archiveReader.getArtifactOfNameInDir("Node2", NODE_INFO_FILE)).thenReturn(archiveArtifact);
        when(archiveReader.getArtifactOfNameInDir("Node3", NODE_INFO_FILE)).thenReturn(archiveArtifact);
        when(schemaService.readSchemas(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, NODE_INFO_ARTIFACT_TYPE)).thenReturn(schemas);
    }

    @Test
    public void whenAllNodeinfoArtifactsAreValidThenReturnTrue() {
        final boolean result = validateNodeInfoArtifactAgainstSchema.execute(validationContext);
        assertTrue(result);
        assertTrue(validationContext.getValidationErrors().isEmpty());
    }

    @Test
    public void whenAllNodeinfoArtifactsAreInvalidThenReturnFalse() {
        doThrow(SchemaValidationException.class).when(xmlValidator).validateAgainstSchema(NODEINFO_XML, schemas);
        final boolean result = validateNodeInfoArtifactAgainstSchema.execute(validationContext);
        assertFalse(result);
    }

    @Test
    public void whenWorkOrderIdInNodeInfoIsInvalidThenReturnFalse() {
        final ArchiveArtifact archiveArtifact = new ArchiveArtifact("NodeInfo", NODEINFO_XML.replace(VALID_WORK_ORDER_ID, INVALID_WORK_ORDER_ID));
        when(archiveReader.getArtifactOfNameInDir("Node1", NODE_INFO_FILE)).thenReturn(archiveArtifact);
        when(archiveReader.getArtifactOfNameInDir("Node2", NODE_INFO_FILE)).thenReturn(archiveArtifact);
        when(archiveReader.getArtifactOfNameInDir("Node3", NODE_INFO_FILE)).thenReturn(archiveArtifact);

        doThrow(SchemaValidationException.class).when(xmlValidator).validateAgainstSchema(NODEINFO_XML.replace(VALID_WORK_ORDER_ID, INVALID_WORK_ORDER_ID), schemas);
        final boolean result = validateNodeInfoArtifactAgainstSchema.execute(validationContext);
        assertFalse(result);
    }

    @Test
    public void whenEmailInNodeInfoIsInvalidThenReturnFalse() {
        final ArchiveArtifact archiveArtifact = new ArchiveArtifact("NodeInfo", NODEINFO_XML.replace(VALID_EMAIL, INVALID_EMAIL));
        when(archiveReader.getArtifactOfNameInDir("Node1", NODE_INFO_FILE)).thenReturn(archiveArtifact);
        when(archiveReader.getArtifactOfNameInDir("Node2", NODE_INFO_FILE)).thenReturn(archiveArtifact);
        when(archiveReader.getArtifactOfNameInDir("Node3", NODE_INFO_FILE)).thenReturn(archiveArtifact);

        doThrow(SchemaValidationException.class).when(xmlValidator).validateAgainstSchema(NODEINFO_XML.replace(VALID_EMAIL, INVALID_EMAIL), schemas);
        final boolean result = validateNodeInfoArtifactAgainstSchema.execute(validationContext);
        assertFalse(result);
    }

    @Test
    public void whenUserLabelInNodeInfoIsInvalidThenReturnFalse() {
        final ArchiveArtifact archiveArtifact = new ArchiveArtifact("NodeInfo", NODEINFO_XML.replace(VALID_USER_LABEL, INVALID_USER_LABEL));
        when(archiveReader.getArtifactOfNameInDir("Node1", NODE_INFO_FILE)).thenReturn(archiveArtifact);
        when(archiveReader.getArtifactOfNameInDir("Node2", NODE_INFO_FILE)).thenReturn(archiveArtifact);
        when(archiveReader.getArtifactOfNameInDir("Node3", NODE_INFO_FILE)).thenReturn(archiveArtifact);

        doThrow(SchemaValidationException.class).when(xmlValidator).validateAgainstSchema(NODEINFO_XML.replace(VALID_USER_LABEL, INVALID_USER_LABEL), schemas);
        final boolean result = validateNodeInfoArtifactAgainstSchema.execute(validationContext);
        assertFalse(result);
    }

    @Test
    public void whenOneNodeinfoInvalidAndOthersAreValidThenReturnFalse() {
        doNothing().doNothing().doThrow(SchemaValidationException.class).when(xmlValidator).validateAgainstSchema(NODEINFO_XML, schemas);
        final boolean result = validateNodeInfoArtifactAgainstSchema.execute(validationContext);
        assertFalse(result);
    }

    @Test
    public void whenNodeinfoArtifactsAreInvalidThenTheErrorsAreAddedToTheValidationContext() {
        doThrow(SchemaValidationException.class).when(xmlValidator).validateAgainstSchema(NODEINFO_XML, schemas);

        validateNodeInfoArtifactAgainstSchema.execute(validationContext);

        assertEquals(3, validationContext.getValidationErrors().size());
        assertEquals(String.format("Node1 - nodeInfo.xml failed to validate against schema. %s", "null"),
                validationContext.getValidationErrors().get(0));
        assertEquals(String.format("Node2 - nodeInfo.xml failed to validate against schema. %s", "null"),
                validationContext.getValidationErrors().get(1));
        assertEquals(String.format("Node3 - nodeInfo.xml failed to validate against schema. %s", "null"),
                validationContext.getValidationErrors().get(2));
    }

    @Test
    public void whenErrorAccessingNodeinfoSchemaThenReturnFalse() {
        doThrow(SchemaAccessException.class).when(schemaService).readSchemas(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, NODE_INFO_ARTIFACT_TYPE);
        final boolean result = validateNodeInfoArtifactAgainstSchema.execute(validationContext);
        assertFalse(result);
        verify(xmlValidator, never()).validateAgainstSchema(NODEINFO_XML, schemas);
    }

    @Test
    public void whenErrorAccessingNodeinfoSchemaThenErrorsAreAddedToTheValidationContext() {
        doThrow(SchemaAccessException.class).when(schemaService).readSchemas(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, NODE_INFO_ARTIFACT_TYPE);
        validateNodeInfoArtifactAgainstSchema.execute(validationContext);
        assertEquals("Node1 - Issue accessing schema for nodeInfo.xml", validationContext.getValidationErrors().get(0));
    }
}
