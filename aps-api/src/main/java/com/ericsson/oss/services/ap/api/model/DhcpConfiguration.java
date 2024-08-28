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
 -----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object to communicate with DHCP Service
 */
public class DhcpConfiguration implements Serializable {

    private static final long serialVersionUID = 7543512054574829794L;

    private String clientIdentifier;
    private String hostname;
    private String fixedAddress;
    private String defaultRouter;
    private List<String> ntpServers;
    private List<String> domainNameServers;

    public DhcpConfiguration() {
    }

    public DhcpConfiguration(final String clientIdentifier, final String hostname, final String fixedAddress, final String defaultRouter,
            final List<String> ntpServers, final List<String> dnsServers) {
        this.clientIdentifier = clientIdentifier;
        this.hostname = hostname;
        this.fixedAddress = fixedAddress;
        this.defaultRouter = defaultRouter;
        this.ntpServers = ntpServers;
        this.domainNameServers = dnsServers;
    }

    public String getClientIdentifier() {
        return clientIdentifier;
    }

    public void setClientIdentifier(final String clientIdentifier) {
        this.clientIdentifier = clientIdentifier;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    public String getFixedAddress() {
        return fixedAddress;
    }

    public void setFixedAddress(final String fixedAddress) {
        this.fixedAddress = fixedAddress;
    }

    public String getDefaultRouter() {
        return defaultRouter;
    }

    public void setDefaultRouter(final String defaultRouter) {
        this.defaultRouter = defaultRouter;
    }

    public List<String> getNtpServers() {
        return ntpServers;
    }

    public void setNtpServers(List<String> ntpServers) {
        this.ntpServers = ntpServers;
    }

    public List<String> getDomainNameServers() {
        return domainNameServers;
    }

    public void setDomainNameServers(List<String> dnsServers) {
        this.domainNameServers = dnsServers;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        final DhcpConfiguration that = (DhcpConfiguration) o;
        return Objects.equals(clientIdentifier, that.clientIdentifier) &&
                Objects.equals(hostname, that.hostname) &&
                Objects.equals(fixedAddress, that.fixedAddress) &&
                Objects.equals(defaultRouter, that.defaultRouter) &&
                Objects.equals(ntpServers, that.ntpServers) &&
                Objects.equals(domainNameServers, that.domainNameServers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientIdentifier, hostname, fixedAddress, defaultRouter, ntpServers, domainNameServers);
    }

    @Override
    public String toString() {
        return "DhcpConfiguration{" +
            "clientIdentifier='" + clientIdentifier + '\'' +
            ", hostname='" + hostname + '\'' +
            ", fixedAddress='" + fixedAddress + '\'' +
            ", defaultRouter='" + defaultRouter + '\'' +
            ", ntpServers=" + ntpServers +
            ", domainNameServers=" + domainNameServers +
            '}';
    }
}
