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
package com.ericsson.oss.services.ap.common.workflow.task.order;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.context.classic.ContextConstants;
import com.ericsson.oss.itpf.sdk.context.classic.ContextServiceBean;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.ap.common.workflow.task.common.BpmnErrorKeyHandler;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Abstract class for a service task to provisiong security for an AP node.
 */
public abstract class AbstractGenerateSecurityTask extends AbstractServiceTask {

    protected final ServiceFinderBean serviceFinder = new ServiceFinderBean();
    private final DdpTimer ddpTimer = new DdpTimer();
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ContextServiceBean contextService = new ContextServiceBean();

    @Override
    public void executeTask(final TaskExecution execution) {
        ddpTimer.start(CommandLogName.GENERATE_SECURITY.toString());
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        setUserIdContext(workflowVariables);
        final String apNodeFdn = workflowVariables.getApNodeFdn();
        final String hardwareSerialNumber = workflowVariables.getHardwareSerialNumber();
        logger.info("Executing {} for node {}", getClass().getSimpleName(), apNodeFdn);

        try {
            generateSecurity(apNodeFdn, hardwareSerialNumber);
        } catch (final Exception e) {
            logger.warn("Error executing {} for node {}: {}", getClass().getSimpleName(), apNodeFdn, e.getMessage(), e);
            final String errorKey = BpmnErrorKeyHandler.handleWorkflowForFailedUseCase(workflowVariables);
            throwBpmnError(errorKey, e.getMessage(), execution);
        }
        ddpTimer.end(apNodeFdn);
    }

    /**
     * Generates security for an AP node.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param hardwareSerialNumber
     *            the hardware serial number of the node
     */
    protected abstract void generateSecurity(final String apNodeFdn, final String hardwareSerialNumber);

    private void setUserIdContext(final AbstractWorkflowVariables workflowVariables) {
        final String userId = workflowVariables.getUserId();
        contextService.setContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY, userId);
    }

}
