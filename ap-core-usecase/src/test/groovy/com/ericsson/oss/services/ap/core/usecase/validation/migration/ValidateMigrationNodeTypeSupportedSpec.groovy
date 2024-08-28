/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.validation.migration

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.api.validation.ValidationContext
import com.ericsson.oss.services.ap.api.workflow.AutoProvisioningWorkflowService
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.common.model.access.ModelReader
import com.ericsson.oss.services.ap.common.model.access.NodeTypeMapperImpl
import com.ericsson.oss.services.ap.core.usecase.archive.Archive
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey
import com.ericsson.oss.services.ap.core.usecase.workflow.ApWorkflowServiceResolver
import org.mockito.internal.util.reflection.Whitebox
import javax.inject.Inject

class ValidateMigrationNodeTypeSupportedSpec extends CdiSpecification {

    @MockedImplementation
    private Archive archive;

    @MockedImplementation
    private ModelReader modelReader;

    @MockedImplementation
    private NodeTypeMapperImpl nodeTypeMapper;

    @MockedImplementation
    private ApWorkflowServiceResolver apWorkflowServiceResolver;

    @MockedImplementation
    private AutoProvisioningWorkflowService autoProvisioningWorkflowService;

    @MockedImplementation
    private AutoProvisioningWorkflowService autoProvisioningWorkflowServiceUnsupportedNode;

    @ObjectUnderTest
    private ValidateMigrationNodeTypeSupported validateMigrationNodeTypeSupported;

    private ValidationContext validationContext;

    @Inject
    private DpsQueries dpsQueries

    private ArchiveArtifact nodeInfoArtifact;

    RuntimeConfigurableDps dps

    private final Map<String, Object> projectDataContentTarget = new HashMap<>();

    private static final String NODE_NAME = "Node1"
    private static final String NODE_INFO = "nodeInfo.xml";

    private static final String INVALID_MIGRATION_NODE_TYPE_2 = "ERBS";
    private static final String SUPPORTED_MIGRATION_NODE_TYPE = "RadioNode";

    private static final List<String> SUPPORTED_NODE_TYPES = Arrays.asList(INVALID_MIGRATION_NODE_TYPE_2, SUPPORTED_MIGRATION_NODE_TYPE);

    def setup() {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)

        Whitebox.setInternalState(dpsQueries, "dps", dps.build())
        List<String> directoryList = new ArrayList<>();
        directoryList.add(NODE_NAME);
        archive.getAllDirectoryNames() >> directoryList;

        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), "testProject.zip");
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archive);
        projectDataContentTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryList);
        validationContext = new ValidationContext("import", projectDataContentTarget);

        modelReader.getSupportedNodeTypes() >> SUPPORTED_NODE_TYPES;

        nodeTypeMapper.getInternalEjbQualifier(SUPPORTED_MIGRATION_NODE_TYPE) >> "ecim"
        nodeTypeMapper.getInternalEjbQualifier(INVALID_MIGRATION_NODE_TYPE_2) >> "erbs"

        apWorkflowServiceResolver.getApWorkflowService("ecim") >> autoProvisioningWorkflowService
        apWorkflowServiceResolver.getApWorkflowService("erbs") >> autoProvisioningWorkflowServiceUnsupportedNode
    }

    def "Verify validation passed when corresponding node type is supported" () {
        given: "Migration nodeInfo xml"
        addNetworkElementMo(SUPPORTED_MIGRATION_NODE_TYPE)
        nodeInfoArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo("Node1"))
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> nodeInfoArtifact
        autoProvisioningWorkflowService.getMigrationWorkflowName() >> "auto_migration_ecim"

        when: "node type is supported"
        boolean isNodeTypeSupp = validateMigrationNodeTypeSupported.execute(validationContext)

        then: "the check returns true "
        isNodeTypeSupp == true
    }

    def "Verify validation failed when corresponding node type is not supported" () {
        given: "Migration nodeInfo xml"
        addNetworkElementMo(INVALID_MIGRATION_NODE_TYPE_2)
        nodeInfoArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo("Node1"))
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> nodeInfoArtifact
        autoProvisioningWorkflowServiceUnsupportedNode.getMigrationWorkflowName() >> null

        when: "node type is not supported"
        boolean isNodeTypeSupp = validateMigrationNodeTypeSupported.execute(validationContext)

        then: "the check returns false "
        isNodeTypeSupp == false
    }

    def getNodeInfo(final String nodeName) {
        return  """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
          <nodeInfo xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:noNamespaceSchemaLocation="MigrationNodeInfo.xsd">
              <name>${nodeName}</name>
              <artifacts>
                  <configurations>
                      <nodeConfiguration>radio.xml</nodeConfiguration>
                  </configurations>
              </artifacts>
          </nodeInfo>"""
    }

    def addNetworkElementMo(final String nodeType) {
        final Map<String, Object> networkElementAttributes = new HashMap<String, Object>()
        networkElementAttributes.put("neType", nodeType)

        dps.addManagedObject()
                .withFdn("NetworkElement=" + NODE_NAME)
                .namespace("OSS_NE_DEF")
                .version("2.0.0")
                .name(NODE_NAME)
                .addAttributes(networkElementAttributes)
                .build()
    }
}
