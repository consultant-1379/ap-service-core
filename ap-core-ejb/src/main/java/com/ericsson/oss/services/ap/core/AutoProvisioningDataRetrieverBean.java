/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.services.ap.api.AutoProvisioningDataRetriever;
import com.ericsson.oss.services.ap.core.usecase.workflow.ApWorkflowServiceResolver;

/**
 * Implementation of AutoProvisioningDataRetriever
 */
@Stateless
@EService
public class AutoProvisioningDataRetrieverBean implements AutoProvisioningDataRetriever {

    public static final String SUPPORTED_NODE_TYPES = "supportedNodeTypes";

    @Inject
    ApWorkflowServiceResolver apWorkflowServiceResolver;

    /**
     * Returns supported node types for Profile Management
     *
     * @return {@link List} of supported node types
     */
    @Override
    public List<String> getSupportedNodeTypes() {
        return apWorkflowServiceResolver.getProfileManagementCapability(SUPPORTED_NODE_TYPES).getSupportedNodeTypes();
    }
}
