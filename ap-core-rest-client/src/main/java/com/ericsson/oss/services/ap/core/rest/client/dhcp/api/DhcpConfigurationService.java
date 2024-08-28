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
package com.ericsson.oss.services.ap.core.rest.client.dhcp.api;

import com.ericsson.oss.services.ap.api.model.DhcpConfiguration;

/**
 * Interface to DHCP Configuration service which allow to create, update and delete DHCP configuration for node<br>
 * Any of method may throw {@link com.ericsson.oss.services.ap.api.exception.DhcpRestServiceException}
 */
public interface DhcpConfigurationService {

    /**
     * Send request to DHCP Configuration service to create configuration for node
     *
     * @param dhcpConfiguration - DHCP configuration to create client
     * @return
     *          REST request
     */
    String create(final DhcpConfiguration dhcpConfiguration);

    /**
     * Send request to DHCP Configuration service to update node with given configuration
     *
     * @param configurationId - clientID in DHCP configuration to update configuration
     * @param dhcpConfiguration - DHCP configuration to update client
     * @return
     *          REST request
     */
    String update(final String configurationId, final DhcpConfiguration dhcpConfiguration);

    /**
     * Send request to DHCP Configuration service to delete node configuration
     *
     * @param configurationId - clientID in DHCP configuration to remove configuration
     * @return
     *          REST request
     */
    Boolean delete(final String configurationId);
}
