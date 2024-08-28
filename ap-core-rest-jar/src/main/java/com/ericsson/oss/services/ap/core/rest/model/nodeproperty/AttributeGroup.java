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
package com.ericsson.oss.services.ap.core.rest.model.nodeproperty;

import java.util.List;

/**
 * POJO model for representing an Attribute Group within Node properties.
 */
public class AttributeGroup  {

    private final String type;
    private final List<Object> properties;

    public AttributeGroup(final String type, final List<Object> properties) {
        this.type = type;
        this.properties = properties;
    }

    public String getType() {
        return type;
    }

    public List<Object> getProperties() {
        return properties;
    }
}
