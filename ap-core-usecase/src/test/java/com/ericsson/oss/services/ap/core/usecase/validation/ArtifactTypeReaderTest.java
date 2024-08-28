/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.validation;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_IDENTIFIER_VALUE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.services.ap.api.exception.ArtifactDataNotFoundException;
import com.ericsson.oss.services.ap.api.schema.SchemaData;
import com.ericsson.oss.services.ap.api.schema.SchemaService;

/**
 * Unit tests for {@link ArtifactTypeReader}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ArtifactTypeReaderTest {

    private static final String SCHEMA = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">"
            + "<xs:complexType name=\"Artifacts\">" + "<xs:all>" + "<xs:element name=\"siteBasic\" type=\"xs:string\" minOccurs=\"1\" />"
            + "<xs:element name=\"siteEquipment\" type=\"xs:string\" minOccurs=\"1\" />"
            + "<xs:element name=\"siteInstall\" type=\"xs:string\" minOccurs=\"1\" />"
            + "<xs:element name=\"transport\" type=\"xs:string\" minOccurs=\"0\" />"
            + "<xs:element name=\"radio\" type=\"xs:string\" minOccurs=\"0\" />"
            + "<xs:element name=\"configurations\" type=\"configs\" minOccurs=\"0\" />" + "</xs:all>" + "</xs:complexType>"
            + "<xs:complexType name=\"configurations\">" + "<xs:sequence>"
            + "<xs:element name=\"configuration\" type=\"xs:string\" minOccurs=\"1\" maxOccurs=\"unbounded\" />" + "</xs:sequence>"
            + " </xs:complexType> + </xs:schema>";

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private SchemaService schemaService;

    @InjectMocks
    private ArtifactTypeReader retriever;

    @Test
    public void when_getAllArtifactTypes_then_names_of_all_string_elements_in_artifacts_schema_is_returned() {
        final byte[] schemaByte = SCHEMA.getBytes();
        final SchemaData schemaData = new SchemaData("erbsArtifacts.xml", VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE, schemaByte, "/schema_location");
        when(schemaService.readSchema(anyString(), anyString(), anyString())).thenReturn(schemaData);

        final List<String> artifacts = retriever.getAllArtifactTypes(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE);

        assertEquals(6, artifacts.size());
    }

    @Test(expected = ArtifactDataNotFoundException.class)
    public void when_artifacts_schema_cannot_be_found_then_getAllArtifactTypes_throws_the_exception() {
        doThrow(ArtifactDataNotFoundException.class).when(schemaService).readSchema(anyString(), anyString(), anyString());
        retriever.getAllArtifactTypes(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE);
    }

    @Test(expected = IllegalStateException.class)
    public void when_error_reading_artifacts_then_getAllArtifactTypes_throws_the_exception() {
        doThrow(IllegalStateException.class).when(schemaService).readSchema(anyString(), anyString(), anyString());
        retriever.getAllArtifactTypes(VALID_NODE_TYPE, NODE_IDENTIFIER_VALUE);
    }
}
