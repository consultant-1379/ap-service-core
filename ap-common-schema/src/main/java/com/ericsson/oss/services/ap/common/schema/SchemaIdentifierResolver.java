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
package com.ericsson.oss.services.ap.common.schema;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.schema.cache.SchemaArtifacts;
import com.ericsson.oss.services.ap.common.util.string.MimVersionComparator;

/**
 * Resolves schemas which are compatible with specific node identifiers.
 */
public class SchemaIdentifierResolver {

    @Inject
    private ModelReader modelReader;

    private final MimVersionComparator mimVersionComparator = new MimVersionComparator();

    /**
     * Finds the schema artifacts which are compatible with the specified node identifier.
     * <p>
     * If schemas exist with a identifier exactly matching the specified identifier then these will be returned. If no exact match found then the
     * earliest schemas will be returned.
     *
     * @param ossModelIdentity
     *            the node identifier, may be null or empty in which case the default schemas will be assumed
     * @param schemasForNodeType
     *            schemas for all identifiers of a single node type
     * @param nodeType
     *            the node type
     * @return the schema artifacts
     */
    public SchemaArtifacts findCompatibleSchemasForNodeIdentifier(final String ossModelIdentity,
            final List<SchemaArtifacts> schemasForNodeType, final String nodeType) {

        sortByNodeIdentifier(schemasForNodeType);
        SchemaArtifacts closestIdentifierMatch = schemasForNodeType.get(0);

        if (defaultSchemasRequested(ossModelIdentity)) {
            return closestIdentifierMatch;
        }

        final String mimVersionForOmi = modelReader.getMimVersionMappedToOssModelIdentity(nodeType, ossModelIdentity);
        final String convertedMimVersion = MimVersionComparator.convert(mimVersionForOmi); // If mappedMimVersion in format 7.1.260, then Convert to G.1.260

        for (final SchemaArtifacts schemasForNodeIdentifier : schemasForNodeType) {

            final String currentMimVersion = schemasForNodeIdentifier.getNodeIdentifier();

            final int result = mimVersionComparator.compare(convertedMimVersion, currentMimVersion);

            if (result == 0) {
                return schemasForNodeIdentifier;
            } else if (result < 0) {
                return closestIdentifierMatch;
            } else {
                closestIdentifierMatch = schemasForNodeIdentifier;
            }
        }
        return closestIdentifierMatch;
    }

    private static boolean defaultSchemasRequested(final String requestedNodeIdentifier) {
        return StringUtils.isBlank(requestedNodeIdentifier);
    }

    private static void sortByNodeIdentifier(final List<SchemaArtifacts> artifacts) {
        Collections.sort(artifacts, new SchemaIdentifierComparator());
    }
}
