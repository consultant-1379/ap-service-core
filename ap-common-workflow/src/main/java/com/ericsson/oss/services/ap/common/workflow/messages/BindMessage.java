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

import com.ericsson.oss.services.ap.common.model.NodeAttribute;

/**
 * Correlation message to bind a node.
 */
public final class BindMessage {

    private static final String BIND_MESSAGE_KEY = "BIND";

    private BindMessage() {

    }

    /**
     * Correlation message key to bind a node.
     *
     * @return the bind message key
     */
    public static String getMessageKey() {
        return BIND_MESSAGE_KEY;
    }

    /**
     * The message variables to correlate the message with.
     *
     * @param hardwareSerialNumber
     *            the hardware serial number for the node
     * @return the workflow variables
     */
    public static Map<String, Object> getMessageVariables(final String hardwareSerialNumber) {
        final Map<String, Object> workflowVariables = new HashMap<>();
        workflowVariables.put(NodeAttribute.HARDWARE_SERIAL_NUMBER.toString(), hardwareSerialNumber);
        return workflowVariables;
    }
}
