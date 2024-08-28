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
package com.ericsson.oss.services.ap.core.cluster;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.services.ap.api.cluster.APServiceClusterMember;
import com.ericsson.oss.services.ap.api.cluster.APServiceClusterMemberCallback;
import com.ericsson.oss.services.ap.commom.cluster.member.APServiceClusterMembership;

/**
 * Ejb to inject the cluster member and fetch master node status in AP cluster.
 */
@Stateless
public class APServiceClusterMemberEjb implements APServiceClusterMember {

    @Inject
    private APServiceClusterMembership apServiceClusterMembership;

    @Override
    public boolean isMasterNode() {
        return apServiceClusterMembership.isMasterNode();
    }

    @Override
    public void registerApServiceClusterMembershipChangeListener(final APServiceClusterMemberCallback callback) {
        apServiceClusterMembership.registerApServiceClusterMembershipChangeListener(callback);
    }

}
