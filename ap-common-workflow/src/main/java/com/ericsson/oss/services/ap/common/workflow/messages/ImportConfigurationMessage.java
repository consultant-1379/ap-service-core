/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
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
 * Correlation message to indicate the end of the importing of configuration files.
 */
public final class ImportConfigurationMessage {

    private static final String IMPORT_SUCCESSFUL_MESSAGE = "importConfigurationsSuccessful";
    private static final String IMPORT_CONFIG_COMPLETION_KEY = "IMPORT_CONFIG_COMPLETION";
    private static final String FAILED_FILE_KEY = "failedFile";
    private static final String FAILURE_POINT_KEY = "importFailurePoint";

    private ImportConfigurationMessage() {

    }

    /**
     * The message variables to correlate the message with.
     *
     * @param success
     *            if the import was a success
     * @param failedFile
     *            the name of the file the import failed on
     * @param importFailurePoint
     *            the type of configuration file the import failed on
     * @return the workflow variables
     */
    public static Map<String, Object> getMessageVariables(final boolean success, final String failedFile, final String importFailurePoint) {
        final Map<String, Object> workflowVariables = new HashMap<>();
        workflowVariables.put(IMPORT_SUCCESSFUL_MESSAGE, success);
        workflowVariables.put(FAILED_FILE_KEY, failedFile);
        workflowVariables.put(FAILURE_POINT_KEY, importFailurePoint);
        return workflowVariables;
    }

    /**
     * Correlation message key to indicate completion of the import of configurations files.
     *
     * @return the import message key
     */
    public static String getMessageKey() {
        return IMPORT_CONFIG_COMPLETION_KEY;
    }

    /**
     * Workflow variables key for the failure point of the import.
     *
     * @return the key for the failure point of the import
     */
    public static String getFailurePointKey() {
        return FAILURE_POINT_KEY;
    }

    /**
     * Workflow variables the failure point of the import..
     *
     * @return the failure point of the import.
     */
    public static String getFailedFileKey() {
        return FAILED_FILE_KEY;
    }
}
