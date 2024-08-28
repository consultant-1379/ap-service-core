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
package com.ericsson.oss.services.ap.common.workflow.task.common;

import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.ap.common.workflow.BpmnErrorKey;

/**
 * <p> BpmnErrorKeyHandler is common code which returns / sets the BpmnErrorKey based on the use case failure.</p>
 */
public final class BpmnErrorKeyHandler {

    private BpmnErrorKeyHandler() {
        // not called
    }

    /**
     * @param workflowVariables
     * @return BpmnErrorKey
     */
    public static String handleWorkflowForFailedUseCase(final AbstractWorkflowVariables workflowVariables) {
        if (workflowVariables.isMigrationNodeUsecase()) {
            workflowVariables.setPreMigrationSuccessful(false);
            return BpmnErrorKey.PREMIGRATION_FAIL;
        } else {
            workflowVariables.setOrderSuccessful(false);
            return BpmnErrorKey.ORDER_WORKFLOW_ERROR_KEY;
        }
    }

    /**
     * @param workflowVariables
     */
    public static void handleWorkflowForWarnings(final AbstractWorkflowVariables workflowVariables) {
        if (workflowVariables.isMigrationNodeUsecase()) {
            workflowVariables.setMigrationTaskWarning(true);
        } else {
            workflowVariables.setIntegrationTaskWarning(true);
        }
    }
}
