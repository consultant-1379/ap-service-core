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
package com.ericsson.oss.services.ap.core.dhcp;


import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.services.ap.api.model.DhcpConfiguration;
import com.ericsson.oss.services.ap.api.workflow.DhcpRestClientService;
import com.ericsson.oss.services.ap.core.rest.client.dhcp.api.DhcpConfigurationService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.List;

/**
 * {@inheritDoc}
 */
@Stateless
@EService
public class DhcpConfigurationServiceEjb implements DhcpRestClientService {

    @Inject
    private DhcpConfigurationService dhcpConfigurationService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void create(final String hostName, final String hardwareSerialNumber, final String initialIpAddress, final String defaultRouter,
        final List<String> ntpServers, final List<String> dnsServers) {
        final DhcpConfiguration dhcpConfiguration = new DhcpConfiguration(hardwareSerialNumber, hostName, initialIpAddress, defaultRouter, ntpServers, dnsServers);
        dhcpConfigurationService.create(dhcpConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(final String hostName, final String oldHardwareSerialNumber, final String hardwareSerialNumber, final String initialIpAddress,
        final String defaultRouter, final List<String> ntpServers, final List<String> dnsServers) {
        final DhcpConfiguration dhcpConfiguration = new DhcpConfiguration(hardwareSerialNumber, hostName, initialIpAddress, defaultRouter, ntpServers, dnsServers);
        dhcpConfigurationService.update(oldHardwareSerialNumber, dhcpConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final String hardwareSerialNumber) {
        dhcpConfigurationService.delete(hardwareSerialNumber);
    }
}
