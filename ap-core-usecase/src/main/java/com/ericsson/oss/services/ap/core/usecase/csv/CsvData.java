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
package com.ericsson.oss.services.ap.core.usecase.csv;

import static java.lang.Math.max;

/**
 * Read-only class that abstracts the data obtained from {@link CsvReader} into easy-to-use methods.
 */
public final class CsvData {

    private final String[][] data;

    CsvData(final String[][] data) {
        this.data = data.clone();
    }

    /**
     * Returns the number of data rows in the CSV file. Excludes the header row.
     *
     * @return the number of rows
     */
    public int getRowCount() {
        return max(data.length - 1, 0);
    }

    public int getHeaderCount() {
        if (data.length == 0) {
            return 0;
        }

        return data[0].length;
    }

    /**
     * Returns the nth header name.
     *
     * @param index
     *            the index of the header, 0-based
     * @return the header name
     */
    public String getHeader(final int index) {
        return data[0][index];
    }

    /**
     * Get a value from the csv read file
     *
     * @param row
     *            the specific row (0-based, excluding header row)
     * @param col
     *            the specific column (0-based)
     * @return the value at the specific row / column
     * @throws IllegalArgumentException
     *             if the column or row are invalid
     */
    public String getValue(final int row, final int col) {
        final int actualRow = row + 1;
        validateRow(actualRow);
        validateColumn(col);

        return data[actualRow][col];
    }

    /**
     * @return an iterableMap, to facilitate the transformation from an array of string to a key - value map. It is iterable, meaning that you will
     *         get 1 map per csv line, and the mapping is header - value.
     */
    public CsvIterableMap asIterableMap() {
        return new CsvIterableMap(this);
    }

    private void validateColumn(final int col) {
        if ((col < 0) || (col >= getHeaderCount())) {
            throw new IllegalArgumentException("Please choose a valid column");
        }
    }

    private void validateRow(final int actualRow) {
        if ((actualRow <= 0) || (actualRow >= data.length)) {
            throw new IllegalArgumentException("Please choose a valid row");
        }
    }
}
