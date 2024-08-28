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

import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.PROJECTINFO

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

class ValidateProjectNameMatchesInNrmSpec extends CdiSpecification {

    @MockedImplementation
    private Archive archive;

    @ObjectUnderTest
    private ValidateProjectNameMatchesInNrm validateExpansionProjectNameMatchesInNrm;

    @Inject
    private DpsQueries dpsQueries

    private ArchiveArtifact projectArtifact;
    private ArchiveArtifact nodeInfoArtifact;

    RuntimeConfigurableDps dps

    private ValidationContext validationContext;

    @MockedImplementation
    private ManagedObject projectMo

    private ManagedObject nodeMo

    private final Map<String, Object> projectDataContentTarget = new HashMap<>();

    private static final String PROJECT_NAME = "PROJECT_NAME"
    private static final String DUMMYPROJECTNAME = "DUMMY_PROJECT_NAME";

    private static final String NODE_NAME = "Node1"
    private static final String NODE_INFO = "nodeInfo.xml";

    def setup() {
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        projectMo = getProjectMo()
        Whitebox.setInternalState(dpsQueries, "dps", dps.build())
        List<String> directoryList = new ArrayList<>();
        directoryList.add(NODE_NAME);
        List<String> artifactList = new ArrayList<>();
        artifactList.add(PROJECTINFO);
        archive.getAllDirectoryNames() >> directoryList;
        archive.getAllArtifacts() >> artifactList

        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), "testProject.zip");
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archive);
        projectDataContentTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), directoryList);
        validationContext = new ValidationContext("import", projectDataContentTarget);
    }

    def "Verify validation passed when corresponding projectMo does not exist in ENM" () {
        given: "Expansion projectInfo xml and nodeInfo xml"
        projectArtifact = new ArchiveArtifact("ProjectInfo", getProjectInfo(PROJECT_NAME))
        archive.getArtifactContentAsString(PROJECTINFO.toString()) >> projectArtifact.getContentsAsString()

        nodeInfoArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo("Node1"))
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> nodeInfoArtifact

        when: "projectMo does not exist"
        boolean isProjNameMatch = validateExpansionProjectNameMatchesInNrm.execute(validationContext)

        then: "the check returns true "
        isProjNameMatch == true
    }

    def "Verify validation passed when imported project name matches the projectMo in ENM" () {
        given: "Expansion projectInfo xml and nodeInfo xml, and nodeMo presents"
        nodeMo = addNodeMo()

        projectArtifact = new ArchiveArtifact("ProjectInfo", getProjectInfo(PROJECT_NAME))
        archive.getArtifactContentAsString(PROJECTINFO.toString()) >> projectArtifact.getContentsAsString()

        nodeInfoArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo("Node1"))
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> nodeInfoArtifact

        when: "imported project name matches that in projectMo"
        boolean isProjNameMatch = validateExpansionProjectNameMatchesInNrm.execute(validationContext)

        then: "the check returns true "
        isProjNameMatch == true
    }

    def "Verify validation failed when imported project name does not match the projectMo in ENM" () {
        given: "Expansion projectInfo xml and nodeIfno xml, and nodeMo presents"
        nodeMo = addNodeMo()

        projectArtifact = new ArchiveArtifact("ProjectInfo", getProjectInfo(DUMMYPROJECTNAME))
        archive.getArtifactContentAsString(PROJECTINFO.toString()) >> projectArtifact.getContentsAsString()

        nodeInfoArtifact = new ArchiveArtifact("NodeInfo", getNodeInfo("Node1"))
        archive.getArtifactOfNameInDir(NODE_NAME, NODE_INFO) >> nodeInfoArtifact

        when: "imported project name does not match that in projectMo"
        boolean isProjNameMatch = validateExpansionProjectNameMatchesInNrm.execute(validationContext)

        then: "the check returns false "
        isProjNameMatch == false
    }

    def getProjectInfo(final String projectName) {
        return  """<?xml version=\"1.0\" encoding=\"UTF-8\"?>
        <projectInfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
            xsi:noNamespaceSchemaLocation=\"ProjectInfo.xsd\">
            <name>${projectName}</name>
            <description>RadioNode Project</description>
            <creator>John Doe</creator>
        </projectInfo>"""
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

    def addNodeMo() {

        return dps.addManagedObject().parent(projectMo)
                .withFdn("Node=" + NODE_NAME)
                .namespace("ap")
                .version("2.0.0")
                .name(NODE_NAME)
                .build()
    }

    def getProjectMo() {

        return dps.addManagedObject()
                .withFdn("Name=" + PROJECT_NAME)
                .name(PROJECT_NAME)
                .build()
    }
}