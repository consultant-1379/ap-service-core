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

import com.ericsson.oss.services.ap.api.workflow.AutoProvisioningWorkflowService;
import com.ericsson.oss.services.ap.api.workflow.HardwareReplaceCapabilty;
import com.ericsson.oss.services.ap.api.workflow.ProfileManagementCapability;

/**
 * Resolves the {@link AutoProvisioningWorkflowService} based on the node type. Resolves the {@link HardwareReplaceCapabilty} based on the node type.
 * Resolves the {@link ProfileManagementCapability} based on the capability.
 */
public interface ApWorkflowServiceResolver {

    /**
     * Returns an instance of the {@link AutoProvisioningWorkflowService} for the given node type.
     * <p>
     * Can be used to retrieve workflow names and supported commands for a given node type.
     *
     * @param nodeType
     *            the type of the node
     * @return an instance of the AutoProvisioningWorkflowService
     */
    AutoProvisioningWorkflowService getApWorkflowService(final String nodeType);

    /**
     * Returns an instance of the {@link HardwareReplaceCapabilty} for the given node type.
     * <p>
     * Can be used to retrieve supported commands for a given node type.
     *
     * @param nodeType
     *            the type of the node
     * @return an instance of the HardwareReplaceCapabilty
     */
    HardwareReplaceCapabilty getHardwareReplaceCapability(final String nodeType);

    /**
     * Returns an instance of the {@link ProfileManagementCapability} for the given capability
     *
     * @param capability
     *            the capability of Profile Management
     * @return an instance of the ProfileManagementCapability
     */
    ProfileManagementCapability getProfileManagementCapability(final String capability);

}
