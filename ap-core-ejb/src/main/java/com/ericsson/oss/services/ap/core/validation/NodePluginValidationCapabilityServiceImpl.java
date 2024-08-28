/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.validation;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.services.ap.api.NodePluginCapabilityType;
import com.ericsson.oss.services.ap.api.NodePluginCapabilityVersion;
import com.ericsson.oss.services.ap.api.workflow.NodePluginCapabilityValidationService;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration.NodePluginRestClient;
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration.util.NodePluginCapability;

@EService
@Stateless
public class NodePluginValidationCapabilityServiceImpl implements NodePluginCapabilityValidationService {

    @Inject
    private NodePluginRestClient nodePluginRestClient;

    @Override
    public boolean validateCapability(final String nodeType, final NodePluginCapabilityVersion version, final NodePluginCapabilityType capability) {
        final List<NodePluginCapability> capabilitiesList = nodePluginRestClient.getCapabilities(nodeType);
        if (capabilitiesList != null) {
            return capabilitiesList.stream()
                    .anyMatch(capablity -> capablity.getVersionOfInterface().equals(version.toString())
                            && capablity.getCapabilities().contains(capability.toString()));
        }
        return false;
    }

}
