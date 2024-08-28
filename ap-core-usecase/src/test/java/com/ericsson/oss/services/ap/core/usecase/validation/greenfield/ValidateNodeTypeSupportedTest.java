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
package com.ericsson.oss.services.ap.core.usecase.validation.greenfield;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.modeling.modelservice.typed.core.edt.EnumDataTypeSpecification;
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader;
import com.ericsson.oss.services.ap.core.usecase.validation.common.ZipContentGenerator;

/**
 * Unit tests for {@link ValidateNodeTypeSupported}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateNodeTypeSupportedTest {

    private static final String ZIPFILE_NAME = "test.zip";

    private static final String PROJECTINFO_FILENAME = "projectInfo.xml";
    private static final String PROJECTINFO_CONTENT = "testcontent";

    private static final String NODEINFO_FILENAME = "nodeInfo.xml";
    private static final String NODE_FOLDER_1 = "Folder1";
    private static final String NODE_FOLDER_2 = "Folder2";
    private static final String NODE_NAME1 = "NodeName1";
    private static final String NODE_NAME2 = "NodeName2";
    private static final String INVALID_NODE_TYPE = "INVALID";
    private static final String PROJECT_FILE_VALIDATION_GROUP = "validate_project_content";

    private static final List<String> SUPPORTED_NODE_TYPES = Arrays.asList("ERBS", "MSRBS_V1", "Router6000-2", "Router60002");

    private static final String NODEINFO_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"macroNodeInfo.xsd\">"
            + "<name>NODE_NAME</name>"
            + "<nodeIdentifier>" + NODE_IDENTIFIER_VALUE + "</nodeIdentifier>"
            + "<ipAddress>192.168.5.18</ipAddress>"
            + "<nodeType>NODE_TYPE</nodeType>"
            + "<artifacts>"
            + "<siteBasic>SiteBasic.xml</siteBasic>"
            + "<siteInstall>SiteInstallation.xml</siteInstall>"
            + "<siteEquipment>SiteEquipment.xml</siteEquipment>"
            + "<transport>TN_Data.xml</transport>"
            + "</artifacts>"
            + "</nodeInfo>";

    private ValidationContext context;

    @Mock
    private ModelReader modelReader;

    @Mock
    private NodeInfoReader nodeInfoReader;

    @Mock
    private NodeInfo nodeInfo;

    @Mock
    private EnumDataTypeSpecification enumSpecification;

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    @InjectMocks
    @Spy
    private ValidateNodeTypeSupported validateNodeTypeSupported;

    @Before
    public void setUp() {
        when(modelReader.getLatestEnumDataTypeSpecification(SchemaConstants.OSS_EDT, "ap", "NodeType")).thenReturn(enumSpecification);
        when(modelReader.getSupportedNodeTypes()).thenReturn(SUPPORTED_NODE_TYPES);
        when(enumSpecification.getMemberNames()).thenReturn(SUPPORTED_NODE_TYPES);

        when(nodeInfoReader.read(any(Archive.class), anyString())).thenReturn(nodeInfo);
        when(nodeInfo.getName()).thenReturn(NODE_NAME1).thenReturn(NODE_NAME2);
    }

    @Test
    public void testRuleReturnsTrueWhenNodeTypeAreSupportedForAllNodesInProject() {
        when(nodeTypeMapper.toApRepresentation(VALID_NODE_TYPE)).thenReturn(VALID_NODE_TYPE);
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, createTarget(NODEINFO_CONTENT, VALID_NODE_TYPE));
        when(nodeInfo.getNodeType()).thenReturn(VALID_NODE_TYPE);
        final boolean result = validateNodeTypeSupported.execute(context);
        assertTrue(result);
    }

    @Test
    public void testRuleReturnsTrueWhenNodeTypeAreSupportedForAllNodesInProjectForRouter() {
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, createTarget(NODEINFO_CONTENT, VALID_NODE_TYPE_R6K_IN_OSS));
        when(nodeInfo.getNodeType()).thenReturn(VALID_NODE_TYPE_R6K_IN_AP);
        final boolean result = validateNodeTypeSupported.execute(context);
        assertFalse(result);
    }

    @Test
    public void testRuleFailsWhenNodeTypeAreMissedForNodesInProject() {
        when(nodeTypeMapper.toApRepresentation(VALID_NODE_TYPE)).thenReturn(VALID_NODE_TYPE);
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, createTarget(NODEINFO_CONTENT, ""));
        when(nodeInfo.getNodeType()).thenReturn("");

        validateNodeTypeSupported.execute(context);

        assertEquals(2, context.getValidationErrors().size());
        final String errorMessage = String.format("The value of node attribute %s is not set in nodeInfo.xml file", "nodeType");
        assertEquals(String.format("Folder1 - %s", errorMessage), context.getValidationErrors().get(0));
        assertEquals(String.format("Folder2 - %s", errorMessage), context.getValidationErrors().get(1));
    }

    @Test
    public void testRuleFailsWhenNodeTypeAreNotSupportedForNodesInProject() {
        when(nodeTypeMapper.toApRepresentation(VALID_NODE_TYPE)).thenReturn(VALID_NODE_TYPE);
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, createTarget(NODEINFO_CONTENT, INVALID_NODE_TYPE));
        when(nodeInfo.getNodeType()).thenReturn(INVALID_NODE_TYPE);

        validateNodeTypeSupported.execute(context);

        assertEquals(2, context.getValidationErrors().size());
        final String errorMessage = String.format("Unsupported node type %s in nodeInfo.xml. Valid node types are: %s", INVALID_NODE_TYPE,
                SUPPORTED_NODE_TYPES.toString());
        assertEquals(String.format("Folder1 - %s", errorMessage), context.getValidationErrors().get(0));
        assertEquals(String.format("Folder2 - %s", errorMessage), context.getValidationErrors().get(1));
    }

    private Map<String, Object> createTarget(final String nodeInfoTemplate, final String nodeType) {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(PROJECTINFO_FILENAME, PROJECTINFO_CONTENT);

        final String nodeInfo1 = createNodeInfoWithSubstitutions(nodeInfoTemplate, NODE_NAME1, nodeType);
        zcg.createFileInZip(NODE_FOLDER_1, NODEINFO_FILENAME, nodeInfo1);
        final String nodeInfo2 = createNodeInfoWithSubstitutions(nodeInfoTemplate, NODE_NAME2, nodeType);
        zcg.createFileInZip(NODE_FOLDER_2, NODEINFO_FILENAME, nodeInfo2);

        return zcg.getZipData(ZIPFILE_NAME);
    }

    private String createNodeInfoWithSubstitutions(final String nodeInfoTemplate, final String nodeName, final String nodeType) {
        final String nodeInfoSub = nodeInfoTemplate.replace("NODE_NAME", nodeName);
        return nodeInfoSub.replace("NODE_TYPE", nodeType);
    }
}
