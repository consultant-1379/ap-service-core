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
package com.ericsson.oss.services.ap.core.rest.view.properties.data;

import java.util.List;

/**
 * Object containing a property type and corresponding list of properties
 */
public class ViewProperties {

    private final String type;
    private final List<Object> properties;

    public ViewProperties(final String type, final List<Object> properties) {
        this.type = type;
        this.properties = properties;
    }

    public List<Object> getProperties() {
        return properties;
    }

    public String getType() {
        return type;
    }

}
