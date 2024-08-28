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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * Caches data relating to all schema and sample files installed on the filesystem.
 * <p>
 * Application scoped bean. Reads all schemas and samples from the filesystem during PostContruct.
 */
@ApplicationScoped
public class SchemaCache {

    private final Map<String, List<SchemaArtifacts>> schemasByNodeType = new ConcurrentHashMap<>();

    private final Map<String, List<SchemaArtifacts>> samplesByNodeType = new ConcurrentHashMap<>();

    private static final String[] SET_VALUES = new String[] { "ROUTER6X71", "ROUTER6672", "ROUTER6675", "ROUTER6673", "ROUTER6000-2" };
    private static final Set<String> NODE_TYPES = new HashSet<>(Arrays.asList(SET_VALUES));
    private static final Set<String> nodeTypeSchemasAndSamplesKeys = Collections.unmodifiableSet(NODE_TYPES);

    @Inject
    private SchemaArtifactsFileReader artifactsReader;

    @Inject
    private Logger logger;

    /**
     * Populates the cache on startup and registers listener for modifications in the schema directory.
     */
    @PostConstruct
    public void init() {
        loadCache();
    }

    /**
     * Read the schema and sample files installed on the file system and updates the cache.
     */
    public void loadCache() {
        try {
            final Map<String, List<SchemaArtifacts>> schemasOnFileSystem = artifactsReader.readSchemasFromFileSystem();
            schemasByNodeType.putAll(schemasOnFileSystem);

            final Map<String, List<SchemaArtifacts>> samplesOnFileSystem = artifactsReader.readSamplesFromFileSystem();
            samplesByNodeType.putAll(samplesOnFileSystem);

        } catch (final Exception e) {
            logger.error("Error reading schema files", e);
            throw new IllegalStateException("Unable to read schemas");
        }
    }

    /**
     * Gets schemas for all node types and identifiers.
     *
     * @return the schemas
     */
    public List<SchemaArtifacts> getSchemas() {
        final List<SchemaArtifacts> schemasForAllNodeTypes = new ArrayList<>();
        for (final List<SchemaArtifacts> schemasForNodeType : schemasByNodeType.values()) {
            schemasForAllNodeTypes.addAll(schemasForNodeType);
        }
        return schemasForAllNodeTypes;
    }

    /**
     * Gets schemas for all node types and identifiers as a map.
     *
     * @return the schemas
     */
    public Map<String, List<SchemaArtifacts>> getAllSchemas() {
        return Collections.unmodifiableMap(schemasByNodeType);
    }

    /**
     * Gets samples for all node types and identifiers as a map.
     *
     * @return the samples
     */
    public Map<String, List<SchemaArtifacts>> getAllSamples() {
        return Collections.unmodifiableMap(samplesByNodeType);
    }

    /**
     * Gets samples for all node types and identifiers.
     *
     * @return the samples
     */
    public List<SchemaArtifacts> getSamples() {
        final List<SchemaArtifacts> samplesForAllNodeTypes = new ArrayList<>();
        for (final List<SchemaArtifacts> samplesForNodeType : samplesByNodeType.values()) {
            samplesForAllNodeTypes.addAll(samplesForNodeType);
        }
        return samplesForAllNodeTypes;
    }

    /**
     * Gets schemas for all identifiers of the specified node type.
     *
     * @return the schemas
     */
    public List<SchemaArtifacts> getSchemasForNodeType(final String nodeType) {
        final String nodeTypeSchemasKey = getNodeTypeSchemaAndSamplesKey(nodeType.toUpperCase());
        final List<SchemaArtifacts> schemasOfNodeType = schemasByNodeType.get(nodeTypeSchemasKey);
        return schemasOfNodeType == null ? Collections.<SchemaArtifacts> emptyList() : schemasOfNodeType;
    }

    /**
     * Gets samples for all identifiers of the specified node type.
     *
     * @return the schemas
     */
    public List<SchemaArtifacts> getSamplesForNodeType(final String nodeType) {
        final String nodeTypeSamplesKey = getNodeTypeSchemaAndSamplesKey(nodeType.toUpperCase());
        final List<SchemaArtifacts> samplesOfNodeType = samplesByNodeType.get(nodeTypeSamplesKey);
        return samplesOfNodeType == null ? Collections.<SchemaArtifacts> emptyList() : samplesOfNodeType;
    }

    private String getNodeTypeSchemaAndSamplesKey(final String nodeType) {
        if (nodeTypeSchemasAndSamplesKeys.contains(nodeType)) {
            return "ROUTER6K";
        }
        return nodeType;
    }
}
