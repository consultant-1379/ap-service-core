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
package com.ericsson.oss.services.ap.common.util.xml.internal;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link XsdInput}.
 */
@RunWith(MockitoJUnitRunner.class)
public class XsdInputTest {

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
    public void whenGetStringDataThenInputStreamContentAsStringIsReturned() {
        final InputStream input = new ByteArrayInputStream(XSD_SAMPLE.getBytes());
        final XsdInput xsdInput = new XsdInput("publicId", "systemId", input);
        assertEquals(XSD_SAMPLE, xsdInput.getStringData());
    }
}