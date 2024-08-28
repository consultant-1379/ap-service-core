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
package com.ericsson.oss.services.ap.core.rest.view.properties.navigator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.core.metadata.cli.api.FilterableMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.Metadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewItemMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewMetadata;
import com.ericsson.oss.services.ap.core.rest.view.properties.components.Component;
import com.ericsson.oss.services.ap.core.rest.view.properties.components.FilterableComponent;
import com.ericsson.oss.services.ap.core.rest.view.properties.components.factory.ComponentFactory;
import com.ericsson.oss.services.ap.core.rest.view.properties.data.ViewProperties;

/**
 * Navigates through the metadata model and builds components as specified in the model.
 */
public class MetadataModelNavigator {

    @Inject
    private ComponentFactory viewComponentFactory;

    private List<ViewProperties> viewProperties = new ArrayList<>();

    /**
     * Navigates through the metadata and constructs, via <code>Components</code>, and retrieves the {@link ViewProperties} objects that render
     * the view in the CLI.
     *
     * @param viewMetadata
     *            metadata determines what attributes are displayed and how they will be displayed
     * @param dataSource
     *            contains the data types and attribute name value pairs that the metadata will use to construct the view
     * @return the view as defined in the metadata
     */
    public List<ViewProperties> constructView(final ViewMetadata viewMetadata, final List<?> dataSource) {
        viewProperties = new ArrayList<>();
        for (final ViewItemMetadata viewItemMetadata : viewMetadata.getViewItems()) {
            final String dataType = viewItemMetadata.getType();
            final String moStruct = viewItemMetadata.getMoStruct();
            navigateMetadataModel(viewItemMetadata.getViewComponentsMetadata(), dataSource, dataType, moStruct);
        }

        return viewProperties;
    }

    private void navigateMetadataModel(final Collection<Metadata> metadataList, final List<?> dataSource, final String dataType, final String moStruct) {
        for (final Metadata metadata : metadataList) {
            final Component<Metadata> viewComponent = viewComponentFactory.getViewComponent(metadata);

            if (viewComponent instanceof FilterableComponent) {
                // Table in MetaData view
                final FilterableMetadata fm = (FilterableMetadata) metadata;
                viewProperties.addAll(((FilterableComponent) viewComponent).getClientDtos(dataSource, dataType, moStruct, fm.getFilter()));
            } else {
                // Group in MetaData view
                viewProperties.addAll(viewComponent.getClientDtos(dataSource, dataType, moStruct));
            }

            final Collection<Metadata> childMetadata = viewComponent.getChildMetadata();
            navigateMetadataModel(childMetadata, dataSource, dataType, moStruct);
        }
    }
}
