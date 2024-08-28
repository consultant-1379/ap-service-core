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

/**
 * Thrown to indicate that an AP node already exists on the system. The hardware replace usecase requires that no AP node MO exists on invocation.
 */
public class ApNodeExistsException extends ApApplicationException {

    private static final long serialVersionUID = 6099380035958366042L;

    private final String nodeName;

    /**
     * Exception with only the name of the node.
     *
     * @param nodeName
     *            the name of the node
     */
    public ApNodeExistsException(final String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * Returns the name of AP node which exists in the database.
     *
     * @return the name of the node
     */
    public String getNodename() {
        return nodeName;
    }
}
