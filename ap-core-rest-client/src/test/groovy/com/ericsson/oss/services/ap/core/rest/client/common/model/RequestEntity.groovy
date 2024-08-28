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
package com.ericsson.oss.services.ap.core.rest.client.common.model;

class RequestEntity {

    private String clientIdentifier
    private String hostname
    private String fixedAddress
    private String defaultRouter

    RequestEntity(final String clientIdentifier, final String hostname, final String fixedAddress, final String defaultRouter) {
        this.clientIdentifier = clientIdentifier
        this.hostname = hostname
        this.fixedAddress = fixedAddress
        this.defaultRouter = defaultRouter
    }

    String getClientIdentifier() {
        return clientIdentifier
    }

    void setClientIdentifier(final String clientIdentifier) {
        this.clientIdentifier = clientIdentifier
    }

    String getHostname() {
        return hostname
    }

    void setHostname(final String hostname) {
        this.hostname = hostname
    }

    String getFixedAddress() {
        return fixedAddress
    }

    void setFixedAddress(final String fixedAddress) {
        this.fixedAddress = fixedAddress
    }

    String getDefaultRouter() {
        return defaultRouter
    }

    void setDefaultRouter(final String defaultRouter) {
        this.defaultRouter = defaultRouter
    }
}
