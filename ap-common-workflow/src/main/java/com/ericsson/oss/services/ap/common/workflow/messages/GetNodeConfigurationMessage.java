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

import java.util.HashMap;
import java.util.Map;

/**
 * Correlation message to indicate result.
 */
public final class GetNodeConfigurationMessage {

    private boolean result;
    private String additionalInfo;

    private static final String GET_NODE_CONFIGURATION_COMPLETED_KEY = "GET_NODE_CONFIGURATION_COMPLETED";
    private static final String RESULT_KEY = "result";
    private static final String ADDITIONAL_INFO_KEY = "additionalInfo";

    /**
     * @return the result
     */
    public boolean isSuccessful() {
        return result;
    }

    /**
     * @param result
     *            the result to set
     */
    public void setResult(final boolean result) {
        this.result = result;
    }

    /**
     * @return the additionalInfo
     */
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * @param additionalInfo
     *            the additionalInfo to set
     */
    public void setAdditionalInfo(final String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    /**
     * The message variables to correlate the message with.
     *
     * @return the workflow variables
     */
    public Map<String, Object> convertToWorkflowVariables() {
        final Map<String, Object> workflowVariables = new HashMap<>();
        workflowVariables.put(RESULT_KEY, result);
        workflowVariables.put(ADDITIONAL_INFO_KEY, additionalInfo);
        return workflowVariables;
    }

    /**
     * @return the message type key
     */
    public static String getMessageKey() {
        return GET_NODE_CONFIGURATION_COMPLETED_KEY;
    }

    /**
     * @return the result key
     */
    public static String getResultKey() {
        return RESULT_KEY;
    }

    /**
     * @return the additionalInfo key
     */
    public static String getAdditionalInfoKey() {
        return ADDITIONAL_INFO_KEY;
    }
}
