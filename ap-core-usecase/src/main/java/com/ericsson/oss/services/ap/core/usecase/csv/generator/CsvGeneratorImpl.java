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
package com.ericsson.oss.services.ap.core.usecase.csv.generator;

import java.util.LinkedHashSet;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import org.slf4j.Logger;

import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.csv.generator.exception.GenerateCsvFailedException;
import com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact;

/**
 * Implementation of {@code CsvGenerator}
 */
public class CsvGeneratorImpl implements CsvGenerator {

    @Inject
    private ArchiveParser archiveParser;

    @Inject
    private CsvWriter csvWriter;

    @Inject
    private Logger logger;

    @Override
    public String generateCsv(final Archive archive) throws GenerateCsvFailedException {
        final String projectName = getProjectName(archive);
        final List<ArchiveArtifact> archiveArtifacts = archive.getAllArtifacts();
        return getFileURI(projectName, DirectoryConfiguration.getDownloadDirectory(), archiveArtifacts);
    }

    @Override
    public String generateCsv(final Archive archive, final String path, final String fileName) throws GenerateCsvFailedException {
        final List<ArchiveArtifact> archiveArtifacts = archive.getAllArtifacts();
        return getFileURI(fileName, path, archiveArtifacts);
    }

    private String getProjectName(final Archive archive) {
        final String projectInfoContent = archive.getArtifactContentAsString(ProjectArtifact.PROJECTINFO.toString());
        final DocumentReader projectInfoDoc = new DocumentReader(projectInfoContent);
        return projectInfoDoc.getElementValue("name");
    }

    private String getFileURI(final String fileName, final String csvFileUri, final List<ArchiveArtifact> archiveArtifacts) throws GenerateCsvFailedException {
        String exceptionMessage;
        Throwable exceptionCause = null;
        try {
            final LinkedHashSet<String> substitutionVariables = (LinkedHashSet<String>) archiveParser.parse(archiveArtifacts);
            if (substitutionVariables.isEmpty()) {
                exceptionMessage = "No valid substitution variables found.";
            } else {
                return csvWriter.write(substitutionVariables, csvFileUri, fileName);
            }

        } catch (final Exception exception) {
            exceptionMessage = "Generate CSV Failed";
            exceptionCause = exception;
            logger.error(exceptionMessage, exception);
        }
        throw new GenerateCsvFailedException(exceptionMessage, exceptionCause);
    }
}
