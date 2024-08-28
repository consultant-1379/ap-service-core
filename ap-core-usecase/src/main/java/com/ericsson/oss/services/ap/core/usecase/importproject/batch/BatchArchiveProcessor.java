/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.importproject.batch;

import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.PROJECTINFO;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.importproject.batch.template.TemplateProcessor;

/**
 * Converts a batch archive into a regular archive.
 */
public class BatchArchiveProcessor {

    private static final String FILE_WITH_CSV_EXTENSION_PATTERN = "^.+.(?i)\\.csv";

    /**
     * Converts a batch AP project archive into a standard project archive.
     *
     * @param archive
     *            a project archive containing a projectInfo.xml file, a nodeInfo.xml file, a csv file and other optional xml files
     * @return an archive that represents the applied transformation of the argument archive
     */
    public Archive process(final Archive archive) {
        final TemplateProcessor templateProcessor = new TemplateProcessor(archive);
        final Map<String, ArchiveArtifact> result = addNodesToArchive(templateProcessor, createStructureWithProjectInfo(archive));
        return new Archive(result);
    }

    /**
     * Validates if a given archive is a batch archive. A batch artifact is defined by an AP project which includes no directories in it.
     *
     * @param archive
     *            the archive to be validated
     * @return true if the archive does not contain any directory
     */
    public boolean isBatch(final Archive archive) {
        if (archive.getAllArtifacts().isEmpty()) {
            return false;
        }
        final List<String> artifactDirectoryNames = archive.getAllDirectoryNames();
        return artifactDirectoryNames.isEmpty();
    }

    /**
     * Check whether the supplied archive has at least one csv file.
     *
     * @param archive
     *            the archive to be checked
     * @return true if Archive has at least one .csv file
     */
    public boolean hasCsv(final Archive archive) {
        final List<ArchiveArtifact> artifacts = archive.getArtifactsByPattern(FILE_WITH_CSV_EXTENSION_PATTERN);
        return !artifacts.isEmpty();
    }

    private Map<String, ArchiveArtifact> createStructureWithProjectInfo(final Archive archive) {
        final ArchiveArtifact projectInfoArtifact = getProjectInfoArtifact(archive);
        final Map<String, ArchiveArtifact> result = new LinkedHashMap<>();
        result.put(PROJECTINFO.artifactName(), projectInfoArtifact);

        return result;
    }

    private ArchiveArtifact getProjectInfoArtifact(final Archive archive) {
        final List<ArchiveArtifact> artifacts = archive.getArtifactsOfName(PROJECTINFO.artifactName());
        if (artifacts.isEmpty()) {
            throw new IllegalArgumentException("No projectInfo.xml file found");
        } else if (artifacts.size() > 1) {
            throw new IllegalArgumentException("Multiple projectInfo.xml files found");
        }

        return artifacts.get(0);
    }

    private Map<String, ArchiveArtifact> addNodesToArchive(final TemplateProcessor processor, final Map<String, ArchiveArtifact> data) {
        final List<ArchiveArtifact> artifacts = processor.apply();
        for (final ArchiveArtifact artifact : artifacts) {
            data.put(artifact.getAbsoluteName(), artifact);
        }

        return data;
    }

}
