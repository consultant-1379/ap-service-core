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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.api.schema.SchemaData;
import com.ericsson.oss.services.ap.common.schema.cache.SchemaArtifacts;
import com.ericsson.oss.services.ap.common.schema.cache.SchemaCache;

/**
 * Reads sample files for all supported node types and identifier.
 */
public class SampleReader {

    private static final String FILE_TYPE = "SAMPLE";

    @Inject
    private SchemaCache cache;

    private ResourceService resourceService;

    @PostConstruct
    public void init() {
        resourceService = new ServiceFinderBean().find(ResourceService.class);
    }

    /**
     * Reads samples files for all identifiers of all node types.
     *
     * @return the samples file data or empty map if no samples found
     */
    public Map<String, List<SchemaData>> read() {
        final Map<String, List<SchemaArtifacts>> allSamples = cache.getAllSamples();
        final Map<String, List<SchemaData>> allSamplesData = new HashMap<>();

        for (final Entry<String, List<SchemaArtifacts>> sample : allSamples.entrySet()) {
            final List<SchemaData> fileDataForAllSamples = new ArrayList<>();

            for (final SchemaArtifacts schemasForNodeType : sample.getValue()) {
                fileDataForAllSamples.addAll(readFileDataForAllSamples(schemasForNodeType));
            }

            allSamplesData.put(sample.getKey(), fileDataForAllSamples);
        }

        return allSamplesData;
    }

    /**
     * Reads samples files for all identifiers of the specified node type.
     *
     * @param nodeType
     *            the node type
     * @return the samples file data or empty list if no samples for specified identifier
     */
    public List<SchemaData> read(final String nodeType) {
        final List<SchemaData> fileDataForAllSamples = new ArrayList<>();
        final List<SchemaArtifacts> allSamples = cache.getSamplesForNodeType(nodeType);

        for (final SchemaArtifacts schemasForNodeType : allSamples) {
            fileDataForAllSamples.addAll(readFileDataForAllSamples(schemasForNodeType));
        }

        return fileDataForAllSamples;
    }

    private List<SchemaData> readFileDataForAllSamples(final SchemaArtifacts sampleArtifacts) {
        final List<SchemaData> schemaData = new ArrayList<>();

        for (final String artifactType : sampleArtifacts.getArtifactTypes()) {

            final String artifactLocation = sampleArtifacts.getArtifactLocation(artifactType);
            final String artifactName = new File(artifactLocation).getName();
            final byte[] artifactFileContents = resourceService.getBytes(artifactLocation);

            final SchemaData artifactData = new SchemaData(artifactName, FILE_TYPE, sampleArtifacts.getNodeIdentifier(), artifactFileContents,
                    artifactLocation);
            schemaData.add(artifactData);
        }
        return schemaData;
    }
}
