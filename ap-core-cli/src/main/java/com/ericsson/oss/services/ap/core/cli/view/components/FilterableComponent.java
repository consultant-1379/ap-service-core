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

import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;

public interface FilterableComponent {

    /**
     * Creates the component. The component could for example be a Line or a Table etc. The result is a collection of <code>AbstractDto</code> which
     * contain the constructed component as defined by the Metadata.
     *
     * @param dataSource
     *            contains the data types and attribute name value pairs that the component will display. The metadata defines the parts of the data
     *            that are relevant to this component
     * @param dataType
     *            the data type that the component is displaying
     * @param filter
     *            a filter for the data
     * @return collection of constructed AbstractDto elements that make up the component
     */
    Collection<AbstractDto> getAbstractDtos(final List<?> dataSource, final String dataType, final String filter);
}
