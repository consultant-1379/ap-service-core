/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api;

public enum NodePluginCapabilityVersion {
    V0("v0"),
    V1("v1");

    private final String version;

    private NodePluginCapabilityVersion(final String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return this.version;
    }

}
