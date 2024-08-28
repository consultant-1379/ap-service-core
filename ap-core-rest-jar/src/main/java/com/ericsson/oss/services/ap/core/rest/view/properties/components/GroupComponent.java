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
import java.util.Collections;
import java.util.List;

import com.ericsson.oss.services.ap.core.metadata.cli.api.GroupMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.Metadata;
import com.ericsson.oss.services.ap.core.rest.view.properties.data.ViewProperties;

/**
 * Constructs a group in the UI view. A group provides the ability to add a header to logically group child components.
 * <p>
 * The contents of the group are driven by metadata {@link GroupMetadata}.
 */
class GroupComponent implements Component<GroupMetadata> {

    private GroupMetadata groupMetadata;

    /**
     * Sets the {@link GroupMetadata} that this component uses to determine what is displayed.
     *
     * @param metadata determines what the component displays
     */
    @Override
    public void setComponentMetadata(final Metadata metadata) {
        if (metadata instanceof GroupMetadata) {
            this.groupMetadata = (GroupMetadata) metadata;
        } else {
            groupMetadata = null;
        }
    }

    /**
     * Returns the metadata that is used to construct child components of this component.
     *
     * @return collection of metadata for child components
     */
    @Override
    public Collection<Metadata> getChildMetadata() {
        if (groupMetadata == null) {
            throw new IllegalStateException("groupMetadata has not been set");
        }
        return groupMetadata.getViewComponentsMetadata();
    }

    /**
     * Creates the Group component. The result is a collection of {@link ViewProperties} which contain the constructed component as defined by the
     * metadata.
     *
     * @param dataSource contains the data types and attribute name value pairs that the component displays. The metadata defines the parts of
     *                   the data
     *                   that are relevant to this component
     * @param dataType   the data type that the component is displaying
     * @param moStruct   the data MO struct that the component is displaying
     * @return collection of constructed AbstractDto elements that make up the component
     */
    @Override
    public Collection<ViewProperties> getClientDtos(final List<?> dataSource, final String dataType, final String moStruct) {
        if (groupMetadata == null) {
            throw new IllegalStateException("groupMetadata has not been set");
        }
        // Group with heading is not currently required on AP rest client or used in the metadata xml file, so an empty list is returned.
        // If this changes then logic should be added to build a ViewProperties dto for the group.
        return Collections.emptyList();
    }
}
