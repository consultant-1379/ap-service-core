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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.core.cli.response.ResponseDtoBuilder;
import com.ericsson.oss.services.ap.core.metadata.cli.api.AttributeMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.TableMetadata;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.HeaderRowDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.RowCell;
import com.ericsson.oss.services.scriptengine.spi.dtos.RowDto;

/**
 * Unit tests for {@link TableComponent}.
 */
@RunWith(MockitoJUnitRunner.class)
public class TableComponentTest {

    @Spy
    private ResponseDtoBuilder responseBuilder; // NOPMD

    @InjectMocks
    private TableComponent tableComponent;

    @Mock
    private TableMetadata tableMetadata;

    @Mock
    private AttributeMetadata attributeXMetadata, attributeYMetadata;

    private static final String TEST_DATA_TYPE = "testDataType";
    private static final String TEST_ATTRIBUTE_X = "attributeX";
    private static final String TEST_ATTRIBUTE_Y = "attributeY";
    private static final int ROW_ONE_COL_ONE_VALUE = 1;
    private static final int ROW_ONE_COL_TWO_VALUE = 2;
    private static final int ROW_TWO_COL_ONE_VALUE = 11;
    private static final int ROW_TWO_COL_TWO_VALUE = 12;

    public static class TestData {

        private final Map<String, Object> attributes;
        private final String type;

        TestData(final Map<String, Object> attributes, final String type) {
            this.attributes = attributes;
            this.type = type;
        }

        public Map<String, Object> getAttributes() {
            return attributes;
        }

        public String getType() {
            return type;
        }
    }

    @Test
    public void table_created_with_correct_headers_and_values() {
        final List<TestData> dataList = createTestData();
        final List<AttributeMetadata> attributes = new ArrayList<>();
        attributes.add(attributeXMetadata);
        attributes.add(attributeYMetadata);

        when(tableMetadata.getAttributes()).thenReturn(attributes);
        when(attributeXMetadata.getName()).thenReturn("attributes." + TEST_ATTRIBUTE_X);
        when(attributeYMetadata.getName()).thenReturn("attributes." + TEST_ATTRIBUTE_Y);
        tableComponent.setComponentMetadata(tableMetadata);

        final Collection<AbstractDto> result = tableComponent.getAbstractDtos(dataList, "type:" + TEST_DATA_TYPE);

        final List<AbstractDto> actualViewDtos = new ArrayList<>(result);
        final HeaderRowDto actualHeaderRow = (HeaderRowDto) actualViewDtos.get(0);
        final RowDto actualFirstRowDto = (RowDto) actualViewDtos.get(1);
        final RowDto actualSecondRowDto = (RowDto) actualViewDtos.get(2);

        final List<RowCell> headerCells = actualHeaderRow.getElements();
        final String actualHeaderColumnOne = headerCells.get(0).getValue();
        final String actualHeaderColumnTwo = headerCells.get(1).getValue();

        assertEquals(TEST_ATTRIBUTE_X, actualHeaderColumnOne);
        assertEquals(TEST_ATTRIBUTE_Y, actualHeaderColumnTwo);

        final List<RowCell> firstRowCells = actualFirstRowDto.getElements();
        final int actualFirstRowColumnOne = Integer.parseInt(firstRowCells.get(0).getValue());
        final int actualFirstRowColumnTwo = Integer.parseInt(firstRowCells.get(1).getValue());

        assertEquals(ROW_ONE_COL_ONE_VALUE, actualFirstRowColumnOne);
        assertEquals(ROW_ONE_COL_TWO_VALUE, actualFirstRowColumnTwo);

        final List<RowCell> secondRowCells = actualSecondRowDto.getElements();
        final int actualSecondRowColumnOne = Integer.parseInt(secondRowCells.get(0).getValue());
        final int actualSecondRowColumnTwo = Integer.parseInt(secondRowCells.get(1).getValue());

        assertEquals(ROW_TWO_COL_ONE_VALUE, actualSecondRowColumnOne);
        assertEquals(ROW_TWO_COL_TWO_VALUE, actualSecondRowColumnTwo);
    }

    @Test
    public void whenDataObjectsHaveNoValues_thenEmptyListIsReturned() {
        final Collection<AbstractDto> result = tableComponent.getAbstractDtos(Collections.<TestData> emptyList(), "type:" + TEST_DATA_TYPE);
        assertTrue(result.isEmpty());
    }

    private List<TestData> createTestData() {
        final List<TestData> dataList = new ArrayList<>();

        final Map<String, Object> attrsForFirstDataObject = new HashMap<>();
        attrsForFirstDataObject.put(TEST_ATTRIBUTE_X, ROW_ONE_COL_ONE_VALUE);
        attrsForFirstDataObject.put(TEST_ATTRIBUTE_Y, ROW_ONE_COL_TWO_VALUE);
        final TestData testDataOne = new TestData(attrsForFirstDataObject, TEST_DATA_TYPE);

        final Map<String, Object> attrsForSecondDataObject = new HashMap<>();
        attrsForSecondDataObject.put(TEST_ATTRIBUTE_X, ROW_TWO_COL_ONE_VALUE);
        attrsForSecondDataObject.put(TEST_ATTRIBUTE_Y, ROW_TWO_COL_TWO_VALUE);
        final TestData testDataTwo = new TestData(attrsForSecondDataObject, TEST_DATA_TYPE);

        dataList.add(testDataOne);
        dataList.add(testDataTwo);
        return dataList;
    }
}
