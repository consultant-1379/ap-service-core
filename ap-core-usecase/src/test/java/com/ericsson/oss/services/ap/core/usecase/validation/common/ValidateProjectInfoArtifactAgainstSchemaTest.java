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
package com.ericsson.oss.services.ap.core.usecase.validation.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.services.ap.api.schema.SchemaData;
import com.ericsson.oss.services.ap.api.schema.SchemaService;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.common.util.xml.XmlValidator;
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaValidationException;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey;

/**
 * Unit tests for {@link ValidateProjectInfoArtifactAgainstSchema}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ValidateProjectInfoArtifactAgainstSchemaTest {

    private static final String VALIDATION_GROUP = "ValidateProjectInfoArtifactAgainstSchema";
    private static final String ZIP_PROJECT_FILE_NAME = "project.zip";
    private static final String PROJECT_SCHEMA_VALIDATION_FAILURE_MESSAGE_FORMAT = "Artifact %s failed to validate against schema. %s";
    private static final String VALID_PROJECT_XML = "/testfiles/projectInfo.xml";
    private static final String INVALID_PROJECT_XML = "/testfiles/invalid/projectInfo.xml";

    @Mock
    private Archive zipArchiveReader;

    @Mock
    private SchemaService schemaService;

    @Mock
    private XmlValidator xmlValidator;

    @InjectMocks
    private ValidateProjectInfoArtifactAgainstSchema validateProjectInfoAgainstSchema;

    private ValidationContext context;

    @Test
    public void testValidProjectInfoArtifact() throws SchemaTestFailureException {
        final List<SchemaData> projectSchemas = getProjectSchemas();
        final String xml = getProjectinfoXml(VALID_PROJECT_XML);

        // Create the project zip
        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip("projectInfo.xml", xml);

        final Map<String, Object> projectDataContentTarget = new HashMap<>();
        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), ZIP_PROJECT_FILE_NAME);
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), zipArchiveReader);

        context = new ValidationContext(VALIDATION_GROUP, projectDataContentTarget);
        when(zipArchiveReader.getArtifactContentAsString("projectInfo.xml")).thenReturn(xml);
        when(schemaService.readProjectInfoSchemas()).thenReturn(projectSchemas);

        final boolean result = validateProjectInfoAgainstSchema.execute(context);

        assertTrue(result);
    }

    @Test
    public void testInvalidProjectInfoArtifact() throws SchemaTestFailureException {
        final List<SchemaData> projectSchemas = getProjectSchemas();
        final String xml = getProjectinfoXml(INVALID_PROJECT_XML);

        final ZipContentGenerator zcg = new ZipContentGenerator();
        zcg.createFileInZip("projectInfo.xml", xml);

        final Map<String, Object> projectDataContentTarget = new HashMap<>();
        projectDataContentTarget.put(ImportProjectTargetKey.FILENAME.toString(), ZIP_PROJECT_FILE_NAME);
        projectDataContentTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), zipArchiveReader);

        context = new ValidationContext(VALIDATION_GROUP, projectDataContentTarget);

        when(zipArchiveReader.getArtifactContentAsString("projectInfo.xml")).thenReturn(xml);
        when(schemaService.readProjectInfoSchemas()).thenReturn(projectSchemas);
        doThrow(new SchemaValidationException("Code: Error")).when(xmlValidator).validateAgainstSchema(xml, projectSchemas);

        final String validationError = String.format(PROJECT_SCHEMA_VALIDATION_FAILURE_MESSAGE_FORMAT, "projectInfo.xml", "Error");

        validateProjectInfoAgainstSchema.execute(context);

        assertEquals(validationError, context.getValidationErrors().get(0));
    }

    private List<SchemaData> getProjectSchemas() throws SchemaTestFailureException {
        try {
            final List<SchemaData> projectSchemas = new ArrayList<>();
            final URL projectInfoSchemaUrl = getClass().getResource("/testfiles/ProjectInfo.xsd");
            final Path schemaPath = Paths.get(projectInfoSchemaUrl.toURI());
            final SchemaData projectInfoSchema = createSchemaData("ProjectInfo", schemaPath);
            projectSchemas.add(projectInfoSchema);
            return projectSchemas;

        } catch (final IOException | URISyntaxException ex) {
            throw new SchemaTestFailureException(ex);
        }
    }

    private String getProjectinfoXml(final String projectInfoResource) throws SchemaTestFailureException {
        try {
            final URL xmlUrl = getClass().getResource(projectInfoResource);
            final Path xmlPath = Paths.get(xmlUrl.toURI());
            return new String(Files.readAllBytes(xmlPath), StandardCharsets.UTF_8);
        } catch (final IOException | URISyntaxException ex) {
            throw new SchemaTestFailureException(ex);
        }
    }

    private SchemaData createSchemaData(final String artifactName, final Path schemaPath) throws IOException {
        final byte[] artifactFileContents = Files.readAllBytes(schemaPath);
        return new SchemaData(artifactName, "", "", artifactFileContents, "");
    }

    private class SchemaTestFailureException extends Exception {
        private static final long serialVersionUID = 1L;

        public SchemaTestFailureException(final Throwable cause) {
            super(cause);
        }
    }
}
