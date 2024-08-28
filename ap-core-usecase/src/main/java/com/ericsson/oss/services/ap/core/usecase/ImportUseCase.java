/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase;

import java.util.Arrays;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.services.ap.api.exception.CsvFileNotFoundException;
import com.ericsson.oss.services.ap.api.exception.ValidationException;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.archive.ArchiveReader;
import com.ericsson.oss.services.ap.core.usecase.csv.generator.CsvGenerator;
import com.ericsson.oss.services.ap.core.usecase.csv.generator.exception.GenerateCsvFailedException;
import com.ericsson.oss.services.ap.core.usecase.importproject.ProjectImporter;
import com.ericsson.oss.services.ap.core.usecase.importproject.ProjectInfo;
import com.ericsson.oss.services.ap.core.usecase.importproject.ProjectValidator;
import com.ericsson.oss.services.ap.core.usecase.importproject.batch.BatchArchiveProcessor;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * Imports an AP project provided in zip format to the AP model.
 */
@UseCase(name = UseCaseName.IMPORT)
public class ImportUseCase {

    private static final String CSV_GENERATION_EXCEPTION_ORDER = "csv.generation.exception.error.order";

    private final ApMessages apMessages = new ApMessages();

    @Inject
    private Logger logger;

    @Inject
    private BatchArchiveProcessor batchArchiveProcessor;

    @Inject
    private CsvGenerator csvGenerator;

    @Inject
    private ProjectValidator projectValidator;

    @Inject
    private ProjectImporter projectImporter;

    /**
     * Imports the supplied project archive to the ap model.
     *
     * @param projectFileName
     *            the name of the project archive
     * @param projectContents
     *            the contents of the project archive
     * @param validationRequired
     *            is validation required
     * @return the project info
     * @throws ValidationException
     *             if there is a validation error importing the project
     */
    public ProjectInfo execute(final String projectFileName, final byte[] projectContents, final boolean validationRequired) {
        final Archive archive = readProject(projectContents);
        if (batchArchiveProcessor.isBatch(archive)) {
            generateCsvIfRequired(archive);
            if (validationRequired) {
                return executeBatchProcessingWithValidation(projectFileName, archive);
            } else {
                return executeBatchProcessingWithoutValidation(projectFileName, archive);
            }
        } else {
            if (validationRequired) {
                projectValidator.validateStandardProject(projectFileName, archive);
            }
            return projectImporter.importProject(projectFileName, archive);
        }
    }

    private ProjectInfo executeBatchProcessingWithValidation(final String projectFileName, final Archive archive) {
        projectValidator.validateBatchProject(projectFileName, archive);
        final Archive standardArchive = batchArchiveProcessor.process(archive);
        projectValidator.validateArchive(projectFileName, standardArchive, ValidationRuleGroups.ORDER);
        return projectImporter.importProject(projectFileName, standardArchive);
    }

    private ProjectInfo executeBatchProcessingWithoutValidation(final String projectFileName, final Archive batchArchive) {
        final Archive standardArchive = batchArchiveProcessor.process(batchArchive);
        return projectImporter.importProject(projectFileName, standardArchive);
    }

    private void generateCsvIfRequired(final Archive archive) {
        if (!batchArchiveProcessor.hasCsv(archive)) {
            executeCsvGeneration(archive);
        }
    }

    private Archive readProject(final byte[] projectContents) {
        try {
            return ArchiveReader.read(projectContents);
        } catch (final Exception e) {
            logger.error("Error reading zip archive", e);
            throw new ValidationException(Arrays.asList(apMessages.get("validation.project.zip.file.format")),
                apMessages.get("validation.project.error"));
        }
    }

    private void executeCsvGeneration(final Archive archive) {
        try {
            final String csvUri = csvGenerator.generateCsv(archive);
            throw new CsvFileNotFoundException(apMessages.get(CSV_GENERATION_EXCEPTION_ORDER), csvUri);
        } catch (final GenerateCsvFailedException e) {
            throw new CsvFileNotFoundException(apMessages.get(CSV_GENERATION_EXCEPTION_ORDER), e.getCause() == null ? e : e.getCause());
        }
    }
}
