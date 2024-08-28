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
package com.ericsson.oss.services.ap.core.rest.view.properties.data;

import org.apache.commons.lang.StringUtils;

/**
 * Formats a label or heading string into a single word key value. For example e.g. "Integration Artifacts" to "IntegrationArtifacts"
 */
public abstract class MetadataTextFormatter {

    private static final String TYPE_FORMAT_NAME = "type:";
    private static final String ATTRIBUTES_FORMAT_NAME = "attributes.";

    private MetadataTextFormatter () {}
    /**
     * Format a text string into a single word string :
     * <p>
     * Remove spaces between words
     * e.g. "Integration Artifacts" to "IntegrationArtifacts"
     *
     * @param textToFormat  text String to format
     * @return  formatted string
     */
    public static String formatHeadingText(final String textToFormat){
        return StringUtils.deleteWhitespace(textToFormat);
    }

    /**
     * Format a text string for type containing a colon
     * <p>
     * Remove text before and including :
     * e.g. "type:Node" to "Node"
     *
     * @param textToFormat  text String to format
     * @return  formatted string
     */
    public static String formatTypeName(final String textToFormat){
        if(textToFormat.contains(TYPE_FORMAT_NAME)) {
            return StringUtils.remove(textToFormat, TYPE_FORMAT_NAME);
        }

        return textToFormat;
    }

    /**
     * Format a text string for type containing a full stop
     * <p>
     * Remove text before and including .
     * e.g. "attributes.nodeName" to "nodeName"
     *
     * @param textToFormat  text String to format
     * @return  formatted string
     */
    public static String formatAttributeName(final String textToFormat){
        if(textToFormat.contains(ATTRIBUTES_FORMAT_NAME)) {
            return StringUtils.remove(textToFormat, ATTRIBUTES_FORMAT_NAME);
        }

        return textToFormat;
    }
}
