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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class provides methods for accessing data in an XML document.
 */
public class DocumentReader {

    private final Document document;

    public DocumentReader(final Document document) {
        this.document = document;
    }

    public DocumentReader(final String xmlContent) {
        document = DocumentBuilder.getDocument(xmlContent);
    }

    /**
     * Gets all elements in the document which are direct children of the root. Since a Map is returned this method should not be used if there may be
     * multiple child elements of the same name. The value of an element is taken as its text content, {@link Node#getTextContent()}.
     *
     * @return Map containing name-value pairs of all simple elements under root
     */
    public Map<String, String> getRootChildElementsAsMap() {
        final Element root = document.getDocumentElement();
        final NodeList children = root.getChildNodes();
        return convertElementsToNameValues(children);
    }

    /**
     * Gets value of the specified attribute of the root element in the document.
     *
     * @return String value of attribute
     */
    public String getValueOfSpecifiedAttributeInRootElement(final String attributeName) {
        final Element root = document.getDocumentElement();
        final NamedNodeMap attributes = root.getAttributes();
        final Node namedItem = attributes.getNamedItem(attributeName);
        if (namedItem == null) {
            return null;
        }
        return namedItem.getTextContent();
    }

    /**
     * Gets all child elements of the specified parent element in the document. Search is not limited to direct children, traverses all decendents.
     * Since a Map is returned this method should not be used if there may be multiple child elements of the same name.
     * <p>
     * The value of an element is taken as its text content, {@link Node#getTextContent()}.
     *
     * @param parentElementName
     *            the name of the parent element
     * @return Map containing child elements or empty map if parent element does not exist or contains no children
     */
    public Map<String, String> getAllChildElementsAsMap(final String parentElementName) {
        final Element root = document.getDocumentElement();
        final NodeList elements = root.getElementsByTagName(parentElementName);

        if (elements.getLength() == 0) {
            return Collections.<String, String> emptyMap();
        }

        final Element parentElement = (Element) elements.item(0);
        final NodeList children = parentElement.getElementsByTagName("*");

        return convertElementsToNameValues(children);
    }

    /**
     * Get tag name of the root element in the document.
     *
     * @return String of tag name, or empty if not found
     */
    public String getRootTag() {
        return document.getDocumentElement().getTagName();
    }

    /**
     * Gets all child elements of the specified parent element in the document. Search is not limited to direct children, traverses all decendents. If
     * multiple elements of same name exist then the children of the first element will be returned.
     *
     * @param parentElementName
     *            the name of the parent element
     * @return Collection <code>Element</code>, will be empty if parent does not exist or has no children
     */
    public Collection<Element> getAllChildElements(final String parentElementName) {
        final Element root = document.getDocumentElement();
        final NodeList nodes = root.getElementsByTagName(parentElementName);

        if (nodes.getLength() == 0) {
            return Collections.<Element> emptyList();
        }

        final Element parentElement = (Element) nodes.item(0);
        final NodeList children = parentElement.getElementsByTagName("*");
        return convertNodeListToElements(children);
    }

    /**
     * Gets element of the specified name in the document. If multiple elements of the same name exist then the first element of that name will be
     * returned.
     *
     * @param elementName
     *            the name of the element
     * @return <code>Element</code> or null if no such element exists
     */
    public Element getElement(final String elementName) {
        final Collection<Element> elements = getAllElements(elementName);
        return elements.isEmpty() ? null : elements.iterator().next();
    }

    /**
     * Gets value of the first occurrence of element with specified name in the document.
     * <p>
     * The value of an element is taken as its text content, {@link Node#getTextContent()}.
     *
     * @param elementName
     *            the name of the element
     * @return element string value or null if no element found.
     */
    public String getElementValue(final String elementName) {
        final Collection<Element> elements = getAllElements(elementName);
        return elements.isEmpty() ? null : elements.iterator().next().getTextContent();
    }

    /**
     * Get all elements with the specified name in the document.
     *
     * @param elementName
     *            the name of the element
     * @return Collection of <code>Element </code> or empty collection if no elements found
     */
    public Collection<Element> getAllElements(final String elementName) {
        final Element root = document.getDocumentElement();
        final NodeList nodeList = root.getElementsByTagName(elementName);
        return convertNodeListToElements(nodeList);
    }

    /**
     * Find names of all schemas that are included or imported into the document
     *
     * @return Set containing names of included or imported schemas
     */
    public Set<String> getRelatedSchemas() {
        final Set<String> relatedSchemaNames = new HashSet<>();
        relatedSchemaNames.addAll(getSchemaNamesByTag(document, "xs:include"));
        relatedSchemaNames.addAll(getSchemaNamesByTag(document, "xs:import"));

        return relatedSchemaNames;
    }

    /**
     * Get the attribute value of the specified element name in the document.
     *
     * @param elementName
     *            the name of the element
     * @param attributeName
     *            the name of the attribute
     * @return attributeValue
     */
    public String getAttribute(final String elementName, final String attributeName, final String attributeDefaultValue) {
        final Element root = document.getDocumentElement();
        final NodeList elements = root.getElementsByTagName(elementName);

        final Element parentElement = (Element) elements.item(0);
        if (parentElement != null && parentElement.hasAttribute(attributeName)) {
                return parentElement.getAttribute(attributeName);
        } else {
            return attributeDefaultValue;
        }
    }

    /**
     * Gets all comments in the document which are direct in the root level. The comment value is taken as its text content,
     * {@link Node#getTextContent()}.
     *
     * @return List containing all comments
     */
    public List<String> getRootComments() {
        final NodeList childs = document.getChildNodes();
        final List<String> comments = new ArrayList<>();
        for (int i = 0; i < childs.getLength(); i++) {
            final Node child = childs.item(i);
            if (child instanceof Comment) {
                comments.add(((Comment) child).getTextContent());
            }
        }
        return comments;
    }

    /**
     * Get value of Metadata comment in the document which is in root level.
     *
     * @return String containing Metadata comment value, or empty if not found
     */
    public String getMetadataComment() {
        return getRootComments().stream()
                                .filter(comment -> comment.trim().split("\\s+", 2)[0].equals("Metadata"))
                                .findFirst()
                                .map(comment -> comment.replaceFirst("Metadata", ""))
                                .orElse("");
    }

    private static Set<String> getSchemaNamesByTag(final Document document, final String tagName) {
        final NodeList nodeList = document.getElementsByTagName(tagName);
        final Set<String> schemaNames = new HashSet<>();
        final int numberOfSchemas = nodeList.getLength();

        for (int i = 0; i < numberOfSchemas; i++) {
            schemaNames.add(nodeList.item(i).getAttributes().getNamedItem("schemaLocation").getTextContent());
        }
        return schemaNames;
    }

    private static Collection<Element> convertNodeListToElements(final NodeList nodeList) {
        final int numberOfElements = nodeList.getLength();
        final Collection<Element> elements = new ArrayList<>(numberOfElements);

        for (int i = 0; i < numberOfElements; i++) {
            elements.add((Element) nodeList.item(i));
        }
        return elements;
    }

    private static Map<String, String> convertElementsToNameValues(final NodeList children) {
        final Map<String, String> elementNameValues = new HashMap<>();

        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (child instanceof Element) {
                final String value = ((Element) child).getTextContent();
                elementNameValues.put(child.getNodeName(), value);
            }
        }
        return elementNameValues;
    }
}