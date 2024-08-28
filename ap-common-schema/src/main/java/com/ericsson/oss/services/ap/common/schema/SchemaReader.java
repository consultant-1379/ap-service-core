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
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.ArtifactDataNotFoundException;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.api.schema.SchemaData;
import com.ericsson.oss.services.ap.common.schema.cache.SchemaArtifacts;
import com.ericsson.oss.services.ap.common.schema.cache.SchemaCache;

/**
 * Reads schema files for all supported node types and identifiers.
 */
public class SchemaReader {

    private static final String FILE_TYPE = "SCHEMA";
    private static final String NO_SCHEMA_INSTALLED_ERROR_MESSAGE_FORMAT = "No schema data installed for node type %s";
    private static final String PROJECT_INFO_SCHEMA = "ProjectInfo";

    @Inject
    private SchemaCache cache;

    private ResourceService resourceService;

    @Inject
    private SchemaIdentifierResolver schemaIdentifierResolver;

    @PostConstruct
    public void init() {
        resourceService = new ServiceFinderBean().find(ResourceService.class);
    }

    /**
     * Read the projectInfo schema data from the cached schemas. Note the projectInfo schema is common for all node types/identifiers so it is ok to
     * take the first occurrence of the schema.
     *
     * @return SchemaData for projectInfo schema
     */
    public SchemaData readProjectInfo() {
        return readProjectSchema(PROJECT_INFO_SCHEMA);
    }

    /**
     * Reads schemas files for all identifiers of all node types.
     *
     * @return the schemas file data
     */
    public Map<String, List<SchemaData>> read() {
        final Map<String, List<SchemaArtifacts>> allSchemas = cache.getAllSchemas();
        final Map<String, List<SchemaData>> allSchemasData = new HashMap<>();

        for (final Entry<String, List<SchemaArtifacts>> schema : allSchemas.entrySet()) {
            final List<SchemaData> fileDataForAllSchemas = new ArrayList<>();

            for (final SchemaArtifacts schemasForNodeType : schema.getValue()) {
                fileDataForAllSchemas.addAll(readFileDataForAllSchemas(schemasForNodeType));
            }

            allSchemasData.put(schema.getKey(), fileDataForAllSchemas);
        }

        return allSchemasData;
    }

    /**
     * Reads schemas files for all identifiers of the specified node type.
     *
     * @param nodeType
     *            the node type
     * @return the schemas file data
     */
    public List<SchemaData> read(final String nodeType) {
        final List<SchemaArtifacts> allSchemas = cache.getSchemasForNodeType(nodeType);

        if (allSchemas.isEmpty()) {
            throw new ArtifactDataNotFoundException(String.format(NO_SCHEMA_INSTALLED_ERROR_MESSAGE_FORMAT, nodeType));
        }

        final List<SchemaData> fileDataForAllSchemas = new ArrayList<>();

        for (final SchemaArtifacts schemasForNodeType : allSchemas) {
            fileDataForAllSchemas.addAll(readFileDataForAllSchemas(schemasForNodeType));
        }

        return fileDataForAllSchemas;
    }

    /**
     * Reads all schemas which are compatible with the specified identifier of the node. If no schemas are installed which match the specified
     * identifier then the closest identifier which is not later than the specified identifier match will be returned
     *
     * @param nodeType
     *            the node type
     * @param nodeIdentifier
     *            the node identifier
     * @return the schemas file data
     */
    public List<SchemaData> read(final String nodeType, final String nodeIdentifier) {

        final List<SchemaArtifacts> schemasForNodeType = cache.getSchemasForNodeType(nodeType);
        if (schemasForNodeType.isEmpty()) {
            throw new ArtifactDataNotFoundException(String.format(NO_SCHEMA_INSTALLED_ERROR_MESSAGE_FORMAT, nodeType));
        }

        final SchemaArtifacts compatibleSchemaArtifacts = schemaIdentifierResolver.findCompatibleSchemasForNodeIdentifier(nodeIdentifier,
                schemasForNodeType, nodeType);
        return readFileDataForAllSchemas(compatibleSchemaArtifacts);
    }

    /**
     * Reads the schema artifact which is compatible with the specified identifier of the node. If no schemas are installed which match the specified
     * identifier then the closest identifier which is not later than the specified identifier match will be returned
     *
     * @param nodeType
     *            the node type
     * @param nodeIdentifier
     *            the node identifier
     * @param artifactType
     *            the artifact type
     * @return the schema file data
     */
    public SchemaData read(final String nodeType, final String nodeIdentifier, final String artifactType) {
        final List<SchemaArtifacts> schemasForNodeType = cache.getSchemasForNodeType(nodeType);
        if (schemasForNodeType.isEmpty()) {
            throw new ArtifactDataNotFoundException(String.format(NO_SCHEMA_INSTALLED_ERROR_MESSAGE_FORMAT, nodeType));
        }

        final SchemaArtifacts compatibleSchemaArtifacts = schemaIdentifierResolver.findCompatibleSchemasForNodeIdentifier(nodeIdentifier,
                schemasForNodeType, nodeType);
        return readFileDataForArtifactType(nodeIdentifier, artifactType, compatibleSchemaArtifacts);
    }

    private List<SchemaData> readFileDataForAllSchemas(final SchemaArtifacts schemaArtifacts) {
        final List<SchemaData> schemaData = new ArrayList<>();

        for (final String artifactType : schemaArtifacts.getArtifactTypes()) {

            final String artifactLocation = schemaArtifacts.getArtifactLocation(artifactType);
            final String artifactName = new File(artifactLocation).getName();
            final byte[] artifactFileContents = resourceService.getBytes(artifactLocation);

            if (artifactFileContents == null) {
                throw new ApApplicationException("File does not exist: " + artifactLocation);
            }

            final SchemaData artifactData = new SchemaData(artifactName, FILE_TYPE, schemaArtifacts.getNodeIdentifier(), artifactFileContents,
                    artifactLocation);
            schemaData.add(artifactData);
        }
        return schemaData;
    }

    private SchemaData readFileDataForArtifactType(final String requestedNodeIdentifier, final String requestedArtifactType,
            final SchemaArtifacts schemaArtifacts) {
        for (final String currentArtifactType : schemaArtifacts.getArtifactTypes()) {

            if (currentArtifactType.equalsIgnoreCase(requestedArtifactType)) {
                return createSchemaData(schemaArtifacts, currentArtifactType);
            }
        }

        throw new ArtifactDataNotFoundException(String.format("No artifact of type %s found for node type %s and identifier %s",
                requestedArtifactType, schemaArtifacts.getNodeType(), requestedNodeIdentifier));
    }

    private SchemaData createSchemaData(final SchemaArtifacts schemaArtifacts, final String artifactType) {
        final String artifactLocation = schemaArtifacts.getArtifactLocation(artifactType);
        final String artifactName = new File(artifactLocation).getName();
        final byte[] artifactFileContents = resourceService.getBytes(artifactLocation);
        return new SchemaData(artifactName, FILE_TYPE, schemaArtifacts.getNodeIdentifier(), artifactFileContents, artifactLocation);
    }

    private SchemaData readProjectSchema(final String schemaName) {
        final List<SchemaArtifacts> allSchemas = cache.getSchemas();
        for (final SchemaArtifacts schemasForNodeType : allSchemas) {
            for (final String currentArtifactType : schemasForNodeType.getArtifactTypes()) {
                if (currentArtifactType.equalsIgnoreCase(schemaName)) {
                    return createSchemaData(schemasForNodeType, currentArtifactType);
                }
            }
        }
        throw new ArtifactDataNotFoundException(String.format("No artifact of type %s found", schemaName));
    }

}
