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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Read-only class that abstracts the data obtained from CsvReader into easy-to-use methods, but in a key - value store instead.
 */
public class CsvIterableMap {

    private final CsvData data;
    private int index = -1;

    CsvIterableMap(final CsvData data) {
        this.data = data;
    }

    /**
     * @return the next map, if available, or an empty map if none is available.
     */
    public Map<String, String> getMap() {
        if (isValidRowIndex()) {
            return getActualMap();
        }

        return Collections.<String, String> emptyMap();
    }

    /**
     * Checks if there's another key.
     *
     * @return true if there is another key - value store available
     */
    public boolean hasNext() {
        return (index + 1) < data.getRowCount();
    }

    /**
     * Returns a Map representing a single line of the csv file, mapped as header - value.
     *
     * @return a key - value store
     */
    public Map<String, String> next() {
        if (hasNext()) {
            index++;
        }

        return getMap();
    }

    private Map<String, String> getActualMap() {
        final Map<String, String> result = new HashMap<>();
        for (int i = 0; i < data.getHeaderCount(); i++) {
            result.put(data.getHeader(i), data.getValue(index, i));
        }

        return result;
    }

    private boolean isValidRowIndex() {
        return (index >= 0) && (index < data.getRowCount());
    }
}
