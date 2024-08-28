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

/**
 * Indicates that the node is not in the correct state to perform the operation.
 */
public class InvalidNodeStateException extends ApApplicationException {

    private static final long serialVersionUID = 1L;

    private final String invalidNodeState;

    /**
     * Exception with message information and invalid AP node state.
     *
     * @param message
     *            description of the exception
     * @param invalidNodeState
     *            the current state from which the operation could not be performed
     */
    public InvalidNodeStateException(final String message, final String invalidNodeState) {
        super(message);
        this.invalidNodeState = invalidNodeState;
    }

    /**
     * Returns the current AP node state, from which the operation could not be performed.
     *
     * @return the AP node state
     */
    public String getInvalidNodeState() {
        return invalidNodeState;
    }
}
