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
package com.ericsson.oss.services.ap.common.model.access;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.HierarchicalPrimaryTypeSpecification;
import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.PrimaryTypeAttributeSpecification;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataTypeSpecification;

/**
 * Unit tests for {@link ModeledAttributeFilter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ModeledAttributeFilterTest {

    private static final String NAMESPACE = "ap";
    private static final String MODEL_NAME = "Project";

    @Mock
    private HierarchicalPrimaryTypeSpecification modelDefinition;

    @Mock
    private ModelReader modelReader;

    @InjectMocks
    private ModeledAttributeFilter modeledAttributeFilter;

    @Before
    public void setUp() {
        when(modelReader.getLatestPrimaryTypeSpecification(NAMESPACE, MODEL_NAME)).thenReturn(modelDefinition);

        final List<String> modelAttributes = new ArrayList<>();
        addAttributeToModel(modelAttributes, "name", DataType.STRING);
        addAttributeToModel(modelAttributes, "type", DataType.ENUM_REF);
        addAttributeToModel(modelAttributes, "filepath", DataType.STRING);
        addAttributeToModel(modelAttributes, "immutable", DataType.BOOLEAN);
        addAttributeToModel(modelAttributes, "size", DataType.INTEGER);

        when(modelDefinition.getMemberNames()).thenReturn(modelAttributes);
    }

    @Test
    public void whenApplyFilterAndAllAttributesExistInModelAndNoAttributeIsPrimitiveThenReturnedAttributesAreSameAsInputAttributes() {
        final Map<String, String> inputAttributes = new HashMap<>();
        inputAttributes.put("name", "nameValue");
        inputAttributes.put("filepath", "filepathValue");

        final Map<String, Object> result = modeledAttributeFilter.apply(NAMESPACE, MODEL_NAME, inputAttributes);

        assertEquals(inputAttributes, result);
    }

    @Test
    public void whenApplyFilterAndAllAttributesExistInModelAndSomeAttibutesArePrimitiveThenPrimitveAttributesAreConvertedFromStringToPrimitive() {
        final Map<String, String> inputAttributes = new HashMap<>();
        inputAttributes.put("name", "nameValue");
        inputAttributes.put("filepath", "filepathValue");
        inputAttributes.put("type", "typeValue");
        inputAttributes.put("immutable", "true");
        inputAttributes.put("size", "5");

        final Map<String, Object> result = modeledAttributeFilter.apply(NAMESPACE, MODEL_NAME, inputAttributes);

        assertEquals("nameValue", result.get("name"));
        assertEquals("filepathValue", result.get("filepath"));
        assertEquals("typeValue", result.get("type"));
        assertTrue((boolean) result.get("immutable"));
        assertEquals(5, result.get("size"));
    }

    @Test
    public void whenApplyFilterAndOnlySomeAttributesExistInModelThenReturnedAttributesShouldNotIncludeNonModelAttributes() {
        final Map<String, String> inputAttributes = new HashMap<>();
        inputAttributes.put("name", "nameValue");
        inputAttributes.put("filepath", "filepathValue");
        inputAttributes.put("extraAttribute", "extraValue");

        final Map<String, Object> result = modeledAttributeFilter.apply(NAMESPACE, MODEL_NAME, inputAttributes);

        assertEquals(2, result.size());
        assertFalse(result.containsKey("extraAttribute"));
    }

    private void addAttributeToModel(final List<String> modelAttributes, final String attributeName, final DataType dataType) {
        modelAttributes.add(attributeName);
        final PrimaryTypeAttributeSpecification attributeSpecification = mock(PrimaryTypeAttributeSpecification.class);
        final DataTypeSpecification dataSpecification = mock(DataTypeSpecification.class);
        when(modelDefinition.getAttributeSpecification(attributeName)).thenReturn(attributeSpecification);
        when(attributeSpecification.getDataTypeSpecification()).thenReturn(dataSpecification);
        when(dataSpecification.getDataType()).thenReturn(dataType);
    }
}
