/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.view.navigator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.core.cli.view.components.Component;
import com.ericsson.oss.services.ap.core.cli.view.components.factory.ComponentFactory;
import com.ericsson.oss.services.ap.core.metadata.cli.api.Metadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewItemMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewMetadata;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.LineDto;

/**
 * Unit tests for {@link MetadataModelNavigator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class MetadataModelNavigatorTest {

    private static final String VIEW_ITEM_DATA_TYPE = "dataType";
    private static final List<?> DATASOURCE = Collections.emptyList();

    @Mock
    private Component<Metadata> component;

    @Mock
    private ComponentFactory componentFactory;

    @Mock
    private Metadata metadata;

    @Mock
    private ViewMetadata viewMetadata;

    @Mock
    private ViewItemMetadata viewItem;

    @InjectMocks
    private MetadataModelNavigator metadataModelNavigator;

    @Before
    public void setUp() {
        when(viewItem.getType()).thenReturn(VIEW_ITEM_DATA_TYPE);
        final List<Metadata> viewMetadata = new ArrayList<>();
        viewMetadata.add(metadata);
        when(viewItem.getViewComponentsMetadata()).thenReturn(viewMetadata);
    }

    @Test
    public void whenConstructView_thenReturnDtos() {
        final List<ViewItemMetadata> viewItems = new ArrayList<>();
        viewItems.add(viewItem);
        when(viewMetadata.getViewItems()).thenReturn(viewItems);
        when(componentFactory.getViewComponent(metadata)).thenReturn(component);
        final List<AbstractDto> componentDtos = new ArrayList<>();
        componentDtos.add(new LineDto());
        when(component.getAbstractDtos(DATASOURCE, VIEW_ITEM_DATA_TYPE)).thenReturn(componentDtos);

        final List<AbstractDto> result = metadataModelNavigator.constructView(viewMetadata, DATASOURCE);

        assertFalse(result.isEmpty());
    }

    @Test
    public void whenConstructView_andMetadataHasNoViews_thenEmptyListIsReturned() {
        when(viewMetadata.getViewItems()).thenReturn(Collections.<ViewItemMetadata> emptyList());
        final List<AbstractDto> result = metadataModelNavigator.constructView(viewMetadata, DATASOURCE);
        assertTrue(result.isEmpty());
    }
}
