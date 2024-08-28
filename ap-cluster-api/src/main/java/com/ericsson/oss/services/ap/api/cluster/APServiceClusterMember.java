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

import javax.ejb.Local;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;

/**
 * Provide methods to check the master node in the AP cluster
 */
@EService
@Local
public interface APServiceClusterMember {

    /**
     * Retrieve master node status in the cluster
     *
     * @return true is it the master node, false if not
     *
     */
    boolean isMasterNode();

    /**
     * Register a APServiceClusterMemberCallback object to get notified of cluster changes.
     *
     * @param callback
     *              APServiceClusterMemberCallback
     */
    void registerApServiceClusterMembershipChangeListener(final APServiceClusterMemberCallback callback);
}
