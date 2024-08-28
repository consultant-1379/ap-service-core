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
import com.ericsson.oss.services.ap.core.cli.view.search.DataTypeAttributeSearchBuilder;
import com.ericsson.oss.services.ap.core.metadata.cli.api.AttributeMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.LineMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.Metadata;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;

/**
 * Constructs a line in the CLI view. A line displays a name value pair.
 * <p>
 * The contents of the the line is driven by {@link LineMetadata}.
 */
public class LineComponent implements Component<LineMetadata> {

    @Inject
    private ResponseDtoBuilder responseBuilder;

    private LineMetadata lineMetadata;

    @Override
    public void setComponentMetadata(final Metadata metadata) {
        if (metadata instanceof LineMetadata) {
            this.lineMetadata = (LineMetadata) metadata;
        }
    }

    @Override
    public Collection<Metadata> getChildMetadata() {
        return Collections.<Metadata> emptyList();
    }

    @Override
    public Collection<AbstractDto> getAbstractDtos(final List<?> dataSource, final String dataType) {
        return createLineDto(dataSource, dataType, lineMetadata.getAttribute());
    }

    private List<AbstractDto> createLineDto(final List<?> data, final String dataType, final AttributeMetadata attributeMetadata) {
        final List<Map<AttributeMetadata, Object>> attributeValueFromDataObjects = getAttributeValuesFromDataObjects(data, dataType,
                attributeMetadata);

        if (attributeValueFromDataObjects.isEmpty()) {
            return Collections.<AbstractDto> emptyList();
        }

        final Map<String, Object> attributeToDisplay = new HashMap<>();
        final String attributeLabel = attributeMetadata.getLabel();
        final String prettyAttributeName = attributeLabel == null ? getAttributeDisplayName(attributeMetadata.getName()) : attributeLabel;
        attributeToDisplay.put(prettyAttributeName, attributeValueFromDataObjects.get(0).get(attributeMetadata));
        return responseBuilder.buildLineDtosOfNameValuePairs(attributeToDisplay);
    }

    private static List<Map<AttributeMetadata, Object>> getAttributeValuesFromDataObjects(final List<?> data, final String dataType,
            final AttributeMetadata attributeMetadata) {
        return new DataTypeAttributeSearchBuilder()
                .findAttribute(attributeMetadata)
                .inDataType(dataType)
                .fromDataObjects(data)
                .execute();
    }

    private static String getAttributeDisplayName(final String attributeName) {
        final int index = attributeName.lastIndexOf('.');
        return index >= 0 ? attributeName.substring(index + 1) : attributeName;
    }
}
