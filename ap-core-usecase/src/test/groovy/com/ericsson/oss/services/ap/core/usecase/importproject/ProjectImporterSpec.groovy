/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.importproject

import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.common.cm.TransactionalExecutor
import org.mockito.internal.util.reflection.Whitebox

import java.util.concurrent.Callable

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_FDN
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.SHARED_CNF_NODE_TYPE
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.VALID_NODE_TYPE

import javax.inject.Inject

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService
import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.HierarchicalPrimaryTypeSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo
import com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants
import com.ericsson.oss.services.ap.api.exception.ApNodeExistsException
import com.ericsson.oss.services.ap.api.exception.ApServiceException
import com.ericsson.oss.services.ap.api.model.ModelData
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper
import com.ericsson.oss.services.ap.common.model.access.ModelReader
import com.ericsson.oss.services.ap.common.model.access.ModeledAttributeFilter
import com.ericsson.oss.services.ap.common.test.util.assertions.CommonAssertionsSpec
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.ap.common.usecase.UseCaseName
import com.ericsson.oss.services.ap.common.workflow.ActivityType
import com.ericsson.oss.services.ap.core.usecase.DeleteNodeUseCase
import com.ericsson.oss.services.ap.core.usecase.DeleteProjectUseCase
import com.ericsson.oss.services.ap.core.usecase.archive.Archive
import com.ericsson.oss.services.ap.core.usecase.archive.ArchiveReader
import com.ericsson.oss.services.ap.core.usecase.importproject.data.ProjectData
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase
import com.ericsson.oss.services.ap.core.usecase.utilities.ZipUtil



class ProjectImporterSpec extends CdiSpecification {

    private static final String PROJECT_FILE_NAME = "project.zip"
    private static final String GREENFIELD_FDN = "Project=Project1,Node=Node1"
    private static final String RADIO_NODE = "RadioNode"
    private static final String ECIM = "ecim"
    private static final String VERSION = "1.0.0"
    private static final String LICENSE_OPTIONS_FDN = GREENFIELD_FDN + ",LicenseOptions=1"
    private static final String NODE_ARTIFACT_CONTAINER_FDN = GREENFIELD_FDN + ",NodeArtifactContainer=1"
    private static final String AUTOINTEGRATION_OPTIONS_FDN = GREENFIELD_FDN + ",AutoIntegrationOptions=1"
    private static final String SUPERVISION_OPTIONS_FDN = GREENFIELD_FDN + ",SupervisionOptions=1"
    private static final String NODESTATUS_FDN = GREENFIELD_FDN + ",NodeStatus=2"
    private static final String IP_ADDRESS = "ipAddress"
    private static final String CI_REF = "ciRef"
    private static final String SECURITY = "Security"

    final Collection<PersistenceObject> ciRefAssociations = new ArrayList<>()

    @ObjectUnderTest
    private ProjectImporter projectImporter

    @MockedImplementation
    private NodeArtifactMosCreator nodeArtifactMosCreator

    @MockedImplementation
    private ModeledAttributeFilter modeledAttrFilter

    @MockedImplementation
    private ModelReader modelReader

    @MockedImplementation
    private HierarchicalPrimaryTypeSpecification primaryTypeSpecification

    @MockedImplementation
    @UseCase(name = UseCaseName.DELETE_PROJECT)
    private DeleteProjectUseCase deleteProjectUseCase  // NOSONAR

    @MockedImplementation
    @UseCase(name = UseCaseName.DELETE_NODE)
    private DeleteNodeUseCase deleteNodeUseCase

    @Inject
    private PersistenceObject persistenceObject

    @Inject
    private NodeTypeMapper nodeTypeMapper

    @Inject
    private DataPersistenceService dataPersistenceService

    private RuntimeConfigurableDps dps

    @Inject
    private DpsQueries dpsQueries

    @MockedImplementation
    private TransactionalExecutor executor

    def setup() {

        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        dataPersistenceService = dps.build()
        dpsQueries.getDataPersistenceService() >> dataPersistenceService
        executor.execute(_ as Callable) >> { Callable call -> call.call() }
        Whitebox.setInternalState(dpsQueries, "dps", dps.build())
        Whitebox.setInternalState(dpsQueries, "executor", executor)
        MoCreatorSpec.setDps(dps)
        final ManagedObject networkElement = MoCreatorSpec.createNetworkElementMo("HardwareReplace_RadioNode_Node1", RADIO_NODE, persistenceObject)
        final ManagedObject securityFunctionMo = MoCreatorSpec.createSecurityFunctionMo("HardwareReplace_RadioNode_Node1", networkElement)
        MoCreatorSpec.createNetworkElementSecurityMo("HardwareReplace_RadioNode_Node1", securityFunctionMo)
        retrieveConnectivityInformationIpAddress(networkElement)
        modelReader.getLatestPrimaryTypeSpecification(_ as String, SECURITY) >> primaryTypeSpecification
        ModelInfo temp = new ModelInfo(SchemaConstants.DPS_PRIMARYTYPE, "ap_" + VALID_NODE_TYPE.toLowerCase(), "Security", "1.0.0")
        primaryTypeSpecification.getModelInfo() >> (temp)
        modeledAttrFilter.apply(_ as String, _ as String, _ as Map<String, String>) >> { namespace, type, attributes -> attributes }
        nodeTypeMapper.getNamespace(RADIO_NODE) >> ECIM
        modelReader.getLatestPrimaryTypeSpecification(_ as String, _ as String) >> primaryTypeSpecification
        modelReader.getLatestPrimaryTypeModel(_ as String, _ as String) >> (new ModelData(ECIM, VERSION))
    }

    /**
     * This test verifies the following
     * <li> AP Project MO is created </li>
     * <li> AP child Mos are created </li>
     */
    def "AP project.zip is imported successfully all relevent MO's are created"() {

        given: "AP Project with one node"
        final Archive projectArchive = createZipArchive()

        when: "importing project.zip file"
        projectImporter.importProject(PROJECT_FILE_NAME, projectArchive)

        then: "AP project MO should be created with child MO's"
        CommonAssertionsSpec.assertMoCreated(dps, PROJECT_FDN)
        CommonAssertionsSpec.assertMoCreated(dps, NODE_FDN)
        CommonAssertionsSpec.assertMoCreated(dps, LICENSE_OPTIONS_FDN)
        CommonAssertionsSpec.assertMoCreated(dps, NODE_ARTIFACT_CONTAINER_FDN)
        CommonAssertionsSpec.assertMoCreated(dps, AUTOINTEGRATION_OPTIONS_FDN)
        CommonAssertionsSpec.assertMoCreated(dps, SUPERVISION_OPTIONS_FDN)
        CommonAssertionsSpec.assertMoCreated(dps, NODESTATUS_FDN)
    }

    def "AP project.zip is imported successfully when project MO with same name already exists"() {

        given: "AP Project with one node"
        final Archive projectArchive = createZipArchive()

        and: "an AP Project MO already exists with same Project name"
        MoCreatorSpec.createProjectMo(PROJECT_FDN)

        when: "importing project.zip file"
        projectImporter.importProject(PROJECT_FILE_NAME, projectArchive)

        then: "AP Node MO is created"
        CommonAssertionsSpec.assertMoCreated(dps, NODE_FDN)
    }

    def "AP project.zip is imported successfully when containing multiple mixed nodes"() {
        given: "AP project.zip with 4 mixed nodes"
        final Archive projectArchive = createMixedZipArchive()

        and: "networkElement MO exists"
        MoCreatorSpec.createNetworkElementMo("Expansion_RadioNode_Node", RADIO_NODE, persistenceObject)
        MoCreatorSpec.createNetworkElementMo("Migration_RadioNode_Node", RADIO_NODE, persistenceObject)

        when: "import project.zip file"
        final ProjectInfo projectInfo = projectImporter.importProject(PROJECT_FILE_NAME, projectArchive)

        then: "mixed nodes in AP project.zip are imported successfully"
        Map<String, NodeInfo> nodeInfo = projectInfo.getNodeInfos()
        nodeInfo.get("Node1").getActivity() == ActivityType.GREENFIELD_ACTIVITY
        nodeInfo.get("HardwareReplace_RadioNode_Node1").getActivity() == ActivityType.HARDWARE_REPLACE_ACTIVITY
        nodeInfo.get("Expansion_RadioNode_Node").getActivity() == ActivityType.EXPANSION_ACTIVITY
        nodeInfo.get("Migration_RadioNode_Node").getActivity() == ActivityType.MIGRATION_ACTIVITY
    }

    def "when importing AP project.zip and ap Node MO already exists then ApNodeExistsException is thrown"() {

        given: "AP Project with one node"
        final Archive projectArchive = createZipArchive()

        and: "Node MO already exists"
        final ManagedObject projectMo = MoCreatorSpec.createProjectMo(PROJECT_FDN)
        MoCreatorSpec.createNodeMo(GREENFIELD_FDN, projectMo)

        when: "import project.zip file"
        projectImporter.importProject(PROJECT_FILE_NAME, projectArchive)

        then: "AP Node exists exception is thrown"
        thrown(ApNodeExistsException)
    }

    /**
     * Import project.zip is rolled back when exception is thrown.
     * {@link ApServiceException} is thrown when creating Artifact MO
     * The following will be tested:
     * <li> AP Project MO is deleted </li>
     * <li> {@link ApServiceException} is thrown </li>
     */
    def "when importing AP project.zip and create Artifact MO throws an exception then AP usecase will roll back"() {
        given: "AP project.zip with one node"
        final Archive projectArchive = createZipArchive()

        and: "create Artifact MO throws exception"
        nodeArtifactMosCreator.createArtifactsAndMos(_ as String, _ as Archive) >> { throw new ApServiceException() }

        when: "importing AP project.zip file"
        projectImporter.importProject(PROJECT_FILE_NAME, projectArchive)

        then: "AP project MO is deleted"
        thrown(ApServiceException)
        1 * deleteProjectUseCase.execute(PROJECT_FDN, true)
        0 * deleteNodeUseCase.execute(NODE_FDN, false)
    }

    /**
     * Import project.zip is rolled back when exception is thrown.
     * {@link ApServiceException} is thrown when creating Artifact MO
     * The following will be tested:
     * <li> {@link ApServiceException} is thrown </li>
     * <li> AP Project MO is not deleted </li>
     * <li> AP Node MO is deleted </li>
     */
    def "when importing AP project.zip and create Artifact MO throws an exception then AP usecase will roll back node MO only"() {
        given: "AP project.zip with one node"
        final Archive projectArchive = createZipArchive()

        and: "project MO already exists"
        MoCreatorSpec.createProjectMo(PROJECT_FDN)

        and: "create Artifact MO throws exception"
        nodeArtifactMosCreator.createArtifactsAndMos(_ as String, _ as Archive) >> { throw new ApServiceException() }

        when: "importing AP project.zip file"
        projectImporter.importProject(PROJECT_FILE_NAME, projectArchive)

        then: "AP node MO is deleted and AP Project MO is not deleted"
        thrown(ApServiceException)
        0 * deleteProjectUseCase.execute(PROJECT_FDN, true)
        1 * deleteNodeUseCase.execute(NODE_FDN, false)

    }

    def "when Integrate EOI nodes and ap Node MO already exists then ApNodeExistsException is thrown"() {

        given: "List of networkElements"
        final Map<String, Object> nodeData = new HashMap<>();
        nodeData.put("name", "Node1");
        nodeData.put("nodeType", SHARED_CNF_NODE_TYPE);
        nodeData.put("cnfType", "Shared-CNF");
        nodeData.put("ipAddress", "1.2.3.4");
        nodeData.put("ossPrefix", "subNetwork=Autoprovisioning");
        nodeData.put("nodeIdentifier", "23.Q1-R68145");
        final List<Map<String, Object>> networkElements = Arrays.asList(nodeData);

        and: "Node MO already exists"
        final ManagedObject projectMo = MoCreatorSpec.createProjectMo(PROJECT_FDN)
        MoCreatorSpec.createNodeMo(GREENFIELD_FDN, projectMo)

        when: "create eoiNodes"
        projectImporter.createEoiNodes(networkElements, PROJECT_FDN)

        then: "AP Node exists exception is thrown"
        thrown(ApNodeExistsException)
    }


    private Archive createZipArchive() throws IOException {
        final Map<String, String> projectArchive = new HashMap<>()
        projectArchive.put("projectInfo.xml", ProjectData.STANDARD_PROJECT_INFO)
        projectArchive.put("node1/nodeInfo.xml", ProjectData.NODE_INFO)
        projectArchive.put("node1/LicenseRequest.xml", ProjectData.LICENSE_REQUEST)
        final byte[] zipFile = ZipUtil.createProjectZipFile(projectArchive)
        return ArchiveReader.read(zipFile)
    }

    private Archive createMixedZipArchive() throws IOException {
        final Map<String, String> projectArchive = new HashMap<>()
        projectArchive.put("projectInfo.xml", ProjectData.STANDARD_PROJECT_INFO)
        projectArchive.put("node1/nodeInfo.xml", ProjectData.NODE_INFO,)
        projectArchive.put("node1/LicenseRequest.xml", ProjectData.LICENSE_REQUEST)
        projectArchive.put("node2/nodeInfo.xml", ProjectData.REPLACE_NODE_INFO)
        projectArchive.put("node3/nodeInfo.xml", ProjectData.EXPANSION_NODE_INFO)
        projectArchive.put("node4/nodeInfo.xml", ProjectData.MIGRATION_NODE_INFO)
        projectArchive.put("node4/LicenseRequest.xml", ProjectData.LICENSE_REQUEST)
        final byte[] zipFile = ZipUtil.createProjectZipFile(projectArchive)
        return ArchiveReader.read(zipFile)
    }

    private void retrieveConnectivityInformationIpAddress(final ManagedObject networkElement){
        ciRefAssociations.add(persistenceObject)
        networkElement.getTarget().getAssociations(CI_REF) >> ciRefAssociations
        ciRefAssociations.iterator().next() >> persistenceObject
        persistenceObject.getAttribute(IP_ADDRESS) >> "1.1.1.1"
    }
}
