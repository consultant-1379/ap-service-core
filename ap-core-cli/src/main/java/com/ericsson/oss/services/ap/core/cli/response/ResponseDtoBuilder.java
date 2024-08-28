/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.LineDto;
import com.ericsson.oss.services.scriptengine.spi.utils.TableBuilder;

public class ResponseDtoBuilder {

    /**
     * Builds a List of Table Rows with the attributeNames as column headers, horizontally across the top of the table. The values will be printed in
     * a column beneath the relevant attributeNames/headers.
     *
     * @param values
     *            the attribute values
     * @param attributeNames
     *            the attribute names
     * @return a {@link List} of {@link AbstractDto}
     */
    public List<AbstractDto> buildHorizontalTableDto(final List<List<String>> values, final String[] attributeNames) {
        final TableBuilder tableBuilder = new TableBuilder();
        insertTableHeaders(attributeNames, tableBuilder);
        insertTableRows(values, tableBuilder);

        return populateTableContent(tableBuilder);
    }

    /**
     * Builds a List of Table Rows with no column headers, horizontally across the top of the table. The attribute names and their values will be in
     * each row.
     *
     * @param values
     *            the attribute values
     * @param tableHeader
     *            the table header
     * @return a {@link List} of {@link AbstractDto}
     */
    public List<AbstractDto> buildFilteredHorizontalTable(final List<List<String>> values, final String tableHeader) {
        final List<AbstractDto> fullTableContent = new ArrayList<>();
        fullTableContent.add(new LineDto(tableHeader));
        fullTableContent.addAll(LineDtoConverter.convertRowsOfNameValuePairs(values));
        return fullTableContent;
    }

    /**
     * Builds a List of Table Columns with a single table header. The values have no attribute names, so each value will be printed in a single
     * column.
     *
     * @param values
     *            the attribute values
     * @param tableHeader
     *            the table header
     * @return a {@link List} of {@link AbstractDto}
     */
    public List<AbstractDto> buildListedTable(final List<List<String>> values, final String tableHeader) {
        final List<AbstractDto> fullTableContent = new ArrayList<>();
        fullTableContent.add(new LineDto(tableHeader));
        fullTableContent.addAll(LineDtoConverter.convertRowsOfValues(values));
        return fullTableContent;
    }

    /**
     * Builds a List of Table Columns with a single table header as specified. The attributeNames will be printed vertically down the left hand side
     * of the table. The values will be as a row adjacent to the attributeNames.
     *
     * @param values
     *            the attribute values
     * @param attributeNames
     *            the attribute names
     * @param tableHeader
     *            the table header
     * @return a {@link List} of {@link AbstractDto}
     */
    public List<AbstractDto> buildVerticalTable(final List<List<String>> values, final String[] attributeNames, final String tableHeader) {
        final List<List<String>> rowsAsColumns = copyRowsToColumns(values, attributeNames);
        final List<AbstractDto> fullTableContent = new ArrayList<>();

        if (StringUtils.isNotBlank(tableHeader)) {
            fullTableContent.add(new LineDto(tableHeader));
        }
        fullTableContent.addAll(LineDtoConverter.convertRowsOfNameValuePairs(rowsAsColumns));
        return fullTableContent;
    }

    private static List<List<String>> copyRowsToColumns(final List<List<String>> rows, final String[] attributeNames) {
        final List<List<String>> columnAsRow = new ArrayList<>(attributeNames.length);
        for (int i = 0; i < attributeNames.length; ++i) {
            final List<String> row = new ArrayList<>(rows.size() + 1);

            final String header = attributeNames[i];
            row.add(header);

            for (final List<String> inputRow : rows) {
                row.add(inputRow.get(i));
            }
            columnAsRow.add(row);
        }
        return columnAsRow;
    }

    /**
     * Builds a list of {@link LineDto} where each one follows the format defined by {@link LineDtoConverter#mergeNameValuePair(String, String)}.
     *
     * @param nameValues
     *            the map which contains the lines.
     * @return a {@link List} of {@link AbstractDto}
     */
    public List<AbstractDto> buildLineDtosOfNameValuePairs(final Map<String, Object> nameValues) {
        final List<AbstractDto> lineDtos = new ArrayList<>(nameValues.size());
        for (final Entry<String, Object> entry : nameValues.entrySet()) {
            final String attributeValue = entry.getValue() == null ? "" : entry.getValue().toString();
            lineDtos.add(new LineDto(LineDtoConverter.mergeNameValuePair(entry.getKey(), attributeValue)));
        }
        return lineDtos;
    }

    /**
     * Builds a list of {@link LineDto} where each one follow the format: "# : value", where # is a number.
     *
     * @param values
     *            the list of values to be indexed.
     * @return a {@link List} of {@link AbstractDto}
     */
    public List<AbstractDto> buildLineDtosIndexed(final List<String> values) {
        return buildLineDtosIndexed(null, values);
    }

    /**
     * Builds a list of {@link LineDto} where each one follow the format: "# : value", where # is a number.
     *
     * @param header
     *            the header to indicate the content of the list
     * @param values
     *            the list of values to be indexed.
     * @return a {@link List} of {@link AbstractDto}
     */
    public List<AbstractDto> buildLineDtosIndexed(final String header, final List<String> values) {
        final List<AbstractDto> nameValueDtos = new ArrayList<>(values.size() + 1);
        if (header != null) {
            nameValueDtos.add(new LineDto(header));
        }

        int i = 1;
        for (final String value : values) {
            nameValueDtos.add(new LineDto(String.format("%s : %s", i++, value)));
        }
        return nameValueDtos;
    }

    private static List<AbstractDto> populateTableContent(final TableBuilder tableBuilder) {
        final List<AbstractDto> fullTableContent = new ArrayList<>();
        fullTableContent.addAll(tableBuilder.build());
        return fullTableContent;
    }

    private static void insertTableHeaders(final String[] headers, final TableBuilder tableBuilder) {
        for (int i = 0; i < headers.length; i++) {
            tableBuilder.withHeader(i, headers[i]);
        }
    }

    private static void insertTableRows(final List<List<String>> tableRows, final TableBuilder tableBuilder) {
        int rowIndex = 0;
        for (final List<String> row : tableRows) {
            int columnIndex = 0;
            for (final String rowData : row) {
                tableBuilder.withCell(rowIndex, columnIndex, rowData);
                columnIndex++;
            }
            rowIndex++;
        }
    }
}
