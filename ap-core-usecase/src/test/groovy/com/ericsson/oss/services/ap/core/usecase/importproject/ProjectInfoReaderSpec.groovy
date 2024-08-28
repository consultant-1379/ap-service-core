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
package com.ericsson.oss.services.ap.core.usecase.importproject

import static com.ericsson.oss.services.ap.common.model.ProjectAttribute.GENERATED_BY
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME
import static org.junit.Assert.assertEquals

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.common.model.access.ModeledAttributeFilter
import com.ericsson.oss.services.ap.core.usecase.archive.Archive
import com.ericsson.oss.services.ap.core.usecase.archive.ArchiveReader
import com.ericsson.oss.services.ap.core.usecase.utilities.ZipUtil

class ProjectInfoReaderSpec extends CdiSpecification {

    private static final PROJECT_DESCRIPTION = "proj description"
    private static final PROJECT_CREATOR = "APCreator"
    private static final PROVISIONING_TOOL_DUMMY = "DummyTool"

    private static final String VALID_PROJECT_INFO = "<?xml version='1.0' encoding='UTF-8'?> " +
    "<projectInfo xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
    "xsi:noNamespaceSchemaLocation='erbsProjectInfo.xsd'>" +
    "<name>"+ PROJECT_NAME + "</name>" +
    "<description>" + PROJECT_DESCRIPTION + "</description> " +
    "<creator>" + PROJECT_CREATOR + "</creator> "+
    "</projectInfo>"

    private static final String VALID_PROJECT_XML_WITH_BOTH_METADATA_GENERATEDBY = "<?xml version='1.0' encoding='UTF-8'?> " +
    "<projectInfo xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' " +
    "xsi:noNamespaceSchemaLocation='erbsProjectInfo.xsd'>" +
    "<name>"+ PROJECT_NAME + "</name>" +
    "<description>" + PROJECT_DESCRIPTION + "</description> " +
    "<generatedBy>" + PROVISIONING_TOOL_DUMMY + "</generatedBy>" +
    "<creator>" + PROJECT_CREATOR + "</creator> "+
    "</projectInfo>" +
    "<!-- Metadata generatedBy:ECT, version:1.2.3 -->"

    @ObjectUnderTest
    private ProjectInfoReader projectInfoReader

    @MockedImplementation
    private ModeledAttributeFilter modeledAttrFilter

    def "when read a valid project Metadata in ProjectInfo THEN the returned project attributes match the values in the xml file" () {
        given: "a valid project info xml"
            final Archive projectArchiveReader = createZipArchive(xmlString)
            modeledAttrFilter.apply(_ as String, _ as String, _ as Map<String, String>) >> { namespace, type, attributes -> attributes}

        when: "read is called"
            final ProjectInfo projectInfo = projectInfoReader.read(projectArchiveReader)
            final Map<String, Object> actualProjectAttributes = projectInfo.getProjectAttributes()

        then: "get attributes from the xml correctly"
            assertEquals(PROJECT_NAME, projectInfo.getName())
            assertEquals(generatedBy, actualProjectAttributes.get(GENERATED_BY.toString()).toString())
            assertEquals(PROJECT_CREATOR, actualProjectAttributes.get("creator"))
            assertEquals(PROJECT_DESCRIPTION, actualProjectAttributes.get("description"))
            assertEquals(2, projectInfo.getNodeQuantity())

        where:
            generatedBy  | xmlString
            "ECT"        | VALID_PROJECT_INFO + "<!-- Metadata generatedBy:ECT, version:1.2.3 -->"
            "PCI"        | VALID_PROJECT_INFO + "<!-- Metadata generatedBy:PCI -->"
            "OTHER"      | VALID_PROJECT_INFO + "<!-- Metadata generatedBy:PCI, generatedBy:OTHER -->"
            "ECT"        | VALID_PROJECT_INFO + "<!-- Metadata generatedBy:ECT, version:1.2.3 -->" + "<!-- Metadata generatedBy:OTHER, version:1.2.3 -->"
    }

    def "when read a valid project with both generatedBy attribute and Metadata in ProjectInfo THEN value of generatedBy takes higher priority" () {
        given: "a valid project info xml"
            final Archive projectArchiveReader = createZipArchive(VALID_PROJECT_XML_WITH_BOTH_METADATA_GENERATEDBY)
            modeledAttrFilter.apply(_ as String, _ as String, _ as Map<String, String>) >> { namespace, type, attributes -> attributes}

        when: "read is called"
            final ProjectInfo projectInfo = projectInfoReader.read(projectArchiveReader)
            final Map<String, Object> actualProjectAttributes = projectInfo.getProjectAttributes()

        then: "get attributes from the xml correctly"
            assertEquals(PROJECT_NAME, projectInfo.getName())
            assertEquals(PROVISIONING_TOOL_DUMMY, actualProjectAttributes.get(GENERATED_BY.toString()).toString())
            assertEquals(PROJECT_CREATOR, actualProjectAttributes.get("creator"))
            assertEquals(PROJECT_DESCRIPTION, actualProjectAttributes.get("description"))
            assertEquals(2, projectInfo.getNodeQuantity())
    }

    private Archive createZipArchive(final String xml) throws IOException {
        final Map<String, String> projectArchive = new HashMap<>()
        projectArchive.put("projectInfo.xml", xml)
        projectArchive.put("/node1/nodeInfo.xml", "")
        projectArchive.put("/node2/nodeInfo.xml", "")
        final byte[] zipFile = ZipUtil.createProjectZipFile(projectArchive)
        return ArchiveReader.read(zipFile)
    }
}
