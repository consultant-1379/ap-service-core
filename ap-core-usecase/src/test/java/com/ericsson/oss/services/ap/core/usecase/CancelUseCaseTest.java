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
package com.ericsson.oss.services.ap.core.usecase;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.IllegalCancelOperationException;
import com.ericsson.oss.services.ap.api.exception.UnsupportedCommandException;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.workflow.AutoProvisioningWorkflowService;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.core.usecase.workflow.ApWorkflowServiceResolver;
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;

/**
 * Unit tests for {@link CancelUseCase}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CancelUseCaseTest {

    private static final String BUSINESS_KEY = "AP_Node=" + NODE_NAME;

    @Mock
    private ManagedObject apNodeMo;

    @Mock
    private ManagedObject nodeStatusMo;

    @Mock
    private ApWorkflowServiceResolver apWorkflowServiceResolver;

    @Mock
    private AutoProvisioningWorkflowService apWorkflowService;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private DpsOperations dps;

    @Mock
    private DataPersistenceService dpsService;

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    @Mock
    private WorkflowInstanceServiceLocal wfsInstanceService;

    @InjectMocks
    private CancelUseCase cancelUseCase;

    @Before
    public void setUp() {
        when(dps.getDataPersistenceService()).thenReturn(dpsService);
        when(dps.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findMoByFdn(NODE_FDN)).thenReturn(apNodeMo);
        when(apNodeMo.getChild("NodeStatus=1")).thenReturn(nodeStatusMo);
        when(apNodeMo.getAttribute("nodeType")).thenReturn(VALID_NODE_TYPE);
        when(nodeTypeMapper.getInternalRepresentationFor(VALID_NODE_TYPE)).thenReturn(VALID_NODE_TYPE.toLowerCase());
        when(apWorkflowServiceResolver.getApWorkflowService(VALID_NODE_TYPE.toLowerCase())).thenReturn(apWorkflowService);
        when(apWorkflowService.isSupported(anyString())).thenReturn(true);
    }

    @Test(expected = ApApplicationException.class)
    public void whenExecute_andNodeDoesNotExist_thenApApplicationExceptionIsThrown() {
        when(nodeStatusMo.getAttribute("state")).thenReturn(null);
        doThrow(Exception.class).when(liveBucket).findMoByFdn(NODE_FDN);
        cancelUseCase.execute(NODE_FDN);
    }

    @Test(expected = UnsupportedCommandException.class)
    public void whenExecute_andNodeDoesNotSupportCancel_thenUnsupportedCommandExceptionIsThrown() {
        when(nodeStatusMo.getAttribute("state")).thenReturn("ORDER_STARTED");
        when(apWorkflowService.isSupported(anyString())).thenReturn(false);
        cancelUseCase.execute(NODE_FDN);
    }

    @Test(expected = ApApplicationException.class)
    public void whenExecute_andWorkflowCorrelationThrownNonCorrelationException_thenApApplicationExceptionIsThrown()
            throws WorkflowMessageCorrelationException {
        when(nodeStatusMo.getAttribute("state")).thenReturn(null);
        doThrow(Exception.class).when(wfsInstanceService).correlateMessage(anyString(), anyString());
        cancelUseCase.execute(NODE_FDN);
    }

    @Test(expected = IllegalCancelOperationException.class)
    public void whenExecute_andWorkflowCorrelationThrowsCorrelationException_thenIllegalCancelOperationExceptionIsThrown()
            throws WorkflowMessageCorrelationException {
        when(nodeStatusMo.getAttribute("state")).thenReturn("INTEGRATION_STARTED");
        doThrow(WorkflowMessageCorrelationException.class).when(wfsInstanceService).correlateMessage(anyString(), anyString());
        cancelUseCase.execute(NODE_FDN);
    }

    @Test
    public void whenExecute_thenCorrelateMessageIsSent() throws WorkflowMessageCorrelationException {
        when(nodeStatusMo.getAttribute("state")).thenReturn("INTEGRATION_STARTED");
        cancelUseCase.execute(NODE_FDN);
        verify(wfsInstanceService).correlateMessage(anyString(), eq(BUSINESS_KEY));
    }

    @Test
    public void whenExecuteThenCorrelateMigrationCancelMessageIsSent() throws WorkflowMessageCorrelationException {
        when(nodeStatusMo.getAttribute("state")).thenReturn("MIGRATION_STARTED");
        cancelUseCase.execute(NODE_FDN);
        verify(wfsInstanceService).correlateMessage(eq("MIGRATION_CANCEL"), eq(BUSINESS_KEY));
    }
}
