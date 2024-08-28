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
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.common.artifacts.UpgradePackageProductDetails;
import com.ericsson.oss.services.ap.common.artifacts.util.ShmDetailsRetriever;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader;
import com.ericsson.oss.services.ap.core.usecase.validation.common.ZipContentGenerator;

/**
 * Unit tests for {@link ValidateNodeIdentifier}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateNodeIdentifierTest {

    private static final String ZIPFILE_NAME = "test.zip";

    private static final String PROJECTINFO_FILENAME = "projectInfo.xml";
    private static final String PROJECTINFO_CONTENT = "testcontent";

    private static final String NODEINFO_FILENAME = "nodeInfo.xml";
    private static final String NODE_FOLDER_1 = "Folder1";
    private static final String NODE_FOLDER_2 = "Folder2";
    private static final String NODE_FOLDER_3 = "Folder3";
    private static final String NODE_NAME1 = "NodeName1";
    private static final String NODE_NAME2 = "NodeName2";
    private static final String NODE_NAME3 = "NodeName3";

    private static final String PROJECT_FILE_VALIDATION_GROUP = "validate_project_content";
    private static final String UPGRADE_PACKAGE_NAME = "testUpgradePackage";
    private static final String OSS_MODEL_IDENTITY = "testModelIdentity";

    private static final String NODEINFO_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"NodeInfo.xsd\">"
            + "<name>NODE_NAME</name>"
            + "<nodeIdentifier>NODE_VERSION</nodeIdentifier>"
            + "<ipAddress>192.168.5.18</ipAddress>"
            + "<nodeType>ERBS</nodeType>"
            + "<artifacts>"
            + "<siteBasic>SiteBasic.xml</siteBasic>"
            + "<siteInstallation>SiteInstallation.xml</siteInstallation>"
            + "<siteEquipment>SiteEquipment.xml</siteEquipment>"
            + "<transport>TN_Data.xml</transport>"
            + "</artifacts>"
            + "</nodeInfo>";

    private static final String NODEINFO_CONTENT_WITHOUT_NODEIDENTIFIER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"NodeInfo.xsd\">"
            + "<name>NODE_NAME</name>"
            + "<ipAddress>192.168.5.18</ipAddress>"
            + "<nodeType>ERBS</nodeType>"
            + "<artifacts>"
            + "<siteBasic>SiteBasic.xml</siteBasic>"
            + "<siteInstallation>SiteInstallation.xml</siteInstallation>"
            + "<siteEquipment>SiteEquipment.xml</siteEquipment>"
            + "<transport>TN_Data.xml</transport>"
            + "</artifacts>"
            + "</nodeInfo>";
    private static final String UPGRADE_PACKAGE_NAME_ATTRIBUTE = "upgradePackageName";

    @Mock
    private ModelReader modelReader;

    @InjectMocks
    private ValidateNodeIdentifier validateNodeIdentifier;

    @Mock
    private NodeInfoReader nodeInfoReader; // NOPMD

    @Mock
    private NodeInfo nodeInfo;

    @Mock
    private ShmDetailsRetriever shmDetailsRetriever;

    @Mock
    private UpgradePackageProductDetails upgradePackageProductDetails;


    private ValidationContext context;
    private Map<String, Object> target;

    @Before
    public void setUp() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(PROJECTINFO_FILENAME, PROJECTINFO_CONTENT);

        final String nodeInfo1 = createNodeInfoWithSubstitutions(NODE_NAME1, NODE_IDENTIFIER_VALUE);
        zcg.createFileInZip(NODE_FOLDER_1, NODEINFO_FILENAME, nodeInfo1);

        final String nodeInfo2 = createNodeInfoWithSubstitutions(NODE_NAME2, NODE_IDENTIFIER_VALUE);
        zcg.createFileInZip(NODE_FOLDER_2, NODEINFO_FILENAME, nodeInfo2);

        final String nodeInfo3 = createNodeInfoWithSubstitutions(NODE_NAME3, NODE_IDENTIFIER_VALUE);
        zcg.createFileInZip(NODE_FOLDER_3, NODEINFO_FILENAME, nodeInfo3);

        target = zcg.getZipData(ZIPFILE_NAME);

        when(upgradePackageProductDetails.getProductNumber()).thenReturn("testProductNumber");
        when(upgradePackageProductDetails.getProductRevision()).thenReturn("testRevisionNumber");

    }

    private String createNodeInfoWithSubstitutions(final String nodeName, final String nodeIdentifier) {
        return createNodeInfoWithSubstitutions(NODEINFO_CONTENT, nodeName, nodeIdentifier);
    }

    private String createNodeInfoWithSubstitutions(final String nodeInfoTemplate, final String nodeName, final String nodeIdentifier) {
        return nodeInfoTemplate.replace("NODE_NAME", nodeName).replace("NODE_VERSION", nodeIdentifier);
    }

    @Test
    public void testRuleReturnsTrueWhenNodeIdentifiersAreSupportedForAllNodesInProject() {
        when(modelReader.checkOssModelIdentityExists(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE)).thenReturn(true);
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, target);

        final boolean result = validateNodeIdentifier.execute(context);

        assertTrue(result);
    }

    @Test
    public void testRuleReturnsFalseWhenNodeIdentifiersAreUnSupportedForAllNodesInProject() {
        when(modelReader.checkOssModelIdentityExists(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE)).thenReturn(false);
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, target);

        final boolean result = validateNodeIdentifier.execute(context);

        assertFalse(result);
    }

    @Test
    public void testRuleReturnsValidationErrorsWhenNodeIdentifiersAreUnSupportedForAllNodesInProject() {
        when(modelReader.checkOssModelIdentityExists(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE)).thenReturn(false);
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, target);

        validateNodeIdentifier.execute(context);

        assertEquals(3, context.getValidationErrors().size());
        final String errorMessage = String.format("Unsupported node identifier %s in nodeInfo.xml", NODE_IDENTIFIER_VALUE);
        assertEquals(String.format("%s - %s", NODE_FOLDER_1, errorMessage), context.getValidationErrors().get(0));
        assertEquals(String.format("%s - %s", NODE_FOLDER_2, errorMessage), context.getValidationErrors().get(1));
        assertEquals(String.format("%s - %s", NODE_FOLDER_3, errorMessage), context.getValidationErrors().get(2));
    }

    @Test
    public void testRuleReturnsFalseWhenNodeIdentifierIsUnSupportedForFirstNodesInProjectAndSupportedForLastNode() {
        when(modelReader.checkOssModelIdentityExists(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE)).thenReturn(false).thenReturn(false).thenReturn(true);
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, target);

        final boolean result = validateNodeIdentifier.execute(context);

        assertFalse(result);
    }

    @Test
    public void testRuleReturnsTwoValidationErrorWhenNodeIdentifierIsUnSupportedForFirstNodesInProjectAndSupportedForLastNode() {
        when(modelReader.checkOssModelIdentityExists(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE)).thenReturn(false).thenReturn(false).thenReturn(true);
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, target);

        validateNodeIdentifier.execute(context);

        final String errorMessage = String.format("Unsupported node identifier %s in nodeInfo.xml", NODE_IDENTIFIER_VALUE);
        assertEquals(String.format("%s - %s", NODE_FOLDER_1, errorMessage), context.getValidationErrors().get(0));
    }

    @Test
    public void testWhenNodeIdentifierIsNotPresentInNodeInfoAndMatchFoundReturnsTrue() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(PROJECTINFO_FILENAME, PROJECTINFO_CONTENT);
        final String nodeInfo1 = createNodeInfoWithSubstitutions(NODEINFO_CONTENT_WITHOUT_NODEIDENTIFIER, NODE_NAME1, NODE_IDENTIFIER_VALUE);
        zcg.createFileInZip(NODE_FOLDER_1, NODEINFO_FILENAME, nodeInfo1);
        target = zcg.getZipData(ZIPFILE_NAME);
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, target);

        when(validateNodeIdentifier.getNodeInfo(context, NODE_FOLDER_1)).thenReturn(nodeInfo);
        final Map<String, Object> autoIntegrationAttributes = new HashMap<>();
        autoIntegrationAttributes.put(UPGRADE_PACKAGE_NAME_ATTRIBUTE, UPGRADE_PACKAGE_NAME);

        when(nodeInfo.getIntegrationAttributes()).thenReturn(autoIntegrationAttributes);
        when(nodeInfo.getNodeType()).thenReturn("ERBS");
        when(shmDetailsRetriever.getUpgradePackageProductDetails(UPGRADE_PACKAGE_NAME, nodeInfo.getNodeType())).thenReturn(upgradePackageProductDetails);
        when(modelReader.getOssModelIdentity(nodeInfo.getNodeType(), upgradePackageProductDetails.getProductNumber(), upgradePackageProductDetails.getProductRevision())).thenReturn(OSS_MODEL_IDENTITY);
        when(modelReader.checkOssModelIdentityExists(nodeInfo.getNodeType(), OSS_MODEL_IDENTITY)).thenReturn(Boolean.TRUE);

        final boolean result = validateNodeIdentifier.execute(context);
        assertTrue(result);
    }

    @Test
    public void testWhenNodeIdentifierIsNotPresentInNodeInfoAndNoMatchFoundReturnsFalse() {
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip(PROJECTINFO_FILENAME, PROJECTINFO_CONTENT);
        final String nodeInfo1 = createNodeInfoWithSubstitutions(NODEINFO_CONTENT_WITHOUT_NODEIDENTIFIER, NODE_NAME1, NODE_IDENTIFIER_VALUE);
        zcg.createFileInZip(NODE_FOLDER_1, NODEINFO_FILENAME, nodeInfo1);
        target = zcg.getZipData(ZIPFILE_NAME);
        context = new ValidationContext(PROJECT_FILE_VALIDATION_GROUP, target);

        when(validateNodeIdentifier.getNodeInfo(context, NODE_FOLDER_1)).thenReturn(nodeInfo);
        final Map<String, Object> autoIntegrationAttributes = new HashMap<>();
        autoIntegrationAttributes.put(UPGRADE_PACKAGE_NAME_ATTRIBUTE, UPGRADE_PACKAGE_NAME);

        when(nodeInfo.getIntegrationAttributes()).thenReturn(autoIntegrationAttributes);
        when(nodeInfo.getNodeType()).thenReturn("ERBS");
        when(shmDetailsRetriever.getUpgradePackageProductDetails(UPGRADE_PACKAGE_NAME, nodeInfo.getNodeType())).thenReturn(upgradePackageProductDetails);
        when(modelReader.getOssModelIdentity(nodeInfo.getNodeType(), upgradePackageProductDetails.getProductNumber(), upgradePackageProductDetails.getProductRevision())).thenReturn(OSS_MODEL_IDENTITY);
        when(modelReader.checkOssModelIdentityExists(nodeInfo.getNodeType(), "testModelIdentity1")).thenReturn(Boolean.FALSE);

        final boolean result = validateNodeIdentifier.execute(context);
        assertFalse(result);
    }
}
