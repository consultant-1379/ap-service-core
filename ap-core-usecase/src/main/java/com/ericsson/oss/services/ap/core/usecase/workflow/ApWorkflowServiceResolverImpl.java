/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.workflow;

import java.util.Locale;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.workflow.AutoProvisioningWorkflowService;
import com.ericsson.oss.services.ap.api.workflow.HardwareReplaceCapabilty;
import com.ericsson.oss.services.ap.api.workflow.ProfileManagementCapability;

/**
 * Implementation class for resolving {@link AutoProvisioningWorkflowService} based on the node type.
 */
public class ApWorkflowServiceResolverImpl implements ApWorkflowServiceResolver {

    private final ServiceFinderBean serviceFinder = new ServiceFinderBean();

    @Override
    public AutoProvisioningWorkflowService getApWorkflowService(final String nodeType) {
        return serviceFinder.find(AutoProvisioningWorkflowService.class, nodeType.toLowerCase(Locale.US));
    }

    @Override
    public HardwareReplaceCapabilty getHardwareReplaceCapability(final String nodeType) {
        return serviceFinder.find(HardwareReplaceCapabilty.class, nodeType.toLowerCase(Locale.US));
    }

    @Override
    public ProfileManagementCapability getProfileManagementCapability(final String capability) {
        return serviceFinder.find(ProfileManagementCapability.class, capability);
    }
}
