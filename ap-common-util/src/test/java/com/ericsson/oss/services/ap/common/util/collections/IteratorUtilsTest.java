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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * Unit tests for {@link IteratorUtils}.
 */
public class IteratorUtilsTest {

    @Test
    public void whenConvertingIteratorToListAndIteratorHasDuplicatesThenIteratorIsConvertedAndNoElementsAreLost() {
        final List<String> inputData = new ArrayList<>();
        inputData.add("1");
        inputData.add(inputData.get(0));
        inputData.add("2");
        final Iterator<String> input = inputData.iterator();

        final List<String> result = IteratorUtils.convertIteratorToList(input);

        assertEquals(inputData, result);
    }

    @Test
    public void whenConvertingIteratorToListAndIteratorIsEmptyThenIteratorIsConvertedAndListIsEmpty() {
        final List<String> result = IteratorUtils.convertIteratorToList(Collections.<String> emptyIterator());
        assertTrue(result.isEmpty());
    }
}
