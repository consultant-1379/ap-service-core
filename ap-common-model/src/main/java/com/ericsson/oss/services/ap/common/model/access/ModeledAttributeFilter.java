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
package com.ericsson.oss.services.ap.common.model.access;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.HierarchicalPrimaryTypeSpecification;
import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.PrimaryTypeAttributeSpecification;
import com.ericsson.oss.itpf.modeling.common.util.LiteralValueUtil;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.DataType;

/**
 * Class provides methods for filtering of modelled attributes.
 */
public class ModeledAttributeFilter {

    @Inject
    private ModelReader modelReader;

    /**
     * Filters the supplied attributes, returning only those attributes which exist in the specified model.
     * <p>
     * Upon filtering, the datatype of the modelled attribute will be checked. If the attribute datatype in the model is not String or ENUM, then it
     * will be converted to the modelled datatype where possible. For example a modelled attribute of type boolean will be converted from String to
     * boolean.
     *
     * @param namespace
     *            the model namespace
     * @param modelName
     *            the name of the model
     * @param attributes
     *            the attributes to be filtered
     * @return all attributes which are applicable to the specified model
     */
    public Map<String, Object> apply(final String namespace, final String modelName, final Map<String, String> attributes) {
        final HierarchicalPrimaryTypeSpecification modelSpecification = modelReader.getLatestPrimaryTypeSpecification(namespace, modelName);
        final Map<String, Object> modeledAttributes = new HashMap<>();

        for (final String memberName : modelSpecification.getMemberNames()) {
            if (attributes.containsKey(memberName)) {
                final Object attributeValue = convertAttributeFromStringToPrimitiveTypeIfRequired(modelSpecification, memberName, attributes);
                modeledAttributes.put(memberName, attributeValue);
            }
        }

        return modeledAttributes;
    }

    private static Object convertAttributeFromStringToPrimitiveTypeIfRequired(final HierarchicalPrimaryTypeSpecification modelSpecification,
            final String attributeName, final Map<String, String> attributes) {
        Object attributeValue = attributes.get(attributeName);
        final PrimaryTypeAttributeSpecification attributeSpecification = modelSpecification.getAttributeSpecification(attributeName);
        if (attributeIsNotStringOrEnum(attributeSpecification)) {
            attributeValue = LiteralValueUtil.getValueFor(attributeValue.toString());
        }
        return attributeValue;
    }

    private static boolean attributeIsNotStringOrEnum(final PrimaryTypeAttributeSpecification attributeSpecification) {
        final DataType dataType = attributeSpecification.getDataTypeSpecification().getDataType();
        return !DataType.STRING.equals(dataType) && !DataType.ENUM_REF.equals(dataType);
    }
}
