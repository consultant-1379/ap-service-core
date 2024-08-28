/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core;

import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.HARDWARE_SERIAL_NUMBER_VALUE;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.SHARED_CNF_NODE_TYPE;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.core.usecase.importproject.EoiProjectValidator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps;
import com.ericsson.oss.itpf.sdk.context.ContextService;
import com.ericsson.oss.itpf.sdk.context.classic.ContextConstants;
import com.ericsson.oss.services.ap.api.AutoProvisioningService;
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException;
import com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes;
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.status.StatusEntry;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactImportProgress;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.DpsQueries.DpsQueryExecutor;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.test.stubs.dps.StubbedDpsGenerator;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.rest.builder.NodeStatusDataBuilder;
import com.ericsson.oss.services.ap.core.rest.model.NodeStatusData;
import com.ericsson.oss.services.ap.core.rest.model.StatusEntryData;
import com.ericsson.oss.services.ap.core.usecase.BindUseCase;
import com.ericsson.oss.services.ap.core.usecase.DumpSnapshotUseCase;
import com.ericsson.oss.services.ap.core.usecase.EoiOrderProjectUseCase;
import com.ericsson.oss.services.ap.core.usecase.GetSnapshotUseCase;
import com.ericsson.oss.services.ap.core.usecase.ImportUseCase;
import com.ericsson.oss.services.ap.core.usecase.OrderProjectUseCase;
import com.ericsson.oss.services.ap.core.usecase.SkipUseCase;
import com.ericsson.oss.services.ap.core.usecase.StatusNodeUseCase;
import com.ericsson.oss.services.ap.core.usecase.UseCaseFactory;
import com.ericsson.oss.services.ap.core.usecase.ViewProfilesUseCase;
import com.ericsson.oss.services.ap.core.usecase.importproject.ProjectImporter;
import com.ericsson.oss.services.ap.core.usecase.importproject.ProjectInfo;
import org.slf4j.Logger;

/**
 * Unit tests for {@link AutoProvisioningServiceCoreBean}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AutoProvisioningServiceCoreBeanTest {

    private static final String USER_ID = "user1";
    private static final String FILE_NAME = "Project1.zip";
    private static final byte[] FILE_CONTENT = new byte[1024];
    private static final boolean VALIDATION_REQUIRED = true;
    private static final String DATA_TYPE = "node-plugin-request-action";
    public static final String PROFILE_NAME = "Profile1";
    public static final String PROFILE_FDN = PROJECT_FDN + ",ConfigurationProfile=" + PROFILE_NAME;

    @Mock
    private ContextService contextService;

    @Mock
    private AsyncUseCaseExecutorBean asyncUseCaseExecutorBean;

    @Mock
    AutoProvisioningService autoProvisioningService;

    @Mock
    private UseCaseFactory useCaseFactory;

    @Mock
    NodeStatusDataBuilder nodeStatusDataBuilder;

    @Mock
    private DpsQueries dpsQueries;

    @Mock
    private DpsQueryExecutor<ManagedObject> dpsQueryExecutor;

    @InjectMocks
    private AutoProvisioningServiceCoreBean sut;

    @Mock
    private ProjectImporter projectImporter;

    @Mock
    private EoiProjectValidator eoiProjectValidator;

    @Mock
    private Logger logger;

    @Mock
    private DataPersistenceService dps;

    protected ManagedObject projectMo;
    protected ManagedObject nodeMo;
    protected ManagedObject nodeMo1;

    private static final String NODEARTIFACT_CONTAINER_FDN = NODE_FDN + ",NodeArtifactContainer=1";
    private static final String NODEARTIFACT_FDN_1 = NODEARTIFACT_CONTAINER_FDN + ",NodeArtifact=1";
    public static final String NODE_NAME1 = "Node2";
    public static final String NODE_FDN1 = PROJECT_FDN + ",Node=" + NODE_NAME1;
    private Map<String, Object> artifactAttributes = new HashMap<>();

    final StubbedDpsGenerator dpsGenerator = new StubbedDpsGenerator();

    private List<NodeStatus> nodeStatusList1 = new ArrayList<>();
    private List<StatusEntry> tasksStatus1 = new ArrayList<>();
    private List<StatusEntryData> tasksStatus2 = new ArrayList<>();
    private final StatusEntry statusEntry1 = new StatusEntry("task1", "status1", "time1", "additionalText1");
    private final StatusEntryData statusEntry2 = new StatusEntryData("task1", "status2", "time2", "additionalText1");
    protected ManagedObject nodeArtifactMo1;
    protected ManagedObject artifactContainerMo;
    private final Collection<ManagedObject> artifactMos = new ArrayList<>();
    private List<NodeStatusData> nodeStatusList2 = new ArrayList<>();

    RuntimeConfigurableDps configurableDps = new RuntimeConfigurableDps();
    private static final String GENERATED_LOCATION = "/ericsson/autoprovisioning/artifacts" + File.separator + PROJECT_NAME + File.separator + NODE_NAME1 + File.separator + NODE_NAME1+"_day0.json";

    @Before
    public void setUp(){
        dps = configurableDps.build();
        projectMo = configurableDps.addManagedObject().withFdn(PROJECT_FDN).type(MoType.PROJECT.toString()).build();
        nodeMo = addNodeMo(projectMo);
        nodeMo1 = addEoiNodeMo(projectMo);
        tasksStatus1.add(statusEntry1);
        tasksStatus2.add(statusEntry2);
        nodeStatusList1.add(new NodeStatus(NODE_NAME1, PROJECT_NAME, tasksStatus1, State.EOI_INTEGRATION_COMPLETED.toString()));

        artifactAttributes.put(NodeArtifactAttribute.GEN_LOCATION.toString(), GENERATED_LOCATION);
        artifactAttributes.put(NodeArtifactAttribute.NAME.toString(), "Node2_day0");
        artifactAttributes.put(NodeArtifactAttribute.CONFIGURATION_NODE_NAME.toString(), NODE_NAME1);
    }

    @Test
    public void when_project_is_ordered_it_is_executed_asynchronously() {
        final OrderProjectUseCase usecaseSpy = Mockito.spy(new OrderProjectUseCase());

        doReturn(usecaseSpy).when(useCaseFactory).getNamedUsecase(UseCaseName.ORDER_PROJECT);
        doNothing().when(usecaseSpy).execute(PROJECT_FDN, VALIDATION_REQUIRED);
        when(contextService.getContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY)).thenReturn(USER_ID);

        sut.orderProject(PROJECT_FDN);
        verify(asyncUseCaseExecutorBean).orderProject(PROJECT_FDN, USER_ID, VALIDATION_REQUIRED);
    }

    @Test
    public void orderProjectAfterImportVerifyOrderIsExecutedAsynchronously() {
        final ImportUseCase usecaseSpy1 = Mockito.spy(new ImportUseCase());
        final OrderProjectUseCase usecaseSpy2 = Mockito.spy(new OrderProjectUseCase());
        final ProjectInfo projectInfo = new ProjectInfo();
        projectInfo.setName(PROJECT_NAME);

        doReturn(usecaseSpy1).when(useCaseFactory).getNamedUsecase(UseCaseName.IMPORT);
        doReturn(projectInfo).when(usecaseSpy1).execute(FILE_NAME, FILE_CONTENT, VALIDATION_REQUIRED);

        doReturn(usecaseSpy2).when(useCaseFactory).getNamedUsecase(UseCaseName.ORDER_PROJECT);
        doNothing().when(usecaseSpy2).execute(PROJECT_FDN, VALIDATION_REQUIRED, projectInfo);

        when(contextService.getContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY)).thenReturn(USER_ID);

        sut.orderProject(FILE_NAME, FILE_CONTENT, VALIDATION_REQUIRED);
        verify(asyncUseCaseExecutorBean).orderProject(PROJECT_FDN, USER_ID, VALIDATION_REQUIRED, projectInfo);
    }

    @Test
    public void when_bind_using_node_fdn_then_bind_succeeds() {
        final BindUseCase usecaseMock = Mockito.mock(BindUseCase.class);
        doReturn(usecaseMock).when(useCaseFactory).getNamedUsecase(UseCaseName.BIND);

        sut.bind(NODE_FDN, HARDWARE_SERIAL_NUMBER_VALUE);

        verify(usecaseMock).execute(NODE_FDN, HARDWARE_SERIAL_NUMBER_VALUE);
        verifyZeroInteractions(dpsQueries);
    }

    @Test(expected = NodeNotFoundException.class)
    public void whenBindUsingNodeName_andNodeDoesNotExist_thenNodeNotFoundExceptionIsThrown() {
        final BindUseCase usecaseMock = Mockito.mock(BindUseCase.class);
        doReturn(usecaseMock).when(useCaseFactory).getNamedUsecase(UseCaseName.BIND);

        when(dpsQueries.findMoByName(NODE_NAME, "Node", "ap")).thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(Collections.<ManagedObject> emptyIterator());

        sut.bind(NODE_NAME, HARDWARE_SERIAL_NUMBER_VALUE);

        verify(usecaseMock).execute(NODE_FDN, HARDWARE_SERIAL_NUMBER_VALUE);
    }

    @Test
    public void whenViewProfileByProfileTypeByUsingProjectFDNAndDataType() {
        final ViewProfilesUseCase profilesUsecaseMock = Mockito.mock(ViewProfilesUseCase.class);
        doReturn(profilesUsecaseMock).when(useCaseFactory).getNamedUsecase(UseCaseName.VIEW_PROFILES);

        sut.viewProfilesByProfileType(PROJECT_FDN, DATA_TYPE);

        verify(profilesUsecaseMock).execute(PROJECT_FDN, DATA_TYPE);
    }

    @Test
    public void whenViewProfilesByUsingProjectFDN() {
        final ViewProfilesUseCase profilesUsecaseMock = Mockito.mock(ViewProfilesUseCase.class);
        doReturn(profilesUsecaseMock).when(useCaseFactory).getNamedUsecase(UseCaseName.VIEW_PROFILES);

        sut.viewProfiles(PROJECT_FDN);

        verify(profilesUsecaseMock).execute(PROJECT_FDN, null);
    }

    @Test
    public void whenSkipUsingNodeFdnThenSkipSucceeds() {
        final SkipUseCase usecaseMock = Mockito.mock(SkipUseCase.class);
        doReturn(usecaseMock).when(useCaseFactory).getNamedUsecase(UseCaseName.SKIP);

        sut.skip(NODE_FDN);

        verify(usecaseMock).execute(NODE_FDN);
    }

    @Test
    public void whenDumpSnapshotThenDumpSucceeds() {
        final DumpSnapshotUseCase usecaseMock = Mockito.mock(DumpSnapshotUseCase.class);
        doReturn(usecaseMock).when(useCaseFactory).getNamedUsecase(UseCaseName.DUMP_SNAPSHOT);

        sut.dumpSnapshot(PROJECT_FDN, PROFILE_NAME, NODE_FDN, PROFILE_FDN);

        verify(usecaseMock).execute(PROJECT_FDN, PROFILE_NAME, NODE_FDN, PROFILE_FDN);
    }

    @Test
    public void whenGetSnapshotThenGetSucceeds() {
        final GetSnapshotUseCase usecaseMock = Mockito.mock(GetSnapshotUseCase.class);
        doReturn(usecaseMock).when(useCaseFactory).getNamedUsecase(UseCaseName.GET_SNAPSHOT);

        sut.getSnapshot(PROJECT_FDN, PROFILE_NAME, NODE_FDN);

        verify(usecaseMock).execute(PROJECT_FDN, PROFILE_NAME, NODE_FDN);
    }

    @Test
    public void whenProjectIsEoiOrderedItIsExecutedAsynchronously() {
        final String baseUrl = "https://enmapache.athtem.eei.ericsson.se";
        final String sessionId = "dkjdsfkjaflksjld";
        final EoiOrderProjectUseCase usecaseSpy = Mockito.spy(new EoiOrderProjectUseCase());
        final Map<String, Object> nodeData = new HashMap<>();
        nodeData.put("nodeName", "Node1");
        nodeData.put("nodeType", SHARED_CNF_NODE_TYPE);
        nodeData.put("cnfType", "Shared-CNF");
        nodeData.put("ipAddress", "1.2.3.4");
        nodeData.put("ossPrefix", "subNetwork=Autoprovisioning");
        nodeData.put("nodeIdentifier","23.Q1-R68145");
        final List<Map<String, Object>> networkElements = Arrays.asList(nodeData);

        final Map<String,Object> eoiProjectRequest = new HashMap<>();
        eoiProjectRequest.put(ProjectRequestAttributes.PROJECT_NAME.toString(), PROJECT_NAME);
        eoiProjectRequest.put(ProjectRequestAttributes.CREATOR.toString(),"Creator");
        eoiProjectRequest.put(ProjectRequestAttributes.DESCRIPTION.toString(),"Description");
        eoiProjectRequest.put(ProjectRequestAttributes.EOI_NETWORK_ELEMENTS.toString(),networkElements);
        eoiProjectRequest.put("baseUrl", baseUrl);
        eoiProjectRequest.put("sessionId", sessionId);
        doReturn(usecaseSpy).when(useCaseFactory).getNamedUsecase(UseCaseName.EOI_ORDER_PROJECT);
        doNothing().when(usecaseSpy).execute(PROJECT_FDN, baseUrl,sessionId);
        when(contextService.getContextValue(ContextConstants.HTTP_HEADER_USERNAME_KEY)).thenReturn(USER_ID);

        sut.eoiOrderProject(eoiProjectRequest);
        verify(asyncUseCaseExecutorBean).eoiOrderProject(PROJECT_FDN, USER_ID, baseUrl, sessionId);
    }

    @Test
    public void whenDownloadConfigurationDataMethodIsCalled(){
        nodeStatusList2.add(new NodeStatusData(NODE_NAME1, PROJECT_NAME, State.EOI_INTEGRATION_COMPLETED.getDisplayName(), tasksStatus2));
        artifactAttributes.put(NodeArtifactAttribute.IMPORT_PROGRESS.toString(), ArtifactImportProgress.COMPLETED.name());

        artifactContainerMo = createNodeArtifactContainerMo(nodeMo1);
        nodeArtifactMo1 = createNodeArtifactMo(NODEARTIFACT_FDN_1, artifactContainerMo, artifactAttributes);

        artifactMos.add(nodeArtifactMo1);
    	
        final String artifactName = NODE_NAME1+"_day0";
        final List<ManagedObject> nodeArtifactList = new ArrayList<>();
        nodeArtifactList.add(nodeArtifactMo1);
        final StatusNodeUseCase usecaseSpy = Mockito.mock(StatusNodeUseCase.class);
        doReturn(usecaseSpy).when(useCaseFactory).getNamedUsecase(UseCaseName.STATUS_NODE);
        doReturn(nodeStatusList1.get(0)).when(usecaseSpy).execute(NODE_FDN1);
        when(nodeStatusDataBuilder.buildNodeStatusData(nodeStatusList1.get(0))).thenReturn(nodeStatusList2.get(0));
        when(dpsQueries.findMosWithAttributeValue(NodeArtifactAttribute.NAME.toString(), artifactName, Namespace.AP.toString(), MoType.NODE_ARTIFACT.toString()))
            .thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(nodeArtifactList.iterator());

        String generate = sut.downloadConfigurationFile(NODE_FDN1, NODE_NAME1);
        assertTrue(generate.contains("Node2_day0.json"));
    }

    @Test
    public void whenDownloadConfigurationDataMethodIsCalledWithImportProgressValue(){
        nodeStatusList2.add(new NodeStatusData(NODE_NAME1, PROJECT_NAME, State.EOI_INTEGRATION_COMPLETED.getDisplayName(), tasksStatus2));
        artifactAttributes.put(NodeArtifactAttribute.IMPORT_PROGRESS.toString(), ArtifactImportProgress.IN_PROGRESS.name());

        artifactContainerMo = createNodeArtifactContainerMo(nodeMo1);
        nodeArtifactMo1 = createNodeArtifactMo(NODEARTIFACT_FDN_1, artifactContainerMo, artifactAttributes);

        artifactMos.add(nodeArtifactMo1);
        final String artifactName = NODE_NAME1+"_day0";
        final List<ManagedObject> nodeArtifactList = new ArrayList<>();
        nodeArtifactList.add(nodeArtifactMo1);
        final StatusNodeUseCase usecaseSpy = Mockito.mock(StatusNodeUseCase.class);
        doReturn(usecaseSpy).when(useCaseFactory).getNamedUsecase(UseCaseName.STATUS_NODE);
        doReturn(nodeStatusList1.get(0)).when(usecaseSpy).execute(NODE_FDN1);
        when(nodeStatusDataBuilder.buildNodeStatusData(nodeStatusList1.get(0))).thenReturn(nodeStatusList2.get(0));
        when(dpsQueries.findMosWithAttributeValue(NodeArtifactAttribute.NAME.toString(), artifactName, Namespace.AP.toString(), MoType.NODE_ARTIFACT.toString()))
            .thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(nodeArtifactList.iterator());

        String generate = sut.downloadConfigurationFile(NODE_FDN1, NODE_NAME1);
        assertTrue(generate == null);
    }

    @Test
    public void whenDownloadConfigurationDataMethodIsCalledWithIntegrationState(){
        nodeStatusList2.add(new NodeStatusData(NODE_NAME1, PROJECT_NAME, State.EOI_INTEGRATION_COMPLETED.toString(), tasksStatus2));
        artifactAttributes.put(NodeArtifactAttribute.IMPORT_PROGRESS.toString(), ArtifactImportProgress.IN_PROGRESS.name());

        artifactContainerMo = createNodeArtifactContainerMo(nodeMo1);
        nodeArtifactMo1 = createNodeArtifactMo(NODEARTIFACT_FDN_1, artifactContainerMo, artifactAttributes);

        artifactMos.add(nodeArtifactMo1);
        final String artifactName = NODE_NAME1+"_day0";
        final List<ManagedObject> nodeArtifactList = new ArrayList<>();
        nodeArtifactList.add(nodeArtifactMo1);
        final StatusNodeUseCase usecaseSpy = Mockito.mock(StatusNodeUseCase.class);
        doReturn(usecaseSpy).when(useCaseFactory).getNamedUsecase(UseCaseName.STATUS_NODE);
        doReturn(nodeStatusList1.get(0)).when(usecaseSpy).execute(NODE_FDN1);
        when(nodeStatusDataBuilder.buildNodeStatusData(nodeStatusList1.get(0))).thenReturn(nodeStatusList2.get(0));
        when(dpsQueries.findMosWithAttributeValue(NodeArtifactAttribute.NAME.toString(), artifactName, Namespace.AP.toString(), MoType.NODE_ARTIFACT.toString()))
            .thenReturn(dpsQueryExecutor);
        when(dpsQueryExecutor.execute()).thenReturn(nodeArtifactList.iterator());

        String generate = sut.downloadConfigurationFile(NODE_FDN1, NODE_NAME1);
        assertTrue(generate == null);
    }

    private ManagedObject createNodeArtifactMo(String artifactFdn, ManagedObject artifactContainerMo, Map<String, Object> attributes) {
        return configurableDps.addManagedObject()
            .withFdn(artifactFdn)
            .namespace(AP.toString())
            .type(MoType.NODE_ARTIFACT.toString())
            .addAttributes(attributes)
            .version("1.0.0")
            .parent(artifactContainerMo)
            .build();
    }

    private ManagedObject createNodeArtifactContainerMo(final ManagedObject nodeMo) {
        return configurableDps.addManagedObject()
            .withFdn(NODEARTIFACT_CONTAINER_FDN)
            .namespace(AP.toString())
            .type(MoType.NODE_ARTIFACT_CONTAINER.toString())
            .version("1.0.0")
            .parent(nodeMo)
            .build();
    }

    private ManagedObject addNodeMo(final ManagedObject parentMo) {
        final Map<String,Object> attributes = new HashMap<>();
        attributes.put(NodeAttribute.NODE_TYPE.toString(), "RadioNode");
        return configurableDps.addManagedObject()
            .withFdn(NODE_FDN)
            .type(MoType.NODE.toString())
            .namespace("ap")
            .addAttributes(attributes)
            .version("1.0.0")
            .parent(parentMo)
            .build();
    }
    private ManagedObject addEoiNodeMo(final ManagedObject parentMo) {
        final Map<String,Object> attributes = new HashMap<>();
        attributes.put(NodeAttribute.NODE_TYPE.toString(), "Shared-CNF");
        return configurableDps.addManagedObject()
            .withFdn(NODE_FDN1)
            .type(MoType.NODE.toString())
            .namespace(AP.toString())
            .addAttributes(attributes)
            .version("1.0.0")
            .parent(parentMo)
            .build();
    }

}