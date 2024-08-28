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
package com.ericsson.oss.services.ap.core.rest.client.dhcp;

import com.ericsson.oss.itpf.sdk.security.accesscontrol.EAccessControl;
import com.ericsson.oss.services.ap.api.exception.DhcpRestServiceException;
import com.ericsson.oss.services.ap.api.model.DhcpConfiguration;
import com.ericsson.oss.services.ap.core.rest.client.RestUrls;
import com.ericsson.oss.services.ap.core.rest.client.common.RestRequest;
import com.ericsson.oss.services.ap.core.rest.client.dhcp.api.DhcpConfigurationService;
import com.ericsson.oss.services.ap.core.rest.client.dhcp.api.model.ResponseErrorDetails;

import javax.inject.Inject;
import java.util.Optional;

import static com.ericsson.oss.services.ap.core.rest.client.common.HttpMethods.DELETE;
import static com.ericsson.oss.services.ap.core.rest.client.common.HttpMethods.POST;
import static com.ericsson.oss.services.ap.core.rest.client.common.HttpMethods.PUT;
import static com.ericsson.oss.services.ap.core.rest.client.common.RestResponse.getDefaultResponseHandler;

/**
 * {@inheritDoc}
 */
public class DhcpConfigurationRestService implements DhcpConfigurationService {

    private EAccessControl eAccessControl;

    @Inject
    public DhcpConfigurationRestService(final EAccessControl eAccessControl) {
        this.eAccessControl = eAccessControl;
    }

    public DhcpConfigurationRestService() {
        //No-arg constructor required by application server
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String create(final DhcpConfiguration dhcpConfiguration) {

        return RestRequest.Builder.of(RestUrls.DHCP_CONFIGURATION_SERVICE.getFullUrl())
            .setMethod(POST)
            .setEntity(dhcpConfiguration)
            .setAuthorization(eAccessControl.getAuthUserSubject().getSubjectId())
            .setHeader("host", RestUrls.DHCP_CONFIGURATION_SERVICE.getHost())
            .build()
            .send(getDefaultResponseHandler(DhcpConfiguration.class, ResponseErrorDetails.class))
            .ifFailure(this::failureHandler)
            .getData()
            .map(DhcpConfiguration::getClientIdentifier)
            .orElseThrow(() -> new DhcpRestServiceException("Problem with parsing response"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String update(final String clientIdentifier, final DhcpConfiguration dhcpConfiguration) {
        return RestRequest.Builder.of(RestUrls.DHCP_CONFIGURATION_SERVICE.getFullUrl())
            .setMethod(PUT)
            .setResourceId(clientIdentifier)
            .setEntity(dhcpConfiguration)
            .setAuthorization(eAccessControl.getAuthUserSubject().getSubjectId())
            .setHeader("host", RestUrls.DHCP_CONFIGURATION_SERVICE.getHost())
            .build()
            .send(getDefaultResponseHandler(DhcpConfiguration.class, ResponseErrorDetails.class))
            .ifFailure(this::failureHandler)
            .getData()
            .map(DhcpConfiguration::getClientIdentifier)
            .orElseThrow(() -> new DhcpRestServiceException("Problem with parsing response"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean delete(final String clientIdentifier) {
        return RestRequest.Builder.of(RestUrls.DHCP_CONFIGURATION_SERVICE.getFullUrl())
            .setMethod(DELETE)
            .setResourceId(clientIdentifier)
            .setAuthorization(eAccessControl.getAuthUserSubject().getSubjectId())
            .setHeader("host", RestUrls.DHCP_CONFIGURATION_SERVICE.getHost())
            .build()
            .send(getDefaultResponseHandler(Void.class, ResponseErrorDetails.class))
            .ifFailure(this::failureHandler)
            .isValid();
    }

    private void failureHandler(final Optional<ResponseErrorDetails> optionalErrorDetails) {
        final ResponseErrorDetails errorDetails = optionalErrorDetails.orElseThrow(() -> new DhcpRestServiceException("Problem with parsing response."));
        throw new DhcpRestServiceException(errorDetails.getMessage());
    }
}
