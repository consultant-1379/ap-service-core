/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.workflow.task.integrate;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.context.classic.ContextConstants;
import com.ericsson.oss.itpf.sdk.context.classic.ContextServiceBean;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Abstract class for a service task to delete a DHCP Configuration for specific client.
 */
public abstract class AbstractDhcpRemoveClientConfigurationTask extends AbstractServiceTask {

    protected final ServiceFinderBean serviceFinder = new ServiceFinderBean();
    private ContextServiceBean contextService = new ContextServiceBean(); // NOPMD

    private final DdpTimer ddpTimer = new DdpTimer();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void executeTask(final TaskExecution execution) {
        ddpTimer.start(CommandLogName.DHCP_DELETE_CLIENT.toString());
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        setUserIdContext(workflowVariables);
        final String apNodeFdn = workflowVariables.getApNodeFdn();
        logger.info("Executing {} for node {}", getClass().getSimpleName(), apNodeFdn);
        executeDhcpRemoveClient(workflowVariables);
        ddpTimer.end(apNodeFdn);
    }

    /**
     * Delete DHCP Configuration for the specified client identifier.
     *
     * @param hardwareSerialNumber
     */
    protected abstract void dhcpRemoveClient(final String apNodeFdn, final String hardwareSerialNumber);

    private void executeDhcpRemoveClient(final AbstractWorkflowVariables workflowVariables) {
        final String apNodeFdn = workflowVariables.getApNodeFdn();
        final String oldHardwareSerialNumber = workflowVariables.getOldHardwareSerialNumber();
        final boolean isDhcpConfiguration = workflowVariables.isDhcpConfiguration();
        try {
            if (isDhcpConfiguration && isNotBlank(oldHardwareSerialNumber)) {
                dhcpRemoveClient(apNodeFdn, oldHardwareSerialNumber);
            }
        } catch (final Exception e) {
            logger.error("Error executing {} for node {}: {}", this.getClass().getSimpleName(), apNodeFdn, e.getMessage(), e);
        }
    }

    private void setUserIdContext(final AbstractWorkflowVariables workflowVariables) {
        final String userId = workflowVariables.getUserId();
        contextService.setContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY, userId);
    }
}