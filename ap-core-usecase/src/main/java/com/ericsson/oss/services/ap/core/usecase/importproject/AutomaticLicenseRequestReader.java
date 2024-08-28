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
package com.ericsson.oss.services.ap.core.usecase.importproject;

import static java.util.Collections.singletonList;

import java.util.Arrays;
import java.util.List;

import com.ericsson.oss.services.ap.core.usecase.view.AutomaticLicenseRequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.api.exception.ValidationException;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.util.xml.exception.XmlException;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;

/**
 * Reads data from Automatic License Request xml File in project archive.
 */
public class AutomaticLicenseRequestReader {

    private static final String GROUP_ID = "groupId";
    private static final String HARDWARE_TYPE = "hardwareType";
    private static final String RADIO_ACCESS_TECHNOLOGIES = "radioAccessTechnologies";
    private static final String SWLT_ID = "swltId";
    private static final String VALIDATION_FILE_NOT_LISTED_ERROR = "validation.nodeinfo.file.artifact.listed.not.in.node.folder";
    private static final String VALIDATION_XML_PARSE_ERROR = "validation.xml.parse.file";

    private final ApMessages apMessages = new ApMessages();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Read elements from Automatic License Request xml file.
     *
     * @param projectArchive
     *            the project archive reader, provides interface to read the artifacts in the project archive
     * @param nodeDirectoryName
     *            the node directory
     * @param automaticLicenseRequestValue
     *            the name of the AutomaticLicenseRequest xml file that should exist in the project archive
     * @return the AutomaticLicenseRequest element data
     */
    public AutomaticLicenseRequestData read(final Archive projectArchive, final String nodeDirectoryName, final String automaticLicenseRequestValue) {
        final ArchiveArtifact automaticLicenseReqArtifact = projectArchive.getArtifactOfNameInDir(nodeDirectoryName, automaticLicenseRequestValue);
        if (automaticLicenseReqArtifact != null) {
            final DocumentReader autoLicenseRequestDocumentReader = getDocumentReader(automaticLicenseRequestValue,
                automaticLicenseReqArtifact.getContentsAsString(), nodeDirectoryName);
            return buildAutoLicenseRequestAttributes(autoLicenseRequestDocumentReader);
        } else {
            final String validationErrorMessage = String.format(
                "Artifact %s referenced in nodeInfo.xml is not found in directory %s", automaticLicenseRequestValue, nodeDirectoryName);
            logger.error(validationErrorMessage);
            throw new ValidationException(singletonList(validationErrorMessage),
                apMessages.format(VALIDATION_FILE_NOT_LISTED_ERROR, automaticLicenseRequestValue));
        }
    }

    private AutomaticLicenseRequestData buildAutoLicenseRequestAttributes(final DocumentReader documentReader) {
        String radioAccessTechnologiesValues = documentReader.getElementValue(RADIO_ACCESS_TECHNOLOGIES).trim();
        radioAccessTechnologiesValues = radioAccessTechnologiesValues.replace(" ", "");
        final List<String> radioAccessTechnologiesList = Arrays.asList(radioAccessTechnologiesValues.split(","));

        return AutomaticLicenseRequestDataBuilder.newBuilder()
            .with(licenseData -> {
                licenseData.groupId = documentReader.getElementValue(GROUP_ID);
                licenseData.hardwareType = documentReader.getElementValue(HARDWARE_TYPE);
                licenseData.radioAccessTechnologies = radioAccessTechnologiesList;
                licenseData.softwareLicenseTargetId = documentReader.getElementValue(SWLT_ID);
            })
            .build();
    }

    private DocumentReader getDocumentReader(final String fileName, final String fileContent, final String nodeDirectoryName) {
        try {
            return new DocumentReader(fileContent);
        } catch (final XmlException e) {
            final String validationErrorMessage = String.format("Error parsing %s in directory %s: %s", fileName, nodeDirectoryName, e.getMessage());
            logger.error(validationErrorMessage, e);
            throw new ValidationException(singletonList(validationErrorMessage),
                apMessages.format(VALIDATION_XML_PARSE_ERROR, fileName, nodeDirectoryName));
        }
    }
}
