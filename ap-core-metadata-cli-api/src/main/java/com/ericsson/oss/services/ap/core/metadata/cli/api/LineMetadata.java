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
package com.ericsson.oss.services.ap.core.metadata.cli.api;

/**
 * Defines the metadata that is provided for a line element. A line element represents a a single line in the CLI. This metadata is accessed from
 * {@link ViewItemMetadata#getViewComponentsMetadata()}.
 */
public interface LineMetadata extends Metadata {

    /**
     * Returns the attribute metadata for the line.
     *
     * @return the attribute metadata
     */
    AttributeMetadata getAttribute();

    /**
     * Returns the style of line required.
     *
     * @return style of line required or null if default style
     */
    String getStyle();
}
