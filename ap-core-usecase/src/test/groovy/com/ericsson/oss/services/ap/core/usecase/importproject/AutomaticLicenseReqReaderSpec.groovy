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

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ValidationException
import com.ericsson.oss.services.ap.core.usecase.archive.Archive
import com.ericsson.oss.services.ap.core.usecase.archive.ArchiveReader
import com.ericsson.oss.services.ap.core.usecase.utilities.ZipUtil
import com.ericsson.oss.services.ap.core.usecase.view.AutomaticLicenseRequestData
import com.ericsson.oss.services.ap.core.usecase.importproject.data.ProjectData

/**
 * Test to verify the contents of the automaticLicenseRequest file in AP project.zip.
 */
class AutomaticLicenseReqReaderSpec extends CdiSpecification {

    private static final String AUTOMATIC_LICENSE_REQUEST_FILENAME = "LicenseRequest.xml"
    private static final String DIRECTORY_NAME = "node1"

    private static final String INVALID_AUTOMATIC_LICENSE_REQUEST_XML = "<?xml version='1.0' encoding='UTF-8'?><licenseRequest " +
            "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:noNamespaceSchemaLocation='AutomaticLicenseRequest.xsd'>" +
            "<radioAccessTechnologies> LTE, NR </radioAccessTechnologies>"

    @ObjectUnderTest
    private AutomaticLicenseRequestReader automaticLicenseRequestReader

    def "when read a valid automaticLicenseRequest file THEN the returned attributes match the values in the xml file" () {
        given: "a valid automaticLicenseRequest xml file exists"
            final Archive projectArchiveReader = createValidLicenseZipArchive()

        and: "multiple radio access technologies are setup"
            final List<String> radioAccessTechnologies = new ArrayList<String> ()
            radioAccessTechnologies.add("LTE")
            radioAccessTechnologies.add("NR")

        when: "automaticLicenseRequestReader is called"
            final AutomaticLicenseRequestData automaticLicenseRequestData = automaticLicenseRequestReader.
                read(projectArchiveReader, DIRECTORY_NAME, AUTOMATIC_LICENSE_REQUEST_FILENAME)

        then: "the returned attributes match the values in the xml file"
            automaticLicenseRequestData.getGroupId() == "949525"
            automaticLicenseRequestData.getHardwareType() == "BB6648"
            automaticLicenseRequestData.getRadioAccessTechnologies().containsAll(radioAccessTechnologies)
            automaticLicenseRequestData.getSoftwareLicenseTargetId() == "LCS_945587_10081"
    }

    def "when read an invalid automaticLicenseRequest file THEN validation exception is thrown" () {
        given: "an invalid automaticLicenseRequest xml file exists"
            final Archive projectArchiveReader = createInvalidLicenseZipArchive()

        when: "automaticLicenseRequestReader is called"
            automaticLicenseRequestReader.read(projectArchiveReader, DIRECTORY_NAME, AUTOMATIC_LICENSE_REQUEST_FILENAME)

        then: "validation exception is thrown"
            ValidationException validationException = thrown()
            validationException.getMessage() == "Error parsing LicenseRequest.xml file in node1"
    }

    def "when no automaticLicenseRequest file exists THEN validation exception is thrown" () {
        given: "an invalid zip with no automaticLicenseRequest xml file"
            final Archive projectArchiveReader = createNoLicenseZipArchive()

        when: "automaticLicenseRequestReader is called"
            automaticLicenseRequestReader.read(projectArchiveReader, DIRECTORY_NAME, AUTOMATIC_LICENSE_REQUEST_FILENAME)

        then: "ValidationException is thrown"
            ValidationException validationException = thrown()
            validationException.getMessage() == "Artifact LicenseRequest.xml referenced in nodeInfo.xml is not found"
    }

    private Archive createValidLicenseZipArchive() throws IOException {
        final Map<String, String> projectArchive = new HashMap<>()
        projectArchive.put("projectInfo.xml", ProjectData.STANDARD_PROJECT_INFO)
        projectArchive.put("node1/nodeInfo.xml", ProjectData.NODE_INFO)
        projectArchive.put("node1/LicenseRequest.xml", ProjectData.LICENSE_REQUEST)
        final byte[] zipFile = ZipUtil.createProjectZipFile(projectArchive)
        return ArchiveReader.read(zipFile)
    }

    private Archive createInvalidLicenseZipArchive() throws IOException {
        final Map<String, String> projectArchive = new HashMap<>()
        projectArchive.put("projectInfo.xml", ProjectData.STANDARD_PROJECT_INFO)
        projectArchive.put("node1/nodeInfo.xml", ProjectData.NODE_INFO)
        projectArchive.put("node1/LicenseRequest.xml", INVALID_AUTOMATIC_LICENSE_REQUEST_XML)
        final byte[] zipFile = ZipUtil.createProjectZipFile(projectArchive)
        return ArchiveReader.read(zipFile)
    }

    private static Archive createNoLicenseZipArchive() throws IOException {
        final Map<String, String> projectArchive = new HashMap<>()
        projectArchive.put("projectInfo.xml", "")
        projectArchive.put("node1/nodeInfo.xml", "")
        final byte[] zipFile = ZipUtil.createProjectZipFile(projectArchive)
        return ArchiveReader.read(zipFile)
    }
}
