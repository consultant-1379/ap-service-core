/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.model;

/**
 * Attributes in the NetworkElement model.
 */
public enum NetworkElementAttribute {

    ACTIVE("active"),
    IP_ADDRESS("ipAddress"),
    NE_TYPE("neType"),
    NETWORK_ELEMENT_ID("networkElementId"),
    OSS_MODEL_IDENTITY("ossModelIdentity"),
    OSS_PREFIX("ossPrefix"),
    PLATFORM_TYPE("platformType"),
    TIMEZONE("timeZone"),
    MANAGEMENT_STATE("managementState"),
    USER_LABEL("userLabel");

    private String attributeName;

    private NetworkElementAttribute(final String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public String toString() {
        return attributeName;
    }
}
