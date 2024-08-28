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
package com.ericsson.oss.services.ap.core.usecase.importproject.batch.template;

import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.CatchException.verifyException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;

import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;

/**
 * Unit tests for {@link TemplateProcessor}.
 */
public class TemplateProcessorTest {

    @Test(expected = IllegalArgumentException.class)
    public void when_project_has_no_csv_file_then_template_processor_fails_to_initialize() {
        new TemplateProcessor(createProject());
    }

    @Test(expected = IllegalArgumentException.class)
    public void when_project_has_multiple_csv_files_then_template_processor_fails_to_initialize() {
        new TemplateProcessor(
                createProject(
                        createCsvArtifact("/folder/file_1.csv", ""), createCsvArtifact("/folder/file_2.CSV", "")));
    }

    @Test
    public void when_project_has_no_nodeinfo_xml_file_then_template_processor_fails_to_process() {
        final TemplateProcessor uut = new TemplateProcessor(
                createProject(
                        createCsvArtifact("file.csv", "A,B,C", "A,B,C")));

        verifyException(uut).apply();
        assertEquals("Wrong exception raised", IllegalArgumentException.class, caughtException().getClass());
    }

    @Test
    public void when_project_has_one_node_and_one_artifact_then_two_archive_artifacts_are_returned() {
        final TemplateProcessor uut = new TemplateProcessor(
                createProject(
                        createCsvArtifact("file.csv", "name", "testName"),
                        new ArchiveArtifact("nodeInfo.xml", "<nodeInfo><name>%name%</name></nodeInfo>"),
                        new ArchiveArtifact("test.xml", "whatever content")));

        final List<ArchiveArtifact> ll = uut.apply();
        assertNotNull("Template processor returned a null list of artifacts", ll);
        assertEquals("Wrong number of items returned by template processor", 2, ll.size());
    }

    @Test
    public void when_project_has_two_nodes_and_one_artifact_then_four_archive_artifacts_are_returned() {
        final TemplateProcessor uut = new TemplateProcessor(
                createProject(
                        createCsvArtifact("file.csv", "name", "testName1", "testName2"),
                        new ArchiveArtifact("nodeInfo.xml", "<nodeInfo><name>%name%</name></nodeInfo>"),
                        new ArchiveArtifact("test.xml", "whatever content")));

        final List<ArchiveArtifact> ll = uut.apply();
        assertNotNull("Template processor returned a null list of artifacts", ll);
        assertEquals("Wrong number of items returned by template processor", 4, ll.size());
    }

    @Test
    public void when_template_is_processed_then_placeholders_are_replaced() {
        final TemplateProcessor uut = new TemplateProcessor(
                createProject(
                        createCsvArtifact("file.csv", "name,value", "testName,replacement"),
                        new ArchiveArtifact("nodeInfo.xml", "<nodeInfo><name>%name%</name></nodeInfo>"),
                        new ArchiveArtifact("test.xml", "file with %value%")));

        final List<ArchiveArtifact> ll = uut.apply();
        assertThat(ll)
                .as("Archives returned by template processor")
                .containsOnly(
                        new ArchiveArtifact("testName/nodeInfo.xml", "<nodeInfo><name>testName</name></nodeInfo>"),
                        new ArchiveArtifact("testName/test.xml", "file with replacement"));
    }

    @Test
    public void when_template_is_processed_then_placeholders_without_match_are_not_replaced() {
        final TemplateProcessor uut = new TemplateProcessor(
                createProject(
                        createCsvArtifact("file.csv", "name,value", "testName,replacement"),
                        new ArchiveArtifact("nodeInfo.xml", "<nodeInfo><name>%name%</name></nodeInfo>"),
                        new ArchiveArtifact("test.xml", "file with %value% and %more%")));

        final List<ArchiveArtifact> ll = uut.apply();
        assertThat(ll)
                .as("Archives returned by template processor")
                .containsOnly(
                        new ArchiveArtifact("testName/nodeInfo.xml", "<nodeInfo><name>testName</name></nodeInfo>"),
                        new ArchiveArtifact("testName/test.xml", "file with replacement and %more%"));
    }

    @Test
    public void when_template_is_processed_then_folders_are_changed_accordingly() {
        final TemplateProcessor uut = new TemplateProcessor(
                createProject(
                        createCsvArtifact("file.csv", "name", "testName1", "testName2"),
                        new ArchiveArtifact("nodeInfo.xml", "<nodeInfo><name>%name%</name></nodeInfo>"),
                        new ArchiveArtifact("test.xml", "whatever content for %name%")));

        final List<ArchiveArtifact> ll = uut.apply();
        assertThat(ll)
                .as("Archives returned by template processor")
                .containsOnly(
                        new ArchiveArtifact("testName1/test.xml", "whatever content for testName1"),
                        new ArchiveArtifact("testName2/test.xml", "whatever content for testName2"),
                        new ArchiveArtifact("testName1/nodeInfo.xml", "<nodeInfo><name>testName1</name></nodeInfo>"),
                        new ArchiveArtifact("testName2/nodeInfo.xml", "<nodeInfo><name>testName2</name></nodeInfo>"));
    }

    private ArchiveArtifact createCsvArtifact(final String fileName, final String header, final String... lines) {
        final StringBuilder content = new StringBuilder(header);
        for (final String line : lines) {
            content.append("\r\n").append(line);
        }

        return new ArchiveArtifact(fileName, content.toString().getBytes());
    }

    private Archive createProject(final ArchiveArtifact... artifacts) {
        final LinkedHashMap<String, ArchiveArtifact> artifactMap = new LinkedHashMap<>();
        for (final ArchiveArtifact artifact : artifacts) {
            artifactMap.put(artifact.getAbsoluteName(), artifact);
        }

        return new Archive(artifactMap);
    }
}
