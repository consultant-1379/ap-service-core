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
 * Base class for all metadata search implementations. All extending classes must implement the <code>execute()</code> method which will define the
 * search behavior.
 * <p>
 * Also contains functionality common to all search implementations.
 */
public abstract class MetadataSearch {

    /**
     * Searches all the views defined in {@link CliViews} to find matching {@link ViewMetadata} for a given view ID.
     *
     * @param viewId
     *            view ID the that metadata is required for
     * @param cliViews
     *            contains metadata for all view IDs
     * @return metadata for the view ID
     */
    protected ViewMetadata getViewMetadata(final String viewId, final CliViews cliViews) {
        for (final ViewMetadata viewMetadata : cliViews.getViews()) {
            if (viewMetadata.getId().equals(viewId)) {
                return viewMetadata;
            }
        }

        return null;
    }

    /**
     * All extending classes must implement this method which will define the actual search behavior.
     *
     * @param metadataForAllNodeTypes
     *            the metadata the search will be executed against
     * @return the {@link ViewMetadata} matching the given search behavior
     */
    public abstract ViewMetadata execute(final Collection<CliViews> metadataForAllNodeTypes);
}
