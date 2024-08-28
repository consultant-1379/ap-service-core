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
package com.ericsson.oss.services.ap.common.schema.cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Collection of schema or sample artifacts for a single node type and identifier.
 */
public class SchemaArtifacts {

    private final String nodeType;

    private final String nodeIdentifier;

    private final Map<String, String> artifactLocationsByType = new HashMap<>();

    /**
     * Construct Artifacts for a single node type and identifier
     */
    public SchemaArtifacts(final String nodeType, final String nodeIdentifier) {
        this.nodeType = nodeType;
        this.nodeIdentifier = nodeIdentifier;
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getNodeIdentifier() {
        return nodeIdentifier;
    }

    public void addArtifact(final String type, final String location) {
        artifactLocationsByType.put(type, location);
    }

    public Collection<String> getArtifactTypes() {
        return artifactLocationsByType.keySet();
    }

    public String getArtifactLocation(final String type) {
        return artifactLocationsByType.get(type);
    }

    @Override
    public int hashCode() {
        return nodeType.hashCode() + nodeIdentifier.hashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }

        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }

        final SchemaArtifacts otherSchemaArtifacts = (SchemaArtifacts) other;

        return otherSchemaArtifacts.getNodeType().equals(nodeType) && otherSchemaArtifacts.getNodeIdentifier().equals(nodeIdentifier);
    }

}
