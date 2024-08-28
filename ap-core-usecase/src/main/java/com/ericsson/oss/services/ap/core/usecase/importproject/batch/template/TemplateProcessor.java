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

import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.NODEINFO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.csv.CsvData;
import com.ericsson.oss.services.ap.core.usecase.csv.CsvIterableMap;
import com.ericsson.oss.services.ap.core.usecase.csv.CsvReader;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoData;

/**
 * Provides a way to process all templates within a project archive
 */
public class TemplateProcessor {

    private final CsvData data;
    private final TemplateReader reader;

    public TemplateProcessor(final Archive archive) {
        data = new CsvReader(archive).readCsv();
        reader = new TemplateReader(archive);
    }

    /**
     * Applies the transformation to all applicable templates in the provided archive
     *
     * @return a list with all the transformed archive artifacts
     */
    public List<ArchiveArtifact> apply() {
        final CsvIterableMap iterableMap = data.asIterableMap();
        final List<ArchiveArtifact> result = new ArrayList<>();
        while (iterableMap.hasNext()) {
            result.addAll(applyMap(iterableMap.next()));
        }

        return result;
    }

    private List<ArchiveArtifact> applyMap(final Map<String, String> map) {
        final List<Template> templates = reader.listTemplates();
        final List<ArchiveArtifact> artifacts = new ArrayList<>(templates.size());

        for (final Template template : templates) {
            artifacts.add(new ArchiveArtifact(template.getName(), template.process(map)));
        }

        return updateArtifactsPaths(artifacts);
    }

    private static List<ArchiveArtifact> updateArtifactsPaths(final List<ArchiveArtifact> artifacts) {
        final NodeInfoData nodeInfoData = getNodeInfoData(artifacts);
        final String newFolder = nodeInfoData.getNodeName() + "/";

        final List<ArchiveArtifact> result = new ArrayList<>(artifacts.size());
        for (final ArchiveArtifact artifact : artifacts) {
            result.add(new ArchiveArtifact(newFolder + artifact.getName(), artifact.getContentsAsString()));
        }

        return result;
    }

    private static NodeInfoData getNodeInfoData(final List<ArchiveArtifact> artifacts) {
        for (final ArchiveArtifact artifact : artifacts) {
            if (NODEINFO.artifactName().equalsIgnoreCase(artifact.getName())) {
                return new NodeInfoData(artifact);
            }
        }

        throw new IllegalArgumentException("No nodeInfo.xml file found");
    }
}