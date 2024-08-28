/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.restore;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;

import javax.annotation.PostConstruct;

/**
 * Actions that can be performed on a workflow.
 * <p>
 * The restore algorithm will decide on what action to perform on a workflow.
 */
public class WorkflowActions {

    private WorkflowInstanceServiceLocal wfsInstanceService;

    @PostConstruct
    public void init() {
        wfsInstanceService = new ServiceFinderBean().find(WorkflowInstanceServiceLocal.class);
    }


    /**
     * Cancel the workflow.
     *
     * @param suspendedWfInstanceId
     *            ID of the workflow to cancel
     */
    public void cancelWorkflow(final String suspendedWfInstanceId) {
        wfsInstanceService.cancelWorkflowInstance(suspendedWfInstanceId);
    }

    /**
     * Resume the workflow.
     *
     * @param suspendedWfInstanceId
     *            ID of the workflow to resume
     */
    public void resumeWorkflow(final String suspendedWfInstanceId) {
        wfsInstanceService.activateInstance(suspendedWfInstanceId);
    }
}
