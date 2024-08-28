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
package com.ericsson.oss.services.ap.common.cluster.member;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.sdk.cluster.MembershipChangeEvent;
import com.ericsson.oss.itpf.sdk.cluster.MembershipChangeEvent.ClusterMemberInfo;
import com.ericsson.oss.services.ap.commom.cluster.member.APServiceClusterMembership;

/**
 * Unit tests for class {@link APServiceClusterMembership}.
 */
@RunWith(MockitoJUnitRunner.class)
public class APServiceClusterMembershipTest {

    private static final String NODE_ID = "svc-1-apserv";

    @Mock
    private MembershipChangeEvent mce;

    @InjectMocks
    private APServiceClusterMembership apServiceClusterMembership;

    private final List<ClusterMemberInfo> allClusterMembers = new ArrayList<>();

    @Before
    public void setUp() {
        System.setProperty("com.ericsson.oss.sdk.node.identifier", NODE_ID);
    }

    @Test
    public void whenNodeIdMatchesOldestMemberInClusteThenIsMasterNodeReturnsTrue() {
        allClusterMembers.add(new ClusterMemberInfo(NODE_ID, "ap-service-core", "1.0.0"));
        when(mce.getAllClusterMembers()).thenReturn(allClusterMembers);
        apServiceClusterMembership.listenForMembershipChange(mce);
        assertTrue(apServiceClusterMembership.isMasterNode());
    }

    @Test
    public void whenNodeIdDoesNotMatcheOldestMemberInClusterThenIsMasterNodeReturnsFalse() {
        allClusterMembers.add(new ClusterMemberInfo("svc-2-apserv", "ap-service-core", "1.0.0"));
        when(mce.getAllClusterMembers()).thenReturn(allClusterMembers);
        apServiceClusterMembership.listenForMembershipChange(mce);
        assertFalse(apServiceClusterMembership.isMasterNode());
    }

    @Test
    public void whenNoMembersInClusterThenIsMasterNodeReturnsFalse() {
        when(mce.getAllClusterMembers()).thenReturn(Collections.<ClusterMemberInfo> emptyList());
        apServiceClusterMembership.listenForMembershipChange(mce);
        assertFalse(apServiceClusterMembership.isMasterNode());
    }
}
