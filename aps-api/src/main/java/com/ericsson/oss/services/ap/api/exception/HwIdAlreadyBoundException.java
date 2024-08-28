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
package com.ericsson.oss.services.ap.api.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Indicates that hardware bind failed because the specified serial number is already bound to another node.
 */
public class HwIdAlreadyBoundException extends ApApplicationException {

    private static final long serialVersionUID = 1L;

    private final String nodeName;

    private final List<String> bindFailures;

    /**
     * Exception with message information and node name.
     *
     * @param message
     *            the error message
     * @param nodeName
     *            the name of the node to which the hardware serial number is already bound
     */
    public HwIdAlreadyBoundException(final String message, final String nodeName) {
        super(message);
        this.nodeName = nodeName;
        bindFailures = new ArrayList<>();
    }


    public HwIdAlreadyBoundException(final List<String> bindFailures, final String message) {
        super(message);
        this.bindFailures = Collections.unmodifiableList(bindFailures);
        nodeName = null;
    }

    /**
     * Returns the name of the node to which the hardware serial number is already bound.
     *
     * @return the name of the node
     */
    public String getNodename() {
        return nodeName;
    }

    public List<String> getBindFailures() {
        return Collections.unmodifiableList(bindFailures);
    }
}
