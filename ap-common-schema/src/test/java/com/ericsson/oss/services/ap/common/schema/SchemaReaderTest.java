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
package com.ericsson.oss.services.ap.common.schema;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_IDENTIFIER_VALUE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.exception.ArtifactDataNotFoundException;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.api.schema.SchemaData;
import com.ericsson.oss.services.ap.common.schema.cache.SchemaArtifacts;
import com.ericsson.oss.services.ap.common.schema.cache.SchemaCache;

/**
 * Units tests for {@link SampleReader}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SchemaReaderTest {

    private static final String INVALID_NODE_TYPE = "invalidNodeType";
    private static final String MIM_VERSION_D_1_44 = "d.1.44";
    private static final String MIM_VERSION_E_1_120 = "e.1.120";

    private static final String PROJECT_INFO = "ProjectInfo";
    private static final String ARTIFACT1 = "artifact1";
    private static final String ARTIFACT2 = "artifact2";
    private static final String ARTIFACT3 = "artifact3";

    private static final byte[] SCHEMA_CONTENTS = "contents".getBytes();

    private static final SchemaArtifacts SCHEMAS_1 = new SchemaArtifacts(VALID_NODE_TYPE, MIM_VERSION_D_1_44);
    private static final SchemaArtifacts SCHEMAS_2 = new SchemaArtifacts(VALID_NODE_TYPE, MIM_VERSION_E_1_120);

    private final List<SchemaArtifacts> schemas = new ArrayList<>();

    @Mock
    private ResourceService resourceService;

    @Mock
    private SchemaCache schemaCache;

    @Mock
    private SchemaIdentifierResolver schemaIdentifierResovler;

    @InjectMocks
    private SchemaReader schemaReader;

    @Before
    public void setup() {
        SCHEMAS_1.addArtifact(ARTIFACT1, "/tmp/artifact1.xml");
        SCHEMAS_1.addArtifact(ARTIFACT2, "/tmp/artifact2.xml");
        SCHEMAS_1.addArtifact(PROJECT_INFO, "/tmp/ProjectInfo.xml");
        SCHEMAS_2.addArtifact(ARTIFACT1, "/tmp/artifact1.xml");
        SCHEMAS_2.addArtifact(ARTIFACT2, "/tmp/artifact2.xml");

        schemas.add(SCHEMAS_1);
        schemas.add(SCHEMAS_2);

        when(resourceService.getBytes(anyString())).thenReturn(SCHEMA_CONTENTS);
    }

    @Test
    public void whenReadSchemasForNodeTypeThenAllSchemasForThatTypeAreReturned() {
        when(schemaCache.getSchemasForNodeType(VALID_NODE_TYPE)).thenReturn(schemas);
        final int expectedNumSchemas = SCHEMAS_1.getArtifactTypes().size() + SCHEMAS_2.getArtifactTypes().size();
        assertEquals(expectedNumSchemas, schemaReader.read(VALID_NODE_TYPE).size());
    }

    @Test
    public void whenReadSchemasForNodeIdentifierThenAllSchemasForThatIdentifierAreReturned() {
        when(schemaCache.getSchemasForNodeType(VALID_NODE_TYPE)).thenReturn(schemas);
        when(schemaIdentifierResovler.findCompatibleSchemasForNodeIdentifier(NODE_IDENTIFIER_VALUE, schemas, VALID_NODE_TYPE)).thenReturn(SCHEMAS_1);

        final int expectedNumSchemas = SCHEMAS_1.getArtifactTypes().size();
        assertEquals(expectedNumSchemas, schemaReader.read(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE).size());
    }

    @Test
    public void whenReadSchemaForExistingArtifactThenSchemaIsReturned() {
        when(schemaCache.getSchemasForNodeType(VALID_NODE_TYPE)).thenReturn(schemas);
        when(schemaIdentifierResovler.findCompatibleSchemasForNodeIdentifier(NODE_IDENTIFIER_VALUE, schemas, VALID_NODE_TYPE)).thenReturn(SCHEMAS_1);

        assertNotNull(schemaReader.read(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, ARTIFACT1));
    }

    @Test(expected = ArtifactDataNotFoundException.class)
    public void whenReadSchemaForNotSupportedNodeTypeThenExceptionIsThrown() {
        doReturn(Collections.<SchemaArtifacts> emptyList()).when(schemaCache).getSchemasForNodeType(INVALID_NODE_TYPE);
        schemaReader.read(INVALID_NODE_TYPE);
    }

    @Test(expected = ArtifactDataNotFoundException.class)
    public void whenReadSchemasForNodeIdentifierAndNotNodeTypeThenExceptionIsThrown() {
        doReturn(Collections.<SchemaArtifacts> emptyList()).when(schemaCache).getSchemasForNodeType(INVALID_NODE_TYPE);
        schemaReader.read(INVALID_NODE_TYPE, MIM_VERSION_D_1_44);
    }

    @Test(expected = ArtifactDataNotFoundException.class)
    public void whenReadSchemaForNonExistingArtifactThenExceptionIsThrown() {
        when(schemaCache.getSchemasForNodeType(VALID_NODE_TYPE)).thenReturn(schemas);
        when(schemaIdentifierResovler.findCompatibleSchemasForNodeIdentifier(NODE_IDENTIFIER_VALUE, schemas, VALID_NODE_TYPE)).thenReturn(SCHEMAS_1);
        schemaReader.read(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, ARTIFACT3);
    }

    @Test
    public void whenReadSchemasForNodeTypeTheReturnedDataContainsTheFileContentsNameAndIdentifier() {
        when(schemaCache.getSchemasForNodeType(VALID_NODE_TYPE)).thenReturn(schemas);

        final List<SchemaData> schemaData = schemaReader.read(VALID_NODE_TYPE);

        assertEquals(ARTIFACT1, schemaData.get(0).getName());
        assertEquals(MIM_VERSION_D_1_44, schemaData.get(0).getIdentifier());
        assertArrayEquals(SCHEMA_CONTENTS, schemaData.get(0).getData());
    }

    @Test
    public void whenReadSchemasForNodeIdentifierTheReturnedDataContainsTheFileContentsNameAndIdentifier() {
        when(schemaCache.getSchemasForNodeType(VALID_NODE_TYPE)).thenReturn(schemas);
        when(schemaIdentifierResovler.findCompatibleSchemasForNodeIdentifier(NODE_IDENTIFIER_VALUE, schemas, VALID_NODE_TYPE)).thenReturn(SCHEMAS_1);

        final List<SchemaData> schemaData = schemaReader.read(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE);

        assertEquals(ARTIFACT1, schemaData.get(0).getName());
        assertEquals(MIM_VERSION_D_1_44, schemaData.get(0).getIdentifier());
        assertArrayEquals(SCHEMA_CONTENTS, schemaData.get(0).getData());
    }

    @Test
    public void whenReadForArtifactTypeTheReturnedDataContainsTheFileContentsNameAndIdentifier() {
        when(schemaIdentifierResovler.findCompatibleSchemasForNodeIdentifier(NODE_IDENTIFIER_VALUE, schemas, VALID_NODE_TYPE)).thenReturn(SCHEMAS_1);
        when(schemaCache.getSchemasForNodeType(VALID_NODE_TYPE)).thenReturn(schemas);

        final SchemaData schemaData = schemaReader.read(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, ARTIFACT1);

        assertEquals(ARTIFACT1, schemaData.getName());
        assertEquals(MIM_VERSION_D_1_44, schemaData.getIdentifier());
        assertArrayEquals(SCHEMA_CONTENTS, schemaData.getData());
    }

    @Test
    public void whenReadProjectInfoSchemaTheReturnedDataContainsTheFileContentsName() {
        when(schemaCache.getSchemas()).thenReturn(schemas);
        final SchemaData schemaData = schemaReader.readProjectInfo();
        assertEquals(PROJECT_INFO, schemaData.getName());
    }

    @Test(expected = ArtifactDataNotFoundException.class)
    public void whenReadProjectInfSchemaForNonExistingArtifactThenExceptionIsThrown() {
        when(schemaCache.getSchemas()).thenReturn(schemas);
        schemas.remove(0);
        schemaReader.readProjectInfo();
    }

    @Test(expected = ArtifactDataNotFoundException.class)
    public void whenReadSchemasForUnsupportedNodeTypeThenExceptionIsThrown() {
        doReturn(Collections.<SchemaArtifacts> emptyList()).when(schemaCache).getSchemasForNodeType(INVALID_NODE_TYPE);
        schemaReader.read(INVALID_NODE_TYPE, MIM_VERSION_D_1_44, ARTIFACT1);
    }

}
