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
package com.ericsson.oss.services.ap.common.util.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Element;

/**
 * Unit tests for {@link DocumentReader}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DocumentReaderTest {

    private static final String FIRST_NAME = "firstname";
    private static final String[] ADDRESS_INFO_CHILD_ELEMENT_NAMES = { "city", "country" };
    private static final String[] MOBILE_NUMBER_CHILD_ELEMENT_NAMES = { "type", "countryCode", "networkCode", "number" };
    private static final String[] ROOT_CHILD_ELEMENT_NAMES = { FIRST_NAME, "surname", "addressInfo", "phoneInfo" };

    private DocumentReader documentReader = new DocumentReader(readFileOnClasspath("/xml/personInfo.xml"));

    @Test
    public void whengetRootChildElementsAsMapThenOnlyElementsDirectlyUnderRootElementAreReturned() {
        final Map<String, String> rootChildElements = documentReader.getRootChildElementsAsMap();
        assertEquals(ROOT_CHILD_ELEMENT_NAMES.length, rootChildElements.size());
    }

    @Test
    public void whenSingleParentElementExistsGetChildElementsAsMapReturnsAllElementsUnderParentElementAreReturned() {
        final Map<String, String> childElements = documentReader.getAllChildElementsAsMap("addressInfo");
        assertEquals(ADDRESS_INFO_CHILD_ELEMENT_NAMES.length, childElements.size());
    }

    @Test
    public void whenMultipleParentElementsOfSameNameExistGetChildElementsAsMapReturnsChildrenForTheFirstOccurence() {
        final Map<String, String> childElements = documentReader.getAllChildElementsAsMap("mobileNumber");
        assertEquals(MOBILE_NUMBER_CHILD_ELEMENT_NAMES.length, childElements.size());
        assertEquals("work", childElements.get("type"));
    }

    @Test
    public void whenNoParentElementsOfNameExistGetChildElementsAsMapReturnsEmptyList() {
        final Map<String, String> childElements = documentReader.getAllChildElementsAsMap("unknown");
        assertTrue(childElements.isEmpty());
    }

    @Test
    public void whenElementHasNoChildrenGetChildElementsAsMapReturnsEmptyList() {
        final Map<String, String> childElements = documentReader.getAllChildElementsAsMap(FIRST_NAME);
        assertTrue(childElements.isEmpty());
    }

    @Test
    public void whenSingleParentElementExistsGetChildElementsReturnsAllElementsUnderParentElementAreReturned() {
        final Collection<Element> childElements = documentReader.getAllChildElements("addressInfo");
        assertEquals(ADDRESS_INFO_CHILD_ELEMENT_NAMES.length, childElements.size());
    }

    @Test
    public void whenMultipleParentElementsOfSameNameExistGetChildElementReturnsChildrenForFirstOccurrence() {
        final Collection<Element> childElements = documentReader.getAllChildElements("mobileNumber");
        assertEquals(MOBILE_NUMBER_CHILD_ELEMENT_NAMES.length, childElements.size());
        assertEquals("work", childElements.iterator().next().getTextContent());
    }

    @Test
    public void whenNoParentElementsOfNameExistGetChildElementsReturnsEmptyList() {
        final Collection<Element> childElements = documentReader.getAllChildElements("unknown");
        assertTrue(childElements.isEmpty());
    }

    @Test
    public void whenElementHasNoChildrenGetChildElementsReturnsEmptyList() {
        final Collection<Element> childElements = documentReader.getAllChildElements(FIRST_NAME);
        assertTrue(childElements.isEmpty());
    }

    @Test
    public void whenSingleElementOfNameExistsGetElementReturnsThatElement() {
        final Element element = documentReader.getElement(FIRST_NAME);
        assertEquals("joe", element.getTextContent());
    }

    @Test
    public void whenMultipleElementOfSameNameExistsGetElementReturnsTheFirstOccurrence() {
        final Element element = documentReader.getElement("number");
        assertEquals("1111111", element.getTextContent());
    }

    @Test
    public void whenNoElementOfSameNameExistsGetElementReturnsNull() {
        final Element element = documentReader.getElement("unknown");
        assertNull(element);
    }

    @Test
    public void whenSingleElementOfNameExistsGetElementValueReturnsTheValueForThatElement() {
        final String elementValue = documentReader.getElementValue(FIRST_NAME);
        assertEquals("joe", elementValue);
    }

    @Test
    public void whenMultipleElementOfSameNameExistGetElementValueReturnsValueForFirstOccurrence() {
        final String elementValue = documentReader.getElementValue("number");
        assertEquals("1111111", elementValue);
    }

    @Test
    public void whenNoElementOfNameExistsGetElementValueReturnsNull() {
        final String elementValue = documentReader.getElementValue("unknown");
        assertNull(elementValue);
    }

    @Test
    public void whenMultipleElementsOfSameNameExistGetAllElementsReturnsAllOccurences() {
        final Collection<Element> elements = documentReader.getAllElements("number");
        assertEquals(2, elements.size());
    }

    @Test
    public void whenNoElementsOfNameExistGetAllElementsReturnsEmptyList() {
        final Collection<Element> elements = documentReader.getAllElements("unknown");
        assertTrue(elements.isEmpty());
    }

    @Test
    public void whenSchemaIncludedGetRelatedSchemasReturnsTheNamesOfEach() {
        final String xmlFile = readFileOnClasspath("/xml/personInfo.xsd");
        documentReader = new DocumentReader(xmlFile);

        final Set<String> includedSchemas = documentReader.getRelatedSchemas();
        assertEquals(1, includedSchemas.size());
    }

    @Test
    public void whenNamespaceSchemaElementExistsAndReturnValue() {
        final String attributeValue = documentReader.getValueOfSpecifiedAttributeInRootElement("xsi:noNamespaceSchemaLocation");
        assertEquals(attributeValue, "personInfo.xsd");
    }

    @Test
    public void testWhenAttributePresentInAnElementAndReturnValue() {
        final String attributeValue = documentReader.getAttribute("phoneInfo", "suspend", "false");
        assertEquals(Boolean.valueOf(attributeValue), true);
    }

    @Test
    public void testWhenGivenElementNameIsNotFoundAndDefaultValueReturn() {
        final String attributeValue = documentReader.getAttribute("notFoundElement", "suspend", "true");
        assertEquals(Boolean.valueOf(attributeValue), true);
    }

    @Test
    public void testWhenGivenAttributeNameIsNotFoundAndDefaultValueReturn() {
        final String attributeValue = documentReader.getAttribute("phoneInfo", "suspendNotFound", "false");
        assertEquals(Boolean.valueOf(attributeValue), false);
    }

    private String readFileOnClasspath(final String path) {
        try (final InputStream is = this.getClass().getResourceAsStream(path);
            final Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.toString())) {
            return scanner.useDelimiter("\\A").next();
        } catch (final IOException e) {
            fail("Error reading classpath resource " + path);
        }
        return null;
    }
}
