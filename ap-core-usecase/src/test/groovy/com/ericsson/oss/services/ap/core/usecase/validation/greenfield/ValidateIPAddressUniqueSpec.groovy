/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.ap.core.usecase.validation.greenfield

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.validation.ValidationContext
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact
import com.ericsson.oss.services.ap.core.usecase.archive.Archive
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader
import com.ericsson.oss.services.nedo.ipaddress.validator.DuplicateIpAddressValidationResult
import com.ericsson.oss.services.nedo.ipaddress.validator.DuplicateIpAddressValidator

class ValidateIPAddressUniqueSpec extends CdiSpecification {

    private static final String FAILURE_FDN = "Project=Test,Node=Node00001"
    private static final String TEST_IP = "1.1.1.1"

    private static final String NODE = "Node"
    private static final String NODE_INFO = "nodeInfo.xml"

    @MockedImplementation
    private Archive archive

    @MockedImplementation
    private DuplicateIpAddressValidator duplicateIpAddressValidator

    @MockedImplementation
    private NodeInfoReader nodeInfoReader

    @ObjectUnderTest
    private ValidateIPAddressUnique validateIPAddressUnique

    private ValidationContext validationContext

    private ArchiveArtifact nodeInfoArtifact

    private final Map<String, Object> projectDataContentTarget = new HashMap<>()

    private static final successfulValidationResult = new DuplicateIpAddressValidationResult()
    private static final failureValidationResult = new DuplicateIpAddressValidationResult(FAILURE_FDN, TEST_IP)

    private static final invalidValidationResult = new DuplicateIpAddressValidationResult("Invalid IP", new Exception())

    def setup() {
        final NodeInfo testNodeInfo = new NodeInfo()
        testNodeInfo.name = NODE
        testNodeInfo.ipAddress = TEST_IP

        nodeInfoReader.read(archive, NODE) >> testNodeInfo

        List<String> directoryList = new ArrayList<>()
        directoryList.add(NODE)
        archive.getAllDirectoryNames() >> directoryList

        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), "testProject.zip")
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archive)
        projectDataContentTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryList)
        validationContext = new ValidationContext("import", projectDataContentTarget)

        nodeInfoArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo(NODE))
        archive.getArtifactOfNameInDir(NODE, NODE_INFO) >> nodeInfoArtifact
    }

    def "Validation passes when ip is unique accross the system in project file"() {
        given: "Project file and test ip is NOT in the system"
            duplicateIpAddressValidator.checkForValidIpAddress(_ as List<String>, false) >> successfulValidationResult

        when: "There is a node in the project file with the test ip"
            boolean isValid = validateIPAddressUnique.execute(validationContext)

        then: "Validation passed"
            isValid
    }

    def "Validation fails when ip is NOT unique across the system in project file"() {
        given: "Project file and test ip IS in the system"
            duplicateIpAddressValidator.checkForValidIpAddress(_ as List<String>, false) >> failureValidationResult

        when: "There is a node in the project file with the test ip"
            boolean isValid = validateIPAddressUnique.execute(validationContext)

            def errors = validationContext.validationErrors
            boolean hasIpAddressValidationError = false
            for (final String error : errors) {
                if (error.contains(FAILURE_FDN)) {
                    hasIpAddressValidationError = true
                }
            }

        then: "Validation failed and validation error is in context"
            !isValid
            hasIpAddressValidationError
    }

    /**
     * Test AP does not fail with an invalid IP response from DuplicatedIpAddressValidator,
     * since AP performs it's own validity check.
     *
     * @return void
     */
    def "Validation passes when invalid ip is provided"() {
        given: "Project file and invalid ip address"
            duplicateIpAddressValidator.checkForValidIpAddress(_ as List<String>, false) >> invalidValidationResult

        when: "There is a node in the project file with an invalid ip"
            boolean isValid = validateIPAddressUnique.execute(validationContext)

        then: "Validation passed"
            isValid
    }

    def getNodeInfo(final String nodeName) {
        return """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
        <nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
            xsi:noNamespaceSchemaLocation=\"ExpansionNodeInfo.xsd\">
            <name>${nodeName}</name>
            <ipAddress>${TEST_IP}</ipAddress>
            <artifacts>
                <configurations>
                    <nodeConfiguration>radio.xml</nodeConfiguration>
                </configurations>
            </artifacts>
        </nodeInfo>"""
    }
}
