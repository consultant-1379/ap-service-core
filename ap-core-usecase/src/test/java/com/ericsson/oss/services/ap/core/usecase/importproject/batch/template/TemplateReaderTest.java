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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;

import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;

/**
 * Unit tests for {@link TemplateReader}.
 */
public class TemplateReaderTest {

    @Test
    public void when_project_contains_no_artifacts_then_template_reader_returns_no_templates() {
        final TemplateReader uut = new TemplateReader(createProject());
        final List<Template> templates = uut.listTemplates();
        assertTrue("Did not get an empty template list from empty project", templates.isEmpty());
    }

    @Test
    public void when_project_contains_no_xml_artifacts_then_template_reader_returns_no_templates() {
        final Archive archive = createProject(
                new ArchiveArtifact("folder_1/file.csv", "test content"),
                new ArchiveArtifact("folder_2/file.doc", "test content"));

        final TemplateReader uut = new TemplateReader(archive);

        final List<Template> templates = uut.listTemplates();
        assertEquals("Artifact was not included in project archive", 2, archive.getAllArtifacts().size());
        assertTrue("Did not get an empty template list from project without xmls", templates.isEmpty());
    }

    @Test
    public void when_project_contains_multiple_files_then_only_valid_xml_templates_should_be_returned() {
        final Archive archive = createProject(
                new ArchiveArtifact("folder_1/file.csv", "test content - file.csv"),
                new ArchiveArtifact("folder_1/file.xml", "test content - file.xml"),
                new ArchiveArtifact("projectInfo.xml", "test content - projectInfo.xml"));

        final TemplateReader uut = new TemplateReader(archive);

        final List<Template> templates = uut.listTemplates();
        assertEquals("Not all artifacts were included in project archive", 3, archive.getAllArtifacts().size());
        assertEquals("Did not get correct template list from project", 1, templates.size());
    }

    @Test
    public void when_project_contains_xml_files_then_template_should_be_filled_correctly() {
        final TemplateReader uut = new TemplateReader(createProject(new ArchiveArtifact("folder_1/file.xml", "test content - file.xml")));

        final List<Template> templates = uut.listTemplates();
        assertEquals("Did not get correct template list from project", 1, templates.size());
        assertEquals("Did not get correct template name", "file.xml", templates.get(0).getName());
        assertEquals("Did not get correct template contents", "test content - file.xml", templates.get(0).getContents());
    }

    private Archive createProject(final ArchiveArtifact... artifacts) {
        final LinkedHashMap<String, ArchiveArtifact> artifactMap = new LinkedHashMap<>();
        for (final ArchiveArtifact artifact : artifacts) {
            artifactMap.put(artifact.getAbsoluteName(), artifact);
        }

        return new Archive(artifactMap);
    }
}
