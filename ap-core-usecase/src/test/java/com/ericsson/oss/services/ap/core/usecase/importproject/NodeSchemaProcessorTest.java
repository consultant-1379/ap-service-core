/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.importproject;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_IDENTIFIER_VALUE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;

/**
 * Unit tests for {@link NodeSchemaProcessor}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeSchemaProcessorTest {

    @InjectMocks
    private NodeSchemaProcessor nodeSchemaProcessor;

    @Mock
    private Archive archiveReader;

    private static final String DIRECTORY_1 = "DirectoryOne";
    private static final String NODE_INFO_FILE = "nodeInfo.xml";
    private static final String IP_ADDRESS = "localhost";

    @Before
    public void setup() {
        final List<String> directoryNames = Arrays.asList(DIRECTORY_1);
        when(archiveReader.getAllDirectoryNames()).thenReturn(directoryNames);
    }

    @Test
    public void testReturnTrueWhenReconfigurationNodeInfoMatches() {
        final ArchiveArtifact archiveArtifact = new ArchiveArtifact(DIRECTORY_1, getNodeInfoXml("ReconfigureNodeInfo.xsd"));
        when(archiveReader.getArtifactOfNameInDir(DIRECTORY_1, NODE_INFO_FILE)).thenReturn(archiveArtifact);
        final boolean result = nodeSchemaProcessor.isNodeReconfiguration(archiveReader);
        assertTrue(result);
    }

    @Test
    public void testReturnFalseWhenReconfigurationNodeInfoDoesnotMatches() {
        final ArchiveArtifact archiveArtifact = new ArchiveArtifact(DIRECTORY_1, getNodeInfoXml("DummyReconfigureNodeInfo.xsd"));
        when(archiveReader.getArtifactOfNameInDir(DIRECTORY_1, NODE_INFO_FILE)).thenReturn(archiveArtifact);
        final boolean result = nodeSchemaProcessor.isNodeReconfiguration(archiveReader);
        assertFalse(result);
    }

    @Test
    public void testReturnFalseWhenReconfigurationNodeInfoIsNull() {
        final ArchiveArtifact archiveArtifact = new ArchiveArtifact(DIRECTORY_1, getNodeInfoXml(null));
        when(archiveReader.getArtifactOfNameInDir(DIRECTORY_1, NODE_INFO_FILE)).thenReturn(archiveArtifact);
        final boolean result = nodeSchemaProcessor.isNodeReconfiguration(archiveReader);
        assertFalse(result);
    }

    private static String getNodeInfoXml(final String xsd) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=" + "\"" + xsd + "\"" + ">"
                + "<name>nodeName</name>" + "<nodeIdentifier>" + NODE_IDENTIFIER_VALUE + "</nodeIdentifier>" + "<ipAddress>" + IP_ADDRESS
                + "</ipAddress>" + "<nodeType>" + VALID_NODE_TYPE + "</nodeType>" + "<artifacts>" + "<siteBasic>SiteBasic.xml</siteBasic>"
                + "<siteInstallation>SiteInstallation.xml</siteInstallation>" + "<siteEquipment>SiteEquipment.xml</siteEquipment>" + "</artifacts>"
                + "</nodeInfo>";
    }
}