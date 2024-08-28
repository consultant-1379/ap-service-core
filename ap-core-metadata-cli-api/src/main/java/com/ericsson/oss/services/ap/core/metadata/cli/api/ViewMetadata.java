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

import java.util.List;

/**
 * This is the entry point for clients needing CLI view metadata. Defines the metadata that is provided for a view. A view as a logical way of
 * arranging other metadata elements to provide the complete view to the end user. A view could be for example 'view all projects'.
 */
public interface ViewMetadata extends Metadata {

    /**
     * Returns the unique ID for a particular view.
     *
     * @return the view ID
     */
    String getId();

    /**
     * Returns the metadata for all the view items that make up this particular view.
     *
     * @return {@link ViewItemMetadata} elements that make up this particular view
     */
    List<ViewItemMetadata> getViewItems();
}
