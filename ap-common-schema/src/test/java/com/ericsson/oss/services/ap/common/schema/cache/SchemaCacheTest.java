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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

/**
 * Units tests for {@code SchemaCache}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SchemaCacheTest {

    @Mock
    private SchemaArtifactsFileReader artifactsReader;

    @InjectMocks
    private SchemaCache cache;

    private static final String ERBS_NODE_TYPE = "erbs";
    private static final String MSRBS_V1_NODE_TYPE = "msrbs_v1";
    private static final String UNKNOWN = "unknown";

    private static final SchemaArtifacts ERBS_SCHEMA = new SchemaArtifacts(ERBS_NODE_TYPE, "1");
    private static final SchemaArtifacts MSRBS_V1_SCHEMA = new SchemaArtifacts(MSRBS_V1_NODE_TYPE, "1");
    private static final SchemaArtifacts ERBS_SAMPLE = new SchemaArtifacts(ERBS_NODE_TYPE, "1");
    private static final SchemaArtifacts MSRBS_V1_SAMPLE = new SchemaArtifacts(MSRBS_V1_NODE_TYPE, "1");

    private final Map<String, List<SchemaArtifacts>> schemasByNodeType = new HashMap<>();
    private final Map<String, List<SchemaArtifacts>> samplesByNodeType = new HashMap<>();
    private final List<SchemaArtifacts> erbsSchemas = new ArrayList<>();
    private final List<SchemaArtifacts> msrbsv1Schemas = new ArrayList<>();
    private final List<SchemaArtifacts> erbsSamples = new ArrayList<>();
    private final List<SchemaArtifacts> msrbsv1Samples = new ArrayList<>();

    @Before
    public void setUp() {
        erbsSchemas.add(ERBS_SCHEMA);
        msrbsv1Schemas.add(MSRBS_V1_SCHEMA);
        erbsSamples.add(ERBS_SAMPLE);
        msrbsv1Samples.add(MSRBS_V1_SAMPLE);

        schemasByNodeType.put(ERBS_NODE_TYPE.toUpperCase(), erbsSchemas);
        schemasByNodeType.put(MSRBS_V1_NODE_TYPE.toUpperCase(), msrbsv1Schemas);
        samplesByNodeType.put(ERBS_NODE_TYPE.toUpperCase(), erbsSamples);
        samplesByNodeType.put(MSRBS_V1_NODE_TYPE.toUpperCase(), msrbsv1Samples);

        when(artifactsReader.readSchemasFromFileSystem()).thenReturn(schemasByNodeType);
        when(artifactsReader.readSamplesFromFileSystem()).thenReturn(samplesByNodeType);
    }

    @Test
    public void whenPostConstructCalledCacheIsPopulatedWithSchemas() {
        cache.loadCache();
        assertFalse(cache.getSchemas().isEmpty());
    }

    @Test
    public void whenPostConstructCalledCacheIsPopulatedWithSamples() {
        cache.loadCache();
        assertFalse(cache.getSamples().isEmpty());
    }

    @Test
    public void whenGetSchemasThenSchemasAreReturnedForAllNodeTypes() {
        cache.loadCache();
        assertEquals((erbsSchemas.size() + msrbsv1Schemas.size()), cache.getSchemas().size());
    }

    @Test
    public void whenGetSamplesThenSamplesAreReturnedForAllNodeTypes() {
        cache.loadCache();
        assertEquals((erbsSamples.size() + msrbsv1Samples.size()), cache.getSamples().size());
    }

    @Test
    public void whenGetSchemasByNodeTypeThenSchemasReturnedOnlyForThatNodeType() {
        cache.loadCache();
        assertEquals(1, cache.getSchemasForNodeType(ERBS_NODE_TYPE).size());
    }

    @Test
    public void whenNoSchemasForNodeTypeThenEmptyListReturned() {
        cache.loadCache();
        assertTrue(cache.getSchemasForNodeType(UNKNOWN).isEmpty());
    }

    @Test
    public void whenGetSamplesByNodeTypeThenSamplesReturnedOnlyForThatNodeType() {
        cache.loadCache();
        assertEquals(1, cache.getSamplesForNodeType(ERBS_NODE_TYPE).size());
    }

    @Test
    public void whenNoSamplesForNodeTypeThenEmptyListReturned() {
        cache.loadCache();
        assertTrue(cache.getSamplesForNodeType(UNKNOWN).isEmpty());
    }
}
