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
package com.ericsson.oss.services.ap.api.exception;

import java.util.List;

/**
 * Thrown to indicate delete failed for one or more nodes in the project.
 */
public class PartialProjectDeletionException extends ApApplicationException {

    private static final long serialVersionUID = 4451623971208767576L;

    private final List<String> deletedNodes;
    private final List<String> remainingNodes;

    /**
     * Exception with lists of successfully deleted nodes, and remaining nodes.
     *
     * @param deletedNodes
     *            AP nodes which were successfully deleted from the project
     * @param remainingNodes
     *            AP nodes which were not deleted and remain on the system
     */
    public PartialProjectDeletionException(final List<String> deletedNodes, final List<String> remainingNodes) {
        super("Delete failed for one or more nodes");
        this.deletedNodes = deletedNodes;
        this.remainingNodes = remainingNodes;
    }

    /**
     * A list of the nodes successfully deleted from the project.
     *
     * @return the deletedNodes
     */
    public List<String> getDeletedNodes() {
        return deletedNodes;
    }

    /**
     * A list of the nodes not deleted from the project and which remain on the system.
     *
     * @return the remainingNodes
     */
    public List<String> getRemainingNodes() {
        return remainingNodes;
    }
}
