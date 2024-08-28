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
package com.ericsson.oss.services.ap.core.status;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;

/**
 * Unit tests for {@link StateTransitionManagerEjb}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeStateTransitionManagerEjbTest {

    private static final String NODE_STATUS_MO_FDN = NODE_FDN + ",NodeStatus=1";

    @Mock
    private DpsOperations dps;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private ManagedObject nodeStatusMo;

    @InjectMocks
    private StateTransitionManagerEjb nodeStateTransitionManagerEjb;

    @Mock
    private DataPersistenceService dpsService;

    @Before
    public void setUp() {
        when(dps.getDataPersistenceService()).thenReturn(dpsService);
        when(dps.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findMoByFdn(NODE_STATUS_MO_FDN)).thenReturn(nodeStatusMo);
    }

    @Test
    public void when_validating_a_valid_StateTransition_then_return_true() {
        assertTrue(nodeStateTransitionManagerEjb.isValidStateTransition(State.ORDER_STARTED.name(), StateTransitionEvent.ORDER_SUCCESSFUL));
    }

    @Test
    public void when_validating_an_invalid_StateTransition_then_return_false() {
        assertFalse(nodeStateTransitionManagerEjb.isValidStateTransition(State.INTEGRATION_FAILED.name(), StateTransitionEvent.ORDER_SUCCESSFUL));
    }
}
