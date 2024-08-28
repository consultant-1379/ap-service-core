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

/**
 * Interface for <code>TextRenderer</code> classes.
 */
public interface TextRenderer {

    /**
     * Renders a string of text for use in a view.
     *
     * @param objectToRender
     *            the object to render
     * @return the rendered object as a string
     */
    String render(final Object objectToRender);
}
