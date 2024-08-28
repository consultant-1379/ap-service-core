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
package com.ericsson.oss.services.ap.api.schema;

import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.ap.api.exception.ArtifactDataNotFoundException; //NOPMD

/**
 * SchemaService providing schema management for AutoProvisioning-related schemas and associated sample XML files. This service provides install, read
 * and delete support for schemas XSDs and sample XMLs. These schemas and samples for the different node types and node identifiers.
 */
public interface SchemaService {

    /**
     * Read all the schema samples for all node types and node identifiers.
     *
     * @return a {@link Map} of {@link SchemaData} keyed by each node type
     */
    Map<String, List<SchemaData>> readSamples();

    /**
     * Read all the schema samples for a given node type (all identifiers).
     *
     * @param nodeType
     *            the type of the node
     * @return a {@link List} of {@link SchemaData} representing all the schema samples found
     */
    List<SchemaData> readSamples(final String nodeType);

    /**
     * Read the projectInfo {@link SchemaData}.
     *
     * @return the projectInfo schema data
     * @throws ArtifactDataNotFoundException
     *             if the associated schema file does not exist
     */
    SchemaData readProjectInfoSchema();

    /**
     * Read the common project schemas, i.e. ProjectInfo and Profile {@link SchemaData} instances.
     *
     * @return a {@link List} of {@link SchemaData} representing all the schema files for the project info
     */
    List<SchemaData> readProjectInfoSchemas();

    /**
     * Reads schema matching specified schema name for a given node type and identifier.
     *
     * @param nodeType
     *            the type of the node
     * @param nodeIdentifier
     *            the identifier of the node, may be null or empty string in which case the default identifier will be used
     * @param schemaName
     *            the type of schemaName
     * @return artifact schema contents as {@link SchemaData}
     * @throws ArtifactDataNotFoundException
     *             if the associated schema file does not exist
     */
    SchemaData readSchema(final String nodeType, final String nodeIdentifier, final String schemaName);

    /**
     * Read all the schemas for all node types and node identifiers.
     *
     * @return a {@link Map} of {@link SchemaData} keyed by each node type
     */
    Map<String, List<SchemaData>> readSchemas();

    /**
     * Read all the schemas for a given node type.
     *
     * @param nodeType
     *            the type of the node
     * @return a {@link List} of {@link SchemaData} representing all the schema files for the given node type
     */
    List<SchemaData> readSchemas(final String nodeType);

    /**
     * Read all the schemas for a given node type and node identifier.
     *
     * @param nodeType
     *            the type of the node
     * @param nodeIdentifier
     *            the identifier of the node
     * @return a {@link List} of {@link SchemaData} representing all the schema files for the given node type and node identifier
     */
    List<SchemaData> readSchemas(final String nodeType, final String nodeIdentifier);

    /**
     * Read schema matching a given schema name and its associated schemas (imported/included schemas) for a given node type and node identifier.
     *
     * @param nodeType
     *            the type of the node
     * @param nodeIdentifier
     *            the identifier of the node, may be null or empty string in which case the default identifier will be used
     * @param schemaName
     *            the type of schemaName
     * @return a {@link List} of {@link SchemaData} representing the schema for schemaName and all its associated schema files for the given node Type
     *         and node identifier
     */
    List<SchemaData> readSchemas(final String nodeType, final String nodeIdentifier, final String schemaName);
}
