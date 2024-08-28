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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.core.cli.response.ResponseDtoBuilder;
import com.ericsson.oss.services.ap.core.cli.view.renderer.TextRenderer;
import com.ericsson.oss.services.ap.core.cli.view.renderer.TextRendererFactory;
import com.ericsson.oss.services.ap.core.cli.view.search.DataTypeAttributeSearchBuilder;
import com.ericsson.oss.services.ap.core.metadata.cli.api.AttributeMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.Metadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.TableMetadata;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;

/**
 * Constructs a table in the CLI view.
 * <p>
 * The contents of the the table is driven by {@link TableMetadata}.
 */
class TableComponent implements Component<TableMetadata>, FilterableComponent {

    private static final String VERTICAL_TABLE_STYLE = "vertical";
    private static final String LIST_TABLE_STYLE = "list";
    private static final String LIST_WITH_DYNAMIC_LABEL_TABLE_STYLE = "listWithDynamicLabel";

    private static final String TABBED_TEXT = "    %1$s";

    @Inject
    private ResponseDtoBuilder responseBuilder;

    @Inject
    private TextRendererFactory textRendererFactory;

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
    public Collection<AbstractDto> getAbstractDtos(final List<?> dataSource, final String dataType) {
        return getAbstractDtos(dataSource, dataType, "");
    }

    @Override
    public Collection<AbstractDto> getAbstractDtos(final List<?> dataSource, final String dataType, final String filter) {
        return createTableDto(dataSource, dataType, filter, tableMetadata.getAttributes());
    }

    private List<AbstractDto> createTableDto(final List<?> data, final String dataType, final String filter,
            final List<AttributeMetadata> attributes) {
        final List<Map<AttributeMetadata, Object>> attributeNameValuePairsFromDataObjects = new DataTypeAttributeSearchBuilder()
                .findAttributes(attributes)
                .inDataType(dataType)
                .fromDataObjects(data)
                .withFilter(filter)
                .execute();

        if (attributeNameValuePairsFromDataObjects.isEmpty()) {
            return Collections.emptyList();
        }

        return buildTableDto(attributes, attributeNameValuePairsFromDataObjects);

    }

    private List<AbstractDto> buildTableDto(final List<AttributeMetadata> attributes,
            final List<Map<AttributeMetadata, Object>> attributeNameValuePairs) {

        final String tableStyle = tableMetadata.getStyle();

        if (VERTICAL_TABLE_STYLE.equals(tableStyle)) {
            return buildVerticalTable(attributes, attributeNameValuePairs);
        } else if (LIST_TABLE_STYLE.equals(tableStyle)) {
            return buildListedTable(attributeNameValuePairs);
        } else if (LIST_WITH_DYNAMIC_LABEL_TABLE_STYLE.equals(tableStyle)) {
            return buildHorizontalTableWithDynamicLabels(attributeNameValuePairs);
        } else {
            return buildHorizontalTableDto(attributes, attributeNameValuePairs);
        }
    }

    private List<AbstractDto> buildHorizontalTableDto(final List<AttributeMetadata> attributes,
            final List<Map<AttributeMetadata, Object>> attributeNameValuePairsFromDataObjects) {
        return responseBuilder.buildHorizontalTableDto(
                getTableRows(attributeNameValuePairsFromDataObjects),
                getTableHeaders(attributes));
    }

    private List<AbstractDto> buildHorizontalTableWithDynamicLabels(
            final List<Map<AttributeMetadata, Object>> attributeNameValuePairsFromDataObjects) {
        return responseBuilder.buildFilteredHorizontalTable(
                getTableRows(attributeNameValuePairsFromDataObjects),
                tableMetadata.getHeading());
    }

    private List<AbstractDto> buildListedTable(final List<Map<AttributeMetadata, Object>> attributeNameValuePairsFromDataObjects) {
        return responseBuilder.buildListedTable(
                getTableRows(attributeNameValuePairsFromDataObjects),
                tableMetadata.getHeading());
    }

    private List<AbstractDto> buildVerticalTable(final List<AttributeMetadata> attributes,
            final List<Map<AttributeMetadata, Object>> attributeNameValuePairsFromDataObjects) {
        return responseBuilder.buildVerticalTable(
                getTableRows(attributeNameValuePairsFromDataObjects),
                getTableHeaders(attributes),
                tableMetadata.getHeading());
    }

    private String[] getTableHeaders(final List<AttributeMetadata> attributes) {
        final String[] tableHeaders = new String[attributes.size()];
        for (int i = 0; i < tableHeaders.length; i++) {
            tableHeaders[i] = getTableHeader(attributes.get(i));
        }

        return tableHeaders;
    }

    private String getTableHeader(final AttributeMetadata attribute) {
        final String prettyAttributeName = (attribute.getLabel() == null) ? getAttributeDisplayName(attribute.getName()) : attribute.getLabel();

        if ((isTableVertical()) && (attribute.getTabbed())) {
            return String.format(TABBED_TEXT, prettyAttributeName);
        }

        return prettyAttributeName;
    }

    private List<List<String>> getTableRows(final List<Map<AttributeMetadata, Object>> attributeNameValuePairsList) {
        final List<List<String>> tableRows = new ArrayList<>(attributeNameValuePairsList.size());
        for (final Map<AttributeMetadata, Object> attributeNameValuePair : attributeNameValuePairsList) {
            tableRows.add(retrieveRowEntriesInEachRow(attributeNameValuePair));
        }

        return tableRows;
    }

    private List<String> retrieveRowEntriesInEachRow(final Map<AttributeMetadata, Object> attributeNameValuePair) {
        final List<String> rowEntries = new ArrayList<>(attributeNameValuePair.size());
        for (final Entry<AttributeMetadata, Object> entry : attributeNameValuePair.entrySet()) {
            final AttributeMetadata attributeMetaData = entry.getKey();
            final Object rowValue = entry.getValue();

            if (rowValue == null) {
                rowEntries.add("");
            } else {
                rowEntries.add(getRowValue(attributeMetaData, rowValue));
            }
        }

        return rowEntries;
    }

    private String getRowValue(final AttributeMetadata attributeMetaData, final Object rowValue) {
        final String renderedValue = renderRowValue(attributeMetaData, rowValue);
        if ((!isTableVertical()) && (attributeMetaData.getTabbed())) {
            return String.format(TABBED_TEXT, renderedValue);
        } else {
            return renderedValue;
        }
    }

    private String renderRowValue(final AttributeMetadata attributeMetaData, final Object rowValue) {
        final String attributeRenderer = attributeMetaData.getRenderer();
        if (attributeRenderer == null) {
            return String.valueOf(rowValue);
        }

        final TextRenderer textRenderer = textRendererFactory.getTextRenderer(attributeRenderer);
        return textRenderer.render(rowValue);
    }

    private static String getAttributeDisplayName(final String attributeName) {
        final int index = attributeName.lastIndexOf('.');
        return index >= 0 ? attributeName.substring(index + 1) : attributeName;
    }

    private boolean isTableVertical() {
        final String tableStyle = tableMetadata.getStyle();
        return VERTICAL_TABLE_STYLE.equals(tableStyle);
    }
}