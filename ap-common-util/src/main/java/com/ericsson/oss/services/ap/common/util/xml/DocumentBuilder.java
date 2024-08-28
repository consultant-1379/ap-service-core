/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.util.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ericsson.oss.services.ap.common.util.xml.exception.XmlException;

/**
 * Class provides methods to obtain {@link Document} instances from XML document and to transform {@link Document} instances.
 */
public final class DocumentBuilder {

    private DocumentBuilder() {

    }

    /**
     * Get a DOM Document from an XML String (i.e. the XML content as a string).
     *
     * @param xmlString
     *            the XML content as a string
     * @return a DOM Document
     * @throws XmlException
     *             thrown on invalid input XML
     */
    public static Document getDocument(final String xmlString) {

        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            final javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xmlString)));
        } catch (final ParserConfigurationException | SAXException | IOException e) {
            throw new XmlException(e.getMessage(), e);
        }
    }

    /**
     * Get a DOM Document from an {@link InputStream}.
     *
     * @param input
     *            an {@link InputStream} contain the XML
     * @return a DOM Document
     * @throws XmlException
     *             thrown on invalid input XML
     */
    public static Document getDocument(final InputStream input) {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final javax.xml.parsers.DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(input);
        } catch (final ParserConfigurationException | SAXException | IOException e) {
            throw new XmlException(e.getMessage(), e);
        }
    }

    /**
     * Returns an XML String (i.e. the XML content as a string) from a DOM Document.
     *
     * @param document
     *            an input DOM Document
     * @return the XML content as a string
     * @throws XmlException
     *             thrown if DOM Document is invalid
     */
    public static String getDocumentAsString(final Document document) {
        Transformer transformer;
        try {
            final TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            // Initialize StreamResult with File object to save to file
            final StreamResult result = new StreamResult(new StringWriter());
            final DOMSource source = new DOMSource(document);
            transformer.transform(source, result);
            return result.getWriter().toString();
        } catch (final TransformerException e) {
            throw new XmlException(e.getMessage(), e);
        }
    }
}
