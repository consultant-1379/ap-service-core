/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.status;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.services.ap.api.cluster.APServiceClusterMemberCallback;

/**
 * Provides methods for finding nodes to poll at startup, adding nodes to be polled, and to be notified of nodes to be polled from other cluster
 * member
 */
@EService
public interface APNodePoller extends APServiceClusterMemberCallback {

    /**
     * Find and polls all nodes required for the specific polling function
     */
    void poll();

    /**
     * Adds the AP Node to the collection of nodes to be polled.
     *
     * @param apNodeFdn
     *            the FDN of the AP Node
     */
    void addNodeToPoller(final String apNodeFdn);

    /**
     * A callback to notify the poller of new Nodes to be polled
     */
    void addNodeCallback();

    /**
     * Remove the AP Node from poller.
     *
     * @param apNodeFdn
     *            the FDN of the AP Node
     */
    void removeNodeFromPoller(final String apNodeFdn);
}
