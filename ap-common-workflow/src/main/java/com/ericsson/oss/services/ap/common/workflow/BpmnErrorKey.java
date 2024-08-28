/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.workflow;

/**
 * Contains error keys that are sent by tasks on BPMN errors.
 */
public final class BpmnErrorKey {

    public static final String INTEGRATION_WORKFLOW_ERROR_KEY = "INTEGRATION_FAIL";
    public static final String ORDER_WORKFLOW_ERROR_KEY = "ORDER_FAIL";
    public static final String HARDWARE_REPLACE_WORKFLOW_ERROR_KEY = "HARDWARE_REPLACE_FAIL";
    public static final String INSTALL_LICENSE_KEY_FILE_ERROR_KEY = "INSTALL_LICENSE_KEY_FILE_FAIL";
    public static final String PREMIGRATION_FAIL = "PREMIGRATION_FAIL";
    public static final String EOI_INTEGRATION_WORKFLOW_ERROR_KEY = "EOI_FAIL";

    private BpmnErrorKey() {

    }
}
