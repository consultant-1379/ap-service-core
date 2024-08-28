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
package com.ericsson.oss.services.ap.core.usecase.validation.common

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ArtifactDataNotFoundException
import com.ericsson.oss.services.ap.api.exception.ValidationCrudException
import com.ericsson.oss.services.ap.api.validation.ValidationContext
import com.ericsson.oss.services.ap.core.usecase.archive.Archive
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey

class ValidateNodeArtifactsExistSpec extends CdiSpecification {

    @MockedImplementation
    private Archive archive

    @MockedImplementation
    private NodeInfoReader nodeInfoReader

    @ObjectUnderTest
    private ValidateNodeArtifactsExist validateNodeArtifactsExist

    private ValidationContext validationContext

    private ArchiveArtifact archiveArtifact

    private NodeInfo nodeInfo

    private ArchiveArtifact siteBasicArtifact

    private ArchiveArtifact siteEquipmentArtifact

    private ArchiveArtifact siteInstallationArtifact

    private ArchiveArtifact optionalFeatureArtifact

    private ArchiveArtifact graphicFileArtifact

    private final Map<String, Object> projectDataContentTarget = new HashMap<>()
    private final List<String> requiredFileNames = new ArrayList<>()
    private final Map<String, List<String>> artifacts = new HashMap<>()
    private final List<ArchiveArtifact> artifactList = new ArrayList<>()

    private static final String NODE_NAME = "Node1"
    private static final String NODE_INFO = "nodeInfo.xml"
    private static final String RADIO_NODE = "RadioNode"
    private static final String NODE_IDENTIFIER = "1998-184-092"
    private static final String SITE_BASIC = "SiteBasic.xml"
    private static final String SITE_BASIC_CONTENT = "dummycontents"
    private static final String SITE_INSTALLATION = "SiteInstallation.xml"
    private static final String SITE_INSTALLATION_CONTENT = "dummycontents"
    private static final String SITE_EQUIPMENT = "SiteEquipment.xml"
    private static final String DIAGRAM_FILE = "diagram.svg"
    private static final String SITE_EQUIPMENT_CONTENT = "dummycontents"
    private static final String OPTIONAL_FEATURE = "optionalFeature.xml"
    private static final String OPTFEATURE_CONTENT = "dummycontents"

    def setup() {
        List<String> directoryList = new ArrayList<>()
        directoryList.add(NODE_NAME)
        archive.getAllDirectoryNames() >> directoryList

        siteBasicArtifact = new ArchiveArtifact("SiteBasic.xml", SITE_BASIC_CONTENT)
        archive.getArtifactOfNameInDir(NODE_NAME, SITE_BASIC) >> siteBasicArtifact

        siteInstallationArtifact = new ArchiveArtifact("SiteInstallation.xml", SITE_INSTALLATION_CONTENT)
        archive.getArtifactOfNameInDir(NODE_NAME, SITE_INSTALLATION) >> siteInstallationArtifact

        siteEquipmentArtifact = new ArchiveArtifact("SiteEquipment.xml", SITE_EQUIPMENT_CONTENT)
        archive.getArtifactOfNameInDir(NODE_NAME, SITE_BASIC) >> siteEquipmentArtifact

        graphicFileArtifact = new ArchiveArtifact("diagram.svg",DIAGRAM_FILE)

        requiredFileNames.add(SITE_BASIC);
        requiredFileNames.add(SITE_INSTALLATION);
        requiredFileNames.add(SITE_EQUIPMENT);

        nodeInfo = new NodeInfo();
        nodeInfo.setName(NODE_NAME);
        nodeInfo.setNodeType(RADIO_NODE);
        nodeInfo.setNodeIdentifier(NODE_IDENTIFIER);

        artifactList.add(siteBasicArtifact);
        artifactList.add(siteInstallationArtifact);
        artifactList.add(siteEquipmentArtifact);
        artifactList.add(graphicFileArtifact)

        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), "migrationProject.zip");
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archive);
        projectDataContentTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryList);
        validationContext = new ValidationContext("", projectDataContentTarget);
    }

    def "Validation passed when artifacts in node directory match the ones described in nodeInfo.xml" () {

        given: "Migration NodeInfo xml and artifacts in node directory"

        archiveArtifact = new ArchiveArtifact("NodeInfo", getValidNodeInfo());
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        artifacts.put("artifacts", requiredFileNames)
        nodeInfo.setNodeArtifacts(artifacts)
        nodeInfoReader.read(archive, NODE_NAME) >> nodeInfo
        archive.getArtifactsInDirectory(NODE_NAME) >> artifactList

        when: "The name list of artifacts in node directory are the same as the ones described in nodeInfo.xml"

        boolean isIncluded = validateNodeArtifactsExist.execute(validationContext)

        then: "Validation passes"

        isIncluded
    }

    def "Validation failed when artifacts in node directory are more than the ones described in nodeInfo.xml" () {

        given: "Migration NodeInfo xml and artifacts in node directory"

        archiveArtifact = new ArchiveArtifact("NodeInfo", getValidNodeInfo())
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        optionalFeatureArtifact = new ArchiveArtifact("optionalFeature.xml", OPTFEATURE_CONTENT)
        archive.getArtifactOfNameInDir(NODE_NAME, OPTIONAL_FEATURE) >> optionalFeatureArtifact

        artifacts.put("artifacts", requiredFileNames)
        nodeInfo.setNodeArtifacts(artifacts)
        nodeInfoReader.read(archive, NODE_NAME) >> nodeInfo

        artifactList.add(optionalFeatureArtifact)
        archive.getArtifactsInDirectory(NODE_NAME) >> artifactList

        when: "The name list of artifacts in node directory are more than the ones described in nodeInfo.xml"

        boolean isIncluded = validateNodeArtifactsExist.execute(validationContext)

        then: "Validation fails"

        !isIncluded
    }

    def "Validation failed when artifacts in node directory are less than ones described in nodeInfo.xml" () {

        given: "Migration NodeInfo xml and artifacts in node directory"

        archiveArtifact = new ArchiveArtifact("NodeInfo", getInvalidNodeInfo())
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        requiredFileNames.add(OPTIONAL_FEATURE)
        artifacts.put("artifacts", requiredFileNames)
        nodeInfo.setNodeArtifacts(artifacts)
        nodeInfoReader.read(archive, NODE_NAME) >> nodeInfo
        archive.getArtifactsInDirectory(NODE_NAME) >> artifactList

        when: "The name list of artifacts in node directory are less than the ones described in nodeInfo.xml"

        boolean isIncluded = validateNodeArtifactsExist.execute(validationContext)

        then: "Validation fails"

        !isIncluded
    }
    def "Validation failed when artifacts not available in node directory" () {

        given: "Migration NodeInfo xml and artifacts not available node directory"

        archiveArtifact = new ArchiveArtifact("NodeInfo", getInvalidNodeInfo())
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> archiveArtifact

        requiredFileNames.add(OPTIONAL_FEATURE)
        artifacts.put("artifacts", requiredFileNames)
        nodeInfo.setNodeArtifacts(artifacts)
        nodeInfoReader.read(archive, NODE_NAME) >> nodeInfo
        archive.getArtifactsInDirectory(NODE_NAME) >> {throw new ArtifactDataNotFoundException("No artifact data found")}

        when: "The list of artifacts not available in node directory"

        def message;
        try{
            validateNodeArtifactsExist.execute(validationContext)
        }catch(ValidationCrudException e)
        {
            message = e.getMessage();
        }
        then:"throws the ArtifactDataNotFoundException"

        assert(message == "Internal Server Error")

    }


    def getValidNodeInfo() {
        return  """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
        <nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
            xsi:noNamespaceSchemaLocation=\"MigrationNodeInfo.xsd\">
            <name>Node1</name>
            <artifacts>
                <siteBasic>SiteBasic.xml</siteBasic>
                <siteInstallation>SiteInstallation.xml</siteInstallation>
                <siteEquipment>SiteEquipment.xml</siteEquipment>
            </artifacts>
        </nodeInfo>"""
    }

    def getInvalidNodeInfo() {
        return  """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
        <nodeInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
            xsi:noNamespaceSchemaLocation=\"MigrationNodeInfo.xsd\">
            <name>Node1</name>
            <artifacts>
                <siteBasic>SiteBasic.xml</siteBasic>
                <siteInstallation>SiteInstallation.xml</siteInstallation>
                <siteEquipment>SiteEquipment.xml</siteEquipment>
                <optionalFeature>optionalFeature.xml</optionalFeature>
            </artifacts>
        </nodeInfo>"""
    }
}
