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
package com.ericsson.oss.services.ap.core.test.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cucumber.api.DataTable;

public class TestUtils {

    private TestUtils() {

    }

    public static List<Map<String, String>> toMapList(final DataTable dataTable) {
        final List<Map<String, String>> result = new ArrayList<>();
        final List<List<String>> rawList = dataTable.raw();
        final List<String> headers = rawList.get(0);

        for (int i = 1; i < rawList.size(); i++) {
            final Map<String, String> entry = new HashMap<>();
            final List<String> cells = rawList.get(i);

            for (int j = 0; j < cells.size(); j++) {
                entry.put(headers.get(j), cells.get(j));
            }
            result.add(entry);
        }
        return result;
    }
}
