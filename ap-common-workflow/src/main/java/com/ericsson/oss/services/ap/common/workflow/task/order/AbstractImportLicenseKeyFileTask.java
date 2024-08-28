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
package com.ericsson.oss.services.ap.common.workflow.task.order;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;


import com.ericsson.oss.services.shm.licenseservice.remoteapi.ImportLicenseRemoteResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.ap.common.workflow.BpmnErrorKey;
import com.ericsson.oss.services.shm.licenseservice.remoteapi.exception.ImportLicenseException;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Abstract class for a service task to import license key file to SHM.
 */
public abstract class AbstractImportLicenseKeyFileTask extends AbstractServiceTask {

    protected ServiceFinderBean serviceFinder = new ServiceFinderBean();

    private final DdpTimer ddpTimer = new DdpTimer();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void executeTask(final TaskExecution execution) {
        ddpTimer.start(CommandLogName.IMPORT_LICENSEKEYFILE.toString());
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        final String apNodeFdn = workflowVariables.getApNodeFdn();
        executeImportLicenseKeyFile(execution, apNodeFdn, workflowVariables);
        ddpTimer.end(apNodeFdn);
    }

    private void executeImportLicenseKeyFile(final TaskExecution execution, final String apNodeFdn, final AbstractWorkflowVariables workflowVariables) {
        try {
            final ImportLicenseRemoteResponse importLicenseRemoteResponse = importLicenseKeyFile(apNodeFdn);
            if (importLicenseRemoteResponse != null) {
                workflowVariables.setFingerPrint(importLicenseRemoteResponse.getFingerPrint());
                workflowVariables.setSequenceNumber(importLicenseRemoteResponse.getSequenceNumber());
            }
        } catch (final Exception e) {
            logger.error("Error executing {} for node {}: {}", this.getClass().getSimpleName(), apNodeFdn, e.getMessage(), e);
            workflowVariables.setOrderSuccessful(false);
            throwBpmnError(BpmnErrorKey.ORDER_WORKFLOW_ERROR_KEY, e.getMessage(), execution);
        }
    }

    /**
     * Import License Key File for the specified AP node.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @return ImportLicenseRemoteResponse
     *             the Import License Remote Response from SHM
     */
    protected abstract ImportLicenseRemoteResponse importLicenseKeyFile(final String apNodeFdn) throws ImportLicenseException;

}
