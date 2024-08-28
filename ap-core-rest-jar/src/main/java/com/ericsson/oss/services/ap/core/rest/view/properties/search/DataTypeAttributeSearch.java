/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.view.properties.search;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.core.metadata.cli.api.AttributeMetadata;

/**
 * Searches through a list of data objects to find attributes from a given type. This object is built using {@link DataTypeAttributeSearchBuilder}.
 */
class DataTypeAttributeSearch {

    private static final ThreadLocal<ScriptEngine> LOCAL_INTERPRETER = new ThreadLocal<ScriptEngine>() {

        @Override
        protected ScriptEngine initialValue() {
            final ScriptEngineManager manager = new ScriptEngineManager();
            return manager.getEngineByName("nashorn");
        }
    };

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String requiredDataType;
    private String dataTypePropertyName;
    private List<? extends Object> dataObjects;
    private List<AttributeMetadata> requiredAttributesPropertyName;
    private String filter;

    /**
     * The data source to search.
     *
     * @param dataObjects
     */
    public void setDataObjects(final List<? extends Object> dataObjects) {
        this.dataObjects = dataObjects;
    }

    /**
     * The attributes to search for.
     *
     * @param requiredAttributes
     */
    public void setRequiredAttributes(final List<AttributeMetadata> requiredAttributes) {
        requiredAttributesPropertyName = requiredAttributes;
    }

    /**
     * The data type to find attributes in.
     *
     * @param requiredDataType
     */
    public void setRequiredDataType(final String requiredDataType) {
        this.requiredDataType = requiredDataType;
    }

    /**
     * The property name that identifies the type of the data object.
     *
     * @param dataTypePropertyName
     */
    public void setDataTypePropertyName(final String dataTypePropertyName) {
        this.dataTypePropertyName = dataTypePropertyName;
    }

    /**
     * The filter by which the data is filtered. May be empty or null.
     *
     * @param filter
     */
    public void setFilter(final String filter) {
        this.filter = filter;
    }

    /**
     * Searches through the data to find the required attributes.
     *
     * @return list containing a map for each attribute required. The map contains the name value pair for the attribute.
     */
    public List<Map<AttributeMetadata, Object>> findAttributes() {
        logger.debug("Searching for attributes {} in type {}", requiredAttributesPropertyName, requiredDataType);
        final List<Object> matchingDataObjects = findDataObjectsMatchingRequiredDataType();
        final List<Map<AttributeMetadata, Object>> requiredAttributes = new ArrayList<>(matchingDataObjects.size());

        for (final Object dataObject : matchingDataObjects) {
            final Map<AttributeMetadata, Object> attributes = findRequiredAttributesFromDataObject(dataObject);
            if (!attributes.isEmpty()) {
                requiredAttributes.add(attributes);
            }
        }

        return requiredAttributes;
    }

    private List<Object> findDataObjectsMatchingRequiredDataType() {
        final List<Object> matchingDataObjects = new ArrayList<>(dataObjects.size());
        for (final Object dataObject : dataObjects) {
            final String objectDataType = getDataTypeOfDataObject(dataObject);

            if (isValidDataObject(dataObject, objectDataType)) {
                matchingDataObjects.add(dataObject);
            }
        }

        return matchingDataObjects;
    }

    private boolean isValidDataObject(final Object dataObject, final String objectDataType) {
        return objectDataType != null && objectDataType.equals(requiredDataType) && matchesFilter(dataObject);
    }

    private boolean matchesFilter(final Object dataObject) {
        if (StringUtils.isEmpty(filter)) {
            return true;
        }

        try {
            return checkDataObject(dataObject);
        } catch (final ScriptException e) {
            logger.warn("There was an error evaluating the expression '{}' for {}, so returning a default of false", filter, dataObject, e);
            return false;
        }
    }

    private boolean checkDataObject(final Object dataObject) throws ScriptException {
        final ScriptEngine engine = LOCAL_INTERPRETER.get();
        engine.put("item", dataObject);
        return (Boolean) engine.eval(filter);
    }

    private String getDataTypeOfDataObject(final Object dataObject) {
        try {
            return (String) PropertyUtils.getProperty(dataObject, dataTypePropertyName);
        } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.warn("Cannot call 'getType' method on metadata object", e);
            return null;
        }
    }

    private Map<AttributeMetadata, Object> findRequiredAttributesFromDataObject(final Object dataObject) {
        final Map<AttributeMetadata, Object> attributesFound = new LinkedHashMap<>();
        for (final AttributeMetadata requiredAttribute : requiredAttributesPropertyName) {
            final Object requiredAttributeValue = findRequiredAttributeFromDataObject(dataObject, requiredAttribute.getName());
            attributesFound.put(requiredAttribute, requiredAttributeValue);
        }

        return attributesFound;
    }

    private Object findRequiredAttributeFromDataObject(final Object dataObject, final String requiredAttributePropertyName) {
        try {
            final Object value = PropertyUtils.getProperty(dataObject, requiredAttributePropertyName);
            return value == null ? "" : value;
        } catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.warn("Cannot call 'getAttributes' method on metadata object", e);
            return null;
        }
    }
}
