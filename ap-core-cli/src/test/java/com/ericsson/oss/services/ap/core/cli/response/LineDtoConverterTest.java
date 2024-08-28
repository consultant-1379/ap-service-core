/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.scriptengine.spi.dtos.LineDto;

/**
 * Unit tests for {@link LineDtoConverter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LineDtoConverterTest {

    @Test
    public void whenInputDataIsEmpty_thenDefaultElementIsReturned() {
        final List<LineDto> result = LineDtoConverter.convertRowsOfNameValuePairs(Collections.<List<String>> emptyList());
        assertEquals(1, result.size());
        assertTrue(result.get(0).getValue().isEmpty());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void whenInputDataHasOneRow_andRowIsEmpty_thenExceptionIsThrown() {
        final List<List<String>> inputData = new ArrayList<>();
        inputData.add(Collections.<String> emptyList());

        LineDtoConverter.convertRowsOfNameValuePairs(inputData);
    }

    @Test
    public void whenInputDataHasOneNameValuePair_thenListIsReturned_andElementTextDoesNotEndWithSeparator() {
        final List<List<String>> inputData = new ArrayList<>();
        final List<String> rowData = new ArrayList<>();
        rowData.add("rowStart");
        rowData.add("rowEnd");
        inputData.add(rowData);

        final List<LineDto> result = LineDtoConverter.convertRowsOfNameValuePairs(inputData);

        assertTrue(result.get(0).getValue().matches("(rowStart).*(rowEnd).*"));
    }

    @Test
    public void whenInputDataHasTwoNameValuePairs_thenListIsReturned_andElementTextsDoNotEndWithSeparator() {
        final List<List<String>> inputData = new ArrayList<>();
        final List<String> firstRowData = new ArrayList<>();
        firstRowData.add("firstRowStart");
        firstRowData.add("firstRowEnd");

        final List<String> secondRowData = new ArrayList<>();
        secondRowData.add("secondRowStart");
        secondRowData.add("secondRowEnd");
        inputData.add(firstRowData);
        inputData.add(secondRowData);

        final List<LineDto> result = LineDtoConverter.convertRowsOfNameValuePairs(inputData);

        assertTrue(result.get(0).getValue().matches("(firstRowStart).*(firstRowEnd).*"));
        assertTrue(result.get(1).getValue().matches("(secondRowStart).*(secondRowEnd).*"));
    }

    @Test
    public void whenInputDataHasValuesOnly_thenValuesAreConvertedToLineDtos() {
        final List<List<String>> inputData = new ArrayList<>();
        final List<String> firstValue = new ArrayList<>();
        firstValue.add("firstValue");
        final List<String> secondValue = new ArrayList<>();
        secondValue.add("secondValue");
        inputData.add(firstValue);
        inputData.add(secondValue);

        final List<LineDto> result = LineDtoConverter.convertRowsOfValues(inputData);

        assertEquals("firstValue", result.get(0).getValue());
        assertEquals("secondValue", result.get(1).getValue());
    }

    @Test
    public void whenInputDataHasValuesOnly_andInputListIsEmpty_thenSingleEmptyLineDtoIsReturnedInList() {
        final List<List<String>> inputData = new ArrayList<>();
        final List<LineDto> result = LineDtoConverter.convertRowsOfValues(inputData);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getValue().isEmpty());
    }

    @Test
    public void whenMergingNameValueStrings_thenValuesArePaddedCorrectly() {
        final String result = LineDtoConverter.mergeNameValuePair("name", "value");
        assertEquals("name                                    value", result); //Using ALT+255, not space
    }
}
