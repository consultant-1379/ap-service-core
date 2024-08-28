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
package com.ericsson.oss.services.ap.common.workflow.task.order;

import static com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables.WORKFLOW_VARIABLES_KEY;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.model.DhcpConfiguration;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.common.workflow.AbstractWorkflowVariables;
import com.ericsson.oss.services.wfs.task.api.AbstractServiceTask;
import com.ericsson.oss.services.wfs.task.api.TaskExecution;

/**
 * Abstract class for a service task to create a DHCP Configuration for an AP node.
 */
public abstract class AbstractDhcpConfigurationTask extends AbstractServiceTask {

    protected final ServiceFinderBean serviceFinder = new ServiceFinderBean();

    private final DdpTimer ddpTimer = new DdpTimer();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void executeTask(final TaskExecution execution) {
        ddpTimer.start(CommandLogName.DHCP_CONFIGURATION.toString());
        final AbstractWorkflowVariables workflowVariables = (AbstractWorkflowVariables) execution.getVariable(WORKFLOW_VARIABLES_KEY);
        final String apNodeFdn = workflowVariables.getApNodeFdn();
        logger.info("Executing {} for node {}", getClass().getSimpleName(), apNodeFdn);
        executeDhcpConfiguration(execution, workflowVariables, apNodeFdn);
        ddpTimer.end(apNodeFdn);
    }

    /**
     * Creates the DHCP Configuration for the specified AP node.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @param oldHardwareSerialNumber
     *            previous hardware serial number of the node
     * @param dhcpConfiguration
     *            dto for DHCP service
     * @return result of creates the DHCP Configuration
     */
    protected abstract boolean dhcpConfiguration(final String apNodeFdn, final String oldHardwareSerialNumber,
        final DhcpConfiguration dhcpConfiguration);

    private void executeDhcpConfiguration(final TaskExecution execution, final AbstractWorkflowVariables workflowVariables, final String apNodeFdn) {
        try {
            final boolean isDhcpConfiguration = workflowVariables.isDhcpConfiguration();
            final String hardwareSerialNumber = workflowVariables.getHardwareSerialNumber();

            if (isDhcpConfiguration && isNotBlank(hardwareSerialNumber)) {

                final String initialIpAddress = workflowVariables.getInitialIpAddress();
                final String defaultRouter = workflowVariables.getDefaultRouter();
                final List<String> ntpServers = workflowVariables.getNtpServers();
                final List<String> dnsServers = workflowVariables.getDnsServers();
                final String oldHardwareSerialNumber = workflowVariables.getOldHardwareSerialNumber() != null
                    ? workflowVariables.getOldHardwareSerialNumber()
                    : "";
                final String hostname = FDN.get(apNodeFdn).getRdnValueOfType("Node");
                final DhcpConfiguration dhcpConfiguration = new DhcpConfiguration(hardwareSerialNumber, hostname, initialIpAddress, defaultRouter,
                    ntpServers,
                    dnsServers);

                final boolean isSuccess = dhcpConfiguration(apNodeFdn, oldHardwareSerialNumber, dhcpConfiguration);
                if (isSuccess) {
                    workflowVariables.setOldHardwareSerialNumber(hardwareSerialNumber);
                    execution.setVariable(AbstractWorkflowVariables.DHCP_CLIENT_ID_TO_REMOVE_KEY, hardwareSerialNumber);
                }
                workflowVariables.setDhcpSuccess(isSuccess);
            }
        } catch (final Exception e) {
            logger.warn("Error executing {} for node {}: {}", this.getClass().getSimpleName(), apNodeFdn, e.getMessage(), e);
        }
    }
}
