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

import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.ap.core.metadata.cli.api.AttributeMetadata;

/**
 * This fluent interface wraps the class that performs the attribute searches {@link DataTypeAttributeSearch}. Example usage:
 *
 * <pre>
 * List{@literal <}Map{@literal <}AttributeMetadata, Object{@literal >}{@literal >} attributeValueFromDataObjects = new DataTypeAttributeSearchBuilder()
 *         .findAttribute(attributeMetadata)
 *         .inDataType(dataTypeHolder.getDataType())
 *         .fromDataObjects(data)
 *         .execute();
 * </pre>
 */
public class DataTypeAttributeSearchBuilder {

    private final DataTypeAttributeSearch searchWorker;

    public DataTypeAttributeSearchBuilder() {
        searchWorker = new DataTypeAttributeSearch();
    }

    public DataTypeAttributeSearchBuilder findAttributes(final List<AttributeMetadata> attributes) {
        searchWorker.setRequiredAttributes(attributes);
        return this;
    }

    public DataTypeAttributeSearchBuilder inDataType(final String dataType) {
        final String[] dataTypeSplit = dataType.split(":");
        final String dataTypePropertyName = dataTypeSplit[0];
        final String requiredDataType = dataTypeSplit[1];

        searchWorker.setRequiredDataType(requiredDataType);
        searchWorker.setDataTypePropertyName(dataTypePropertyName);

        return this;
    }

    public DataTypeAttributeSearchBuilder withFilter(final String filter) {
        searchWorker.setFilter(filter);
        return this;
    }

    public DataTypeAttributeSearchBuilder fromDataObjects(final List<? extends Object> dataObjects) {
        searchWorker.setDataObjects(dataObjects);
        return this;
    }

    public List<Map<AttributeMetadata, Object>> execute() {
        return searchWorker.findAttributes();
    }
}
