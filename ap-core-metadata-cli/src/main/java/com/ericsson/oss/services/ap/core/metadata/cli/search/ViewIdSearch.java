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
 * Defines the search behavior for finding {@link ViewMetadata} matching the given view ID.
 */
public class ViewIdSearch extends MetadataSearch {

    private final String viewId;

    /**
     * Constructor sets the search criteria.
     *
     * @param viewId
     *            find the {@link ViewMetadata} that matches this ID
     */
    public ViewIdSearch(final String viewId) {
        this.viewId = viewId;
    }

    /**
     * Finds the {@link ViewMetadata} matching the given view ID. Each {@link CliViews} is searched to find a view that matches the given view ID. The
     * first {@link ViewMetadata} matching the view ID is returned.
     *
     * @return the first {@link ViewMetadata} matching the given view ID
     */
    @Override
    public ViewMetadata execute(final Collection<CliViews> metadataForAllNodeTypes) {
        for (final CliViews cliMetadata : metadataForAllNodeTypes) {
            return getViewMetadata(viewId, cliMetadata);
        }

        return null;
    }
}
