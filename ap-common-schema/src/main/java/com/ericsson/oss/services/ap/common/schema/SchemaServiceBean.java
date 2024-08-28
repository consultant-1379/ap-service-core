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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.w3c.dom.Document;

import com.ericsson.oss.services.ap.api.schema.SchemaData;
import com.ericsson.oss.services.ap.api.schema.SchemaService;
import com.ericsson.oss.services.ap.common.util.xml.DocumentBuilder;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;

/**
 * Service implementation providing access to schema and samples for all supported node types and identifiers.
 */
@Default
public class SchemaServiceBean implements SchemaService {

    private static final String SCHEMA_EXTENSION = ".xsd";

    @Inject
    private SampleReader sampleReader;

    @Inject
    private SchemaReader schemaReader;

    @Override
    public Map<String, List<SchemaData>> readSamples() {
        return sampleReader.read();
    }

    @Override
    public List<SchemaData> readSamples(final String nodeType) {
        return sampleReader.read(nodeType);
    }

    @Override
    public SchemaData readSchema(final String nodeType, final String nodeIdentifier, final String schemaName) {
        return schemaReader.read(nodeType, nodeIdentifier, schemaName);
    }

    @Override
    public Map<String, List<SchemaData>> readSchemas() {
        return schemaReader.read();
    }

    @Override
    public List<SchemaData> readSchemas(final String nodeType) {
        return schemaReader.read(nodeType);
    }

    @Override
    public List<SchemaData> readSchemas(final String nodeType, final String nodeIdentifier) {
        return schemaReader.read(nodeType, nodeIdentifier);
    }

    @Override
    public List<SchemaData> readSchemas(final String nodeType, final String nodeIdentifier, final String schemaName) {
        final SchemaData schema = schemaReader.read(nodeType, nodeIdentifier, schemaName);
        final List<SchemaData> schemas = new ArrayList<>();
        schemas.add(schema);

        final Set<String> includedSchemaNames = findIncludedSchemaNames(schema);

        for (final String includedSchemaName : includedSchemaNames) {
            final String nameWithoutExtension = includedSchemaName.trim().split(SCHEMA_EXTENSION)[0];
            final SchemaData includedSchemaData = schemaReader.read(nodeType, nodeIdentifier, nameWithoutExtension);
            schemas.add(includedSchemaData);
        }

        return schemas;
    }

    private static Set<String> findIncludedSchemaNames(final SchemaData schema) {
        final InputStream inputStream = new ByteArrayInputStream(schema.getData());
        final Document document = DocumentBuilder.getDocument(inputStream);
        return new DocumentReader(document).getRelatedSchemas();
    }

    @Override
    public SchemaData readProjectInfoSchema() {
        return schemaReader.readProjectInfo();
    }

    @Override
    public List<SchemaData> readProjectInfoSchemas(){
        final List<SchemaData> schemas = new ArrayList<>();
        schemas.add(schemaReader.readProjectInfo());
        return schemas;
    }

}
