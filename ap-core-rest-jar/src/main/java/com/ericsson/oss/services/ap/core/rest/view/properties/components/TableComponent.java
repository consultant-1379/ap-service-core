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

import com.ericsson.oss.services.ap.core.metadata.cli.api.AttributeMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.Metadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.TableMetadata;
import com.ericsson.oss.services.ap.core.rest.view.properties.data.MetadataTextFormatter;
import com.ericsson.oss.services.ap.core.rest.view.properties.data.PropertyNameValue;
import com.ericsson.oss.services.ap.core.rest.view.properties.data.PropertyValue;
import com.ericsson.oss.services.ap.core.rest.view.properties.data.ViewProperties;
import com.ericsson.oss.services.ap.core.rest.view.properties.renderer.TextRenderer;
import com.ericsson.oss.services.ap.core.rest.view.properties.renderer.TextRendererFactory;
import com.ericsson.oss.services.ap.core.rest.view.properties.search.DataTypeAttributeSearchBuilder;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

/**
 * Constructs objects for the UI view from Table items read from view xml.
 * <p>
 * The contents of the the table are driven by {@link TableMetadata}.
 */
class TableComponent implements Component<TableMetadata>, FilterableComponent {

    private static final String VERTICAL_TABLE_STYLE = "vertical";
    private static final String LIST_TABLE_STYLE = "list";
    private static final String LIST_WITH_DYNAMIC_LABEL_TABLE_STYLE = "listWithDynamicLabel";

    private static final int NODE_ARTIFACT_TYPE_ATTRIBUTE_INDEX = 0;
    private static final int NODE_ARTIFACT_NAME_ATTRIBUTE_INDEX = 1;


    @Inject
    private TextRendererFactory textRendererFactory;

    @Inject
    private Logger logger;

    private TableMetadata tableMetadata;

    @Override
    public void setComponentMetadata(final Metadata metadata) {
        if (metadata instanceof TableMetadata) {
            this.tableMetadata = (TableMetadata) metadata;
        }
    }

    @Override
    public Collection<Metadata> getChildMetadata() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ViewProperties> getClientDtos(final List<?> dataSource, final String dataType, final String moStruct) {
        return getClientDtos(dataSource, dataType, moStruct, "");
    }

    /**
     *
     * @param dataSource
     *            contains the data types and attribute name value pairs that the component displays. The metadata defines the parts of the data
     *            that are relevant to this component
     * @param dataType
     *            the data type that the component is displaying
     * @param moStruct
     *            the data MO struct that the component is displaying
     * @param filter
     *            a filter for the data
     * @return the table DTO
     */
    @Override
    public Collection<ViewProperties> getClientDtos(final List<?> dataSource, final String dataType, final String moStruct, final String filter) {
        return createTableDto(dataSource, dataType, moStruct, filter, tableMetadata.getAttributes());
    }

    private List<ViewProperties> createTableDto(final List<?> data, final String dataType, final String moStruct, final String filter,
            final List<AttributeMetadata> attributes) {

        // Get the List Map of AttributeMetaData  -> Attribute Value
        final List<Map<AttributeMetadata, Object>> attributeNameValuePairsFromDataObjects = new DataTypeAttributeSearchBuilder()
                .findAttributes(attributes)
                .inDataType(dataType)
                .fromDataObjects(data)
                .withFilter(filter)
                .execute();

        if (attributeNameValuePairsFromDataObjects.isEmpty()) {
            return Collections.emptyList();
        }

        return buildTableDto(attributeNameValuePairsFromDataObjects, dataType, moStruct);

    }

    private List<ViewProperties> buildTableDto(final List<Map<AttributeMetadata, Object>> attributeNameValuePairs, final String dataType, final String moStruct) {

        final String tableStyle = tableMetadata.getStyle();
        final String heading = tableMetadata.getHeading();

        switch(tableStyle) {
            case VERTICAL_TABLE_STYLE:
                return buildNameValueProperties(attributeNameValuePairs, dataType, moStruct);
            case LIST_WITH_DYNAMIC_LABEL_TABLE_STYLE:
                return buildIntegrationArtifactProperties(attributeNameValuePairs, heading);
            case LIST_TABLE_STYLE:
                return buildConfigurationsPropertyList(attributeNameValuePairs, heading);
            default:
                return new ArrayList<>(1);
        }
    }

    private List<ViewProperties> buildNameValueProperties(final List<Map<AttributeMetadata, Object>> attributeNameValuePairsFromDataObjects, String dataType, final String moStruct) {

        final List<ViewProperties> viewPropertiesList = new ArrayList<>(1);
        final List<Object> nameValuePropertiesList = new ArrayList<>(attributeNameValuePairsFromDataObjects.size());
        for (final Map<AttributeMetadata, Object> attributeNameValuePairEntry : attributeNameValuePairsFromDataObjects) {

            for (final Entry<AttributeMetadata, Object> entry : attributeNameValuePairEntry.entrySet()) {
                final AttributeMetadata attributeMetaData = entry.getKey();

                String attributeName = attributeMetaData.getName();

                final Object attributeValue = entry.getValue() == null ? "" : entry.getValue();
                attributeName = MetadataTextFormatter.formatAttributeName(attributeName);
                nameValuePropertiesList.add(new PropertyNameValue(attributeName, (Serializable) attributeValue));
            }
        }

        ViewProperties viewProperties;

        if(StringUtils.isNotBlank(moStruct)) {
           viewProperties = new ViewProperties(moStruct, nameValuePropertiesList);
        } else {
            dataType = MetadataTextFormatter.formatTypeName(dataType);
            viewProperties = new ViewProperties(dataType, nameValuePropertiesList);
        }

        viewPropertiesList.add(viewProperties);
        return viewPropertiesList;
    }

    /**
     * Build a List of {@link ViewProperties} where the key for the properties is the first attribute entry in the metadata.
     * The value is the second entry in the metadata.  See the "listWithDynamicLabel" in the metadata xml
     * for the view in ecim_model
     *
     * @param attributeNameValuePairsFromDataObjects  all attributes for this list
     * @return a list of {@link ViewProperties} instances with key/value pairs for the client
     */
    private List<ViewProperties> buildIntegrationArtifactProperties(
        final List<Map<AttributeMetadata, Object>> attributeNameValuePairsFromDataObjects, final String dataHeading) {

        final List<ViewProperties> viewPropertiesList = new ArrayList<>(1);
        final List<Object> nameValuePropertiesList = new ArrayList<>();

        for (final Map<AttributeMetadata, Object> attributeNameValuePairEntry : attributeNameValuePairsFromDataObjects) {
            AttributeMetadata attributeMetadata = null;
            final List<Object> attributeValues = new ArrayList<>(attributeNameValuePairEntry.size());
            for (final Entry<AttributeMetadata, Object> entry : attributeNameValuePairEntry.entrySet()) {
                final Object attributeValue = entry.getValue() == null ? "" : entry.getValue();
                attributeMetadata = entry.getKey();
                attributeValues.add(attributeValue);

            }
            // For Integration Artifacts :  Key= NodeArtifact.type  and Value=NodeArtifact.name attributes value
            // only want to use renderer for the artifact name value to add ".xml"
            // don't want to use renderer for artifact type as this transforms camel case to more readable labels
            final String nodeArtifactNameXml = renderValue(attributeMetadata, attributeValues.get(NODE_ARTIFACT_NAME_ATTRIBUTE_INDEX));
            final Serializable property = new PropertyNameValue((String)attributeValues.get(NODE_ARTIFACT_TYPE_ATTRIBUTE_INDEX), nodeArtifactNameXml);

            nameValuePropertiesList.add(property);
        }

        final String heading = MetadataTextFormatter.formatHeadingText(dataHeading);
        final ViewProperties viewProperties = new ViewProperties(heading, nameValuePropertiesList);
        viewPropertiesList.add(viewProperties);
        return viewPropertiesList;
    }

    private List<ViewProperties> buildConfigurationsPropertyList(final List<Map<AttributeMetadata, Object>> attributeNameValuePairsFromDataObjects,
        final String dataHeading) {

        final List<ViewProperties> viewPropertiesList = new ArrayList<>(1);
        final List<Object> valuePropertiesList = new ArrayList<>();
        for (final Map<AttributeMetadata, Object> attributeNameValuePairEntry : attributeNameValuePairsFromDataObjects) {

            for (final Entry<AttributeMetadata, Object> entry : attributeNameValuePairEntry.entrySet()) {
                Object attributeValue = entry.getValue();
                if (attributeValue instanceof String) {
                    // Get the name and value strings from the Object
                    attributeValue = renderValue(entry.getKey(), attributeValue);
                    valuePropertiesList.add(new PropertyValue((String) attributeValue));
                } else {
                    valuePropertiesList.add(new PropertyValue(""));
                }
            }
        }

        final String heading = MetadataTextFormatter.formatHeadingText(dataHeading);
        final ViewProperties viewProperties = new ViewProperties(heading, valuePropertiesList);
        viewPropertiesList.add(viewProperties);
        return viewPropertiesList;
    }

    private String renderValue(final AttributeMetadata attributeMetaData, final Object rowValue) {
        if (attributeMetaData == null || attributeMetaData.getRenderer() == null) {
            return String.valueOf(rowValue);
        }

        final TextRenderer textRenderer = textRendererFactory.getTextRenderer(attributeMetaData.getRenderer());
        if (null == textRenderer) {
            logger.warn("No textRenderer defined for type {}", attributeMetaData.getRenderer());
            return String.valueOf(rowValue);
        }
        return textRenderer.render(rowValue);
    }

}
