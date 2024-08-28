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
package com.ericsson.oss.services.ap.common.schema.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;

/**
 * Reads schema and sample files installed on the file system.
 */
public class SchemaArtifactsFileReader {

    private static final String DELIMETER = "/";
    private static final String DEFAULT = "default";

    @Inject
    private Logger logger;

    private ResourceService resourceService;

    @PostConstruct
    public void init() {
        resourceService = new ServiceFinderBean().find(ResourceService.class);
    }


    /**
     * Reads all schemas installed on the file system.
     *
     * @return list of <code>SchemaArtifacts</code> keyed by node type
     */
    public Map<String, List<SchemaArtifacts>> readSchemasFromFileSystem() {
        final List<SchemaArtifacts> schemas = readArtifactsFromFileSystem(DirectoryConfiguration.getSchemasDirectory());
        return groupByNodeType(schemas);
    }

    /**
     * Reads all samples installed on the file system.
     *
     * @return list of <code>SchemaArtifacts</code> keyed by node type
     */
    public Map<String, List<SchemaArtifacts>> readSamplesFromFileSystem() {
        final List<SchemaArtifacts> samples = readArtifactsFromFileSystem(DirectoryConfiguration.getSamplesDirectory());
        return groupByNodeType(samples);
    }

    private List<SchemaArtifacts> readArtifactsFromFileSystem(final String baseDir) {
        final List<SchemaArtifacts> allArtifacts = new ArrayList<>();
        final Collection<String> nodeTypeDirs = resourceService.listDirectories(baseDir);

        for (final String nodeType : nodeTypeDirs) {
            final String nodeTypePath = baseDir + DELIMETER + nodeType;
            final Collection<String> nodeIdentifierDirs = resourceService.listDirectories(nodeTypePath);

            final Map<String, String> defaultSchemasForNodeType = new HashMap<>();
            if (nodeIdentifierDirs.contains(DEFAULT)) {
                final String defaultSchemasPath = String.format("%s%s%s", nodeTypePath, DELIMETER, DEFAULT);
                final Collection<Resource> artifactResources = resourceService.listFiles(defaultSchemasPath);
                for (final Resource artifactResource : artifactResources) {
                    final String artifactType = getFileNameWithoutExtension(artifactResource.getName());
                    final String artifactPath = String.format("%s%s%s", defaultSchemasPath, DELIMETER, artifactResource.getName());
                    defaultSchemasForNodeType.put(artifactType, artifactPath);
                }
            }

            if ((nodeIdentifierDirs.size() > 1) && (nodeIdentifierDirs.contains(DEFAULT))) {
                nodeIdentifierDirs.remove(DEFAULT);
            }

            for (final String nodeIdentifier : nodeIdentifierDirs) {
                final SchemaArtifacts newSchemaArtifact = createSchemaArtifactsForNodeTypeAndIdentifier(nodeTypePath, nodeType, nodeIdentifier, defaultSchemasForNodeType);
                allArtifacts.add(newSchemaArtifact);
            }
        }

        return allArtifacts;
    }

    private SchemaArtifacts createSchemaArtifactsForNodeTypeAndIdentifier(final String nodeTypePath, final String nodeType,
        final String nodeIdentifier, final Map<String, String> defaultSchemas) {
        logger.debug("Reading schemas on file system for node type {} and identifier {}", nodeType, nodeIdentifier);

        final SchemaArtifacts newSchemaArtifacts = new SchemaArtifacts(nodeType, nodeIdentifier);
        final String nodeIdentifierPath = nodeTypePath + DELIMETER + nodeIdentifier;
        final Collection<Resource> artifactResources = resourceService.listFiles(nodeIdentifierPath);

        for (final Resource artifactResource : artifactResources) {
            final String artifactType = getFileNameWithoutExtension(artifactResource.getName());
            final String artifactPath = nodeIdentifierPath + DELIMETER + artifactResource.getName();

            newSchemaArtifacts.addArtifact(artifactType, artifactPath);
            logger.debug("Read artifact from file system, type {} and location {}", artifactType, artifactPath);
        }

        defaultSchemas.forEach(newSchemaArtifacts::addArtifact);

        return newSchemaArtifacts;
    }

    private static String getFileNameWithoutExtension(final String artifactFileName) {
        return artifactFileName.substring(0, artifactFileName.lastIndexOf('.'));
    }

    private static Map<String, List<SchemaArtifacts>> groupByNodeType(final List<SchemaArtifacts> artifactsForAllNodeTypes) {
        final Map<String, List<SchemaArtifacts>> artifactsByNodeType = new HashMap<>();
        for (final SchemaArtifacts artifactForNodeIdentifier : artifactsForAllNodeTypes) {
            final String nodeType = artifactForNodeIdentifier.getNodeType().toUpperCase();
            if (!artifactsByNodeType.containsKey(nodeType)) {
                artifactsByNodeType.put(nodeType, new ArrayList<SchemaArtifacts>());
            }
            artifactsByNodeType.get(nodeType).add(artifactForNodeIdentifier);
        }
        return artifactsByNodeType;
    }
}
