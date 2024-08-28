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
package com.ericsson.oss.services.ap.core.usecase.validation.common

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.security.accesscontrol.EAccessControl
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecuritySubject
import com.ericsson.oss.services.ap.api.validation.ValidationContext
import com.ericsson.oss.services.ap.common.util.capability.SecurityCapability
import com.ericsson.oss.services.ap.common.util.capability.ServiceCapabilityModel
import com.ericsson.oss.services.ap.core.usecase.archive.Archive
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey

class ValidateUserIsAuthorizedSpec extends CdiSpecification {

    @MockedImplementation
    private Archive archive

    @MockedImplementation
    private NodeInfo nodeInfo

    @MockedImplementation
    private NodeInfoReader nodeInfoReader

    @MockedImplementation
    private EAccessControl eAccessControl

    @MockedImplementation
    private ESecuritySubject authUser

    @ObjectUnderTest
    private ValidateUserIsAuthorized validateUserIsAuthorized

    private ValidationContext validationContext

    private SecurityCapability securityCapability1
    private SecurityCapability securityCapability2

    private static final String NODE_NAME = "Node1"
    private static final String BASELINE_ARTIFACT_TAG = "baseline"
    private static final String BASELINE_ARTIFACT1 = "postIntegration.mos"
    private static final String APPLY_MOS_SCRIPT = "APPLY_AMOS_SCRIPT"

    private final Map<String, Object> projectDataContentTarget = new HashMap<>();
    private final List<String> directoryList = new ArrayList<>()
    private final Map<String, List<String>> nodeArtifacts = new HashMap<>()
    private final List<String> baselineArtifacts = new ArrayList<>()
    private final List<SecurityCapability> securityCapabilities = new ArrayList<>()

    def setup() {

        directoryList.add(NODE_NAME)

        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archive);
        validationContext = new ValidationContext("import", projectDataContentTarget);

        nodeInfo.getNodeArtifacts() >> nodeArtifacts
        nodeInfoReader.read(archive, NODE_NAME) >> nodeInfo

        List<String> actions1 = new ArrayList<>()
        actions1.add("execute")
        actions1.add("read")
        securityCapability1 = new SecurityCapability()
        securityCapability1.setResource("flowautomation")
        securityCapability1.setOperations(actions1)

        List<String> actions2 = new ArrayList<>()
        actions2.add("execute")
        securityCapability2 = new SecurityCapability()
        securityCapability2.setResource("scripting_cli_access")
        securityCapability2.setOperations(actions2)

        eAccessControl.getAuthUserSubject() >> authUser

    }

    def "Validation passes when nodeInfo has baseline artifact configured, and RBAC verify user is authorized." () {
        given: "NodeInfo.xml has baseline artifact configured. And RBAC verify user is authorized."
                baselineArtifacts.add(BASELINE_ARTIFACT1)
                nodeArtifacts.put(BASELINE_ARTIFACT_TAG, baselineArtifacts)

                securityCapabilities.add(securityCapability1)
                ServiceCapabilityModel.INSTANCE.getRequiredCapabilities(APPLY_MOS_SCRIPT) >> securityCapabilities
                eAccessControl.isAuthorized(authUser, _, _) >> true

        when:
                boolean isAuthorized = validateUserIsAuthorized.validate(validationContext, directoryList)

        then: "Validation passes"
                isAuthorized == true
    }

    def "Validation fails when nodeInfo has baseline artifact configured, and RBAC verify user is not authorized." () {
        given: "NodeInfo.xml has baseline artifact configured. And RBAC verify user is not authorized."
                baselineArtifacts.add(BASELINE_ARTIFACT1)
                nodeArtifacts.put(BASELINE_ARTIFACT_TAG, baselineArtifacts)

                securityCapabilities.add(securityCapability1)
                securityCapabilities.add(securityCapability2)
                ServiceCapabilityModel.INSTANCE.getRequiredCapabilities(APPLY_MOS_SCRIPT) >> securityCapabilities
                eAccessControl.isAuthorized(authUser, _, _) >> false

        when:
                boolean isAuthorized = validateUserIsAuthorized.validate(validationContext, directoryList)

        then: "Validation fails"
                isAuthorized == false
    }

    def "Validation passes when nodeInfo does not have baseline artifact configured, even RBAC verify user is not authorized." () {
        given: "NodeInfo.xml does not have baseline artifact configured. And RBAC verify user is not authorized."
                securityCapabilities.add(securityCapability2)
                ServiceCapabilityModel.INSTANCE.getRequiredCapabilities(APPLY_MOS_SCRIPT) >> securityCapabilities
                eAccessControl.isAuthorized(authUser, _, _) >> false

        when:
                boolean isAuthorized = validateUserIsAuthorized.validate(validationContext, directoryList)

        then: "Validation passes"
                isAuthorized == true
    }
}
