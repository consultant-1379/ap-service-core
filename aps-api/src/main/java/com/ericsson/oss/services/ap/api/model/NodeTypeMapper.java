/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.model;

/**
 * Provides mappings from node type to Auto Provisioning internal representation.
 */
public interface NodeTypeMapper {

    /**
     * Returns the internal representation for a node type. If no internal representation is found, a default value is returned.
     *
     * @param nodeType
     *            external node type
     * @return a {@link String} representing the mapped internal AP type
     */
    String getInternalRepresentationFor(final String nodeType);

    /**
     * Returns the internal EJB EService qualifier for a node type.
     *
     * @param nodeType
     *            external node type
     * @return a {@link String} representing the mapped internal AP type
     */
    String getInternalEjbQualifier(final String nodeType);

    /**
     * Returns the correct DPS namespace for a node type.
     *
     * @param nodeType
     *            external node type
     * @return a {@link String} representing the AP namespace
     */
    String getNamespace(final String nodeType);

    /**
     * Transform Fronthaul6k node type representation to OSS model format if there be any.
     *
     * @param nodeType
     *            node type
     * @return a {@link String} representing the OSS model format for Fronthaul6k node type if it exists
     */
    String toOssRepresentation(final String nodeType);

    /**
     * Transform Fronthaul6k node type representation to AP model format if there be any.
     *
     * @param nodeType
     *            node type
     * @return a {@link String} representing the AP model format for Fronthaul6k node type if it exists
     */
    String toApRepresentation(final String nodeType);
}
