/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
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
 * An attribute in <code>CmNodeHeartbeatSupervision</code> model.
 */
public enum CmNodeHeartbeatSupervisionAttribute {

    HEARTBEAT_INTERVAL("heartbeatInterval"),
    HEARTBEAT_TIMEOUT("heartbeatTimeout"),
    HEARTBEAT_TIMESTAMP("heartbeatTimestamp"),
    NUMBER_OF_RETRIES("numberOfRetries"),
    ACTIVE("active");

    private String attributeName;

    private CmNodeHeartbeatSupervisionAttribute(final String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public String toString() {
        return attributeName;
    }
}
