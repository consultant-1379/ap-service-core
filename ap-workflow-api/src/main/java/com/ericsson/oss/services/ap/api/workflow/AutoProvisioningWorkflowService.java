/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
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
 * Interface to get information from each of the deployed workflows for different node types.
 */
public interface AutoProvisioningWorkflowService {

    /**
     * Gets the name of the delete workflow.
     *
     * @return the name of the delete workflow
     */
    String getDeleteWorkflowName();

    /**
     * Gets the name of the order workflow.
     *
     * @return the name of the Order workflow
     */
    String getOrderWorkflowName();

    /**
     * Gets the name of the hardware replace workflow.
     *
     * @return the name of the Hardware Replace workflow
     */
    String getHardwareReplaceWorkflowName();

    /**
     * Checks if messageCorrelation key is supported by the workflow.
     *
     * @param messageCorrelationKey
     *            the correlation key to check
     * @return true if the correlation key is supported
     */
    boolean isSupported(String messageCorrelationKey);

    /**
     * Gets the name of the reconfiguration order workflow.
     *
     * @return the name of the Reconfiguration Order workflow
     */
    String getReconfigurationOrderWorkflowName();

    /**
     * Gets the name of the expansion order workflow.
     *
     * @return the name of the Expansion Order workflow
     */
    String getExpansionOrderWorkflowName();

    /**
     * Returns a List of all workflow names
     *
     * @return list of all supported workflow names
     */
    List<String> getAllWorkflowNames();

    /**
     * Gets the name of the Migration order workflow.
     *
     * @return the name of the Migration Order workflow
     */
    String getMigrationWorkflowName();


    /**
     * Gets the name of the Eoi Integartion workflow.
     *
     * @return the name of the Migration Order workflow
     */
    String getEoiIntegrationWorkflow();

}
