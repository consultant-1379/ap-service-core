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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.w3c.dom.Element;

import com.ericsson.oss.services.ap.api.schema.SchemaService;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;

/**
 * Retrieves required artifacts from the artifacts schema.
 */
public class ArtifactTypeReader {

    private static final String ARTIFACTS = "Artifacts";

    @Inject
    private Logger logger;

    @Inject
    private SchemaService schemaService;

    /**
     * Get all artifact types.
     *
     * @param nodeType
     *            the node type
     * @param nodeIdentifier
     *            the node identifier
     * @return a list of names of all artifact types
     */
    public List<String> getAllArtifactTypes(final String nodeType, final String nodeIdentifier) {
        final String schemaContent = readSchemaAsString(nodeType, nodeIdentifier);
        final DocumentReader documentReader = new DocumentReader(schemaContent);
        final Collection<Element> elements = documentReader.getAllElements("xs:element");
        final List<String> artifacts = new ArrayList<>();

        for (final Element element : elements) {
            if (isStringElement(element)) {
                final String artifactType = element.getAttribute("name");
                artifacts.add(artifactType);
            }
        }

        return artifacts;
    }

    private static boolean isStringElement(final Element element) {
        return ("xs:string").equals(element.getAttribute("type"));
    }

    private String readSchemaAsString(final String nodeType, final String nodeIdentifier) {
        try {
            final String artifactType = nodeType.toLowerCase(Locale.US) + ARTIFACTS;
            final byte[] schema = schemaService.readSchema(nodeType, nodeIdentifier, artifactType).getData();
            return new String(schema, StandardCharsets.UTF_8);
        } catch (final Exception e) {
            logger.error("Failed to read schema: {}", e.getMessage(), e);
            throw e;
        }
    }
}
