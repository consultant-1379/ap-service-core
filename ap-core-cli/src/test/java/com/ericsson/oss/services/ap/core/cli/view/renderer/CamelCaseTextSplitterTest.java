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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Unit tests for {@link CamelCaseTextSplitter}.
 */
public class CamelCaseTextSplitterTest {

    private final CamelCaseTextSplitter camelCaseTextSplitter = new CamelCaseTextSplitter();

    @Test
    public void when_text_splitter_renders_camelcasetext_then_the_first_letter_is_capitalized_and_a_space_is_put_before_the_second_capital_letter() {
        final String renderedText = camelCaseTextSplitter.render("siteBasic");
        assertEquals("Site Basic", renderedText);
    }

    @Test
    public void when_text_splitter_renders_lower_case_text_then_first_letter_is_capitalized_only() {
        final String renderedText = camelCaseTextSplitter.render("configuration");
        assertEquals("Configuration", renderedText);
    }

    @Test
    public void when_text_splitter_renders_null_then_null_is_returned() {
        final String renderedText = camelCaseTextSplitter.render(null);
        assertNull(renderedText);
    }
}
