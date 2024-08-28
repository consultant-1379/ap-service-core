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

import java.util.List;

/**
 * Defines the metadata that is provided for a table. A table element represents a table in the CLI. This metadata is accessed from
 * {@link ViewItemMetadata#getViewComponentsMetadata()}.
 */
public interface TableMetadata extends Metadata, FilterableMetadata {

    /**
     * Returns the style of table required.
     *
     * @return style of table required or null if default style
     */
    String getStyle();

    /**
     * Returns the heading of table required.
     *
     * @return table heading or null if none.
     */
    String getHeading();

    /**
     * Returns the metadata for the attributes that make up the table columns.
     *
     * @return the {@link AttributeMetadata} for the table columns
     */
    List<AttributeMetadata> getAttributes();
}
