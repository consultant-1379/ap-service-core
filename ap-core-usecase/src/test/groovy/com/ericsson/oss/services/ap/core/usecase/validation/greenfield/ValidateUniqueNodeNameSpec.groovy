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

package com.ericsson.oss.services.ap.core.usecase.validation.greenfield;

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.validation.ValidationContext
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact
import com.ericsson.oss.services.ap.core.usecase.archive.Archive

class ValidateUniqueNodeNameSpec extends CdiSpecification {

    @MockedImplementation
    private Archive archive;

    @ObjectUnderTest
    private ValidateUniqueNodeName uniqueNodeNameValidator;

    private ValidationContext validationContext;

    private ArchiveArtifact nodeInfoArtifact1;
    private ArchiveArtifact nodeInfoArtifact2;

    private final Map<String, Object> projectDataContentTarget = new HashMap<>();

    private static final String NODE_1 = "Node1"
    private static final String NODE_2 = "Node2"
    private static final String NODE_INFO = "nodeInfo.xml";

    def "Validation passes when node is unique in project file" () {
        given: "Project file"
        List<String> directoryList = new ArrayList<>();
        directoryList.add(NODE_1);
        archive.getAllDirectoryNames() >> directoryList;

        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), "testProject.zip");
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archive);
        projectDataContentTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryList);
        validationContext = new ValidationContext("import", projectDataContentTarget);

        nodeInfoArtifact1 = new ArchiveArtifact("NodeInfo", getNodeInfo(NODE_1))
        archive.getArtifactOfNameInDir(NODE_1, NODE_INFO) >> nodeInfoArtifact1

        when: "There is unique node in the project file"
        boolean isTrue = uniqueNodeNameValidator.execute(validationContext)

        then: "Validation passed"
        isTrue == true
    }

    def "Validation pass when two folder with the different node name in the project name" () {
        given: "NodeInfo xml file1, NodeInfo xml file2"
        List<String> directoryList = new ArrayList<>();
        directoryList.add(NODE_1);
        directoryList.add(NODE_2);
        archive.getAllDirectoryNames() >> directoryList;

        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), "testProject.zip");
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archive);
        projectDataContentTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryList);
        validationContext = new ValidationContext("import", projectDataContentTarget);

        nodeInfoArtifact1 = new ArchiveArtifact("NodeInfo", getNodeInfo(NODE_1))
        archive.getArtifactOfNameInDir(NODE_1, NODE_INFO) >> nodeInfoArtifact1

        nodeInfoArtifact2 = new ArchiveArtifact("NodeInfo", getNodeInfo(NODE_2))
        archive.getArtifactOfNameInDir(NODE_2, NODE_INFO) >> nodeInfoArtifact2

        when: "There is unique node in the project file"
        boolean isUniqueNode = uniqueNodeNameValidator.execute(validationContext)

        then: "Verification failed"
        isUniqueNode == true
    }

    def "Validation fails when two folder with the same node name in the project name" () {
        given: "NodeInfo xml file1, NodeInfo xml file2"
        List<String> directoryList = new ArrayList<>();
        directoryList.add(NODE_1);
        directoryList.add(NODE_2);
        archive.getAllDirectoryNames() >> directoryList;

        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), "testProject.zip");
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archive);
        projectDataContentTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryList);
        validationContext = new ValidationContext("import", projectDataContentTarget);

        nodeInfoArtifact1 = new ArchiveArtifact("NodeInfo", getNodeInfo(NODE_1))
        archive.getArtifactOfNameInDir(NODE_1, NODE_INFO) >> nodeInfoArtifact1

        nodeInfoArtifact2 = new ArchiveArtifact("NodeInfo", getNodeInfo(NODE_1))
        archive.getArtifactOfNameInDir(NODE_2, NODE_INFO) >> nodeInfoArtifact2

        when: "There is unique node in the project file"
        boolean isUniqueNode = uniqueNodeNameValidator.execute(validationContext)

        then: "Verification failed"
        isUniqueNode == false
    }

    def "Validation fails when the nodeinfo.xml has no node name" () {
        given: "NodeInfo xml file"
        List<String> directoryList = new ArrayList<>();
        directoryList.add(NODE_1);
        archive.getAllDirectoryNames() >> directoryList;

        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), "testProject.zip");
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archive);
        projectDataContentTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryList);
        validationContext = new ValidationContext("import", projectDataContentTarget);

        nodeInfoArtifact1 = new ArchiveArtifact("NodeInfo", getInvalidNodeInfo())
        archive.getArtifactOfNameInDir(NODE_1, NODE_INFO) >> nodeInfoArtifact1

        when: "There is unique node in the project file"
        boolean isValidateName = uniqueNodeNameValidator.execute(validationContext)

        then: "Verification failed"
        isValidateName == false
    }

    def "Validation fails when the nodeInfo.xml has an invalid name" () {
        given: "Invalid NodeInfo xml file name"
        String NODE_INFO = "nodeInfox.xml";
        List<String> directoryList = new ArrayList<>();
        directoryList.add(NODE_1);
        archive.getAllDirectoryNames() >> directoryList;

        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), "testProject.zip");
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archive);
        projectDataContentTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryList);
        validationContext = new ValidationContext("import", projectDataContentTarget);

        ArchiveArtifact nodeInfoArchiveArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo())

        archive.getArtifactOfNameInDir(NODE_1, NODE_INFO) >> nodeInfoArchiveArtifact

        when: "There is an invalid nodeInfo.xml in the project file"
        boolean isValidName = uniqueNodeNameValidator.execute(validationContext)

        then: "Validation failed"
        isValidName == false
    }

    def getNodeInfo(final String nodeName) {
        return  """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
        <nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
            xsi:noNamespaceSchemaLocation=\"ExpansionNodeInfo.xsd\">
            <name>${nodeName}</name>
            <artifacts>
                <configurations>
                    <nodeConfiguration>radio.xml</nodeConfiguration>
                </configurations>
            </artifacts>
        </nodeInfo>"""
    }

    def getInvalidNodeInfo() {
        return  """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
        <nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
            xsi:noNamespaceSchemaLocation=\"ExpansionNodeInfo.xsd\">
            <artifacts>
                <configurations>
                    <nodeConfiguration>radio.xml</nodeConfiguration>
                </configurations>
            </artifacts>
        </nodeInfo>"""
    }
}
