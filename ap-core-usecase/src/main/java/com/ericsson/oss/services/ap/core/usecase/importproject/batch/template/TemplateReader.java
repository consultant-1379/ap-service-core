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

import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.PROJECTINFO;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;

/**
 * Provides a way to extract all the Templates included in a project archive.
 */
public class TemplateReader {

    private final Archive archive;

    public TemplateReader(final Archive archive) {
        this.archive = archive;
    }

    /**
     * @return a list of templates within the archive
     */
    public List<Template> listTemplates() {
        final List<Template> result = new ArrayList<>();
        for (final ArchiveArtifact artifact : archive.getAllArtifacts()) {
            if (shouldApply(artifact)) {
                result.add(toTemplate(artifact));
            }
        }

        return result;
    }

    private static Template toTemplate(final ArchiveArtifact artifact) {
        final String name = artifact.getName();
        final String contents = artifact.getContentsAsString();
        return new Template(name, contents);
    }

    private static boolean shouldApply(final ArchiveArtifact artifact) {
        final String name = artifact.getName().toLowerCase(Locale.US);
        return (name.endsWith(".xml") || name.endsWith(".cfg")) && !PROJECTINFO.artifactName().equalsIgnoreCase(name);
    }
}
