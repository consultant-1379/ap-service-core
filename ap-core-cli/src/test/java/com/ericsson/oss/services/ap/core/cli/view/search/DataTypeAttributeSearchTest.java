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
package com.ericsson.oss.services.ap.core.cli.view.search;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.core.metadata.cli.api.AttributeMetadata;
import com.ericsson.oss.services.ap.core.metadata.cli.model.Attribute;

/**
 * Unit tests for {@link DataTypeAttributeSearch}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DataTypeAttributeSearchTest {

    private static final String ATTRIBUTE_X = "attributeX";
    private static final String ATTRIBUTE_Y = "attributeY";
    private static final String ATTRIBUTE_Z = "attributeZ";
    private static final String ATTRIBUTE_M = "attributeM";
    private static final String ATTRIBUTE_N = "attributeN";

    private static final String MO_TYPE_X = "moTypeX";
    private static final String MO_TYPE_Y = "moTypeY";

    private final List<MoData> moDataList = new ArrayList<>();

    private final Map<String, Object> expectedMoDataAttributesForMoDataOne = new HashMap<>();
    private final Map<String, Object> expectedMoDataAttributesForMoDataTwo = new HashMap<>();
    private final Map<String, Object> expectedMoDataAttributesForMoDataThree = new HashMap<>();

    @Before
    public void setUp() {
        expectedMoDataAttributesForMoDataOne.put(ATTRIBUTE_X, "1");
        expectedMoDataAttributesForMoDataOne.put(ATTRIBUTE_Y, "11");
        expectedMoDataAttributesForMoDataOne.put(ATTRIBUTE_Z, "111");

        expectedMoDataAttributesForMoDataTwo.put(ATTRIBUTE_X, "2");
        expectedMoDataAttributesForMoDataTwo.put(ATTRIBUTE_Y, "22");
        expectedMoDataAttributesForMoDataTwo.put(ATTRIBUTE_Z, "222");

        expectedMoDataAttributesForMoDataThree.put(ATTRIBUTE_M, "3");
        expectedMoDataAttributesForMoDataThree.put(ATTRIBUTE_N, "333");

        final MoData moDataOne = new MoData(null, expectedMoDataAttributesForMoDataOne, MO_TYPE_X, null);
        final MoData moDataTwo = new MoData(null, expectedMoDataAttributesForMoDataTwo, MO_TYPE_X, null);
        final MoData moDataThree = new MoData(null, expectedMoDataAttributesForMoDataThree, MO_TYPE_Y, null);

        moDataList.add(moDataOne);
        moDataList.add(moDataTwo);
        moDataList.add(moDataThree);
    }

    @Test
    public void finds_correct_attributes_in_two_matching_mos() {
        final List<AttributeMetadata> attributesToFind = new ArrayList<>();
        final Attribute attribute1 = new Attribute();
        attribute1.setLabel("Attribute Label 1");
        attribute1.setName("attributes." + ATTRIBUTE_X);

        final Attribute attribute2 = new Attribute();
        attribute2.setLabel("Attribute Label 2");
        attribute2.setName("attributes." + ATTRIBUTE_Y);

        attributesToFind.add(attribute1);
        attributesToFind.add(attribute2);

        final List<Map<AttributeMetadata, Object>> actualSearchResults = new DataTypeAttributeSearchBuilder().findAttributes(attributesToFind)
                .inDataType("type:" + MO_TYPE_X)
                .fromDataObjects(moDataList).execute();

        final Map<AttributeMetadata, Object> resultOne = actualSearchResults.get(0);
        final Map<AttributeMetadata, Object> resultTwo = actualSearchResults.get(1);

        assertThat(actualSearchResults.size(), equalTo(2));

        assertThat(resultOne.get(attribute1), equalTo(expectedMoDataAttributesForMoDataOne.get(ATTRIBUTE_X)));
        assertThat(resultTwo.get(attribute2), equalTo(expectedMoDataAttributesForMoDataTwo.get(ATTRIBUTE_Y)));

    }

    @Test
    public void finds_correct_attribute_in_one_matching_mo() {
        final List<AttributeMetadata> attributesToFind = new ArrayList<>();
        final Attribute attribute1 = new Attribute();
        attribute1.setLabel("Attribute Label 1");
        attribute1.setName("attributes." + ATTRIBUTE_N);
        attributesToFind.add(attribute1);
        final List<Map<AttributeMetadata, Object>> actualSearchResults = new DataTypeAttributeSearchBuilder().findAttribute(attributesToFind.get(0))
                .inDataType("type:" + MO_TYPE_Y)
                .fromDataObjects(moDataList).execute();

        final Map<AttributeMetadata, Object> resultOne = actualSearchResults.get(0);
        assertThat(actualSearchResults.size(), equalTo(1));

        assertThat(resultOne.get(attribute1), equalTo(expectedMoDataAttributesForMoDataThree.get(ATTRIBUTE_N)));
    }

    @Test
    public void does_not_find_attributes_as_mo_does_not_exist() {
        final List<AttributeMetadata> attributesToFind = new ArrayList<>();
        final Attribute attribute1 = new Attribute();
        attribute1.setLabel("Attribute Label 1");
        attribute1.setName("attributes." + ATTRIBUTE_X);
        attributesToFind.add(attribute1);

        final List<Map<AttributeMetadata, Object>> actualSearchResults = new DataTypeAttributeSearchBuilder().findAttributes(attributesToFind)
                .inDataType("type:moTypeDoesNotExist")
                .fromDataObjects(moDataList).execute();

        assertThat(actualSearchResults.size(), equalTo(0));
    }
}
