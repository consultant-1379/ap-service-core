/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.util.capability;

import java.util.List;

/**
 * Define the data model for a required Security Capability
 */
public class SecurityCapability {

    private String resource;
    private List<String> operations;

    /**
     * Get the resource name
     *
     * @return the resource
     */
    public String getResource() {
        return resource;
    }

    /**
     * Set the resource name
     *
     * @param resource
     *            the resource to set
     */
    public void setResource(final String resource) {
        this.resource = resource;
    }

    /**
     * Get the required operations for a resource
     *
     * @return the operations
     */
    public List<String> getOperations() {
        return operations;
    }

    /**
     * Set the required operations for a resource
     *
     * @param operations
     *            the operations to set
     */
    public void setOperations(final List<String> operations) {
        this.operations = operations;
    }
}
