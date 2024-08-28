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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.api.schema.SchemaData;
import com.ericsson.oss.services.ap.common.schema.cache.SchemaArtifacts;
import com.ericsson.oss.services.ap.common.schema.cache.SchemaCache;

/**
 * Units tests for {@link SampleReader}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SampleReaderTest {

    private static final byte[] SAMPLE_CONTENTS = "content".getBytes();
    private static final SchemaArtifacts SAMPLES_1 = new SchemaArtifacts("erbs", "d.1.44");
    private static final SchemaArtifacts SAMPLES_2 = new SchemaArtifacts("erbs", "e.1.120");

    private final List<SchemaArtifacts> samples = new ArrayList<>();

    @Mock
    private ResourceService resourceService;

    @Mock
    private SchemaCache schemaCache;

    @InjectMocks
    private SampleReader sampleReader;

    @Before
    public void setUp() {
        SAMPLES_1.addArtifact("artifact1", "/tmp/artifact1.xml");
        SAMPLES_1.addArtifact("artifact2", "/tmp/artifact2.xml");
        SAMPLES_2.addArtifact("artifact1", "/tmp/artifact1.xml");
        SAMPLES_2.addArtifact("artifact2", "/tmp/artifact2.xml");

        samples.add(SAMPLES_1);
        samples.add(SAMPLES_2);

        when(resourceService.getBytes(anyString())).thenReturn(SAMPLE_CONTENTS);
    }

    @Test
    public void whenNoSamplesInCacheThenReturnedListIsEmpty() {
        doReturn(Collections.<SchemaArtifacts> emptyList()).when(schemaCache).getSamplesForNodeType("erbs");
        assertTrue(sampleReader.read("erbs").isEmpty());
    }

    @Test
    public void whenReadSamplesForNodeTypeThenAllSamplesForThatTypeAreReturned() {
        when(schemaCache.getSamplesForNodeType("erbs")).thenReturn(samples);
        final int expectedNumSchemas = SAMPLES_1.getArtifactTypes().size() + SAMPLES_2.getArtifactTypes().size();
        assertEquals(expectedNumSchemas, sampleReader.read("erbs").size());
    }

    @Test
    public void whenReadSamplesTheReturnedDataContainsTheSampleFileContents() {
        when(schemaCache.getSamplesForNodeType("erbs")).thenReturn(samples);
        final List<SchemaData> samplesData = sampleReader.read("erbs");
        assertArrayEquals(SAMPLE_CONTENTS, samplesData.get(0).getData());
    }

    @Test
    public void whenReadSamplesTheReturnedDataContainsTheFileNameAndIdentifier() {
        when(schemaCache.getSamplesForNodeType("erbs")).thenReturn(samples);
        final List<SchemaData> samplesData = sampleReader.read("erbs");
        assertEquals("artifact1", samplesData.get(0).getName());
        assertEquals("d.1.44", samplesData.get(0).getIdentifier());
    }

    @Test
    public void whenReadAllSamplesAndNoSamplesFoundThenReturnEmptyMap() {
        final Map<String, List<SchemaArtifacts>> allSamples = new HashMap<>();
        when(schemaCache.getAllSamples()).thenReturn(allSamples);
        final Map<String, List<SchemaData>> result = sampleReader.read();
        assertTrue(result.isEmpty());
    }

    @Test
    public void whenReadAllSamplesThenReturnAllSamples() {
        final Map<String, List<SchemaArtifacts>> allSamples = new HashMap<>();
        allSamples.put("erbs", samples);
        allSamples.put("rbs", samples);
        when(schemaCache.getAllSamples()).thenReturn(allSamples);
        final Map<String, List<SchemaData>> result = sampleReader.read();

        assertEquals(2, result.size());
        final Iterator<List<SchemaData>> nodeSamples = result.values().iterator();
        assertEquals(8, getTotalSizeOfIteratorContents(nodeSamples));
    }

    private int getTotalSizeOfIteratorContents(final Iterator<List<SchemaData>> iteratorToCount) {
        int count = 0;
        while (iteratorToCount.hasNext()) {
            count += iteratorToCount.next().size();
        }
        return count;
    }
}
