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
 * Defines the metadata that is provided for a view item. A view item is a data source from which data can be extracted for the view. For example the
 * data source could be an MO from the AP model. This metadata is accessed from {@link ViewMetadata#getViewItems()}.
 */
public interface ViewItemMetadata extends Metadata {

    /**
     * Returns the data source type i.e. could be an MO from the AP model of type Project, Node etc.
     *
     * @return the data source type
     */
    String getType();

    /**
     * Returns the MO struct name. E.g. "NodeLocation" struct in the Node MO.
     *
     * @return the struct name of an MO
     */
    String getMoStruct();

    /**
     * Returns the group, line and/or table metadata elements that the data source applies to.
     *
     * @return a {@link List} of {@link Metadata} elements, returned in the order that they are defined in the metadata XML
     */
    List<Metadata> getViewComponentsMetadata();
}
