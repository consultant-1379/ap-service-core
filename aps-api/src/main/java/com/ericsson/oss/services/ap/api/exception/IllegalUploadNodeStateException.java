/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.exception;

import java.util.List;

/**
 * Indicates that the node is not in the correct state to perform the upload operation.
 */
public class IllegalUploadNodeStateException extends ApApplicationException {

    private static final long serialVersionUID = 1L;

    private final String invalidNodeState;
    private final List<String> validStates;

    /**
     * Exception with message information and invalid AP node state.
     *
     * @param message
     *            description of the exception
     * @param invalidNodeState
     *            the current state from which the upload could not be performed
     * @param validStates
     *            the valid states for upload
     */
    public IllegalUploadNodeStateException(final String message, final String invalidNodeState, final List<String> validStates) {
        super(message);
        this.invalidNodeState = invalidNodeState;
        this.validStates = validStates;
    }

    /**
     * Returns the current AP node state, from which the upload could not be performed.
     *
     * @return the AP node state
     */
    public String getInvalidNodeState() {
        return invalidNodeState;
    }

    /**
     * Returns a list of AP node states, from which the upload can be performed.
     *
     * @return the AP node state
     */
    public List<String> getValidNodeStates() {
        return validStates;
    }
}
