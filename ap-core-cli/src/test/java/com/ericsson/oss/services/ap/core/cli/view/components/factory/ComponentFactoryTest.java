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
package com.ericsson.oss.services.ap.core.cli.view.components.factory;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Iterator;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.core.cli.view.components.Component;
import com.ericsson.oss.services.ap.core.cli.view.components.LineComponent;
import com.ericsson.oss.services.ap.core.metadata.cli.api.LineMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.Metadata;

/**
 * Unit tests for {@link ComponentFactory}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ComponentFactoryTest {

    @SuppressWarnings("rawtypes")
    private final Component viewComponentBuilder = new LineComponent();

    @Mock
    private Instance<Component<? extends Metadata>> viewComponentBuilders;

    @Mock
    private Iterator<Component<? extends Metadata>> instanceIterator;

    @Mock
    private LineMetadata metadataToHandle;

    @InjectMocks
    private final ComponentFactory viewComponentFactory = new ComponentFactory();

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        when(viewComponentBuilders.iterator()).thenReturn(instanceIterator);
        when(instanceIterator.hasNext()).thenReturn(true).thenReturn(false);
        when(instanceIterator.next()).thenReturn(viewComponentBuilder);
    }

    @Test
    public void whenGettingViewComponent_thenFactoryReturnsComponentBuilderForMatchingMetadataType() {
        final Component<? extends Metadata> actualComponentBuilderReturned = viewComponentFactory.getViewComponent(metadataToHandle);
        assertThat("Incorrect handler returned", actualComponentBuilderReturned, instanceOf(LineComponent.class));
    }

    @Test
    public void whenGettingViewComonent_andNoComponentExists_thenNullIsReturned() {
        when(viewComponentBuilders.iterator()).thenReturn(Collections.<Component<?>> emptyIterator());
        final Component<? extends Metadata> actualComponentBuilderReturned = viewComponentFactory.getViewComponent(metadataToHandle);
        assertNull(actualComponentBuilderReturned);
    }
}
