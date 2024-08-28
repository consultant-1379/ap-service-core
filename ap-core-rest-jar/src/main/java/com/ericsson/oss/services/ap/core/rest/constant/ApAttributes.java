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
package com.ericsson.oss.services.ap.core.rest.constant;

/**
 * Enum representing local AP constant attributes.
 */
public enum ApAttributes {

    CREATION_DATE("creationDate"),
    CREATOR("creator"),
    DESCRIPTION("description"),
    GENERATED_BY("generatedby"),
    NODE_QUANTITY("nodeQuantity"),
    PROJECT_NAME("projectName"),
    NODE_ID("NodeId"),
    NODE_IDENTIFIER("nodeIdentifier"),
    IP_ADDRESS("ipAddress"),
    WORK_ORDER_ID("workOrderId"),
    NODE_TYPE("nodeType"),
    HARDWARE_SERIAL_NUMBER("hardwareSerialNumber"),
    NODES("nodes"),
    INTEGRATION_PROFILE_ID("integrationProfile"),
    EXPANSION_PROFILE_ID("expansionProfile");

    private final String attributeName;

    ApAttributes(final String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeName() {
        return attributeName;
    }
}
