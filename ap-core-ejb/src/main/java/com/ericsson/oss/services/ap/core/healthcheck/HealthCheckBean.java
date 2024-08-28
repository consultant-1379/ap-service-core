/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.healthcheck;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.jboss.ejb3.annotation.TransactionTimeout;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.context.ContextService;
import com.ericsson.oss.itpf.sdk.context.classic.ContextConstants;
import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.ap.api.exception.HealthCheckRestServiceException;
import com.ericsson.oss.services.ap.api.workflow.HealthCheckService;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.common.workflow.BusinessKeyGenerator;
import com.ericsson.oss.services.ap.common.workflow.messages.HealthCheckMessage;
import com.ericsson.oss.services.ap.core.rest.client.healthcheck.HealthCheckRestClient;
import com.ericsson.oss.services.ap.core.rest.client.healthcheck.model.Report;
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;

/**
 * {@inheritDoc}
 */
@Stateless
@Asynchronous
@EService
public class HealthCheckBean implements HealthCheckService {

    private static final int INITIAL_DURATION = 20000;
    private static final int INTERVAL_DURATION = 20000;
    private static final String WEB_HOST = "web_host_default";

    @Inject
    private Logger logger;

    @EServiceRef
    private WorkflowInstanceServiceLocal wfsInstanceService;

    @Inject
    private HealthCheckRestClient healthCheckRestService;

    @Inject
    private ContextService contextService;

    @Resource
    private TimerService timerService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void createReport(final String userId, final String apNodeFdn, final String healthCheckPhase) {
        final HealthCheckData healthCheckData = new HealthCheckData(userId, apNodeFdn, healthCheckPhase);
        final TimerConfig timerConfig = new TimerConfig(healthCheckData, false);
        timerService.createIntervalTimer(INITIAL_DURATION, INTERVAL_DURATION, timerConfig);
    }

    @Timeout
    @TransactionTimeout(value = 15, unit = TimeUnit.MINUTES)
    private void execute(final Timer timer) {
        final HealthCheckData healthCheckData = (HealthCheckData) timer.getInfo();
        final String userId = healthCheckData.getUserId();
        contextService.setContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY, userId);

        final String apNodeFdn = healthCheckData.getNodeFdn();
        final String healthCheckPhase = healthCheckData.getHealthCheckPhase();
        final String reportName = executeCreateReport(apNodeFdn);
        executeView(apNodeFdn, reportName, healthCheckPhase);

        timer.cancel();
    }

    private String executeCreateReport(final String apNodeFdn) {
        final String nodeName = FDN.get(apNodeFdn).getRdnValue();
        try {
            return healthCheckRestService.createReport(apNodeFdn);
        } catch (final HealthCheckRestServiceException healthCheckRestServiceException) {
            final String additionalInfo = String.format("Error creating health check report: %s", healthCheckRestServiceException.getMessage());
            resumeWorkflow(nodeName, additionalInfo, Boolean.FALSE, Boolean.FALSE);
            logger.error("Error creating health check report for node {}", apNodeFdn, healthCheckRestServiceException);
            throw healthCheckRestServiceException;
        }
    }

    private void executeView(final String apNodeFdn, final String reportName, final String healthCheckPhase) {
        final String nodeName = FDN.get(apNodeFdn).getRdnValue();
        final Report generatedReport = healthCheckRestService.viewReport(apNodeFdn, reportName, healthCheckPhase);
        final String additionalInfo = getAdditionalInfo(generatedReport);
        if (completeTask(generatedReport)) {
            resumeWorkflow(nodeName, additionalInfo, Boolean.TRUE, Boolean.TRUE);
        } else {
            resumeWorkflow(nodeName, additionalInfo, Boolean.FALSE, Boolean.TRUE); 
        }
    }

    private void resumeWorkflow(final String nodeName, final String additionalInfo, final Boolean success, final Boolean reportCompleted) {
        try {
            correlateMessage(nodeName, additionalInfo, success, reportCompleted);
        } catch (final WorkflowMessageCorrelationException e) {
            logger.warn("Error correlating the Health Check Completion message for node {}: {}", nodeName, e.getMessage(), e);
        }
    }

    private void correlateMessage(final String nodeName, final String additionalInfo, final Boolean success, final Boolean reportCompleted)
        throws WorkflowMessageCorrelationException {
        final String businessKey = BusinessKeyGenerator.generateBusinessKeyFromNodeName(nodeName);
        final Map<String, Object> correlationMessageVariables = new HashMap<>();
        correlationMessageVariables.put(HealthCheckMessage.getJobSuccessKey(), success);
        correlationMessageVariables.put(HealthCheckMessage.getAdditionalInfoKey(), additionalInfo);
        correlationMessageVariables.put(HealthCheckMessage.getReportCompletedKey(), reportCompleted);
        wfsInstanceService.correlateMessage(HealthCheckMessage.getMessageKey(), businessKey, correlationMessageVariables);
    }

    private boolean completeTask(final Report report) {
        final Integer unhealthyNodes = report.getHealthStatusCounts().getUnhealthyNodes();
        final Integer undeterminedNodes = report.getHealthStatusCounts().getUndeterminedNodes();
        if (unhealthyNodes > 0 || undeterminedNodes > 0) {
            return false;
        }
        return true;
    }

    private String getAdditionalInfo(final Report report) {
        final Integer warningNodes = report.getHealthStatusCounts().getWarningNodes();
        final String mainReportId = String.valueOf(report.getMainReportId());
        final String fullLink = new StringBuilder().append("https://").append(System.getProperty(WEB_HOST))
            .append("/#nhc/reportdetails/").append(mainReportId).toString();
        final String additionalInformation = String.format("See report %s %s", report.getReportName(), fullLink);
        if (warningNodes > 0) {
            return String.format("Warning. %s", additionalInformation);
        }
        return additionalInformation;
    }
}
