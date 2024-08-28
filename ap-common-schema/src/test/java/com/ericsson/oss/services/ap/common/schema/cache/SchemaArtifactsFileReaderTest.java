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
package com.ericsson.oss.services.ap.common.schema.cache;

import static com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration.getSamplesDirectory;
import static com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration.getSchemasDirectory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.resources.FileResourceImpl;
import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.services.ap.api.resource.ResourceService;

/**
 * Unit tests for {@code SchemaArtifactsFileReader}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SchemaArtifactsFileReaderTest {

    @Mock
    private ResourceService resourceService;

    @Mock
    private Logger logger; // NOPMD

    @InjectMocks
    private SchemaArtifactsFileReader schemaArtifactReader;

    private final Collection<String> nodeTypes = new ArrayList<>();
    private final Collection<String> nodeIdentifiers = new ArrayList<>();
    private final Collection<Resource> artifacts = new ArrayList<>();

    @Before
    public void setUp() {
        when(resourceService.listDirectories(getSchemasDirectory())).thenReturn(nodeTypes);
        when(resourceService.listDirectories(getSamplesDirectory())).thenReturn(nodeTypes);
    }

    @Test
    public void whenReadFilesystemWithNoSchemasTheReturnedMapIsEmpty() {
        doReturn(Collections.<String> emptyList()).when(resourceService).listDirectories(getSchemasDirectory());
        final Map<String, List<SchemaArtifacts>> schemaArtifacts = schemaArtifactReader.readSchemasFromFileSystem();
        assertTrue(schemaArtifacts.isEmpty());
    }

    @Test
    public void whenReadFilesystemSchemasWithNoArtifactsTheArtifactTypeListIsEmpty() {
        nodeTypes.add("erbs");
        nodeIdentifiers.add("d.1.2.3");
        when(resourceService.listDirectories(getSchemasDirectory() + "/erbs")).thenReturn(nodeIdentifiers);
        when(resourceService.listFiles(getSchemasDirectory() + "/erbs/d.1.2.3")).thenReturn(artifacts);

        final Map<String, List<SchemaArtifacts>> schemaArtifacts = schemaArtifactReader.readSchemasFromFileSystem();

        final List<SchemaArtifacts> erbsSchemas = schemaArtifacts.get("ERBS");
        final Collection<String> artifactTypes = erbsSchemas.iterator().next().getArtifactTypes();
        assertTrue(artifactTypes.isEmpty());
    }

    @Test
    public void whenReadFilesystemSchemasWithTwoNodeTypesTheReturnedMapContainsKeyForEachNodeType() {
        nodeTypes.add("erbs");
        nodeTypes.add("perbs");
        nodeIdentifiers.add("d.1.2.3");

        when(resourceService.listDirectories(getSchemasDirectory() + "/erbs")).thenReturn(nodeIdentifiers);
        when(resourceService.listDirectories(getSchemasDirectory() + "/perbs")).thenReturn(nodeIdentifiers);

        final Map<String, List<SchemaArtifacts>> schemaArtifacts = schemaArtifactReader.readSchemasFromFileSystem();
        assertEquals(2, schemaArtifacts.size());
    }

    @Test
    public void whenReadFilesystemSchemasWithSingleNodeTypeAndTwoNodeIdentifiersTheReturnedMapContainsSchemasForBothIdentifiers() {
        nodeTypes.add("erbs");
        nodeIdentifiers.add("d.1.2.3");
        nodeIdentifiers.add("d.2.3.4");

        when(resourceService.listDirectories(getSchemasDirectory() + "/erbs")).thenReturn(nodeIdentifiers);

        final Map<String, List<SchemaArtifacts>> schemaArtifacts = schemaArtifactReader.readSchemasFromFileSystem();
        assertEquals(2, schemaArtifacts.get("ERBS").size());
    }

    @Test
    public void whenReadFilesystemSchemasWithSingleNodeTypeAndMultipleNodeIdentifiersIncludingDefaultThenDefaultIsNotIncluded() {
        nodeTypes.add("erbs");
        nodeIdentifiers.add("d.1.2.3");
        nodeIdentifiers.add("d.2.3.4");
        nodeIdentifiers.add("default");

        when(resourceService.listDirectories(getSchemasDirectory() + "/erbs")).thenReturn(nodeIdentifiers);

        final Map<String, List<SchemaArtifacts>> schemaArtifacts = schemaArtifactReader.readSchemasFromFileSystem();
        assertEquals(2, schemaArtifacts.get("ERBS").size());
    }

    @Test
    public void whenReadFilesystemSchemasWithTwoNodeTypesAndOneNodeIdentifierTheReturnedMapContainsSchemasForBothIdentifiers() {
        nodeTypes.add("erbs");
        nodeTypes.add("msrbs_v1");
        nodeIdentifiers.add("d.1.2.3");
        when(resourceService.listDirectories(getSchemasDirectory() + "/erbs")).thenReturn(nodeIdentifiers);
        when(resourceService.listDirectories(getSchemasDirectory() + "/msrbs_v1")).thenReturn(nodeIdentifiers);

        final Map<String, List<SchemaArtifacts>> schemaArtifacts = schemaArtifactReader.readSchemasFromFileSystem();

        assertEquals(1, schemaArtifacts.get("ERBS").size());
        assertEquals(1, schemaArtifacts.get("MSRBS_V1").size());
    }

    @Test
    public void whenReadFilesystemSchemasWithTwoArtifactsTheArtifactTypeListSizeIsTwo() {
        nodeTypes.add("erbs");
        nodeIdentifiers.add("d.1.2.3");
        artifacts.add(new FileResourceImpl("aritfact1.xml"));
        artifacts.add(new FileResourceImpl("aritfact2.xml"));
        when(resourceService.listDirectories(getSchemasDirectory() + "/erbs")).thenReturn(nodeIdentifiers);
        when(resourceService.listFiles(getSchemasDirectory() + "/erbs/d.1.2.3")).thenReturn(artifacts);

        final Map<String, List<SchemaArtifacts>> schemaArtifacts = schemaArtifactReader.readSchemasFromFileSystem();
        final List<SchemaArtifacts> erbsSchemas = schemaArtifacts.get("ERBS");
        final Collection<String> artifactTypes = erbsSchemas.iterator().next().getArtifactTypes();

        assertEquals(2, artifactTypes.size());
    }

    @Test
    public void whenReadFilesystemSchemasWithMultipleNodeIdentifiersIncludingDefultThenDefaultArtifactsAddedToOtherNodeIdentifierArtifacts() {
        nodeTypes.add("erbs");
        nodeIdentifiers.add("d.1.2.3");
        nodeIdentifiers.add("default");
        artifacts.add(new FileResourceImpl("aritfact1.xml"));
        artifacts.add(new FileResourceImpl("aritfact2.xml"));
        final Collection<Resource> defaultArtifacts = new ArrayList<>();
        defaultArtifacts.add(new FileResourceImpl("defaultAritfact1.xml"));
        when(resourceService.listDirectories(getSchemasDirectory() + "/erbs")).thenReturn(nodeIdentifiers);
        when(resourceService.listFiles(getSchemasDirectory() + "/erbs/d.1.2.3")).thenReturn(artifacts);
        when(resourceService.listFiles(getSchemasDirectory() + "/erbs/default")).thenReturn(defaultArtifacts);

        final Map<String, List<SchemaArtifacts>> schemaArtifacts = schemaArtifactReader.readSchemasFromFileSystem();
        final List<SchemaArtifacts> erbsSchemas = schemaArtifacts.get("ERBS");
        final Collection<String> artifactTypes = erbsSchemas.iterator().next().getArtifactTypes();

        assertEquals(3, artifactTypes.size());
    }

    @Test
    public void whenReadFilesystemSchemasTheNodeTypeAndIdentifierAreSet() {
        nodeTypes.add("erbs");
        nodeIdentifiers.add("d.1.2.3");
        when(resourceService.listDirectories(getSchemasDirectory() + "/erbs")).thenReturn(nodeIdentifiers);

        final Map<String, List<SchemaArtifacts>> schemaArtifacts = schemaArtifactReader.readSchemasFromFileSystem();
        final List<SchemaArtifacts> erbsSchemas = schemaArtifacts.get("ERBS");
        final SchemaArtifacts erbsSchema = erbsSchemas.iterator().next();

        assertEquals("erbs", erbsSchema.getNodeType());
        assertEquals("d.1.2.3", erbsSchema.getNodeIdentifier());
    }

    @Test
    public void whenReadFilesystemSchemasTheArtifactTypeAndLocationAreSet() {
        nodeTypes.add("erbs");
        nodeIdentifiers.add("d.1.2.3");
        artifacts.add(new FileResourceImpl("artifact1.xml"));
        when(resourceService.listDirectories(getSchemasDirectory() + "/erbs")).thenReturn(nodeIdentifiers);
        when(resourceService.listFiles(getSchemasDirectory() + "/erbs/d.1.2.3")).thenReturn(artifacts);

        final Map<String, List<SchemaArtifacts>> schemaArtifacts = schemaArtifactReader.readSchemasFromFileSystem();
        final List<SchemaArtifacts> erbsSchemas = schemaArtifacts.get("ERBS");
        final SchemaArtifacts erbsSchema = erbsSchemas.iterator().next();

        final Collection<String> artifactTypes = erbsSchema.getArtifactTypes();

        assertTrue(artifactTypes.contains("artifact1"));
        assertEquals(getSchemasDirectory() + "/erbs/d.1.2.3/artifact1.xml", erbsSchema.getArtifactLocation("artifact1"));
    }

    @Test
    public void whenReadFilesystemWithNoSamplesTheReturnedMapIsEmpty() {
        doReturn(Collections.<String> emptyList()).when(resourceService).listDirectories(getSamplesDirectory());
        final Map<String, List<SchemaArtifacts>> schemaArtifacts = schemaArtifactReader.readSamplesFromFileSystem();
        assertTrue(schemaArtifacts.isEmpty());
    }

    @Test
    public void whenReadFilesystemSamplesWithOneNodeTypeAndTwoIdentifiersTheReturnedMapContainsSchemasForBothIdentifiers() {
        nodeTypes.add("erbs");
        nodeIdentifiers.add("d.1.2.3");
        nodeIdentifiers.add("d.2.3.4");
        when(resourceService.listDirectories(getSamplesDirectory() + "/erbs")).thenReturn(nodeIdentifiers);

        final Map<String, List<SchemaArtifacts>> samplesArtifacts = schemaArtifactReader.readSamplesFromFileSystem();

        assertEquals(2, samplesArtifacts.get("ERBS").size());
    }

    @Test
    public void whenReadFilesystemSamplesWithTwoNodeTypesTheReturnedMapContainsKeysForEachType() {
        nodeTypes.add("erbs");
        nodeTypes.add("msrbs_v1");
        nodeIdentifiers.add("d.1.2.3");
        when(resourceService.listDirectories(getSamplesDirectory() + "/erbs")).thenReturn(nodeIdentifiers);
        when(resourceService.listDirectories(getSamplesDirectory() + "/msrbs_v1")).thenReturn(nodeIdentifiers);

        final Map<String, List<SchemaArtifacts>> samplesArtifacts = schemaArtifactReader.readSamplesFromFileSystem();

        assertEquals(2, samplesArtifacts.size());
    }

    @Test
    public void whenReadFilesystemSamplesWithTwoNodeTypesAndOneIdentifierTheReturnedMapContainsSchemasForBothIdentifiers() {
        nodeTypes.add("erbs");
        nodeTypes.add("msrbs_v1");
        nodeIdentifiers.add("d.1.2.3");

        when(resourceService.listDirectories(getSamplesDirectory() + "/erbs")).thenReturn(nodeIdentifiers);
        when(resourceService.listDirectories(getSamplesDirectory() + "/msrbs_v1")).thenReturn(nodeIdentifiers);

        final Map<String, List<SchemaArtifacts>> samplesArtifacts = schemaArtifactReader.readSamplesFromFileSystem();

        assertEquals(1, samplesArtifacts.get("ERBS").size());
        assertEquals(1, samplesArtifacts.get("MSRBS_V1").size());
    }

    @Test
    public void wheReadFilesystemSamplesWithTwoArtifactsTheArtifactTypeListSizeIsTwo() {
        nodeTypes.add("erbs");
        nodeIdentifiers.add("d.1.2.3");
        artifacts.add(new FileResourceImpl("aritfact1.xml"));
        artifacts.add(new FileResourceImpl("aritfact2.xml"));
        when(resourceService.listDirectories(getSamplesDirectory() + "/erbs")).thenReturn(nodeIdentifiers);
        when(resourceService.listFiles(getSamplesDirectory() + "/erbs/d.1.2.3")).thenReturn(artifacts);

        final Map<String, List<SchemaArtifacts>> samplesArtifacts = schemaArtifactReader.readSamplesFromFileSystem();

        final List<SchemaArtifacts> erbsSamples = samplesArtifacts.get("ERBS");
        final SchemaArtifacts erbsSample = erbsSamples.iterator().next();
        final Collection<String> artifactTypes = erbsSample.getArtifactTypes();
        assertEquals(2, artifactTypes.size());
    }

    @Test
    public void whenReadFilesystemSamplesTheNodeTypeAndIdentifierAreSet() {
        nodeTypes.add("erbs");
        nodeIdentifiers.add("d.1.2.3");
        when(resourceService.listDirectories(getSamplesDirectory() + "/erbs")).thenReturn(nodeIdentifiers);

        final Map<String, List<SchemaArtifacts>> samplesArtifacts = schemaArtifactReader.readSamplesFromFileSystem();

        final List<SchemaArtifacts> erbsSamples = samplesArtifacts.get("ERBS");
        final SchemaArtifacts erbsSample = erbsSamples.iterator().next();
        assertEquals("erbs", erbsSample.getNodeType());
        assertEquals("d.1.2.3", erbsSample.getNodeIdentifier());
    }

    @Test
    public void whenReadFilesystemSamplesTheArtifactTypeAndLocationAreSet() {
        nodeTypes.add("erbs");
        nodeIdentifiers.add("d.1.2.3");
        artifacts.add(new FileResourceImpl("artifact1.xml"));
        when(resourceService.listDirectories(getSamplesDirectory() + "/erbs")).thenReturn(nodeIdentifiers);
        when(resourceService.listFiles(getSamplesDirectory() + "/erbs/d.1.2.3")).thenReturn(artifacts);

        final Map<String, List<SchemaArtifacts>> samplesArtifacts = schemaArtifactReader.readSamplesFromFileSystem();

        final List<SchemaArtifacts> erbsSamples = samplesArtifacts.get("ERBS");
        final SchemaArtifacts erbsSample = erbsSamples.iterator().next();
        final Collection<String> artifactTypes = erbsSample.getArtifactTypes();
        assertTrue(artifactTypes.contains("artifact1"));
        assertEquals(getSamplesDirectory() + "/erbs/d.1.2.3/artifact1.xml", erbsSample.getArtifactLocation("artifact1"));
    }
}
