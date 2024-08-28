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
package com.ericsson.oss.services.ap.common.workflow.messages;

/**
 * Correlation message indicating the health check job has been completed
 */
public final class HealthCheckMessage {

    private static final String ADDITIONAL_INFO_KEY = "additionalInfo";
    private static final String HEALTH_CHECK_JOB_COMPLETED_KEY = "HEALTH_CHECK_JOB_COMPLETED";
    private static final String JOB_SUCCESS_KEY = "healthCheckSuccess";
    private static final String REPORT_COMPLETED = "reportCompleted";

    private HealthCheckMessage() {

    }

    /**
     * Correlation message key indicating the report creation has completed
     *
     * @return the health check complete message key
     */
    public static String getMessageKey() {
        return HEALTH_CHECK_JOB_COMPLETED_KEY;
    }

    /**
     * Workflow variables key for the message from the report generation
     *
     * @return the key for the message from the event
     */
    public static String getAdditionalInfoKey() {
        return ADDITIONAL_INFO_KEY;
    }

    /**
     * Workflow variables key for the success or failure of the report generation
     *
     * @return the key for the success variable
     */
    public static String getJobSuccessKey() {
        return JOB_SUCCESS_KEY;
    }

    /**
     * Workflow variables key for number of undetermined nodes from the report generation
     *
     * @return the key for number of undetermined nodes from the event
     */
    public static String getReportCompletedKey() {
        return REPORT_COMPLETED;
    }
}
