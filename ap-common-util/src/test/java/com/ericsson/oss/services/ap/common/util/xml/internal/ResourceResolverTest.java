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
package com.ericsson.oss.services.ap.common.util.xml.internal;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.ls.LSInput;

import com.ericsson.oss.services.ap.api.schema.SchemaData;

/**
 * Unit tests for {@link ResourceResolver}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceResolverTest {

    private static final String TEST_VALUE = "projectInfo";
    private static final String XSD_SAMPLE = "<?xml version='1.0' encoding='UTF-8'?>" +
        "<xs:schema xmlns:xs='http://www.w3.org/2001/XMLSchema'>" +
        "<xs:include schemaLocation='projectInfo.xsd'/>" + "<xs:import schemaLocation='AutoIntegration.xsd'/>" +
        "<xs:element name='projectInfo' type='projectInfo'/>" +
        "<xs:complexType name='" + TEST_VALUE + "'>" + "<xs:complexContent>" + "<xs:extension base='projectContent'>" + "<xs:sequence>"
        + "<xs:element name='autoIntegration' type='autoIntegrationType' minOccurs='0' />" + "</xs:sequence>" + "</xs:extension>"
        + "</xs:complexContent>" + "</xs:complexType>" +
        "<xs:import schemaLocation='someDummySchema.xsd'/>" + "<xs:import schemaLocation='someDummySchema.xsd'/>" + "</xs:schema>";

    @Test
    public void whenResolveResourceAndResourceExistsThenCorrectResourceIsReturned() {
        final List<SchemaData> schemas = new ArrayList<>();
        schemas.add(new SchemaData("test", "SCHEMA", "D.1.44", XSD_SAMPLE.getBytes(), "/schema_location"));

        final ResourceResolver resourceResolver = new ResourceResolver(schemas);
        final LSInput resource = resourceResolver.resolveResource(null, null, null, "test.xsd", "");

        assertEquals("test.xsd", resource.getSystemId());
        assertTrue(resource.getStringData().contains(TEST_VALUE));
    }

    @Test
    public void whenResolveResourceAndNoResourcesExiThenNullIsReturned() {
        final List<SchemaData> schemas = new ArrayList<>();
        final ResourceResolver resourceResolver = new ResourceResolver(schemas);
        final LSInput resource = resourceResolver.resolveResource(null, null, null, "test.xsd", "");
        assertNull(resource);
    }

    @Test
    public void whenResolveResourceAndWantedResourceDoesNotExistThenNullIsReturned() {
        final List<SchemaData> schemas = new ArrayList<>();
        schemas.add(new SchemaData("test", "SCHEMA", VALID_NODE_TYPE, XSD_SAMPLE.getBytes(), "/schema_location"));

        final ResourceResolver resourceResolver = new ResourceResolver(schemas);
        final LSInput resource = resourceResolver.resolveResource(null, null, null, "someOtherSchema.xsd", "");

        assertNull(resource);
    }
}
