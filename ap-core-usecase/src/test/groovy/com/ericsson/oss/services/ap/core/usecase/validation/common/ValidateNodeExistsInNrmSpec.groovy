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

package com.ericsson.oss.services.ap.core.usecase.validation.common;

import javax.inject.Inject

import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.api.validation.ValidationContext
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact
import com.ericsson.oss.services.ap.core.usecase.archive.Archive
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey

class ValidateNodeExistsInNrmSpec extends CdiSpecification {

    @MockedImplementation
    private Archive archive;

    @ObjectUnderTest
    private ValidateNodeExistsInNrm validateExpansionNodeExistsInNrm;

    @Inject
    private DpsQueries dpsQueries

    private ValidationContext validationContext;

    private ArchiveArtifact archiveArtifact;

    RuntimeConfigurableDps dps

    private final Map<String, Object> projectDataContentTarget = new HashMap<>();

    private static final String NODE_NAME = "Node1"
    private static final String NODE_INFO = "nodeInfo.xml";

    def setup() {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        addNetworkElementMo()
        Whitebox.setInternalState(dpsQueries, "dps", dps.build())
        List<String> directoryList = new ArrayList<>();
        directoryList.add(NODE_NAME);
        archive.getAllDirectoryNames() >> directoryList;

        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), "testProject.zip");
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archive);
        projectDataContentTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryList);
    }

    def "Validation passes when Expansion node MO exists in ENM" () {
        given: "Expansion NodeInfo xml"
        validationContext = new ValidationContext("import", projectDataContentTarget);
        archiveArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo("Node1"))
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        when: "The node specified in nodeInfo.xml has corresponding NE MO in ENM"
        boolean isExisting = validateExpansionNodeExistsInNrm.execute(validationContext)

        then: "Verification passed"
        isExisting == true
    }

    def "Validation fails when Expansion node MO does NOT exist in ENM" () {
        given: "Expansion NodeInfo xml"
        validationContext = new ValidationContext("import", projectDataContentTarget);
        archiveArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo("Node2"))
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        when: "The node specified in nodeInfo.xml does not have corresponding NE MO in ENM"
        boolean isExisting = validateExpansionNodeExistsInNrm.execute(validationContext)

        then: "Verification failed"
        isExisting == false
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

    def addNetworkElementMo() {

    dps.addManagedObject()
       .withFdn("NetworkElement=" + NODE_NAME)
       .namespace("OSS_NE_DEF")
       .version("2.0.0")
       .name(NODE_NAME)
       .build()
    }
}