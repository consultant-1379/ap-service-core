/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.util.capability;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.google.gson.Gson;

/**
 * The node capability model utility.
 */
public enum NodeCapabilityModel {
    INSTANCE;

    private static final String NODE_CAPABILTIY_JSON_PATH = "nodecapability/nodecapability.json";
    private static final String DEFAULT_NODE_TYPE = "default";
    private static final String GET_ATTRIBUTE_EXCEPTION_FORMAT = "Cannot get capability [%s, %s, %s] from model. Reason: %s.";
    private static final String LOAD_MODEL_EXCEPTION_FORMAT = "Cannot load capability from model. Reason: %s.";
    private static final String CAST_FAILURE = "Cast failure";
    private static final String UNDEFINED_FAILURE = "Capability is undefined";
    private final Map<?, ?> capabilityModel = loadCapabilityModel();

    /**
     * The base function to get the value from capability model.
     *
     * @param nodeType
     *            the generic node type, such as "RadionNode" or "ecim"
     * @param useCase
     *            the use case
     * @param attributeName
     *            the name of attribute
     * @return the value of one capability
     */
    public Object getAttributeValue(final String nodeType, final String useCase, final String attributeName) {
        final Map<?, ?> nodeMap = (Map<?, ?>) capabilityModel.get(nodeType);

        if (nodeMap != null) {
            final Map<?, ?> attributeMap = (Map<?, ?>) nodeMap.get(useCase);
            if (attributeMap != null) {
                final Object attribute = attributeMap.get(attributeName);
                if (attribute != null) {
                    return attribute;
                }
            }
        }
        return getDefaultValue(nodeType, useCase, attributeName);
    }

    /**
     * Get Boolean value from capability model.
     *
     * @param nodeType
     *            the generic node type, such as RadionNode or ECIM
     * @param useCase
     *            the use case
     * @param attributeName
     *            the name of attribute
     * @return the Boolean value of one capability
     */
    public Boolean getAttributeAsBoolean(final String nodeType, final String useCase, final String attributeName) {

        final Object obj = getAttributeValue(nodeType, useCase, attributeName);
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        throw new ApApplicationException(String.format(GET_ATTRIBUTE_EXCEPTION_FORMAT, nodeType, useCase, attributeName, CAST_FAILURE));
    }

    /**
     * Get String value from capability model.
     *
     * @param nodeType
     *            the generic node type, such as RadionNode or ECIM
     * @param useCase
     *            the use case
     * @param attributeName
     *            the name of attribute
     * @return the String value of one capability
     */
    public String getAttributeAsString(final String nodeType, final String useCase, final String attributeName) {

        final Object obj = getAttributeValue(nodeType, useCase, attributeName);
        if (obj instanceof String) {
            return (String) obj;
        }
        throw new ApApplicationException(String.format(GET_ATTRIBUTE_EXCEPTION_FORMAT, nodeType, useCase, attributeName, CAST_FAILURE));
    }

    private Map<?, ?> loadCapabilityModel() {
        final Gson gson = new Gson();
        try {
            final InputStream sourceFile = Thread.currentThread().getContextClassLoader().getResourceAsStream(NODE_CAPABILTIY_JSON_PATH);
            return gson.fromJson(new InputStreamReader(sourceFile), Map.class);
        } catch (final Exception e) {
            throw new ApApplicationException(String.format(LOAD_MODEL_EXCEPTION_FORMAT, NODE_CAPABILTIY_JSON_PATH));
        }

    }

    private Object getDefaultValue(final String nodeType, final String useCase, final String attributeName) {
        final Map<?, ?> defaultNodeMap = (Map<?, ?>) capabilityModel.get(DEFAULT_NODE_TYPE);

        if (defaultNodeMap != null) {
            final Map<?, ?> attributeMap = (Map<?, ?>) defaultNodeMap.get(useCase);

            if (attributeMap != null) {
                final Object attribute = attributeMap.get(attributeName);
                if (attribute != null) {
                    return attribute;
                }
            }
        }

        throw new ApApplicationException(String.format(GET_ATTRIBUTE_EXCEPTION_FORMAT, nodeType, useCase, attributeName, UNDEFINED_FAILURE));
    }

}
