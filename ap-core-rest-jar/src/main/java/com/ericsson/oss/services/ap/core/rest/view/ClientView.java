/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.view;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewMetadata;
import com.ericsson.oss.services.ap.core.rest.view.properties.data.ViewProperties;
import com.ericsson.oss.services.ap.core.rest.view.properties.data.ViewPropertiesResponse;
import com.ericsson.oss.services.ap.core.rest.view.properties.navigator.MetadataModelNavigator;

/**
 * Builds a REST Client view in the form of a {@link ViewPropertiesResponse}. The view is constructed based on the view metadata retrieved from the metadata service.
 * Data must also be provided consisting of the data types and attributes that make up the view. The metadata will determine how the view is
 * constructed.
 */
public class ClientView {

    @Inject
    private MetadataModelNavigator metadataModelNavigator;

    /**
     * Entry point method for handling all the <code>ViewMetadata</code> objects that make up a particular view.
     *
     * @param viewMetadata
     *            metadata will determine what the component displays
     * @param dataSource
     *            Contains the data types and attribute name value pairs that the metadata uses to construct the view
     * @return the view as defined in the metadata
     */
    public ViewPropertiesResponse buildViewFromMetadata(final ViewMetadata viewMetadata, final List<?> dataSource) {
        if (dataSource.isEmpty()) {
            return new ViewPropertiesResponse(Collections.<ViewProperties> emptyList());
        }

        final List<ViewProperties> restResponseViewData = metadataModelNavigator.constructView(viewMetadata, dataSource);
        return new ViewPropertiesResponse(restResponseViewData);
    }
}
