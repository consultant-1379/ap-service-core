/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.util.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class for {@link Iterator} objects.
 */
public final class IteratorUtils {

    private IteratorUtils() {

    }

    /**
     * Converts the supplied {@link Iterator} to a {@link List}.
     *
     * @param iteratorToConvert
     *            the {@link Iterator} to convert
     * @return the iterator as a {@link List}
     */
    public static <T> List<T> convertIteratorToList(final Iterator<T> iteratorToConvert) {
        final List<T> convertedList = new ArrayList<>();

        while (iteratorToConvert.hasNext()) {
            convertedList.add(iteratorToConvert.next());
        }

        return convertedList;
    }
}