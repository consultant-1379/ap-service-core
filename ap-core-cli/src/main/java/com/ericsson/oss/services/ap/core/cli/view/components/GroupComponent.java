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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.core.cli.response.ResponseDtoBuilder;
import com.ericsson.oss.services.ap.core.metadata.cli.api.GroupMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.Metadata;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;

/**
 * Constructs a group in the CLI view. A group provides the ability to add a header to logically group child components.
 * <p>
 * The contents of the the group is driven by metadata {@link GroupMetadata}.
 */
class GroupComponent implements Component<GroupMetadata> { //NOSONAR EAP7 CDI libraries supports different way of handling generics.

    @Inject
    private ResponseDtoBuilder responseBuilder;

    private GroupMetadata groupMetadata;

    /**
     * Returns the metadata that will be used to construct child components of this component.
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
     * Sets the {@link GroupMetadata} that this component will use to determine what will be displayed.
     *
     * @param groupMetadata
     *            metadata will determine what the component will display
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
     * Creates the Group component. The result is a collection of {@link AbstractDto} which contain the constructed component as defined by the
     * metadata.
     *
     * @param dataSource
     *            contains the data types and attribute name value pairs that the component will display. The metadata defines the parts of the data
     *            that are relevant to this component
     * @param dataType
     *            the data type that the component is displaying
     * @return collection of constructed AbstractDto elements that make up the component
     */
    @Override
    public Collection<AbstractDto> getAbstractDtos(final List<?> dataSource, final String dataType) {
        if (groupMetadata == null) {
            throw new IllegalStateException("groupMetadata has not been set");
        }
        return createGroupHeadingDto(groupMetadata.getHeading());
    }

    private List<AbstractDto> createGroupHeadingDto(final String groupHeading) {
        if (groupHeading == null) {
            return Collections.emptyList();
        }

        final Map<String, Object> nameValuePairWithBlankValue = new HashMap<>();
        nameValuePairWithBlankValue.put(groupHeading, null);
        return responseBuilder.buildLineDtosOfNameValuePairs(nameValuePairWithBlankValue);
    }
}