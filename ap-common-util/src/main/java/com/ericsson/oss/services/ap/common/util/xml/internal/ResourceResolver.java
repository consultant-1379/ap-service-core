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

import java.io.ByteArrayInputStream;
import java.util.List;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import com.ericsson.oss.services.ap.api.schema.SchemaData;

/**
 * Resolve the location of all the schema resources.
 */
public class ResourceResolver implements LSResourceResolver {

    private final List<SchemaData> schemaInfo;

    /**
     * Schemas resource resolver.
     *
     * @param schemaInfo
     *            a list of the schemas
     */
    public ResourceResolver(final List<SchemaData> schemaInfo) {
        this.schemaInfo = schemaInfo;
    }

    @Override
    public LSInput resolveResource(final String type, final String namespaceUri, final String publicId, final String systemId, final String baseUri) {
        final String systemIdName = systemId.split(".xsd")[0];

        for (final SchemaData schemaData : schemaInfo) {
            if (schemaData.getName().equals(systemIdName)) {
                return new XsdInput(publicId, systemId, new ByteArrayInputStream(schemaData.getData()));
            }
        }
        return null;
    }
}
