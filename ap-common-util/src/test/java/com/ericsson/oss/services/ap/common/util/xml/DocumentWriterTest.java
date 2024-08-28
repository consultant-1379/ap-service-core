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
package com.ericsson.oss.services.ap.common.util.xml;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Unit tests for {@link DocumentWriter}.
 */
public class DocumentWriterTest {

    private static final String XML_FILE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
        + "<root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
        + "<element1 attr1=\"attr1_value\"></element1>"
        + "<element2 attr2=\"attr2_value\"></element2>"
        + "<element3 attr2=\"attr2_value\"></element3>"
        + "</root>";

    private final Document document = DocumentBuilder.getDocument(XML_FILE);
    private final DocumentWriter documentWriter = new DocumentWriter(document);

    @Test
    public void whenSingleOccurenceOfAttributeSetAttributeValuesUpdatesTheAttributeValue() {
        final String updatedAttrValue = "newValue";
        final Map<String, String> attributeToUpdate = new HashMap<>();
        attributeToUpdate.put("attr1", updatedAttrValue);

        documentWriter.setAttributeValues(attributeToUpdate);

        final DocumentReader docReader = new DocumentReader(document);

        final Element element1 = docReader.getElement("element1");
        assertEquals(updatedAttrValue, element1.getAttribute("attr1"));
    }

    @Test
    public void whenMultipleOccurenceOfAttributeSetAttributeValuesUpdatesTheAttributeValueForAll() {
        final String updatedAttrValue = "newValue";
        final Map<String, String> attributeToUpdate = new HashMap<>();
        attributeToUpdate.put("attr2", updatedAttrValue);

        documentWriter.setAttributeValues(attributeToUpdate);

        final DocumentReader docReader = new DocumentReader(document);

        final Element element2 = docReader.getElement("element2");
        final Element element3 = docReader.getElement("element3");

        assertEquals(updatedAttrValue, element2.getAttribute("attr2"));
        assertEquals(updatedAttrValue, element3.getAttribute("attr2"));
    }

    @Test
    public void whenNoOccurenceOfAttributeSetAttributeValuesTheDocumentIsUnchanged() {
        final String updatedAttrValue = "newValue";
        final Map<String, String> attributeToUpdate = new HashMap<>();
        attributeToUpdate.put("unknown", updatedAttrValue);

        final String documentBeforeUpdate = DocumentBuilder.getDocumentAsString(document);

        documentWriter.setAttributeValues(attributeToUpdate);

        final String documentAfterUpdate = DocumentBuilder.getDocumentAsString(document);

        assertEquals(documentBeforeUpdate, documentAfterUpdate);
    }

    @Test
    public void whenAddAttributeToElementWithOneAttributeThenNewAttributeAddedToElement() {
        final String updatedAttrValue = "newValue";
        final Map<String, String> attributeToUpdate = new HashMap<>();
        attributeToUpdate.put("attr3", updatedAttrValue);

        documentWriter.setElementAttributesValues("element1", attributeToUpdate);

        final DocumentReader docReader = new DocumentReader(document);

        final Element element1 = docReader.getElement("element1");
        assertEquals(element1.getAttributes().getLength(), 2);
        assertEquals(updatedAttrValue, element1.getAttribute("attr3"));
    }

    @Test
    public void whenAddAttributeToElementWithMultipleAttributesThenNewAttributesAddedToElement() {
        final Map<String, String> attributeToUpdate = new HashMap<>();
        final String updatedAttr3Value = "newValue3";
        attributeToUpdate.put("attr3", updatedAttr3Value);
        final String updatedAttr4Value = "newValue3";
        attributeToUpdate.put("attr4", updatedAttr4Value);
        final String updatedAttr5Value = "newValue3";
        attributeToUpdate.put("attr5", updatedAttr5Value);

        documentWriter.setElementAttributesValues("element1", attributeToUpdate);

        final DocumentReader docReader = new DocumentReader(document);

        final Element element1 = docReader.getElement("element1");
        assertEquals(updatedAttr3Value, element1.getAttribute("attr3"));
        assertEquals(updatedAttr4Value, element1.getAttribute("attr4"));
        assertEquals(updatedAttr5Value, element1.getAttribute("attr5"));
    }

    @Test
    public void whenAddAttributeToElementForExistingAttributeThenExistingAttributeIsUpdated() {
        final String updatedAttrValue = "newValue";
        final Map<String, String> attributeToUpdate = new HashMap<>();
        attributeToUpdate.put("attr1", updatedAttrValue);

        documentWriter.setElementAttributesValues("element1", attributeToUpdate);

        final DocumentReader docReader = new DocumentReader(document);

        final Element element1 = docReader.getElement("element1");

        assertEquals(element1.getAttributes().getLength(), 1);
        assertEquals(updatedAttrValue, element1.getAttribute("attr1"));
    }
}
