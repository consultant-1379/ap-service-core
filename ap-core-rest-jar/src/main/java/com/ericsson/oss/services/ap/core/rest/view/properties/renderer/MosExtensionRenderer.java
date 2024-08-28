/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.view.properties.renderer;

/**
 * A text renderer for mos script that simply adds a file extension to the text if it does not exist.
 */
@Renderer(type = "mosFile")
public class MosExtensionRenderer implements TextRenderer {

    @Override
    public String render(final Object textToRender) {
        if (((String) textToRender).endsWith(".mos")) {
            return (String) textToRender;
        }
        return textToRender + ".mos";
    }
}
