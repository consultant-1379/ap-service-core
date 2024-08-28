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
import com.ericsson.oss.services.ap.core.metadata.cli.api.LineMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.api.Metadata;
import com.ericsson.oss.services.scriptengine.spi.dtos.AbstractDto;
import com.ericsson.oss.services.scriptengine.spi.dtos.LineDto;

/**
 * Unit tests for {@link LineComponent}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LineComponentTest {

    @Spy
    private ResponseDtoBuilder responseBuilder; // NOPMD

    @Mock
    private LineMetadata lineMetadata;

    @InjectMocks
    private LineComponent lineComponent;

    @Mock
    private AttributeMetadata attributeMetadata;

    private static final String TEST_DATA_TYPE = "testDataType";
    private static final String TEST_ATTRIBUTE_X_NAME = "attributeX";
    private static final String TEST_ATTRIBUTE_Y_NAME = "attributeY";
    private static final String TEST_ATTRIBUTE_X_VALUE = "1";

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
    public void default_line_created_with_correct_name_and_value() {
        final List<TestData> dataList = createTestData();
        when(lineMetadata.getAttribute()).thenReturn(attributeMetadata);
        when(attributeMetadata.getName()).thenReturn("attributes." + TEST_ATTRIBUTE_X_NAME);

        lineComponent.setComponentMetadata(lineMetadata);
        final Collection<AbstractDto> result = lineComponent.getAbstractDtos(dataList, "type:" + TEST_DATA_TYPE);

        final List<AbstractDto> actualViewDtos = new ArrayList<>(result);
        final LineDto actualLineDto = (LineDto) actualViewDtos.get(0);
        final String actualValue = actualLineDto.getValue();

        assertTrue(actualValue.contains(TEST_ATTRIBUTE_X_NAME) && actualValue.contains(TEST_ATTRIBUTE_X_VALUE));
    }

    @Test
    public void whenDataObjectsHaveNoValues_thenEmptyListIsReturned() {
        final Collection<AbstractDto> result = lineComponent.getAbstractDtos(Collections.<TestData> emptyList(), "type:" + TEST_DATA_TYPE);
        assertTrue(result.isEmpty());

        final Collection<Metadata> childMetadata = lineComponent.getChildMetadata();
        assertTrue(childMetadata.isEmpty());
    }

    private List<TestData> createTestData() {
        final List<TestData> testData = new ArrayList<>();
        final Map<String, Object> attrsForFirstDataObject = new HashMap<>();
        attrsForFirstDataObject.put(TEST_ATTRIBUTE_X_NAME, TEST_ATTRIBUTE_X_VALUE);
        attrsForFirstDataObject.put(TEST_ATTRIBUTE_Y_NAME, 22);
        final TestData testDataOne = new TestData(attrsForFirstDataObject, TEST_DATA_TYPE);

        testData.add(testDataOne);

        final Map<String, Object> attrsForSecondDataObject = new HashMap<>();
        attrsForSecondDataObject.put(TEST_ATTRIBUTE_X_NAME, TEST_ATTRIBUTE_X_VALUE);
        attrsForSecondDataObject.put(TEST_ATTRIBUTE_Y_NAME, 22);
        final TestData testDataTwo = new TestData(attrsForSecondDataObject, TEST_DATA_TYPE);

        testData.add(testDataTwo);
        return testData;
    }
}
