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
package com.ericsson.oss.services.ap.core.usecase;

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import static org.mockito.Mockito.*;

import java.util.*;
import java.util.concurrent.Callable;

import com.ericsson.oss.services.ap.api.exception.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.status.StateTransitionEvent;
import com.ericsson.oss.services.ap.api.status.StateTransitionManagerLocal;
import com.ericsson.oss.services.ap.api.workflow.AutoProvisioningWorkflowService;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor;
import com.ericsson.oss.services.ap.common.cm.TransactionalExecutor;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;
/**
 * Unit tests for {@link BindUseCase}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BindUseCaseTest { // NOPMD TooManyFields

    private static final String INVALID_HARDWARE_SERIAL_NUMBER = "ABC123I4567";
    private static final String NODE_STATUS_FDN = NODE_FDN + ",NodeStatus=1";
    private static final String ORDER_COMPLETED = "ORDER_COMPLETED";
    private static final String BIND_COMPLETED = "BIND_COMPLETED";
    private static final String PRE_MIGRATION_COMPLETED = "PRE_MIGRATION_COMPLETED";

    @Mock
    private WorkflowInstanceServiceLocal wfsInstanceService;

    @Mock
    private DpsOperations dps;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private ManagedObject nodeStatusMo;

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private DataPersistenceService dpsService;

    @Mock
    private DpsQueryExecutor<ManagedObject> dpsQueryExecutor;

    @Mock
    private TransactionalExecutor executor;

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private StateTransitionManagerLocal nodeStateTransitionManager;

    @Mock
    protected NodeTypeMapper nodeTypeMapper;

    @Mock
    private ManagedObject apNodeMo;

    @Mock
    private ServiceFinderBean serviceFinder;

    @Mock
    private AutoProvisioningWorkflowService apWorkflowService;

    @InjectMocks
    private BindUseCase bindUseCase;

    private final Map<String, Object> bindAttributes = new HashMap<>();
    private final Map<String, Object> nodeStatusAttributes = new HashMap<>();

    private final Answer<Object> transactionalExecutorAnswer = new Answer<Object>() {

        @SuppressWarnings("unchecked")
        @Override
        public Object answer(final InvocationOnMock invocation) throws Throwable {
            final Callable<Object> callable = (Callable<Object>) invocation.getArguments()[0];
            return callable.call();
        }
    };

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        bindAttributes.put("hardwareSerialNumber", HARDWARE_SERIAL_NUMBER_VALUE);
        nodeStatusAttributes.put("state", BIND_COMPLETED);
        when(dps.getDataPersistenceService()).thenReturn(dpsService);
        when(dps.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        when(liveBucket.findMoByFdn(NODE_FDN)).thenReturn(apNodeMo);
        when(apNodeMo.getAttribute("nodeType")).thenReturn(VALID_NODE_TYPE);
        when(nodeTypeMapper.getInternalEjbQualifier(VALID_NODE_TYPE)).thenReturn(VALID_NODE_TYPE.toLowerCase(Locale.ROOT));
        when(serviceFinder.find(AutoProvisioningWorkflowService.class, VALID_NODE_TYPE.toLowerCase(Locale.ROOT))).thenReturn(apWorkflowService);
        when(apWorkflowService.isSupported(anyString())).thenReturn(true);
        when(executor.execute(any(Callable.class))).then(transactionalExecutorAnswer);
    }

    @Test(expected = InvalidNodeStateException.class)
    public void whenNodeIsInIncorrectStateThenInvalidNodeStateExceptionIsThrown() {
        when(dpsQueries.findMosWithAttributeValue("hardwareSerialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "ap", "Node")).thenReturn(dpsQueryExecutor);

        doReturn(Collections.emptyList().iterator()).when(dpsQueryExecutor).execute();

        doThrow(InvalidNodeStateException.class).when(nodeStateTransitionManager).validateAndSetNextState(NODE_FDN,
            StateTransitionEvent.BIND_STARTED);

        when(nodeTypeMapper.toOssRepresentation(VALID_NODE_TYPE)).thenReturn(VALID_NODE_TYPE);

        when(dpsQueries.findMosWithAttributeValue("productData.serialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "ERBS_NODE_MODEL", "HwUnit")).thenReturn(dpsQueryExecutor);

        doReturn(Collections.emptyList().iterator()).when(dpsQueryExecutor).execute();

        bindUseCase.execute(NODE_FDN, HARDWARE_SERIAL_NUMBER_VALUE);
    }

    @Test
    public void whenBindStartsThenNodeStateIsSetToBindStarted(){

        final String NODE_TYPE = "RadioNode";

        when(liveBucket.findMoByFdn(NODE_FDN)).thenReturn(apNodeMo);
        when(apNodeMo.getAttribute("nodeType")).thenReturn(NODE_TYPE);
        when(nodeTypeMapper.getInternalEjbQualifier(NODE_TYPE)).thenReturn(NODE_TYPE.toLowerCase(Locale.ROOT));
        when(serviceFinder.find(AutoProvisioningWorkflowService.class, NODE_TYPE.toLowerCase(Locale.ROOT))).thenReturn(apWorkflowService);
        when(apWorkflowService.isSupported(anyString())).thenReturn(true);

        when(dpsQueries.findMosWithAttributeValue("hardwareSerialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "ap", "Node")).thenReturn(dpsQueryExecutor);
        doReturn(Collections.emptyList().iterator()).when(dpsQueryExecutor).execute();

        when(liveBucket.findMoByFdn(NODE_STATUS_FDN)).thenReturn(nodeStatusMo);
        when(nodeStatusMo.getAttribute("state")).thenReturn(BIND_COMPLETED);

        when(nodeTypeMapper.toOssRepresentation(NODE_TYPE)).thenReturn(NODE_TYPE);

        when(dpsQueries.findMosWithAttributeValue("serialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "RcsHwIM", "HwItem")).thenReturn(dpsQueryExecutor);
        doReturn(Collections.emptyList().iterator()).when(dpsQueryExecutor).execute();

        bindUseCase.execute(NODE_FDN, HARDWARE_SERIAL_NUMBER_VALUE);

        verify(nodeStateTransitionManager).validateAndSetNextState(NODE_FDN, StateTransitionEvent.BIND_STARTED);
    }

    @Test
    public void whenHardwareSerialNumberNotAlreadyBoundThenBindIsSuccessful() throws WorkflowMessageCorrelationException { // NOPMD

        when(dpsQueries.findMosWithAttributeValue("hardwareSerialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "ap", "Node")).thenReturn(dpsQueryExecutor);
        doReturn(Collections.emptyList().iterator()).when(dpsQueryExecutor).execute();

        when(liveBucket.findMoByFdn(NODE_STATUS_FDN)).thenReturn(nodeStatusMo);
        when(nodeStatusMo.getAttribute("state")).thenReturn(BIND_COMPLETED);

        when(nodeTypeMapper.toOssRepresentation(VALID_NODE_TYPE)).thenReturn(VALID_NODE_TYPE);

        when(dpsQueries.findMosWithAttributeValue(
            "productData.serialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "ERBS_NODE_MODEL", "HwUnit")).thenReturn(dpsQueryExecutor);
        doReturn(Collections.emptyList().iterator()).when(dpsQueryExecutor).execute();

        bindUseCase.execute(NODE_FDN, HARDWARE_SERIAL_NUMBER_VALUE);

        verify(wfsInstanceService).correlateMessage("BIND", "AP_Node=Node1", bindAttributes);
    }

    @Test(expected = HwIdAlreadyBoundException.class)
    public void whenHardwareSerialNumberAlreadyBoundToDifferentNodeThenHwIdAlreadyBoundExceptionIsThrown() {
        final ManagedObject moWithMatchingHwId = Mockito.mock(ManagedObject.class);
        final ManagedObject moWithMatchingHwIdfromENM = Mockito.mock(ManagedObject.class);
        final List<ManagedObject> mosWithMatchingHwId = new ArrayList<>();
        final List<ManagedObject> nodesInEnm = new ArrayList<>();
        nodesInEnm.add(moWithMatchingHwId);
        nodesInEnm.add(moWithMatchingHwIdfromENM);


        when(dpsQueries.findMosWithAttributeValue("hardwareSerialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "ap", "Node")).thenReturn(dpsQueryExecutor);
        doReturn(mosWithMatchingHwId.iterator()).when(dpsQueryExecutor).execute();
        when(moWithMatchingHwId.getFdn()).thenReturn("Project=1,NetworkElement=Node2");

        when(nodeTypeMapper.toOssRepresentation(VALID_NODE_TYPE)).thenReturn(VALID_NODE_TYPE);

        when(dpsQueries.findMosWithAttributeValue(
            "productData.serialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "ERBS_NODE_MODEL", "HwUnit")).thenReturn(dpsQueryExecutor);
        doReturn(nodesInEnm.iterator()).when(dpsQueryExecutor).execute();
        when(moWithMatchingHwIdfromENM.getFdn()).thenReturn("Project=1,NetworkElement=Node3");

        bindUseCase.execute(NODE_FDN, HARDWARE_SERIAL_NUMBER_VALUE);
    }

    @Test(expected = HwIdInvalidFormatException.class)
    public void whenHardwareSerialNumberIsIncorrectFormatThenHwIdAlreadyBoundExceptionIsThrown() {
        bindUseCase.execute(NODE_FDN, INVALID_HARDWARE_SERIAL_NUMBER);
    }

    @Test(expected = UnsupportedCommandException.class)
    public void whenNodeTypeDoesNotSupportBindThenUnsupportedCommandExceptionIsThrown() {
        when(apWorkflowService.isSupported(anyString())).thenReturn(false);
        bindUseCase.execute(NODE_FDN, HARDWARE_SERIAL_NUMBER_VALUE);
    }

    @Test
    public void whenHardwareSerialNumberAlreadyBoundtoSameNodeThenDoNothing() {
        final ManagedObject moWithMatchingHwId = Mockito.mock(ManagedObject.class);
        final List<ManagedObject> mosWithMatchingHwId = new ArrayList<>();
        mosWithMatchingHwId.add(moWithMatchingHwId);

        when(dpsQueries.findMosWithAttributeValue("hardwareSerialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "ap", "Node")).thenReturn(dpsQueryExecutor);
        doReturn(mosWithMatchingHwId.iterator()).when(dpsQueryExecutor).execute();
        when(moWithMatchingHwId.getFdn()).thenReturn(NODE_FDN);

        when(nodeTypeMapper.toOssRepresentation(VALID_NODE_TYPE)).thenReturn(VALID_NODE_TYPE);

        when(dpsQueries.findMosWithAttributeValue(
            "productData.serialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "ERBS_NODE_MODEL", "HwUnit")).thenReturn(dpsQueryExecutor);
        doReturn(mosWithMatchingHwId.iterator()).when(dpsQueryExecutor).execute();
        when(moWithMatchingHwId.getFdn()).thenReturn(NODE_FDN);

        bindUseCase.execute(NODE_FDN, HARDWARE_SERIAL_NUMBER_VALUE);

        verifyZeroInteractions(wfsInstanceService, nodeStateTransitionManager);
    }

    @Test(expected = ApServiceException.class)
    public void whenNodeStateNotBindCompletedThenBindFails() {
        final Map<String, Object> nodeStatusAttribute = new HashMap<>();
        nodeStatusAttribute.put("state", ORDER_COMPLETED);
        final List<MoData> nodeStatusMos = new ArrayList<>();
        nodeStatusMos.add(new MoData(NODE_STATUS_FDN, nodeStatusAttribute, "NodeStatus", null));

        final String NODE_TYPE = "Router6672";

        getMosWithMatchingHwId(NODE_TYPE, "serialNumber", "IPR_HwIM", "HwItem");

        when(liveBucket.findMoByFdn(NODE_STATUS_FDN)).thenReturn(nodeStatusMo);
        when(apNodeMo.getChild(MoType.NODE_STATUS.toString() + "=1")).thenReturn(nodeStatusMo);
        when(nodeStatusMo.getAttribute("state")).thenReturn(ORDER_COMPLETED);
        when(nodeStateTransitionManager.isValidStateTransition(ORDER_COMPLETED, StateTransitionEvent.BIND_FAILED)).thenReturn(true);

        when(nodeTypeMapper.toOssRepresentation(NODE_TYPE)).thenReturn(NODE_TYPE);

        bindUseCase.execute(NODE_FDN, HARDWARE_SERIAL_NUMBER_VALUE);

        verify(nodeStateTransitionManager).validateAndSetNextState(NODE_FDN, StateTransitionEvent.BIND_FAILED);
    }

    @Test(expected = ApServiceException.class)
    public void whenErrorCorrelatingTheWorkflowThenBindFailsAndThrowsApServiceException() throws WorkflowMessageCorrelationException {
        final String NODE_TYPE = "RBS";

        getMosWithMatchingHwId(NODE_TYPE, "productData.serialNumber", "RBS_NODE_MODEL", "HwUnit");

        when(nodeTypeMapper.toOssRepresentation(NODE_TYPE)).thenReturn(NODE_TYPE);

        doThrow(WorkflowMessageCorrelationException.class).when(wfsInstanceService).correlateMessage("BIND", "AP_Node=Node1", bindAttributes);

        bindUseCase.execute(NODE_FDN, HARDWARE_SERIAL_NUMBER_VALUE);
    }

    private void getMosWithMatchingHwId(String nodeType, String hardwareSerialNumber, String namespace, String moType) {
        when(liveBucket.findMoByFdn(NODE_FDN)).thenReturn(apNodeMo);
        when(apNodeMo.getAttribute("nodeType")).thenReturn(nodeType);
        when(nodeTypeMapper.getInternalEjbQualifier(nodeType)).thenReturn(nodeType.toLowerCase(Locale.ROOT));
        when(serviceFinder.find(AutoProvisioningWorkflowService.class, nodeType.toLowerCase(Locale.ROOT))).thenReturn(apWorkflowService);
        when(apWorkflowService.isSupported(anyString())).thenReturn(true);

        when(dpsQueries.findMosWithAttributeValue("hardwareSerialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "ap", "Node")).thenReturn(dpsQueryExecutor);
        doReturn(Collections.emptyList().iterator()).when(dpsQueryExecutor).execute();

        when(dpsQueries.findMosWithAttributeValue(
            hardwareSerialNumber, HARDWARE_SERIAL_NUMBER_VALUE, namespace, moType)).thenReturn(dpsQueryExecutor);
        doReturn(Collections.emptyList().iterator()).when(dpsQueryExecutor).execute();
    }

    @Test(expected = ApServiceException.class)
    public void whenNodeStateNotPreMigrationBindCompletedThenBindFails() {
        final Map<String, Object> nodeStatusAttributesMap = new HashMap<>();
        nodeStatusAttributesMap.put("state", PRE_MIGRATION_COMPLETED);
        final List<MoData> nodeStatusMos = new ArrayList<>();
        nodeStatusMos.add(new MoData(NODE_STATUS_FDN, nodeStatusAttributesMap, "NodeStatus", null));

        when(dpsQueries.findMosWithAttributeValue("hardwareSerialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "ap", "Node")).thenReturn(dpsQueryExecutor);
        doReturn(Collections.emptyList().iterator()).when(dpsQueryExecutor).execute();

        when(nodeTypeMapper.toOssRepresentation(VALID_NODE_TYPE)).thenReturn(VALID_NODE_TYPE);

        when(dpsQueries.findMosWithAttributeValue(
            "productData.serialNumber", HARDWARE_SERIAL_NUMBER_VALUE, "ERBS_NODE_MODEL", "HwUnit")).thenReturn(dpsQueryExecutor);
        doReturn(Collections.emptyList().iterator()).when(dpsQueryExecutor).execute();

        when(liveBucket.findMoByFdn(NODE_STATUS_FDN)).thenReturn(nodeStatusMo);
        when(apNodeMo.getChild(MoType.NODE_STATUS.toString() + "=1")).thenReturn(nodeStatusMo);
        when(apNodeMo.getAttribute(NodeAttribute.IS_NODE_MIGRATION.toString())).thenReturn(true);
        when(nodeStatusMo.getAttribute("state")).thenReturn(PRE_MIGRATION_COMPLETED);
        when(nodeStateTransitionManager.isValidStateTransition(PRE_MIGRATION_COMPLETED, StateTransitionEvent.PRE_MIGRATION_BIND_FAILED)).thenReturn(true);

        bindUseCase.execute(NODE_FDN, HARDWARE_SERIAL_NUMBER_VALUE);

        verify(nodeStateTransitionManager).validateAndSetNextState(NODE_FDN, StateTransitionEvent.PRE_MIGRATION_BIND_FAILED);
    }
}
