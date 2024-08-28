/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
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
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.schema.SchemaData;

/**
 * Units tests for {@link SchemaServiceBean}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SchemaServiceBeanTest {

    private static final String SCHEMA_NAME = "schemaName";
    private static final String INCLUDED_SCHEMA_NAME = "includedSchema";
    private static final String SCHEMA_LOCATION = "/schema_location";

    @Mock
    private SchemaReader schemaReader;

    @InjectMocks
    private SchemaServiceBean schemaService;

    @Test
    public void whenGetSchemasAndSchemaIncludesAnotherSchemaThenBothSchemasAreReturned() throws IOException {
        final byte[] testSchemaContent = IOUtils.toByteArray(this.getClass().getResourceAsStream("/schemaServiceXsds/testSchemaWithInclude.xsd"));
        final SchemaData testSchema = new SchemaData(SCHEMA_NAME, "SCHEMA", NODE_IDENTIFIER_VALUE, testSchemaContent, SCHEMA_LOCATION);
        final byte[] includedSchemaContent = IOUtils.toByteArray(this.getClass().getResourceAsStream("/schemaServiceXsds/includedSchema.xsd"));
        final SchemaData includedSchema = new SchemaData(INCLUDED_SCHEMA_NAME, "SCHEMA", NODE_IDENTIFIER_VALUE, includedSchemaContent,
            SCHEMA_LOCATION);
        when(schemaReader.read(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, SCHEMA_NAME)).thenReturn(testSchema);
        when(schemaReader.read(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, INCLUDED_SCHEMA_NAME)).thenReturn(includedSchema);

        final List<SchemaData> schemas = schemaService.readSchemas(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, SCHEMA_NAME);

        assertEquals(2, schemas.size());
        assertEquals(SCHEMA_NAME, schemas.get(0).getName());
        assertEquals(INCLUDED_SCHEMA_NAME, schemas.get(1).getName());
    }

    @Test
    public void whenGetSchemasThenListOfSchemasAreReturned() throws IOException {
        final byte[] testSchemaContent = IOUtils.toByteArray(this.getClass().getResourceAsStream("/schemaServiceXsds/testSchemaWithoutInclude.xsd"));
        final SchemaData testSchema = new SchemaData(SCHEMA_NAME, "SCHEMA", NODE_IDENTIFIER_VALUE, testSchemaContent, SCHEMA_LOCATION);
        when(schemaReader.read(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, SCHEMA_NAME)).thenReturn(testSchema);

        final List<SchemaData> schemas = schemaService.readSchemas(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, SCHEMA_NAME);

        assertEquals(1, schemas.size());
        assertEquals(SCHEMA_NAME, schemas.get(0).getName());
    }
}
