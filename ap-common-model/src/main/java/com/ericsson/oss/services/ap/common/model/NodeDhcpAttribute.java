/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
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
 * Attribute names in the <code>NodeDhcp</code> MO model.
 */
public enum NodeDhcpAttribute {

    INITIAL_IP_ADDRESS("initialIpAddress"),
    DEFAULT_ROUTER("defaultRouter"),
    NTP_SERVER("ntpServer"),
    DNS_SERVER("dnsServer");

    private final String attributeName;

    NodeDhcpAttribute(final String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public String toString() {
        return attributeName;
    }
}