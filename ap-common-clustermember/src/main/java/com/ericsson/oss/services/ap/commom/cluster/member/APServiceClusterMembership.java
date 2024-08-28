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
package com.ericsson.oss.services.ap.commom.cluster.member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import com.ericsson.oss.itpf.sdk.cluster.MembershipChangeEvent;
import com.ericsson.oss.itpf.sdk.cluster.MembershipChangeEvent.ClusterMemberInfo;
import com.ericsson.oss.itpf.sdk.cluster.annotation.ServiceCluster;
import com.ericsson.oss.services.ap.api.cluster.APServiceClusterMember;
import com.ericsson.oss.services.ap.api.cluster.APServiceClusterMemberCallback;

/**
 * Listens for membership changes in APServiceCluster, keeping track of the master node in the cluster.
 */
@ApplicationScoped
public class APServiceClusterMembership implements APServiceClusterMember {

    private static final String CLUSTER_NAME = "APServiceCluster";

    private String nodeId;

    private final AtomicReference<String> masterNodeId = new AtomicReference<>();

    private final List<APServiceClusterMemberCallback> callbacks = new ArrayList<>();

    public APServiceClusterMembership() {
        nodeId = System.getProperty("com.ericsson.oss.sdk.node.identifier");
    }

    /**
     * Observer method will be invoked by ServiceFramework every time there are membership changes in APServiceCluster.
     *
     * @param mce
     *            membership change event
     */
    public void listenForMembershipChange(@Observes @ServiceCluster(CLUSTER_NAME) final MembershipChangeEvent mce) {
        final List<ClusterMemberInfo> allClusterMembers = mce.getAllClusterMembers();
        final String newMasterNodeId = allClusterMembers.isEmpty() ? null : allClusterMembers.get(0).getNodeId();

        masterNodeId.getAndSet(newMasterNodeId);
        for (final APServiceClusterMemberCallback callback : callbacks) {
            callback.onApServiceClusterMasterChange(isMasterNode());
        }
    }

    /**
     * Checks if running on the master node, e.g svc-1-apserv or svc-2-apserv
     * <p>
     * Determines if running on the master node by checking if the nodeId as defined by the system property
     * <code>com.ericsson.oss.sdk.node.identifier</code> is equal to the nodeId of the oldest member in the APServiceCluster.
     *
     * @return true if running on master node
     */
    @Override
    public boolean isMasterNode() {
        return getNodeId().equals(masterNodeId.get());
    }

    private String getNodeId() {
        if (nodeId == null) {
            nodeId = System.getProperty("com.ericsson.oss.sdk.node.identifier");
        }
        return nodeId;
    }

    @Override
    public void registerApServiceClusterMembershipChangeListener(final APServiceClusterMemberCallback callback) {
        callbacks.add(callback);
    }

}
