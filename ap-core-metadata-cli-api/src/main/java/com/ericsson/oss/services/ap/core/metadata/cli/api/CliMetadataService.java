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
package com.ericsson.oss.services.ap.core.metadata.cli.api;

/**
 * This service returns metadata for various AP views in the CLI. The metadata describes the content and layout of the CLI view. The attributes and
 * data types that make up a view are contained in the metadata along with the type of CLI structure to display the data in i.e. in a line, in a table
 * etc.
 */
public interface CliMetadataService {

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
    ViewMetadata getViewMetadata(final String nodeType, final String viewId);

    /**
     * Returns metadata for a given view ID. The first view with matching ID is returned irrespective of node type.
     *
     * @param viewId
     *            the ID of the view for which the metadata is requested for
     * @return the metadata describes the content and layout of the requested CLI view. The attributes and data types that make up a view are
     *         contained in the metadata along with the type of CLI structure to display the data in i.e. in a line, in a table etc
     */
    ViewMetadata getViewMetadata(final String viewId);
}
