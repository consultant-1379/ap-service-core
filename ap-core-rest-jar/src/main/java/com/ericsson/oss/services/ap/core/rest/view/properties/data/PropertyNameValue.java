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

import java.io.Serializable;

/**
 * Simple properties object with name and value attributes
 */
public class PropertyNameValue implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private Serializable value;

    public PropertyNameValue(final String name, final Serializable value){
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Serializable getValue() {
        return value;
    }

    public void setValue(final Serializable value) {
        this.value = value;
    }
}
