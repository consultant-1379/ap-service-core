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
package com.ericsson.oss.services.ap.core.cli.view.renderer;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link TextRendererFactory}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TextRendererFactoryTest {

    @Mock
    private Instance<Object> instance;

    private List<Object> textRenderers;

    private final CamelCaseTextSplitter camelCaseTextSplitter = new CamelCaseTextSplitter();

    @InjectMocks
    private final TextRendererFactory rendererFactory = new TextRendererFactory();

    @Before
    public void setUp() {
        textRenderers = new ArrayList<>();
        textRenderers.add(camelCaseTextSplitter);
        when(instance.iterator()).thenReturn(textRenderers.iterator());
    }

    @Test
    public void when_renderer_type_is_lower_case_camel_then_factory_returns_camelcasetextsplitter() {
        final TextRenderer renderer = rendererFactory.getTextRenderer("camel");
        assertThat("Correct Renderer returned", renderer, instanceOf(CamelCaseTextSplitter.class));
    }

    @Test
    public void when_renderer_type_is_upper_case_camel_then_factory_returns_camelcasetextsplitter() {
        final TextRenderer renderer = rendererFactory.getTextRenderer("CAMEL");
        assertThat("Correct Renderer returned", renderer, instanceOf(CamelCaseTextSplitter.class));
    }

    @Test
    public void when_renderer_type_is_unknown_then_factory_returns_null() {
        final TextRenderer renderer = rendererFactory.getTextRenderer("unknown");
        assertNull(renderer);
    }
}
