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

package com.ericsson.oss.services.ap.core.usecase.validation.common;

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
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey

class ValidateRemoteNodeAvailabilitySpec extends CdiSpecification {

    @MockedImplementation
    private Archive archive;

    @MockedImplementation
    private NodeInfoReader nodeInfoReader

    @ObjectUnderTest
    private ValidateRemoteNodeAvailability remoteNodeVelidator;

    @Inject
    private DpsQueries dpsQueries

    private ValidationContext validationContext;
    private NodeInfo nodeInfo;

    RuntimeConfigurableDps dps

    private final Map<String, Object> projectDataContentTarget = new HashMap<>();

    private static final String NODE_NAME = "Node1"
    private static final String NETCONF_FILE_CONTENT = "<rpc><edit-config/></rpc>"
    private static final String NON_NETCONF_FILE_CONTENT = "<RbsSiteInstallationFile></RbsSiteInstallationFile>"
    private static final String REMOTE_NODE_1 = "RemoteNode1"
    private static final String REMOTE_NODE_2 = "RemoteNode2"
    private static final String REMOTE_NODE_3 = "RemoteNode3"
    private static final String REMOTE_NODE_CONFIGURATION_1 = "remoteNodeConfiguration1.xml"
    private static final String REMOTE_NODE_CONFIGURATION_2 = "remoteNodeConfiguration2.xml"
    private static final String REMOTE_NODE_CONFIGURATION_3 = "remoteNodeConfiguration3.xml"
    private static final String SYNCHRONIZED_STATUS = "SYNCHRONIZED";
    private static final String UNSYNCHRONIZED_STATUS = "UNSYNCHRONIZED";
    private static final String ERBS_NE_TYPE = "ERBS";
    private static final String RADIO_NODE_NE_TYPE = "RadioNode";

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

    def "Validation passes when nodename is provided and node is sync for single Netconf file" () {
        given: "nodename is provided for the Netconf file and remote node is sync"
        ArchiveArtifact remoteArtifact1 = new ArchiveArtifact(NODE_NAME + "/" + REMOTE_NODE_CONFIGURATION_1, NETCONF_FILE_CONTENT)
        archive.getArtifactOfNameInDir(NODE_NAME, REMOTE_NODE_CONFIGURATION_1) >> remoteArtifact1
        final ManagedObject networkElementMo = addNetworkElementMo(REMOTE_NODE_1, RADIO_NODE_NE_TYPE)
        addCmFunctionMo(networkElementMo, SYNCHRONIZED_STATUS)
        nodeInfo = new NodeInfo();
        final Map<String, String> remoteNodeNames = new HashMap<>();
        remoteNodeNames.put(REMOTE_NODE_CONFIGURATION_1, REMOTE_NODE_1);
        nodeInfo.setRemoteNodeNames(remoteNodeNames);
        final Map<String, List<String>> configurations = new HashMap<>();
        final List<String> fileNames = new ArrayList<>();
        fileNames.add(REMOTE_NODE_CONFIGURATION_1);
        configurations.put("remoteNodeConfiguration", fileNames);
        nodeInfo.setConfigurations(configurations);
        nodeInfoReader.read(archive, _ as String) >> nodeInfo

        when: "Validate the remote node"
        boolean isTrue = remoteNodeVelidator.execute(validationContext)

        then: "Validation passes"
        isTrue == true
    }

    def "Validation passes when nodenames are provided for multiple Netconf files and the remote nodes are sync" () {
        given: "nodenames are provided for the multiple Netconf files and remote nodes are sync"
        ArchiveArtifact remoteArtifact1 = new ArchiveArtifact(NODE_NAME + "/" + REMOTE_NODE_CONFIGURATION_1, NETCONF_FILE_CONTENT)
        ArchiveArtifact remoteArtifact2 = new ArchiveArtifact(NODE_NAME + "/" + REMOTE_NODE_CONFIGURATION_2, NETCONF_FILE_CONTENT)
        archive.getArtifactOfNameInDir(NODE_NAME, REMOTE_NODE_CONFIGURATION_1) >> remoteArtifact1
        archive.getArtifactOfNameInDir(NODE_NAME, REMOTE_NODE_CONFIGURATION_2) >> remoteArtifact2
        final ManagedObject networkElementMo1 = addNetworkElementMo(REMOTE_NODE_1, RADIO_NODE_NE_TYPE)
        addCmFunctionMo(networkElementMo1, SYNCHRONIZED_STATUS)
        final ManagedObject networkElementMo2 = addNetworkElementMo(REMOTE_NODE_2, RADIO_NODE_NE_TYPE)
        addCmFunctionMo(networkElementMo2, SYNCHRONIZED_STATUS)
        nodeInfo = new NodeInfo();
        final Map<String, String> remoteNodeNames = new HashMap<>();
        remoteNodeNames.put(REMOTE_NODE_CONFIGURATION_1, REMOTE_NODE_1);
        remoteNodeNames.put(REMOTE_NODE_CONFIGURATION_2, REMOTE_NODE_2);
        nodeInfo.setRemoteNodeNames(remoteNodeNames);
        final Map<String, List<String>> configurations = new HashMap<>();
        final List<String> fileNames = new ArrayList<>();
        fileNames.add(REMOTE_NODE_CONFIGURATION_1);
        fileNames.add(REMOTE_NODE_CONFIGURATION_2);
        configurations.put("remoteNodeConfiguration", fileNames);
        nodeInfo.setConfigurations(configurations);
        nodeInfoReader.read(archive, _ as String) >> nodeInfo

        when: "Validate the remote nodes"
        boolean isTrue = remoteNodeVelidator.execute(validationContext)

        then: "Validation passes"
        isTrue == true
    }

    def "Validation passes when nodename is provided for Netconf remote node file but missing for non-Netconf file" () {
        given: "nodename is provided for the Netconf remote node file but missing for non-Netconf file"
        ArchiveArtifact remoteArtifact1 = new ArchiveArtifact(NODE_NAME + "/" + REMOTE_NODE_CONFIGURATION_1, NETCONF_FILE_CONTENT)
        ArchiveArtifact remoteArtifact2 = new ArchiveArtifact(NODE_NAME + "/" + REMOTE_NODE_CONFIGURATION_2, NON_NETCONF_FILE_CONTENT)
        archive.getArtifactOfNameInDir(NODE_NAME, REMOTE_NODE_CONFIGURATION_1) >> remoteArtifact1
        archive.getArtifactOfNameInDir(NODE_NAME, REMOTE_NODE_CONFIGURATION_2) >> remoteArtifact2
        final ManagedObject networkElementMo = addNetworkElementMo(REMOTE_NODE_1, RADIO_NODE_NE_TYPE)
        addCmFunctionMo(networkElementMo, SYNCHRONIZED_STATUS)
        nodeInfo = new NodeInfo();
        final Map<String, String> remoteNodeNames = new HashMap<>();
        remoteNodeNames.put(REMOTE_NODE_CONFIGURATION_1, REMOTE_NODE_1);
        remoteNodeNames.put(REMOTE_NODE_CONFIGURATION_2, null);
        nodeInfo.setRemoteNodeNames(remoteNodeNames);
        final Map<String, List<String>> configurations = new HashMap<>();
        final List<String> fileNames = new ArrayList<>();
        fileNames.add(REMOTE_NODE_CONFIGURATION_1);
        fileNames.add(REMOTE_NODE_CONFIGURATION_2);
        configurations.put("remoteNodeConfiguration", fileNames);
        nodeInfo.setConfigurations(configurations);
        nodeInfoReader.read(archive, _ as String) >> nodeInfo

        when: "Validate if the nodenames are available for remoteNodeConfiguration"
        boolean isTrue = remoteNodeVelidator.execute(validationContext)

        then: "Validation passes"
        isTrue == true
    }

    def "Validation fails when one nodename is not provided for one of the multiple Netconf files" () {
        given: "one nodename is not provided for one of the multiple Netconf files"
        ArchiveArtifact remoteArtifact1 = new ArchiveArtifact(NODE_NAME + "/" + REMOTE_NODE_CONFIGURATION_1, NETCONF_FILE_CONTENT)
        ArchiveArtifact remoteArtifact2 = new ArchiveArtifact(NODE_NAME + "/" + REMOTE_NODE_CONFIGURATION_2, NETCONF_FILE_CONTENT)
        archive.getArtifactOfNameInDir(NODE_NAME, REMOTE_NODE_CONFIGURATION_1) >> remoteArtifact1
        archive.getArtifactOfNameInDir(NODE_NAME, REMOTE_NODE_CONFIGURATION_2) >> remoteArtifact2
        final ManagedObject networkElementMo = addNetworkElementMo(REMOTE_NODE_1, RADIO_NODE_NE_TYPE)
        addCmFunctionMo(networkElementMo, SYNCHRONIZED_STATUS)
        nodeInfo = new NodeInfo();
        final Map<String, String> remoteNodeNames = new HashMap<>();
        remoteNodeNames.put(REMOTE_NODE_CONFIGURATION_1, REMOTE_NODE_1);
        remoteNodeNames.put(REMOTE_NODE_CONFIGURATION_2, null);
        nodeInfo.setRemoteNodeNames(remoteNodeNames);
        final Map<String, List<String>> configurations = new HashMap<>();
        final List<String> fileNames = new ArrayList<>();
        fileNames.add(REMOTE_NODE_CONFIGURATION_1);
        fileNames.add(REMOTE_NODE_CONFIGURATION_2);
        configurations.put("remoteNodeConfiguration", fileNames);
        nodeInfo.setConfigurations(configurations);
        nodeInfoReader.read(archive, _ as String) >> nodeInfo

        when: "Validate the remote node"
        boolean isTrue = remoteNodeVelidator.execute(validationContext)

        then: "Validation fails"
        isTrue == false
    }

    def "Validation fails when one nodename is not provided for a Netconf file of multiple Netconf and BulkCM files" () {
        given: "nodename is not provided for the last netconf remote node configuration file"
        ArchiveArtifact remoteArtifact1 = new ArchiveArtifact(NODE_NAME + "/" + REMOTE_NODE_CONFIGURATION_1, NETCONF_FILE_CONTENT)
        ArchiveArtifact remoteArtifact2 = new ArchiveArtifact(NODE_NAME + "/" + REMOTE_NODE_CONFIGURATION_2, NON_NETCONF_FILE_CONTENT)
        ArchiveArtifact remoteArtifact3 = new ArchiveArtifact(NODE_NAME + "/" + REMOTE_NODE_CONFIGURATION_3, NETCONF_FILE_CONTENT)
        archive.getArtifactOfNameInDir(NODE_NAME, REMOTE_NODE_CONFIGURATION_1) >> remoteArtifact1
        archive.getArtifactOfNameInDir(NODE_NAME, REMOTE_NODE_CONFIGURATION_2) >> remoteArtifact2
        archive.getArtifactOfNameInDir(NODE_NAME, REMOTE_NODE_CONFIGURATION_3) >> remoteArtifact3
        final ManagedObject networkElementMo = addNetworkElementMo(REMOTE_NODE_1, RADIO_NODE_NE_TYPE)
        addCmFunctionMo(networkElementMo, SYNCHRONIZED_STATUS)
        nodeInfo = new NodeInfo();
        final Map<String, String> remoteNodeNames = new HashMap<>();
        remoteNodeNames.put(REMOTE_NODE_CONFIGURATION_1, REMOTE_NODE_1);
        remoteNodeNames.put(REMOTE_NODE_CONFIGURATION_2, REMOTE_NODE_2);
        remoteNodeNames.put(REMOTE_NODE_CONFIGURATION_3, null);
        nodeInfo.setRemoteNodeNames(remoteNodeNames);
        final Map<String, List<String>> configurations = new HashMap<>();
        final List<String> fileNames = new ArrayList<>();
        fileNames.add(REMOTE_NODE_CONFIGURATION_1);
        fileNames.add(REMOTE_NODE_CONFIGURATION_2);
        fileNames.add(REMOTE_NODE_CONFIGURATION_3);
        configurations.put("remoteNodeConfiguration", fileNames);
        nodeInfo.setConfigurations(configurations);
        nodeInfoReader.read(archive, _ as String) >> nodeInfo

        when: "Validate the remote nodes"
        boolean isTrue = remoteNodeVelidator.execute(validationContext)

        then: "Validation fails"
        isTrue == false
    }

    def "Validation fails when all nodenames are provided for multiple Netconf and BulkCM files but one of the remote node is not sync" () {
        given: "nodenames are provided for multiple Netconf and BulkCM files but one of the remote node is not sync"
        ArchiveArtifact remoteArtifact1 = new ArchiveArtifact(NODE_NAME + "/" + REMOTE_NODE_CONFIGURATION_1, NON_NETCONF_FILE_CONTENT)
        ArchiveArtifact remoteArtifact2 = new ArchiveArtifact(NODE_NAME + "/" + REMOTE_NODE_CONFIGURATION_2, NETCONF_FILE_CONTENT)
        archive.getArtifactOfNameInDir(NODE_NAME, REMOTE_NODE_CONFIGURATION_1) >> remoteArtifact1
        archive.getArtifactOfNameInDir(NODE_NAME, REMOTE_NODE_CONFIGURATION_2) >> remoteArtifact2
        final ManagedObject networkElementMo1 = addNetworkElementMo(REMOTE_NODE_1, ERBS_NE_TYPE)
        final ManagedObject networkElementMo2 = addNetworkElementMo(REMOTE_NODE_2, RADIO_NODE_NE_TYPE)
        addCmFunctionMo(networkElementMo1, SYNCHRONIZED_STATUS)
        addCmFunctionMo(networkElementMo2, UNSYNCHRONIZED_STATUS)
        nodeInfo = new NodeInfo();
        final Map<String, String> remoteNodeNames = new HashMap<>();
        remoteNodeNames.put(REMOTE_NODE_CONFIGURATION_1, REMOTE_NODE_1);
        remoteNodeNames.put(REMOTE_NODE_CONFIGURATION_2, REMOTE_NODE_2);
        nodeInfo.setRemoteNodeNames(remoteNodeNames);
        final Map<String, List<String>> configurations = new HashMap<>();
        final List<String> fileNames = new ArrayList<>();
        fileNames.add(REMOTE_NODE_CONFIGURATION_1);
        fileNames.add(REMOTE_NODE_CONFIGURATION_2);
        configurations.put("remoteNodeConfiguration", fileNames);
        nodeInfo.setConfigurations(configurations);
        nodeInfoReader.read(archive, _ as String) >> nodeInfo

        when: "Validate the remote nodes"
        boolean isTrue = remoteNodeVelidator.execute(validationContext)

        then: "Validation fails"
        isTrue == false
    }

    def "Validation fails when nodename is provided for Netconf file but it is not Radio Node" () {
        given: "nodenames are provided for multiple Netconf and BulkCM files but one of the remote node is not sync"
        ArchiveArtifact remoteArtifact1 = new ArchiveArtifact(NODE_NAME + "/" + REMOTE_NODE_CONFIGURATION_1, NETCONF_FILE_CONTENT)
        ArchiveArtifact remoteArtifact2 = new ArchiveArtifact(NODE_NAME + "/" + REMOTE_NODE_CONFIGURATION_2, NETCONF_FILE_CONTENT)
        ArchiveArtifact remoteArtifact3 = new ArchiveArtifact(NODE_NAME + "/" + REMOTE_NODE_CONFIGURATION_3, NETCONF_FILE_CONTENT)
        archive.getArtifactOfNameInDir(NODE_NAME, REMOTE_NODE_CONFIGURATION_1) >> remoteArtifact1
        archive.getArtifactOfNameInDir(NODE_NAME, REMOTE_NODE_CONFIGURATION_2) >> remoteArtifact2
        archive.getArtifactOfNameInDir(NODE_NAME, REMOTE_NODE_CONFIGURATION_3) >> remoteArtifact3
        final ManagedObject networkElementMo1 = addNetworkElementMo(REMOTE_NODE_1, RADIO_NODE_NE_TYPE)
        final ManagedObject networkElementMo2 = addNetworkElementMo(REMOTE_NODE_2, RADIO_NODE_NE_TYPE)
        final ManagedObject networkElementMo3 = addNetworkElementMo(REMOTE_NODE_3, ERBS_NE_TYPE)
        addCmFunctionMo(networkElementMo1, SYNCHRONIZED_STATUS)
        addCmFunctionMo(networkElementMo2, SYNCHRONIZED_STATUS)
        addCmFunctionMo(networkElementMo3, SYNCHRONIZED_STATUS)
        nodeInfo = new NodeInfo();
        final Map<String, String> remoteNodeNames = new HashMap<>();
        remoteNodeNames.put(REMOTE_NODE_CONFIGURATION_1, REMOTE_NODE_1);
        remoteNodeNames.put(REMOTE_NODE_CONFIGURATION_2, REMOTE_NODE_2);
        remoteNodeNames.put(REMOTE_NODE_CONFIGURATION_3, REMOTE_NODE_3);
        nodeInfo.setRemoteNodeNames(remoteNodeNames);
        final Map<String, List<String>> configurations = new HashMap<>();
        final List<String> fileNames = new ArrayList<>();
        fileNames.add(REMOTE_NODE_CONFIGURATION_1);
        fileNames.add(REMOTE_NODE_CONFIGURATION_2);
        fileNames.add(REMOTE_NODE_CONFIGURATION_3);
        configurations.put("remoteNodeConfiguration", fileNames);
        nodeInfo.setConfigurations(configurations);
        nodeInfoReader.read(archive, _ as String) >> nodeInfo

        when: "Validate the remote nodes"
        boolean isTrue = remoteNodeVelidator.execute(validationContext)

        then: "Validation fails"
        isTrue == false
    }

    def "Validation pass when no remote node configuration" () {
        given: "nodenames are provided for multiple Netconf and BulkCM files but one of the remote node is not sync"
        final NodeInfo nodeInfo = new NodeInfo();
        final Map<String, String> remoteNodeNames = new HashMap<>();
        nodeInfo.setRemoteNodeNames(remoteNodeNames);
        nodeInfoReader.read(archive, _ as String) >> nodeInfo

        when: "Validate the remote nodes"
        boolean isTrue = remoteNodeVelidator.execute(validationContext)

        then: "Validation fails"
        isTrue == true
    }

    private ManagedObject addNetworkElementMo(final String nodeName, final String nodeType) {
        final Map<String, Object> networkElementAttributes = new HashMap<String, Object>()
        networkElementAttributes.put("neType", nodeType)

        return dps.addManagedObject()
                .withFdn("NetworkElement=" + nodeName)
                .namespace("OSS_NE_DEF")
                .version("2.0.0")
                .name(nodeName)
                .addAttributes(networkElementAttributes)
                .build()
    }

    def addCmFunctionMo(final ManagedObject networkElementMo, final String status) {
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
