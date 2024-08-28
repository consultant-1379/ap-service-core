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

import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * Class provides methods for updating {@link Document} instances.
 */
public class DocumentWriter {

    final Document document;

    public DocumentWriter(final Document document) {
        this.document = document;
    }

    /**
     * Sets the value for all matching element attributes in the document.
     * <p>
     * Note: This is a convenience method but may have unwanted side-effects if an attribute with the same name exists in multiple elements in the
     * document or in the case of updating a sequence.
     * <p>
     * Any attributes which do not exist in the document are ignored.
     *
     * @param attrNameValues
     *            Map of attribute names to their new values
     */
    public void setAttributeValues(final Map<String, String> attrNameValues) {
        final NodeList elements = document.getElementsByTagName("*");
        final int numberOfElements = elements.getLength();

        for (int i = 0; i < numberOfElements; ++i) {
            final Element element = (Element) elements.item(i);
            final NamedNodeMap nodeAttributes = element.getAttributes();

            for (int x = 0; x < nodeAttributes.getLength(); ++x) {
                final Attr attr = (Attr) nodeAttributes.item(x);
                if (attrNameValues.containsKey(attr.getNodeName())) {
                    attr.setNodeValue(attrNameValues.get(attr.getNodeName()));
                }
            }
        }
    }

    /**
     * For the specified element, add an attribute if the attribute does not exist, and update it if it does exist.
     * <p>
     * Note: This is a convenience method but may have unwanted side-effects if an attribute with the same name exists in multiple elements in the
     * document or in the case of updating a sequence.
     *
     * @param elementName
     *            Name of the element to which attributes will be added.
     * @param attrNameValues
     *            Map of attribute names to their new values
     */
    public void setElementAttributesValues(final String elementName, final Map<String, String> attrNameValues) {
        final NodeList elements = document.getElementsByTagName(elementName);
        final int numberOfElements = elements.getLength();

        for (int i = 0; i < numberOfElements; ++i) {
            final Element element = (Element) elements.item(i);

            for (final Entry<String, String> nameValue : attrNameValues.entrySet()) {
                element.setAttribute(nameValue.getKey(), nameValue.getValue());
            }
        }
    }
}