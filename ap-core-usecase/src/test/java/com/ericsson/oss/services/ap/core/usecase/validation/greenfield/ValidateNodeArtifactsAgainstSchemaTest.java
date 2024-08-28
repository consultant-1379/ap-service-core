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
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.schema.SchemaData;
import com.ericsson.oss.services.ap.api.schema.SchemaService;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.common.util.xml.XmlValidator;
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaValidationException;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader;
/**
 * Unit tests for {@link ValidateNodeArtifactsAgainstSchema}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateNodeArtifactsAgainstSchemaTest {

    private static final String SCHEMA_FILE_NAME = "schema.xsd";
    private static final String SITE_BASIC_FILE_CONTENT = "sitebasic";
    private static final String RADIO_FILE_CONTENT = "radio";
    private static final String SCHEMA_FILE_CONTENT = "Schema Content";
    private static final String SITE_BASIC_TYPE = "siteBasic";
    private static final String RADIO_FILE_TYPE = "configuration";
    private static final String SITE_BASIC_FILE_NAME = "siteBasic.xml";
    private static final String RADIO_FILE_NAME = "radio.xml";
    private static final String SCHEMA_LOCATION = "/schema_location";
    private static final String ERBSNODE1 = "erbsNode1";

    private final List<String> directoryNames = new ArrayList<>();

    private static final String[] SINGLE_ERBS_NODE_DIRECTORY = { ERBSNODE1 };
    private static final String[] TWO_ERBS_NODE_DIRECTORIES = { ERBSNODE1, "erbsNode2" };

    @Mock
    private NodeInfoReader nodeInfoReader;

    @Mock
    private Archive archiveReader;

    @Mock
    private SchemaService schemaService;

    @Mock
    private XmlValidator xmlValidator;

    @InjectMocks
    @Spy
    private ValidateNodeArtifactsAgainstSchema uut; //Unit Under Test

    private ValidationContext validationContext;
    private Map<String, Object> contextTarget;

    private ArchiveArtifact siteBasic;
    private ArchiveArtifact radio;
    private NodeInfo nodeInfo;
    private SchemaData schemaData;

    @Before
    public void setUp() {
        directoryNames.add(ERBSNODE1);

        contextTarget = new HashMap<>();
        contextTarget.put("fileContent", archiveReader);
        contextTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryNames);

        validationContext = new ValidationContext("", contextTarget);

        final List<String> siteBasicArtifact = new ArrayList<>();
        siteBasicArtifact.add(SITE_BASIC_FILE_NAME);

        final List<String> configArtifacts = new ArrayList<>();
        configArtifacts.add(RADIO_FILE_NAME);

        final Map<String, List<String>> nodeArtifactsByType = new HashMap<>();
        nodeArtifactsByType.put(SITE_BASIC_TYPE, siteBasicArtifact);
        nodeArtifactsByType.put(RADIO_FILE_TYPE, configArtifacts);

        siteBasic = new ArchiveArtifact(SITE_BASIC_FILE_NAME, SITE_BASIC_FILE_CONTENT);
        radio = new ArchiveArtifact(RADIO_FILE_NAME, RADIO_FILE_CONTENT);
        nodeInfo = new NodeInfo();
        nodeInfo.setName(NODE_NAME);
        nodeInfo.setNodeType(VALID_NODE_TYPE);
        nodeInfo.setNodeIdentifier(NODE_IDENTIFIER_VALUE);
        nodeInfo.setNodeArtifacts(nodeArtifactsByType);
        schemaData = new SchemaData(SCHEMA_FILE_NAME, SITE_BASIC_TYPE, NODE_IDENTIFIER_VALUE, SCHEMA_FILE_CONTENT.getBytes(), SCHEMA_LOCATION);
    }

    @Test
    public void whenProjectfileIsEmptyUutShouldValidateContextSuccessfully() {
        contextTarget = new HashMap<>();
        contextTarget.put("fileContent", archiveReader);
        contextTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), Collections.<String> emptyList());
        validationContext = new ValidationContext("", contextTarget);

        final boolean validationResult = uut.execute(validationContext);
        assertTrue("Empty project file not validated", validationResult);
    }

    @Test
    public void whenErbsSingleNodeProjectfileContainsValidArtifactsUutShouldValidateContextSuccessfully() {
        when(nodeInfoReader.read(archiveReader, SINGLE_ERBS_NODE_DIRECTORY[0])).thenReturn(nodeInfo);
        when(archiveReader.getAllDirectoryNames()).thenReturn(Arrays.asList(SINGLE_ERBS_NODE_DIRECTORY));
        when(archiveReader.getArtifactOfNameInDir(SINGLE_ERBS_NODE_DIRECTORY[0], siteBasic.getName())).thenReturn(siteBasic);
        when(archiveReader.getArtifactOfNameInDir(SINGLE_ERBS_NODE_DIRECTORY[0], radio.getName())).thenReturn(radio);
        when(schemaService.readSchema(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, SITE_BASIC_TYPE)).thenReturn(schemaData);
        when(schemaService.readSchema(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, RADIO_FILE_TYPE)).thenReturn(null);

        final boolean validationResult = uut.execute(validationContext);

        assertTrue(validationResult);
    }

    @Test
    public void whenErbsSingleNodeProjectfileContainsValidAndInvalidNodeArtifactsUutShouldNotValidateContextSuccessfully() {
        when(nodeInfoReader.read(archiveReader, SINGLE_ERBS_NODE_DIRECTORY[0])).thenReturn(nodeInfo);
        when(archiveReader.getAllDirectoryNames()).thenReturn(Arrays.asList(SINGLE_ERBS_NODE_DIRECTORY));
        when(archiveReader.getArtifactOfNameInDir(SINGLE_ERBS_NODE_DIRECTORY[0], siteBasic.getName())).thenReturn(siteBasic);
        when(archiveReader.getArtifactOfNameInDir(SINGLE_ERBS_NODE_DIRECTORY[0], radio.getName())).thenReturn(radio);
        when(schemaService.readSchema(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, SITE_BASIC_TYPE)).thenReturn(schemaData);
        when(schemaService.readSchema(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, RADIO_FILE_TYPE)).thenReturn(null);
        doThrow(SchemaValidationException.class).when(xmlValidator).validateAgainstSchema(SITE_BASIC_FILE_CONTENT, SCHEMA_FILE_CONTENT.getBytes());

        final boolean validationResult = uut.execute(validationContext);

        assertFalse(validationResult);
    }

    @Test
    public void whenMultipleErbsNodeProjectfileContainsValidArtifactsUutShouldValidateContextSuccessfully() {
        when(nodeInfoReader.read(archiveReader, TWO_ERBS_NODE_DIRECTORIES[0])).thenReturn(nodeInfo);
        when(nodeInfoReader.read(archiveReader, TWO_ERBS_NODE_DIRECTORIES[1])).thenReturn(nodeInfo);

        when(archiveReader.getAllDirectoryNames()).thenReturn(Arrays.asList(TWO_ERBS_NODE_DIRECTORIES));
        when(archiveReader.getArtifactOfNameInDir(TWO_ERBS_NODE_DIRECTORIES[0], siteBasic.getName())).thenReturn(siteBasic);
        when(archiveReader.getArtifactOfNameInDir(TWO_ERBS_NODE_DIRECTORIES[0], radio.getName())).thenReturn(radio);
        when(archiveReader.getArtifactOfNameInDir(TWO_ERBS_NODE_DIRECTORIES[1], siteBasic.getName())).thenReturn(siteBasic);
        when(archiveReader.getArtifactOfNameInDir(TWO_ERBS_NODE_DIRECTORIES[1], radio.getName())).thenReturn(radio);

        when(schemaService.readSchema(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, SITE_BASIC_TYPE)).thenReturn(schemaData);
        when(schemaService.readSchema(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, RADIO_FILE_TYPE)).thenReturn(null);

        final boolean validationResult = uut.execute(validationContext);

        assertTrue(validationResult);
    }

    @Test
    public void whenMultipleErbsNodeProjectfileContainsValidAndInvalidArtifactsUutShouldNotValidateContextSuccessfully() {
        when(nodeInfoReader.read(archiveReader, SINGLE_ERBS_NODE_DIRECTORY[0])).thenReturn(nodeInfo);
        when(nodeInfoReader.read(archiveReader, TWO_ERBS_NODE_DIRECTORIES[0])).thenReturn(nodeInfo);
        when(nodeInfoReader.read(archiveReader, TWO_ERBS_NODE_DIRECTORIES[1])).thenReturn(nodeInfo);

        when(archiveReader.getAllDirectoryNames()).thenReturn(Arrays.asList(TWO_ERBS_NODE_DIRECTORIES));
        when(archiveReader.getArtifactOfNameInDir(SINGLE_ERBS_NODE_DIRECTORY[0], siteBasic.getName())).thenReturn(siteBasic);
        when(archiveReader.getArtifactOfNameInDir(SINGLE_ERBS_NODE_DIRECTORY[0], radio.getName())).thenReturn(radio);
        when(archiveReader.getArtifactOfNameInDir(TWO_ERBS_NODE_DIRECTORIES[1], siteBasic.getName())).thenReturn(siteBasic);
        when(archiveReader.getArtifactOfNameInDir(TWO_ERBS_NODE_DIRECTORIES[1], radio.getName())).thenReturn(radio);

        when(schemaService.readSchema(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, SITE_BASIC_TYPE)).thenReturn(schemaData);
        when(schemaService.readSchema(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, RADIO_FILE_TYPE)).thenReturn(null);
        doThrow(SchemaValidationException.class).when(xmlValidator).validateAgainstSchema(SITE_BASIC_FILE_CONTENT, SCHEMA_FILE_CONTENT.getBytes());

        final boolean validationResult = uut.execute(validationContext);

        assertFalse(validationResult);
    }

    @Test
    public void whenNoErbsArtifactFoundForFilenameUutShouldValidateContextSuccessfully() {
        when(nodeInfoReader.read(archiveReader, SINGLE_ERBS_NODE_DIRECTORY[0])).thenReturn(nodeInfo);
        when(archiveReader.getAllDirectoryNames()).thenReturn(Arrays.asList(SINGLE_ERBS_NODE_DIRECTORY));
        when(archiveReader.getArtifactOfNameInDir(SINGLE_ERBS_NODE_DIRECTORY[0], siteBasic.getName())).thenReturn(null);
        when(archiveReader.getArtifactOfNameInDir(SINGLE_ERBS_NODE_DIRECTORY[0], radio.getName())).thenReturn(null);

        final boolean validationResult = uut.execute(validationContext);

        assertTrue(validationResult);
    }
}