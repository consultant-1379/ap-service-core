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
package com.ericsson.oss.services.ap.common.util.xml;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import com.ericsson.oss.services.ap.api.schema.SchemaData;
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaAccessException;
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaValidationException;

/**
 * Unit tests for {@link XmlValidator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class XmlValidatorTest {

    @Mock
    private Logger logger; // NOPMD

    @InjectMocks
    private XmlValidator xmlValidator;

    @Test
    public void whenProjectInfoFileConformsToSchemaThenNoExceptionIsThrown() throws SchemaTestFailureException {

        try {
            final List<SchemaData> projectSchemas = new ArrayList<>();
            final URL projectInfoSchemaUrl = getClass().getResource("/schemaXSDFiles/ProjectInfo.xsd");
            final Path schemaPath = Paths.get(projectInfoSchemaUrl.toURI());
            final SchemaData projectInfoSchema = createSchemaData("ProjectInfo", schemaPath);
            projectSchemas.add(projectInfoSchema);

            final URL xmlUrl = getClass().getResource("/schemaXSDFiles/projectInfo.xml");
            final Path xmlPath = Paths.get(xmlUrl.toURI());
            final String xml = new String(Files.readAllBytes(xmlPath), StandardCharsets.UTF_8);

            xmlValidator.validateAgainstSchema(xml, projectSchemas);
        } catch (final IOException | URISyntaxException ex) {
            throw new SchemaTestFailureException(ex);
        }
    }

    @Test(expected = SchemaValidationException.class)
    public void whenProjectInfoDoesNotConformToSchemaThenSchemaValidationExceptionIsThrown() throws SchemaTestFailureException {
        try {
            final List<SchemaData> projectSchemas = new ArrayList<>();
            final URL projectInfoSchemaUrl = getClass().getResource("/schemaXSDFiles/ProjectInfo.xsd");
            final Path schemaPath = Paths.get(projectInfoSchemaUrl.toURI());
            final SchemaData projectInfoSchema = createSchemaData("ProjectInfo", schemaPath);
            projectSchemas.add(projectInfoSchema);

            final URL xmlUrl = getClass().getResource("/schemaXSDFiles/invalid/projectInfo.xml");
            final Path xmlPath = Paths.get(xmlUrl.toURI());
            final String xml = new String(Files.readAllBytes(xmlPath), StandardCharsets.UTF_8);

            xmlValidator.validateAgainstSchema(xml, projectSchemas);

        } catch (final IOException | URISyntaxException ex) {
            throw new SchemaTestFailureException(ex);
        }
    }

    @Test
    public void whenFileConformsToSchemaThenNoExceptionIsThrown() throws IOException, URISyntaxException {
        final URL schemaUrl = getClass().getResource("/schemaXSDFiles/SiteBasic.xsd");
        final Path schemaPath = Paths.get(schemaUrl.toURI());
        final String schema = new String(Files.readAllBytes(schemaPath), StandardCharsets.UTF_8);

        final URL xmlUrl = getClass().getResource("/schemaXSDFiles/SiteBasic.xml");
        final Path xmlPath = Paths.get(xmlUrl.toURI());
        final String xml = new String(Files.readAllBytes(xmlPath), StandardCharsets.UTF_8);

        xmlValidator.validateAgainstSchema(xml, schema.getBytes());
    }

    @Test(expected = SchemaValidationException.class)
    public void whenSchemaIsBadlyFormattedThenSchemaValidationExceptionIsThrown() throws IOException, URISyntaxException {
        final URL schemaUrl = getClass().getResource("/schemaXSDFiles/SiteBasicInvalid.xsd");
        final Path schemaPath = Paths.get(schemaUrl.toURI());
        final String schema = new String(Files.readAllBytes(schemaPath), StandardCharsets.UTF_8);

        final URL xmlUrl = getClass().getResource("/schemaXSDFiles/SiteBasic.xml");
        final Path xmlPath = Paths.get(xmlUrl.toURI());
        final String xml = new String(Files.readAllBytes(xmlPath), StandardCharsets.UTF_8);

        xmlValidator.validateAgainstSchema(xml, schema.getBytes());
    }

    @Test(expected = SchemaAccessException.class)
    public void whenFileIsNullThenSchemaAccessExceptionIsThrown() throws IOException, URISyntaxException {
        final URL schemaUrl = getClass().getResource("/schemaXSDFiles/SiteBasic.xsd");
        final Path schemaPath = Paths.get(schemaUrl.toURI());
        final String schema = new String(Files.readAllBytes(schemaPath), StandardCharsets.UTF_8);

        xmlValidator.validateAgainstSchema(null, schema.getBytes());
    }

    @Test(expected = SchemaValidationException.class)
    public void whenFileDoesNotConformToSchemaThenSchemaValidationExceptionIsThrown() throws IOException, URISyntaxException {
        final URL schemaUrl = getClass().getResource("/schemaXSDFiles/SiteBasic.xsd");
        final Path schemaPath = Paths.get(schemaUrl.toURI());
        final String schema = new String(Files.readAllBytes(schemaPath), StandardCharsets.UTF_8);

        final URL xmlUrl = getClass().getResource("/schemaXSDFiles/SiteInstallation.xml");
        final Path xmlPath = Paths.get(xmlUrl.toURI());
        final String xml = new String(Files.readAllBytes(xmlPath), StandardCharsets.UTF_8);

        xmlValidator.validateAgainstSchema(xml, schema.getBytes());
    }

    @Test
    public void whenFileConformsToOneOfListOfSchemaThenNoExceptionIsThrown() throws IOException, URISyntaxException {
        final URL schemaUrl = getClass().getResource("/schemaXSDFiles/SiteBasic.xsd");
        final Path schemaPath = Paths.get(schemaUrl.toURI());
        final List<SchemaData> schemas = new ArrayList<>();
        schemas.add(new SchemaData("name", "type", "identifier", Files.readAllBytes(schemaPath), "/schema_location"));

        final URL xmlUrl = getClass().getResource("/schemaXSDFiles/SiteBasic.xml");
        final Path xmlPath = Paths.get(xmlUrl.toURI());
        final String xml = new String(Files.readAllBytes(xmlPath), StandardCharsets.UTF_8);

        xmlValidator.validateAgainstSchema(xml, schemas);
    }

    @Test(expected = SchemaValidationException.class)
    public void whenFileConformsToNoneOfListOfSchemaThenSchemaValidationExceptionIsThrown() throws IOException, URISyntaxException {
        final URL xmlUrl = getClass().getResource("/schemaXSDFiles/SiteBasic.xml");
        final Path xmlPath = Paths.get(xmlUrl.toURI());
        final String xml = new String(Files.readAllBytes(xmlPath), StandardCharsets.UTF_8);

        xmlValidator.validateAgainstSchema(xml, Collections.<SchemaData> emptyList());
    }

    @Test(expected = SchemaAccessException.class)
    public void whenFileInNullAndUsingListOfSchemaThenSchemaAccessExceptionIsThrown() {
        xmlValidator.validateAgainstSchema(null, Collections.<SchemaData> emptyList());
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
