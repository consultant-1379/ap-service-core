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
package com.ericsson.oss.services.ap.core.metadata.cli.service.impl;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.core.metadata.cli.api.CliMetadataService;
import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.cache.MetadataCache;
import com.ericsson.oss.services.ap.core.metadata.cli.search.MetadataSearch;
import com.ericsson.oss.services.ap.core.metadata.cli.search.NodeTypeAndViewIdSearch;
import com.ericsson.oss.services.ap.core.metadata.cli.search.ViewIdSearch;

/**
 * This is the implementation of the metadata service. The service returns metadata {@link ViewMetadata} for various AP views in the CLI. The metadata
 * describes the content and layout of the CLI view. The attributes and data types that make up a view are contained in the metadata along with the
 * type of CLI structure to display the data in i.e. in a line, in a table etc.
 */
public class CliMetadataServiceImpl implements CliMetadataService {

    @Inject
    private MetadataCache modelCache;

    /**
     * Returns metadata for a given node type and view ID.
     *
     * @param nodeType
     *            the node type the view is requested for
     * @param viewId
     *            the ID of the view for which the metadata is requested for
     * @return the metadata describes the content and layout of the requested CLI view. The attributes and data types that make up a view are
     *         contained in the metadata along with the type of CLI structure to display the data in i.e. in a line, in a table etc
     */
    @Override
    public ViewMetadata getViewMetadata(final String nodeType, final String viewId) {
        return searchMetadata(new NodeTypeAndViewIdSearch(nodeType, viewId));
    }

    /**
     * Returns metadata for a given view ID. The first view with matching ID is returned irrespective of node type.
     *
     * @param viewId
     *            the ID of the view for which the metadata is requested for
     * @return the metadata describes the content and layout of the requested CLI view. The attributes and data types that make up a view are
     *         contained in the metadata along with the type of CLI structure to display the data in i.e. in a line, in a table etc
     */
    @Override
    public ViewMetadata getViewMetadata(final String viewId) {
        return searchMetadata(new ViewIdSearch(viewId));
    }

    /**
     * Returns contents of the cache as defined in the given <code>MetadataSearch</code>.
     *
     * @param MetadataSearch
     *            search behavior to apply to the cache contents
     * @return metadata for specific view
     */
    private ViewMetadata searchMetadata(final MetadataSearch search) {
        return search.execute(modelCache.getAllMetadata());
    }
}
