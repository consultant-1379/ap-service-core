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
package com.ericsson.oss.services.ap.core.rest.view.properties.components;

import java.util.Collection;
import java.util.List;

import com.ericsson.oss.services.ap.core.metadata.cli.api.Metadata;
import com.ericsson.oss.services.ap.core.rest.view.properties.data.ViewProperties;

/**
 * Any class that constructs a client view component should implement this interface.
 * <p>
 * A component constructs a particular type of client entity such as a table. The component models for example the
 * Group or Table defined in the metadata xml for the view. Each component is driven by a metadata type that describes what
 * the component should display. A datasource provides the data values that are to be displayed.
 *
 * @param <T>
 *            the metadata type used by the component
 */
public interface Component<T extends Metadata> {

    /**
     * Sets the metadata that this component uses to determine what is displayed.
     *
     * @param metadata
     *            metadata determines what the component displays
     */
    void setComponentMetadata(final Metadata metadata);

    /**
     * Creates the component. The component could for example be a table. The result is a collection of {@link ViewProperties} which contain
     * the constructed details component as defined by the metadata.
     *
     * @param dataSource
     *            contains the data types and attribute name value pairs that the component displays. The metadata defines the parts of the data
     *            that are relevant to this component
     * @param dataType
     *            the data type that the component is displaying
     @param moStruct
      *            the data MO struct that the component is displaying
     * @return collection of constructed ViewProperties elements that make up the component. Contains the data that is sent to the client.
     */
    Collection<ViewProperties> getClientDtos(final List<?> dataSource, final String dataType, final String moStruct);

    /**
     * Returns the metadata that is used to construct child components of this component.
     *
     * @return collection of metadata for child components
     */
    Collection<Metadata> getChildMetadata();
}
