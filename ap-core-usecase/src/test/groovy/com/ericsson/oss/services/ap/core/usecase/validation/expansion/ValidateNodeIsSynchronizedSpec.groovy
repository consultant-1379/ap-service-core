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

package com.ericsson.oss.services.ap.core.usecase.validation.common

import com.ericsson.oss.services.ap.api.exception.ValidationCrudException
import com.ericsson.oss.services.ap.core.usecase.validation.expansion.ValidateNodeIsSynchronized

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

class ValidateNodeIsSynchronizedSpec extends CdiSpecification {

    @MockedImplementation
    private Archive archive

    @ObjectUnderTest
    private ValidateNodeIsSynchronized validateExpansionNodeIsSynchronized;

    @Inject
    private DpsQueries dpsQueries

    private ValidationContext validationContext

    private ArchiveArtifact archiveArtifact

    private ManagedObject networkElementMo

    RuntimeConfigurableDps dps

    private final Map<String, Object> projectDataContentTarget = new HashMap<>();

    private static final String NODE_NAME = "Node1"
    private static final String NODE_INFO = "nodeInfo.xml";

    private static final String SYNCHRONIZED_STATUS = "SYNCHRONIZED";
    private static final String UNSYNCHRONIZED_STATUS = "UNSYNCHRONIZED";

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
    }

    def "Validation passes when Expansion node is in sync state in ENM" () {
        given: "Expansion NodeInfo xml"
            archiveArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo("Node1"))
            archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        and: "Network Element MO and CmFunctionMo are created in dps"
            networkElementMo = addNetworkElementMo()
            addCmFunctionMo(SYNCHRONIZED_STATUS)

        when: "CmFunctionMo has sync state"
            boolean isSynced = validateExpansionNodeIsSynchronized.execute(validationContext)

        then: "Validation passed"
            isSynced == true
    }

    def "Validation fails when Expansion node is in unsync state in ENM" () {
        given: "Expansion NodeInfo xml"
            archiveArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo("Node1"))
            archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        and: "Network Element MO and CmFunctionMo are created in dps"
            networkElementMo = addNetworkElementMo()
            addCmFunctionMo(UNSYNCHRONIZED_STATUS)

        when: "CmFunctionMo has unsync state"
            boolean isSynced = validateExpansionNodeIsSynchronized.execute(validationContext)

        then: "Validation failed"
            isSynced == false
    }

    def "Validation fails when CmFunctionMo does not exist in ENM" () {
        given: "Expansion NodeInfo xml"
            archiveArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo("Node1"))
            archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        and: "Network Element MO is created in dps"
            networkElementMo = addNetworkElementMo()

        when: "CmFunctionMo does not exist"
            boolean isSynced = validateExpansionNodeIsSynchronized.execute(validationContext)

        then: "Validation failed"
            isSynced == false
    }

    def "Validation fails when Exception is thrown while trying to find MO" () {
        given: "Expansion NodeInfo xml"
            archiveArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo("Node1"))
            archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        when: "ValidateIsNodeSynchronized rule is executed"
            validateExpansionNodeIsSynchronized.execute(validationContext)

        then: "Correct exception is thrown when MO does not exist in database"
            thrown(ValidationCrudException)
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

        return dps.addManagedObject()
                .withFdn("NetworkElement=" + NODE_NAME)
                .namespace("OSS_NE_DEF")
                .version("2.0.0")
                .name(NODE_NAME)
                .build()
    }

    def addCmFunctionMo(final String status) {
        final Map<String, Object> cmFunctionAttributes = new HashMap<String, Object>()
        cmFunctionAttributes.put("syncStatus", status)

        dps.addManagedObject().parent(networkElementMo)
                .type("CmFunction")
                .version("1.0.1")
                .name("1")
                .addAttributes(cmFunctionAttributes)
                .build()
    }
}
