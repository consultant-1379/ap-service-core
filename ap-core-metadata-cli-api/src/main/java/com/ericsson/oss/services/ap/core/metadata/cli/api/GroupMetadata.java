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
 * Defines the metadata that is provided for a group. A group as a logical way of arranging other metadata elements such as lines or tables. This
 * metadata is accessed from {@link ViewItemMetadata#getViewComponentsMetadata()}.
 */
public interface GroupMetadata extends Metadata {

    /**
     * Returns the heading associated with this group.
     *
     * @return the heading or null if none
     */
    String getHeading();

    /**
     * Returns the line and/or table metadata elements contained within the group.
     *
     * @return <code>LineMetadata</code> and/or <code>TableMetaddata</code> elements contained within the group
     */
    List<Metadata> getViewComponentsMetadata();
}
