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
package com.ericsson.oss.services.ap.common.workflow.messages;

/**
 * Workflow messages and variables for Instantaneous Licensing flow
 */
public final class InstantaneousLicensingMessage {

    enum WorkflowMessages {
        INSTANTANEOUS_LICENSING_RUNNING, INSTANTANEOUS_LICENSING_COMPLETED, INSTANTANEOUS_LICENSING_FAILED
    }

    private static final String ADDITIONAL_INFO_KEY = "additionalInfo";
    private static final String RESULT_KEY = "result";
    private static final String REQUEST_ID = "requestId";

    private InstantaneousLicensingMessage() {

    }

    /**
     * Workflow variables message INSTANTANEOUS_LICENSING_RUNNING for IL workflow
     *
     * @return the licensing INSTANTANEOUS_LICENSING_RUNNING message
     */
    public static String getRunningMessage() {
        return WorkflowMessages.INSTANTANEOUS_LICENSING_RUNNING.toString();
    }

    /**
     * Workflow variables message INSTANTANEOUS_LICENSING_COMPLETED for IL workflow
     *
     * @return the licensing INSTANTANEOUS_LICENSING_COMPLETED message
     */
    public static String getCompletedMessage() {
        return WorkflowMessages.INSTANTANEOUS_LICENSING_COMPLETED.toString();
    }

    /**
     * Workflow variables message INSTANTANEOUS_LICENSING_FAILED for IL workflow
     *
     * @return the licensing INSTANTANEOUS_LICENSING_FAILED message
     */
    public static String getFailedMessage() {
        return WorkflowMessages.INSTANTANEOUS_LICENSING_FAILED.toString();
    }

    /**
     * Workflow variable key for the licensing request job result
     *
     * @return the result key
     */
    public static String getResultKey() {
        return RESULT_KEY;
    }

    /**
     * Workflow variable key for the licensing additional information
     *
     * @return the additional information key
     */
    public static String getAdditionalInfoKey() {
        return ADDITIONAL_INFO_KEY;
    }

    /**
     * Workflow variable key for the ID of the licensing request from SHM
     *
     * @return the request id key
     */
    public static String getRequestId() {
        return REQUEST_ID;
    }

}
