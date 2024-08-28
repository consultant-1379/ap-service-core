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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.schema.cache.SchemaArtifacts;
import com.ericsson.oss.services.ned.modeling.modelservice.typed.MimMappedTo;

/**
 * Units tests for {@code SchemaIdentifierResolver}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SchemaIdentifierResolverTest {

    private static final String NODE_TYPE_ERBS = "ERBS";
    private static final String MIM_VERSION_D_1_44 = "d.1.44";

    private static final String MIM_VERSION_E_1_120 = "e.1.120";
    private static final String MIM_VERSION_5_1_120 = "5.1.120";
    private static final String MIM_VERSION_E_1_200 = "e.1.200";
    private static final String MIM_VERSION_F_1_100 = "f.1.100";
    private static final String MIM_VERSION_NON_EXISTING_LOW_NUMBER = "1.1.120";
    private static final String MIM_VERSION_NON_EXISTING_SIMILAR_NUMBER = "5.1.150";
    private static final String MIM_VERSION_NON_EXISTING_HIGH_NUMBER = "26.1.44";

    private static final String NODE_IDENTIFIER1 = "5504-866-131";
    private static final String NODE_IDENTIFIER2 = "5504-866-132";
    private static final String NODE_IDENTIFIER4 = "5504-866-134";

    private final SchemaArtifacts schemas1 = new SchemaArtifacts(NODE_TYPE_ERBS, MIM_VERSION_D_1_44);
    private final SchemaArtifacts schemas2 = new SchemaArtifacts(NODE_TYPE_ERBS, MIM_VERSION_E_1_120);
    private final SchemaArtifacts schemas3 = new SchemaArtifacts(NODE_TYPE_ERBS, MIM_VERSION_E_1_200);
    private final SchemaArtifacts schemas4 = new SchemaArtifacts(NODE_TYPE_ERBS, MIM_VERSION_F_1_100);

    private final Collection<MimMappedTo> mappedMims = new ArrayList<>();
    private final List<SchemaArtifacts> schemas = new ArrayList<>();

    @Mock
    private ModelReader modelReader;

    @Mock
    private MimMappedTo mimMappedTo1;

    @Mock
    private MimMappedTo mimMappedTo2;

    @InjectMocks
    private SchemaIdentifierResolver schemaIdentifierResolver;

    @Before
    public void setUp() {
        schemas.add(schemas2);
        schemas.add(schemas1);
        schemas.add(schemas4);
        schemas.add(schemas3);

        mappedMims.add(mimMappedTo1);
        mappedMims.add(mimMappedTo2);
    }

    @Test
    public void whenRequestedSchemaIdentifierIsNullThenEarliestSchemaIdentifierIsReturned() {
        final SchemaArtifacts compatibleSchemas = schemaIdentifierResolver.findCompatibleSchemasForNodeIdentifier(null, schemas, NODE_TYPE_ERBS);
        assertEquals(schemas1, compatibleSchemas);
    }

    @Test
    public void whenRequestedSchemaIdentifierIsEmptyStringThenEarliestSchemaIdentifierIsReturned() {
        final SchemaArtifacts compatibleSchemas = schemaIdentifierResolver.findCompatibleSchemasForNodeIdentifier("", schemas, NODE_TYPE_ERBS);
        assertEquals(schemas1, compatibleSchemas);
    }

    @Test
    public void whenRequestSchemasForMatchingIdentifierThenSchemasForThatIdentifierAreReturned() {
        when(modelReader.getMimVersionMappedToOssModelIdentity(NODE_TYPE_ERBS, NODE_IDENTIFIER4)).thenReturn("4.1.100");

        final SchemaArtifacts compatibleSchemas = schemaIdentifierResolver.findCompatibleSchemasForNodeIdentifier(NODE_IDENTIFIER4, schemas,
            NODE_TYPE_ERBS);
        assertEquals(schemas1, compatibleSchemas);
    }

    @Test
    public void whenRequestSchemasForMatchingIdentifierThenSchemasForThatIdentifierAreReturneLetters() {
        when(modelReader.getMimVersionMappedToOssModelIdentity(NODE_TYPE_ERBS, NODE_IDENTIFIER4)).thenReturn(MIM_VERSION_F_1_100);

        final SchemaArtifacts compatibleSchemas = schemaIdentifierResolver.findCompatibleSchemasForNodeIdentifier(NODE_IDENTIFIER4, schemas,
            NODE_TYPE_ERBS);
        assertEquals(schemas4, compatibleSchemas);
    }

    @Test
    public void whenRequestSchemasWithNoMatchingIdentifierThenTheClosestSchemaIdentifierIsReturned() {
        when(modelReader.getMimVersionMappedToOssModelIdentity(NODE_TYPE_ERBS, NODE_IDENTIFIER1)).thenReturn(MIM_VERSION_NON_EXISTING_SIMILAR_NUMBER);

        final SchemaArtifacts compatibleSchemas = schemaIdentifierResolver.findCompatibleSchemasForNodeIdentifier(NODE_IDENTIFIER1, schemas,
            NODE_TYPE_ERBS);
        assertEquals(schemas2, compatibleSchemas);
    }

    @Test
    public void whenRequestSchemasWithNoMatchingIdentifierHighValueThenTheClosestSchemaIdentifierIsReturned() {
        when(modelReader.getMimVersionMappedToOssModelIdentity(NODE_TYPE_ERBS, NODE_IDENTIFIER1)).thenReturn(MIM_VERSION_NON_EXISTING_HIGH_NUMBER);

        final SchemaArtifacts compatibleSchemas = schemaIdentifierResolver.findCompatibleSchemasForNodeIdentifier(NODE_IDENTIFIER1, schemas,
            NODE_TYPE_ERBS);
        assertEquals(schemas4, compatibleSchemas);
    }

    @Test
    public void whenRequestSchemasForIdentifierEarlierThanAllAvailableVerionsThenTheEarliestAvailableIdentifierIsReturned() {
        when(modelReader.getMimVersionMappedToOssModelIdentity(NODE_TYPE_ERBS, NODE_IDENTIFIER1)).thenReturn(MIM_VERSION_NON_EXISTING_LOW_NUMBER);

        final SchemaArtifacts compatibleSchemas = schemaIdentifierResolver.findCompatibleSchemasForNodeIdentifier(NODE_IDENTIFIER1, schemas,
            NODE_TYPE_ERBS);
        assertEquals(schemas1, compatibleSchemas);
    }

    @Test
    public void whenRequestSchemasForSpecificIdentifierThenTheSpecificSchemaIsReturned() {
        when(modelReader.getMimVersionMappedToOssModelIdentity(NODE_TYPE_ERBS, NODE_IDENTIFIER2)).thenReturn(MIM_VERSION_5_1_120);

        final SchemaArtifacts compatibleSchemas = schemaIdentifierResolver.findCompatibleSchemasForNodeIdentifier(NODE_IDENTIFIER2, schemas,
            NODE_TYPE_ERBS);
        assertEquals(schemas2, compatibleSchemas);
    }
}
