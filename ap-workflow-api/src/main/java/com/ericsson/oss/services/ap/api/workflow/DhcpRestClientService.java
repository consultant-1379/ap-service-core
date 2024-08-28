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

package com.ericsson.oss.services.ap.api.workflow;

import java.util.List;

/**
 * Interface to DHCP Configuration service which allow to create, update and delete DHCP configuration for node<br>
 * Any of method may {@code throw} DhcpRestServiceException
 */
public interface DhcpRestClientService {

    /**
     * Send request to DHCP Configuration service to create configuration for node
     *
     * @param hostName             - used as hostname in DHCP configuration
     * @param hardwareSerialNumber - used as ClientID in DHCP configuration
     * @param initialIpAddress     - fix IP address for node
     * @param defaultRouter        - IP address of default router
     * @param ntpServers           - list of NTP Servers
     * @param dnsServers           - list of DNS Servers
     */
    void create(final String hostName, final String hardwareSerialNumber, final String initialIpAddress, final String defaultRouter,
        final List<String> ntpServers, final List<String> dnsServers);

    /**
     * Send request to DHCP Configuration service to update node configuration with given parameters
     *
     * @param hostName                - used as hostname in DHCP configuration
     * @param oldHardwareSerialNumber - used as ClientID in DHCP configuration to remove previous configuration
     * @param hardwareSerialNumber    - new node hardware serial number used as ClientID in DHCP configuration
     * @param initialIpAddress        - fix IP address for node
     * @param defaultRouter           - IP address of default router
     * @param ntpServers              - list of NTP Servers
     * @param dnsServers              - list of DNS Servers
     */
    void update(final String hostName, final String oldHardwareSerialNumber, final String hardwareSerialNumber, final String initialIpAddress,
        final String defaultRouter, final List<String> ntpServers, final List<String> dnsServers);

    /**
     * Send request to DHCP Configuration service to delete node configuration
     *
     * @param hardwareSerialNumber - used as ClientID in DHCP configuration to remove configuration
     */
    void delete(final String hardwareSerialNumber);
}