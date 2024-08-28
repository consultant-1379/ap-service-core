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

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.PROJECT_NAME;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.services.ap.api.exception.CsvFileNotFoundException;
import com.ericsson.oss.services.ap.api.exception.ValidationException;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.csv.generator.CsvGenerator;
import com.ericsson.oss.services.ap.core.usecase.csv.generator.exception.GenerateCsvFailedException;
import com.ericsson.oss.services.ap.core.usecase.importproject.ProjectImporter;
import com.ericsson.oss.services.ap.core.usecase.importproject.ProjectValidator;
import com.ericsson.oss.services.ap.core.usecase.importproject.batch.BatchArchiveProcessor;
import com.ericsson.oss.services.ap.core.usecase.utilities.ZipUtil;

/**
 * Unit tests for {@link ImportUseCase}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ImportUseCaseTest { // NOPMD - Too many fields

    private static final String ZIP_PROJECT_FILE_NAME = "project.zip";
    private static final String VALID_PROJECT_XML = "<?xml version='1.0' encoding='UTF-8'?> "
        + "<projectInfo xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' "
        + "xsi:noNamespaceSchemaLocation='erbsProjectInfo.xsd'>"
        + "<name>"
        + PROJECT_NAME
        + "</name>"
        + "<description>proj description</description> "
        + "<creator>APCreator</creator> "
        + "</projectInfo>";

    @Mock
    private DdpTimer ddpTimer; // NOPMD

    @Mock
    private Logger logger; // NOPMD

    @Mock
    private BatchArchiveProcessor batchArchiveProcessor;

    @Mock
    private CsvGenerator csvGenerator; // NOPMD

    @Mock
    private ProjectValidator projectValidator;

    @Mock
    private ProjectImporter projectImporter;

    @InjectMocks
    @Spy
    private ImportUseCase importUseCase;

    private byte[] zipFile;

    @Before
    public void setUp() throws IOException {
        zipFile = ZipUtil.createProjectZipFile("projectInfo.xml", VALID_PROJECT_XML);
    }

    @Test(expected = ValidationException.class)
    public void whenExecuteImportANDCannotReadProjectContentsTHENValidationExceptionIsThrown() {
        importUseCase.execute(ZIP_PROJECT_FILE_NAME, null, true);
    }

    @Test(expected = ValidationException.class)
    public void whenExecuteImportBatchProjectANDFailTheValidationTHENValidationExceptionIsThrown() {
        when(batchArchiveProcessor.isBatch(any(Archive.class))).thenReturn(true);
        when(batchArchiveProcessor.hasCsv(any(Archive.class))).thenReturn(true);
        doThrow(ValidationException.class).when(projectValidator).validateBatchProject(eq(ZIP_PROJECT_FILE_NAME), any(Archive.class));

        importUseCase.execute(ZIP_PROJECT_FILE_NAME, zipFile, true);
    }

    @Test(expected = ValidationException.class)
    public void whenExecuteImportStandardProjectANDFailTheValidationTHENValidationExceptionIsThrown() {
        when(batchArchiveProcessor.isBatch(any(Archive.class))).thenReturn(false);
        doThrow(ValidationException.class).when(projectValidator).validateStandardProject(eq(ZIP_PROJECT_FILE_NAME), any(Archive.class));

        importUseCase.execute(ZIP_PROJECT_FILE_NAME, zipFile, true);
    }

    @Test
    public void whenExecuteImportWithoutExceptionTHENImportSuccessfully() {
        when(batchArchiveProcessor.isBatch(any(Archive.class))).thenReturn(false);

        importUseCase.execute(ZIP_PROJECT_FILE_NAME, zipFile, true);
        verify(projectImporter).importProject(eq(ZIP_PROJECT_FILE_NAME), any(Archive.class));
    }

    @Test
    public void whenExecuteImportBatchWithoutExceptionTHENImportSuccessfully() {
        when(batchArchiveProcessor.isBatch(any(Archive.class))).thenReturn(true);
        when(batchArchiveProcessor.hasCsv(any(Archive.class))).thenReturn(true);

        importUseCase.execute(ZIP_PROJECT_FILE_NAME, zipFile, true);
        verify(projectImporter).importProject(eq(ZIP_PROJECT_FILE_NAME), any(Archive.class));
    }

    @Test(expected = CsvFileNotFoundException.class)
    public void whenExecuteImportBatchWithoutCsvTHENCsvIsGenerated() throws GenerateCsvFailedException {
        when(batchArchiveProcessor.isBatch(any(Archive.class))).thenReturn(true);
        when(batchArchiveProcessor.hasCsv(any(Archive.class))).thenReturn(false);

        importUseCase.execute(ZIP_PROJECT_FILE_NAME, zipFile, true);
        verify(csvGenerator).generateCsv(any(Archive.class));
    }

    @Test(expected = CsvFileNotFoundException.class)
    public void whenExecuteImportBatchWithoutCsvAndExceptionThrownTHENCsvFileNotFoundExceptionThrown() throws GenerateCsvFailedException {
        when(batchArchiveProcessor.isBatch(any(Archive.class))).thenReturn(true);
        when(batchArchiveProcessor.hasCsv(any(Archive.class))).thenReturn(false);
        doThrow(GenerateCsvFailedException.class).when(csvGenerator).generateCsv(any(Archive.class));

        importUseCase.execute(ZIP_PROJECT_FILE_NAME, zipFile, true);
    }

    @Test
    public void whenExecuteImportBatchWithoutExceptionANDWithoutValidationTHENImportSuccessfully() {
        when(batchArchiveProcessor.isBatch(any(Archive.class))).thenReturn(true);
        when(batchArchiveProcessor.hasCsv(any(Archive.class))).thenReturn(true);

        importUseCase.execute(ZIP_PROJECT_FILE_NAME, zipFile, false);
        verify(projectImporter).importProject(eq(ZIP_PROJECT_FILE_NAME), any(Archive.class));
    }

}