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

import org.apache.commons.lang.StringUtils;

/**
 * Renderer class to capitalise the first letter of a string of text and put a space before subsequent capital letters.
 */
@Renderer(type = "camel")
public class CamelCaseTextSplitter implements TextRenderer {

    /**
     * Capitalises the first letter in a string of text and puts a space before before subsequent capital letters.
     *
     * @param camelCaseText
     *            the text to capitalise and split
     * @return the rendered text
     */
    @Override
    public String render(final Object camelCaseText) {
        final String[] splitCamelCaseTextAsStringArray = StringUtils
                .splitByCharacterTypeCamelCase((camelCaseText == null) ? null : camelCaseText.toString());
        final String jointCamelCaseTextWithSpaceBeforeEachCapital = StringUtils.join(splitCamelCaseTextAsStringArray, ' ');
        return StringUtils.capitalize(jointCamelCaseTextWithSpaceBeforeEachCapital);
    }
}
