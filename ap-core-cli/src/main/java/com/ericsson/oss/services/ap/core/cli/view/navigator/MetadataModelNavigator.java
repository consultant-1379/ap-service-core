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
package com.ericsson.oss.services.ap.core.cli.view.navigator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.core.cli.view.components.Component;
import com.ericsson.oss.services.ap.core.cli.view.components.FilterableComponent;
import com.ericsson.oss.services.ap.core.cli.view.components.factory.ComponentFactory;
import com.ericsson.oss.services.ap.core.metadata.cli.api.FilterableMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.Metadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewItemMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewMetadata;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;

/**
 * Navigates through the metadata model and builds components as specified in the model.
 */
public class MetadataModelNavigator {

    @Inject
    private ComponentFactory viewComponentFactory;

    private List<AbstractDto> abstractDtos = new ArrayList<>();

    /**
     * Navigates through the metadata and constructs, via <code>Components</code>, and retrieves the {@link AbstractDto} that will render the view in
     * the CLI.
     *
     * @param viewMetadata
     *            metadata will determine what attributes will be displayed and how they will be displayed
     * @param dataSource
     *            contains the data types and attribute name value pairs that the metadata will use to construct the view
     * @return the view as defined in the metadata
     */
    public List<AbstractDto> constructView(final ViewMetadata viewMetadata, final List<?> dataSource) {
        abstractDtos = new ArrayList<>();
        for (final ViewItemMetadata viewItemMetadata : viewMetadata.getViewItems()) {
            final String dataType = viewItemMetadata.getType();
            navigateMetadataModel(viewItemMetadata.getViewComponentsMetadata(), dataSource, dataType);
        }

        return abstractDtos;
    }

    private void navigateMetadataModel(final Collection<Metadata> metadataList, final List<?> dataSource, final String dataType) {
        for (final Metadata metadata : metadataList) {
            final Component<Metadata> viewComponent = viewComponentFactory.getViewComponent(metadata);

            if (viewComponent instanceof FilterableComponent) {
                final FilterableMetadata fm = (FilterableMetadata) metadata;
                abstractDtos.addAll(((FilterableComponent) viewComponent).getAbstractDtos(dataSource, dataType, fm.getFilter()));
            } else {
                abstractDtos.addAll(viewComponent.getAbstractDtos(dataSource, dataType));
            }

            final Collection<Metadata> childMetadata = viewComponent.getChildMetadata();
            navigateMetadataModel(childMetadata, dataSource, dataType);
        }
    }
}
