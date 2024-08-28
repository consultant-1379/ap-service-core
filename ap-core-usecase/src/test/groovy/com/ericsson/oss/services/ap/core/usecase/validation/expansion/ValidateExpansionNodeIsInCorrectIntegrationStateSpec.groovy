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

package com.ericsson.oss.services.ap.core.usecase.validation.expansion;

import javax.inject.Inject

import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.api.validation.ValidationContext
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact
import com.ericsson.oss.services.ap.core.usecase.archive.Archive
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey

class ValidateExpansionNodeIsInCorrectIntegrationStateSpec extends CdiSpecification {

    @MockedImplementation
    private Archive archive;

    @ObjectUnderTest
    private ValidateExpansionNodeIsInCorrectIntegrationState validateExpansionNodeIsInCorrectIntegrationState;

    @Inject
    private DpsQueries dpsQueries

    private ValidationContext validationContext;

    private ArchiveArtifact archiveArtifact;

    private ManagedObject nodeMo

    RuntimeConfigurableDps dps

    private final Map<String, Object> projectDataContentTarget = new HashMap<>();

    private static final String NODE_NAME = "Node1"
    private static final String NODE_INFO = "nodeInfo.xml";

    def setup() {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        nodeMo = addNodeMo()
        Whitebox.setInternalState(dpsQueries, "dps", dps.build())
        List<String> directoryList = new ArrayList<>();
        directoryList.add(NODE_NAME);
        archive.getAllDirectoryNames() >> directoryList;

        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), "testProject.zip");
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archive);
        projectDataContentTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryList);
        validationContext = new ValidationContext("import", projectDataContentTarget);
    }

    def "Validate if Expansion node is in correct integration state in ENM" () {
        given: "Expansion NodeInfo xml"
        archiveArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo("Node1"))
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        when: "The node MO exists in ENM, and also has its child nodeStatus MO with different kinds of status"
        addNodeStatusMo(status)

        then: "Verify if the state is correct"
        validateExpansionNodeIsInCorrectIntegrationState.execute(validationContext) == isIntegrationState

        where:
        status                                     |  isIntegrationState
        "INTEGRATION_COMPLETED"                    |   true
        "INTEGRATION_FAILED"                       |   true
        "INTEGRATION_CANCELLED"                    |   true
        "INTEGRATION_COMPLETED_WITH_WARNING"       |   true
        "EXPANSION_STARTED"                        |   false
        "READY_FOR_ORDER"                          |   false
        "READY_FOR_EXPANSION"                      |   false
        "EXPANSION_SUSPENDED"                      |   false
        "EXPANSION_IMPORT_CONFIGURATION_SUSPENDED" |   false
        "EXPANSION_CANCELLED"                      |   true
        "EXPANSION_COMPLETED"                      |   true
        "EXPANSION_FAILED"                         |   true
        "ORDER_STARTED"                            |   false
        "ORDER_COMPLETED"                          |   false
        "ORDER_CANCELLED"                          |   false
    }

    def getNodeInfo(final String nodeName) {
          return  """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
          <nodeInfo xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:noNamespaceSchemaLocation="ExpansionNodeInfo.xsd">
              <name>${nodeName}</name>
              <artifacts>
                  <configurations>
                      <nodeConfiguration>radio.xml</nodeConfiguration>
                  </configurations>
              </artifacts>
          </nodeInfo>"""
    }

    def addNodeMo() {

        return dps.addManagedObject()
                .withFdn("Node=" + NODE_NAME)
                .namespace("ap")
                .version("2.0.0")
                .name(NODE_NAME)
                .build()
    }

    def addNodeStatusMo(final String state) {
        final Map<String, Object> nodeStatusAttributes = new HashMap<String, Object>()
        nodeStatusAttributes.put("state", state)

        dps.addManagedObject().parent(nodeMo)
                .type("NodeStatus")
                .version("1.0.1")
                .name("1")
                .addAttributes(nodeStatusAttributes)
                .build()
    }
}
