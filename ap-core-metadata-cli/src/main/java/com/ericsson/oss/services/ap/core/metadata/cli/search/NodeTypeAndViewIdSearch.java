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
package com.ericsson.oss.services.ap.core.metadata.cli.search;

import java.util.Collection;

import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.model.CliViews;

/**
 * Defines the search behavior for finding {@link ViewMetadata} matching the given view ID and node type. Each given <code>NodeCliMetadata</code> is
 * searched to find a view that matches the given view ID and node type.
 */
public class NodeTypeAndViewIdSearch extends MetadataSearch {

    private final String nodeType;
    private final String viewId;

    /**
     * Constructor sets the search criteria.
     *
     * @param nodeType
     *            find the {@link ViewMetadata} that matches this node type and the following viewID
     * @param viewId
     *            find the {@link ViewMetadata} that matches this ID
     */
    public NodeTypeAndViewIdSearch(final String nodeType, final String viewId) {
        this.nodeType = nodeType;
        this.viewId = viewId;
    }

    /**
     * Finds the {@link ViewMetadata} matching the given view ID and node type. Each <code>NodeCliMetadata</code> is searched until a view that
     * matches the given view ID and node type is found.
     *
     * @param metadataForAllNodeTypes
     *            metadata for all node types
     * @return the {@link ViewMetadata} matching the given filter behavior.
     */
    @Override
    public ViewMetadata execute(final Collection<CliViews> metadataForAllNodeTypes) {
        for (final CliViews cliMetadata : metadataForAllNodeTypes) {
            if (cliMetadata.getNamespace().equalsIgnoreCase(nodeType)) {
                return getViewMetadata(viewId, cliMetadata);
            }
        }

        return null;
    }
}
