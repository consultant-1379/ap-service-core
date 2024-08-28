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
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.shm.licenseservice.remoteapi.exception.DeleteLicenseException;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Abstract class for a service task to delete LicenseKeyFile if imported via AP
 */
public abstract class AbstractDeleteLicenseKeyFileTask extends AbstractServiceTask {

    protected ServiceFinderBean serviceFinder = new ServiceFinderBean();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private DdpTimer ddpTimer = new DdpTimer(); // NOPMD

    @Override
    public void executeTask(final TaskExecution execution) {
        ddpTimer.start(CommandLogName.DELETE_LICENSE_KEY_FILE.toString());
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);

        final String apNodeFdn = workflowVariables.getApNodeFdn();
        final String fingerPrint = workflowVariables.getFingerPrint();
        final String sequenceNumber = workflowVariables.getSequenceNumber();

        if (StringUtils.isNotEmpty(fingerPrint) && StringUtils.isNotEmpty(sequenceNumber)) {
            logger.info("Executing {} for node {}", getClass().getSimpleName(), apNodeFdn);

            try {
                deleteLicenseKeyFile(fingerPrint, sequenceNumber, apNodeFdn);
            } catch (final Exception e) {
                logger.warn("Error executing {} for node {}: {}", getClass().getSimpleName(), apNodeFdn, e.getMessage(), e);
                workflowVariables.setUnorderOrRollbackError(true);
                ddpTimer.endWithError(apNodeFdn);
            }
        }
        ddpTimer.end(apNodeFdn);
    }

    /**
     * Delete License Key File for the specified AP node.
     *
     * @param fingerPrint
     *            the fingerPrint of the node
     * @param sequenceNumber
     *            the sequenceNumber of the node
     * @param apNodeFdn
     *            the FDN of the AP node
     */
    protected abstract void deleteLicenseKeyFile(final String fingerPrint, final String sequenceNumber, final String apNodeFdn) throws DeleteLicenseException;

}
