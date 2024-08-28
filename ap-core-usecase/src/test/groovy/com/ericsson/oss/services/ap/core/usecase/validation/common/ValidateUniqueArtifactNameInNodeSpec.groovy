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

package com.ericsson.oss.services.ap.core.usecase.validation.common;

import javax.inject.Inject

import org.mockito.internal.util.reflection.Whitebox

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.api.validation.ValidationContext
import com.ericsson.oss.services.ap.common.cm.DpsQueries
import com.ericsson.oss.services.ap.core.usecase.archive.Archive
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey

class ValidateUniqueArtifactNameInNodeSpec extends CdiSpecification {

    @MockedImplementation
    private Archive archive;

    @Inject
    private DpsQueries dpsQueries

    @MockedImplementation
    private NodeInfoReader nodeInfoReader

    @ObjectUnderTest
    private ValidateUniqueArtifactNameInNode validateUniqueArtifactNameInNode;

    private ValidationContext validationContext;
    private NodeInfo nodeInfo;

    RuntimeConfigurableDps dps

    private final Map<String, Object> projectDataContentTarget = new HashMap<>();

    private static final String NODE_NAME = "Node1"
    private static final String REMOTE_NODE_CONFIGURATION_1 = "remoteNodeConfiguration1.xml"
    private static final String REMOTE_NODE_CONFIGURATION_2 = "remoteNodeConfiguration2.xml"
    private static final String BASELINE_CONFIGURATION_1 = "test1.mos"
    private static final String BASELINE_CONFIGURATION_2 = "test2.mos"
    private static final String BASELINE_CONFIGURATION_TAG = "baseline";
    private static final String NODE_CONFIGURATION_1 = "nodeConfiguration1.xml"
    private static final String NODE_CONFIGURATION_2 = "nodeConfiguration2.xml"
    private static final String NODE_CONFIGURATION_invalid = "preconfiguration_Node1.xml"
    private static final String UNLOCK_CELL = "unlockCell.xml"
    private static final String OPTIONAL_FEATURE = "optionalFeature.xml"
    private static final String REMOTE_NODE_CONFIGURATION_TAG = "remoteNodeConfiguration";
    private static final String NODE_CONFIGURATION_TAG = "nodeConfiguration";
    private static final String UNLOCK_CELL_TAG = "unlockCell";
    private static final String OPTIONAL_FEATURE_TAG = "optionalFeature";

    def setup() {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        Whitebox.setInternalState(dpsQueries, "dps", dps.build())
        List<String> directoryList = new ArrayList<>();
        directoryList.add(NODE_NAME);
        archive.getAllDirectoryNames() >> directoryList;

        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), "testProject.zip");
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archive);
        projectDataContentTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryList);
    }

    def "Validation fails when multiple remote node configurations use the same file" () {
        given: "nodeinfo are provided with configurations having duplicated baseline files"
            validationContext = new ValidationContext("", projectDataContentTarget);
            nodeInfo = new NodeInfo();
            final Map<String, List<String>> configurations = new HashMap<>();
            final List<String> remoteNodeConfigurationFileNames = new ArrayList<>();
            final List<String> baselineConfigurationFileNames = new ArrayList<>();
            remoteNodeConfigurationFileNames.add(REMOTE_NODE_CONFIGURATION_1);
            remoteNodeConfigurationFileNames.add(REMOTE_NODE_CONFIGURATION_1);
            baselineConfigurationFileNames.add(BASELINE_CONFIGURATION_1);
            baselineConfigurationFileNames.add(BASELINE_CONFIGURATION_2);
            configurations.put(REMOTE_NODE_CONFIGURATION_TAG, remoteNodeConfigurationFileNames);
            configurations.put(BASELINE_CONFIGURATION_TAG, baselineConfigurationFileNames);
            nodeInfo.setConfigurations(configurations);
            nodeInfoReader.read(archive, _ as String) >> nodeInfo

        when: "Validate the artifact"
            boolean isTrue = validateUniqueArtifactNameInNode.execute(validationContext)

        then: "Validation fails"
            isTrue == false
    }

    def "Validation fails when multiple baseline configurations use the same file" () {
        given: "nodeinfo are provided with configurations having duplicated baseline files"
            validationContext = new ValidationContext("", projectDataContentTarget);
            nodeInfo = new NodeInfo();
            final Map<String, List<String>> configurations = new HashMap<>();
            final List<String> remoteNodeConfigurationFileNames = new ArrayList<>();
            final List<String> baselineConfigurationFileNames = new ArrayList<>();
            remoteNodeConfigurationFileNames.add(REMOTE_NODE_CONFIGURATION_1);
            remoteNodeConfigurationFileNames.add(REMOTE_NODE_CONFIGURATION_2);
            baselineConfigurationFileNames.add(BASELINE_CONFIGURATION_1);
            baselineConfigurationFileNames.add(BASELINE_CONFIGURATION_1);
            configurations.put(REMOTE_NODE_CONFIGURATION_TAG, remoteNodeConfigurationFileNames);
            configurations.put(BASELINE_CONFIGURATION_TAG, baselineConfigurationFileNames);
            nodeInfo.setConfigurations(configurations);
            nodeInfoReader.read(archive, _ as String) >> nodeInfo

        when: "Validate the artifact"
            boolean isTrue = validateUniqueArtifactNameInNode.execute(validationContext)

        then: "Validation fails"
            isTrue == false
    }

    def "Validation passes when configurations don't have duplicated files" () {
        given: "nodeinfo are provided with configurations not having duplicated files"
            validationContext = new ValidationContext("", projectDataContentTarget);
            nodeInfo = new NodeInfo();
            final Map<String, List<String>> configurations = new HashMap<>();
            final List<String> remoteNodeConfigurationFileNames = new ArrayList<>();
            final List<String> baselineConfigurationFileNames = new ArrayList<>();
            remoteNodeConfigurationFileNames.add(REMOTE_NODE_CONFIGURATION_1);
            remoteNodeConfigurationFileNames.add(REMOTE_NODE_CONFIGURATION_2);
            baselineConfigurationFileNames.add(BASELINE_CONFIGURATION_1);
            baselineConfigurationFileNames.add(BASELINE_CONFIGURATION_2);
            configurations.put(REMOTE_NODE_CONFIGURATION_TAG, remoteNodeConfigurationFileNames);
            configurations.put(BASELINE_CONFIGURATION_TAG, baselineConfigurationFileNames);
            nodeInfo.setConfigurations(configurations);
            nodeInfoReader.read(archive, _ as String) >> nodeInfo

        when: "Validate the artifact"
            boolean isTrue = validateUniqueArtifactNameInNode.execute(validationContext)

        then: "Validation passes"
            isTrue == true
    }

    def "Validation passes when expansion configurations don't have reserved files" () {
        given: "nodeinfo are provided with configurations not having reserved files"
            validationContext = new ValidationContext("Expansion", projectDataContentTarget);
            final Map<String, List<String>> configurations = new HashMap<>();
            buildConfigurationsWithoutReservedName(configurations)
            nodeInfo = new NodeInfo();
            nodeInfo.setName(NODE_NAME);
            nodeInfo.setConfigurations(configurations);
            nodeInfoReader.read(archive, _ as String) >> nodeInfo

        when: "Validate the artifact"
            boolean isTrue = validateUniqueArtifactNameInNode.execute(validationContext)

        then: "Validation passes"
            isTrue == true
    }

    def "Validation fails when expansion configurations have reserved files" () {
        given: "nodeinfo are provided with configurations having reserved files"
            validationContext = new ValidationContext("Expansion", projectDataContentTarget);
            final Map<String, List<String>> configurations = new HashMap<>();
            buildConfigurationsWithReservedName(configurations);
            nodeInfo = new NodeInfo();
            nodeInfo.setName(NODE_NAME);
            nodeInfo.setConfigurations(configurations);
            nodeInfoReader.read(archive, _ as String) >> nodeInfo

        when: "Validate the artifact"
            boolean isTrue = validateUniqueArtifactNameInNode.execute(validationContext)

        then: "Validation fails"
            isTrue == false
    }

    def "Validation passes when order greenfield configurations have reserved files" () {
        given: "nodeinfo are provided with configurations having reserved files"
            validationContext = new ValidationContext("Order", projectDataContentTarget);
            final Map<String, List<String>> configurations = new HashMap<>();
            buildConfigurationsWithReservedName(configurations)
            nodeInfo = new NodeInfo();
            nodeInfo.setName(NODE_NAME);
            nodeInfo.setConfigurations(configurations);
            nodeInfoReader.read(archive, _ as String) >> nodeInfo

        when: "Validate the artifact"
            boolean isTrue = validateUniqueArtifactNameInNode.execute(validationContext)

        then: "Validation passes"
            isTrue == true
    }

    private void buildConfigurationsWithoutReservedName (final Map<String, List<String>> configurations) {
        final List<String> nodeConfigurationFileNames = new ArrayList<>();
        final List<String> unlockCellFileNames = new ArrayList<>();
        final List<String> optionalFeatureFileNames = new ArrayList<>();
        final List<String> remoteNodeConfigurationFileNames = new ArrayList<>();
        nodeConfigurationFileNames.add(NODE_CONFIGURATION_1);
        nodeConfigurationFileNames.add(NODE_CONFIGURATION_2);
        unlockCellFileNames.add(UNLOCK_CELL);
        optionalFeatureFileNames.add(OPTIONAL_FEATURE);
        remoteNodeConfigurationFileNames.add(REMOTE_NODE_CONFIGURATION_1);
        configurations.put(NODE_CONFIGURATION_TAG, nodeConfigurationFileNames);
        configurations.put(UNLOCK_CELL_TAG, unlockCellFileNames);
        configurations.put(OPTIONAL_FEATURE_TAG, optionalFeatureFileNames);
        configurations.put(REMOTE_NODE_CONFIGURATION_TAG, remoteNodeConfigurationFileNames);
    }

    private void buildConfigurationsWithReservedName (final Map<String, List<String>> configurations) {
        final List<String> nodeConfigurationFileNames = new ArrayList<>();
        final List<String> unlockCellFileNames = new ArrayList<>();
        final List<String> optionalFeatureFileNames = new ArrayList<>();
        final List<String> remoteNodeConfigurationFileNames = new ArrayList<>();
        nodeConfigurationFileNames.add(NODE_CONFIGURATION_1);
        nodeConfigurationFileNames.add(NODE_CONFIGURATION_2);
        nodeConfigurationFileNames.add(NODE_CONFIGURATION_invalid);
        unlockCellFileNames.add(UNLOCK_CELL);
        optionalFeatureFileNames.add(OPTIONAL_FEATURE);
        remoteNodeConfigurationFileNames.add(REMOTE_NODE_CONFIGURATION_1);
        configurations.put(NODE_CONFIGURATION_TAG, nodeConfigurationFileNames);
        configurations.put(UNLOCK_CELL_TAG, unlockCellFileNames);
        configurations.put(OPTIONAL_FEATURE_TAG, optionalFeatureFileNames);
        configurations.put(REMOTE_NODE_CONFIGURATION_TAG, remoteNodeConfigurationFileNames);
    }
}
