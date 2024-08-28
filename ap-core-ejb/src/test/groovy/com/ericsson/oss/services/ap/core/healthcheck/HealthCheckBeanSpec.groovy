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
package com.ericsson.oss.services.ap.core.healthcheck

import org.slf4j.Logger

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.api.exception.HealthCheckRestServiceException
import com.ericsson.oss.services.ap.common.workflow.messages.HealthCheckMessage
import com.ericsson.oss.services.ap.core.rest.client.healthcheck.HealthCheckRestClient
import com.ericsson.oss.services.ap.core.rest.client.healthcheck.model.HealthStatusCounts
import com.ericsson.oss.services.ap.core.rest.client.healthcheck.model.Report
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal

/**
 * HealthCheckBeanSpec is a test class for {@link HealthCheckBean}
 */
class HealthCheckBeanSpec {

    private static final String PROJECT_FDN = "Project=Project1"
    private static final String NODE_FDN = PROJECT_FDN + ",Node=Node1"
    private static final String REPORT_NAME = "AP_Report"
    private static final String PRE_HEALTHCHECK = "PRE_HEALTHCHECK"
    private static final String USER_ID = "User1"
    private static final String ADDITIONAL_INFO = "See Report AP_Report https://localhost/#nhc/reportdetails/12345"

    @ObjectUnderTest
    private final HealthCheckBean healthCheckBean

    @MockedImplementation
    private HealthCheckRestClient restClient

    @MockedImplementation
    private Logger logger

    @MockedImplementation
    private WorkflowInstanceServiceLocal wfsInstanceService

    final Map<String, Object> correlationMessageVariables = new HashMap<>()

    def setup() {
        healthCheckBean.logger = logger
        System.setProperty("web_host_default", "localhost")
    }

    def "When create report throws HealthCheckServiceRestException then correct additional info is displayed" () {

        given: "Rest client throws HealthCheckRestServiceException when creating report"
            restClient.createReport(NODE_FDN) >> { throw new HealthCheckRestServiceException(exception_message) }

        and: "Correlation variables are set"
            correlationMessageVariables.put(HealthCheckMessage.getJobSuccessKey(), Boolean.FALSE)
            correlationMessageVariables.put(HealthCheckMessage.getAdditionalInfoKey(), additionalInfo)

        when: "Report is created"
            healthCheckBean.createReport(USER_ID, NODE_FDN)

        then: "HealthCheckRestServiceException is thrown"
            thrown(HealthCheckRestServiceException)

        and: "Message correlated correctly"
            1 * wfsInstanceService.correlateMessage(HealthCheckMessage.getMessageKey(), _, correlationMessageVariables)

        where: "data is valid"
            exception_message   | additionalInfo
            "Profile Not Found" | "Error creating health check report: Profile Not Found"
            ""                  | "Error creating health check report:"
    }

    def "When view report is executed successfully the workflow is correlated successfully" () {

        given: "Report is successfully generated"
            restClient.createReport(NODE_FDN) >> REPORT_NAME
            final HealthStatusCounts healthStatusCounts = new HealthStatusCounts(undetermined, healthy, unhealthy, warning)
            final Report report = new Report(12345, REPORT_NAME, "COMPLETED", healthStatusCounts)
            restClient.viewReport(NODE_FDN, REPORT_NAME) >> report

        and: "Correlation variables are set"
            correlationMessageVariables.put(HealthCheckMessage.getJobSuccessKey(), success)
            correlationMessageVariables.put(HealthCheckMessage.getAdditionalInfoKey(), additionalInfo)

        when: "Report is created"
            healthCheckBean.createReport(USER_ID, NODE_FDN)

        then: "Workflow is correlated correctly"
            1 * wfsInstanceService.correlateMessage(HealthCheckMessage.getMessageKey(), _, correlationMessageVariables)

        where:
            healthy | unhealthy | warning | undetermined | success       | additionalInfo
            1       | 0         | 0       | 0            | Boolean.TRUE  | ADDITIONAL_INFO
            0       | 1         | 0       | 0            | Boolean.FALSE | ADDITIONAL_INFO
            0       | 0         | 1       | 0            | Boolean.TRUE  | "Warning. " + ADDITIONAL_INFO
            0       | 0         | 0       | 1            | Boolean.FALSE | ADDITIONAL_INFO
    }

    def "When view report throws ApApplicationException the workflow is correlated successfully and task failed" () {

        given: "Rest client throws ApApplicationException when creating report"
            restClient.createReport(NODE_FDN) >> REPORT_NAME
            final String expectedAdditionalInfo = "Failed to retrieve status for health check report AP_Report. Please check the logs for additional information."
            restClient.viewReport(NODE_FDN, REPORT_NAME, PRE_HEALTHCHECK) >> { throw new ApApplicationException("Internal Server Error") }

        and: "Correlation variables are set"
            correlationMessageVariables.put(HealthCheckMessage.getJobSuccessKey(), Boolean.FALSE)
            correlationMessageVariables.put(HealthCheckMessage.getAdditionalInfoKey(), expectedAdditionalInfo)

        when: "Report is created"
            healthCheckBean.createReport(USER_ID, NODE_FDN)

        then: "Message correlated correctly"
            1 * wfsInstanceService.correlateMessage(HealthCheckMessage.getMessageKey(), _, correlationMessageVariables)
    }

    def "When view report throws HealthCheckRestServiceException the workflow is correlated successfully and task failed" () {

        given: "Rest client throws HealthCheckRestServiceException when viewing report"
            restClient.createReport(NODE_FDN) >> REPORT_NAME
            final String expectedAdditionalInfo = "Failed to retrieve status for health check report AP_Report. Please check the logs for additional information."
            restClient.viewReport(NODE_FDN, REPORT_NAME, PRE_HEALTHCHECK) >> { throw new HealthCheckRestServiceException("Report Not Found") }

        and: "Correlation variables are set"
            correlationMessageVariables.put(HealthCheckMessage.getJobSuccessKey(), Boolean.FALSE)
            correlationMessageVariables.put(HealthCheckMessage.getAdditionalInfoKey(), expectedAdditionalInfo)

        when: "Report is created"
            healthCheckBean.createReport(USER_ID, NODE_FDN)

        then: "Message correlated correctly"
            1 * wfsInstanceService.correlateMessage(HealthCheckMessage.getMessageKey(), _, correlationMessageVariables)
    }
}
