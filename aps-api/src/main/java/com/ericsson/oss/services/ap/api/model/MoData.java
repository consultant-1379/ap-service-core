/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Data container for Managed Object data.
 */
public class MoData implements Serializable {

    private static final long serialVersionUID = -7086137129736225866L;

    private final String fdn;
    private final String type;
    private final ModelData modelData;
    private final Map<String, Object> attributes; //NOSONAR

    public MoData(final String fdn, final Map<String, Object> attributes, final String type, final ModelData modelData) {
        this.fdn = fdn;
        this.type = type;
        this.attributes = attributes;
        this.modelData = modelData;
    }

    /**
     * Get the FDN of the MO.
     *
     * @return the FDN
     */
    public String getFdn() {
        return fdn;
    }

    /**
     * Get the attributes for the MO.
     *
     * @return the attributes of the MO
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Get a specific attribute value based on the attribute name.
     *
     * @param attributeName
     *            the attribute value to retrieve
     * @return the attributes or null if attribute is not found
     */
    public Object getAttribute(final String attributeName) {
        for (final Entry<String, Object> attributeEntry : attributes.entrySet()) {
            if (attributeEntry.getKey().equals(attributeName)) {
                return attributeEntry.getValue();
            }
        }
        return null;
    }

    /**
     * Get the MO type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Get the {@link ModelData} associated with the MO.
     *
     * @return the modelData
     */
    public ModelData getModelData() {
        return modelData;
    }
}
