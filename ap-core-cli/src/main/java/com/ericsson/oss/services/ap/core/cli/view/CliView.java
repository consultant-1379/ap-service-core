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
package com.ericsson.oss.services.ap.core.cli.view;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.core.cli.view.navigator.MetadataModelNavigator;
import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewMetadata;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;

/**
 * Builds a CLI view in the form of a {@link ResponseDto}. The view is constructed based on the view metadata retrieved from the metadata service.
 * Data must also be provided consisting of the data types and attributes that make up the view. The metadata will determine how the view is
 * constructed.
 */
public class CliView {

    @Inject
    private MetadataModelNavigator metadataModelNavigator;

    /**
     * Entry point method for handling all the <code>ViewMetadata</code> objects that make up a particular view.
     *
     * @param viewMetadata
     *            metadata will determine what the component will display
     * @param dataSource
     *            Contains the data types and attribute name value pairs that the metadata will use to construct the view
     * @return the view as defined in the metadata
     */
    public ResponseDto buildViewFromMetadata(final ViewMetadata viewMetadata, final List<?> dataSource) {
        if (dataSource.isEmpty()) {
            return new ResponseDto(Collections.<AbstractDto> emptyList());
        }

        final List<AbstractDto> cliView = metadataModelNavigator.constructView(viewMetadata, dataSource);
        return new ResponseDto(cliView);
    }
}
