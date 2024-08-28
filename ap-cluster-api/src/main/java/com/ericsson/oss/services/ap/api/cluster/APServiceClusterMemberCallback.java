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
package com.ericsson.oss.services.ap.api.cluster;

/**
 * Provides method for objects requiring notifications on AP Cluster Member
 * changes
 */
public interface APServiceClusterMemberCallback {

    /**
     * Actions to be taken when Cluster Membership changes occur
     *
     * @param isMasterNode
     *              true if it is the master node, false if not
     */
    void onApServiceClusterMasterChange(final boolean isMasterNode);
}
