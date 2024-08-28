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

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

import com.ericsson.oss.services.ap.api.schema.SchemaData;
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaAccessException;
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaValidationException;
import com.ericsson.oss.services.ap.common.util.xml.internal.ResourceResolver;

/**
 * Used to validate XML files against XSD schemas.
 */
public class XmlValidator {

    private final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    /**
     * Validate XML content against an XSD (Schema).
     *
     * @param xml
     *            the XML content to be validated
     * @param xsd
     *            the XSD to validate against as byte[]
     * @throws SchemaAccessException
     *             is there is an error reading the schema
     * @throws SchemaValidationException
     *             if there is an error validating the schema
     */
    public void validateAgainstSchema(final String xml, final byte[] xsd) {
        try {
            final Schema schema = factory.newSchema(new StreamSource(new ByteArrayInputStream(xsd)));
            final Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new ByteArrayInputStream(xml.getBytes())));
        } catch (final SAXException e) {
            throw new SchemaValidationException(e.getMessage(), e);
        } catch (final Exception e) {
            throw new SchemaAccessException(e.getMessage(), e);
        }
    }

    /**
     * Validate XML content against an XSD (Schema).
     *
     * @param xml
     *            the XML content to be validated
     * @param schemaComponents
     *            the XSD is composed of a list of XSD components
     * @throws SchemaAccessException
     *             is there is an error reading the schema
     * @throws SchemaValidationException
     *             if there is an error validating the schema
     */
    public void validateAgainstSchema(final String xml, final List<SchemaData> schemaComponents) {
        try {
            factory.setResourceResolver(new ResourceResolver(schemaComponents));
            final Schema schema = factory.newSchema(getXsdSources(schemaComponents));
            final Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new ByteArrayInputStream(xml.getBytes())));
        } catch (final SAXException e) {
            throw new SchemaValidationException(e.getMessage(), e);
        } catch (final Exception e) {
            throw new SchemaAccessException(e.getMessage(), e);
        }
    }

    private static Source[] getXsdSources(final List<SchemaData> schemas) {
        final Source[] sources = new Source[schemas.size()];
        int i = 0;
        for (final SchemaData schemaData : schemas) {
            sources[i] = new StreamSource(new ByteArrayInputStream(schemaData.getData()));
            i++;
        }
        return sources;
    }
}
