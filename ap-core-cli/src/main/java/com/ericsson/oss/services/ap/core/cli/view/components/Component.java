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
package com.ericsson.oss.services.ap.core.cli.view.components;

import java.util.Collection;
import java.util.List;

import com.ericsson.oss.services.ap.core.metadata.cli.api.Metadata;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;

/**
 * Any class that constructs a CLI component should implement this interface.
 * <p>
 * A component constructs a particular type of CLI entity such as a line or a table. Each component is driven by a metadata type that describes what
 * the component should display. A datasource provides the data values that are to be displayed.
 *
 * @param <T>
 *            the metadata type used by the component
 */
public interface Component<T extends Metadata> { //NOSONAR EAP7 CDI libraries supports different way of handling generics.

    /**
     * Sets the metadata that this component will use to determine what will be displayed.
     *
     * @param metadata
     *            metadata will determine what the component will display
     */
    default void setComponentMetadata(final Metadata metadata) {}
    /**
     * Creates the component. The component could for example be a line or a table. The result is a collection of {@link AbstractDto} which contain
     * the constructed component as defined by the metadata.
     *
     * @param dataSource
     *            contains the data types and attribute name value pairs that the component will display. The metadata defines the parts of the data
     *            that are relevant to this component
     * @param dataType
     *            the data type that the component is displaying
     * @return collection of constructed AbstractDto elements that make up the component
     */
    Collection<AbstractDto> getAbstractDtos(final List<?> dataSource, final String dataType);

    /**
     * Returns the metadata that will be used to construct child components of this component.
     *
     * @return collection of metadata for child components
     */
    Collection<Metadata> getChildMetadata();
}
