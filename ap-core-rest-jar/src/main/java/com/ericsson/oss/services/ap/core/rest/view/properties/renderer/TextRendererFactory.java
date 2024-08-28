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
package com.ericsson.oss.services.ap.core.rest.view.properties.renderer;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 * Factory for producing the correct {@link TextRenderer}. The factory contains all objects of type {@link TextRenderer}.
 */
public class TextRendererFactory {

    @Inject
    @Any
    private Instance<TextRenderer> textRenderers;

    /**
     * Returns the correct {@link TextRenderer} based on the renderer type.
     * <p>
     * For example, a new instance of {@link XmlExtensionRenderer} is retrieved if the {@link Renderer#type()} parameter is equal to
     * <b>xml</b>.
     *
     * @param rendererType
     *            the type of renderer to find
     * @return the matching text renderer instance, or null if none found
     */
    public TextRenderer getTextRenderer(final String rendererType) {
        for (final TextRenderer textRenderer : textRenderers) {
            final Class<?> textRendererClass = textRenderer.getClass();
            if (rendererType.equalsIgnoreCase(textRendererClass.getAnnotation(Renderer.class).type())) {
                return textRenderer;
            }
        }
        return null;
    }
}
