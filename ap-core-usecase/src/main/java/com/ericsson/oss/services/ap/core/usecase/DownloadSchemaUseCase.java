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

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.exception.UnsupportedNodeTypeException;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.api.schema.SchemaData;
import com.ericsson.oss.services.ap.api.schema.SchemaService;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import java.io.File;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;

/**
 * Download Auto Provisioning schema and schema sample files as a ZIP for a specified node type (or all node types) for all of its versions.
 */
@UseCase(name = UseCaseName.DOWNLOAD_SCHEMA_SAMPLE)
public class DownloadSchemaUseCase {

    private static final String SAMPLE_TYPE = "SAMPLE";

    private ResourceService resourceService;

    @Inject
    private ModelReader modelReader;

    @Inject
    private SchemaService schemaService;

    @PostConstruct
    public void init() {
        resourceService = new ServiceFinderBean().find(ResourceService.class);
    }


    /**
     * Downloads all schema and samples files for all versions of the specified node type. The generated file to be downloaded in stored in the
     * download staging directory from which it can be downloaded.
     *
     * @param nodeType the node type e.g ERBS
     * @return the ID which identifies the file in the download staging directory
     * @throws ApServiceException if there is an error downloading the schemas and samples
     */
    public String execute(final String nodeType) {
        final boolean isNodeTypeBlank = StringUtils.isBlank(nodeType);
        if (!isNodeTypeBlank && !isNodeTypeInModel(nodeType)) {
            throw new UnsupportedNodeTypeException(String.format("Unsupported node type: %s", nodeType));
        }

        try {
            return generateDownloadFile(nodeType, isNodeTypeBlank);
        } catch (final ApServiceException exception) {
            throw exception;
        } catch (final Exception exception) {
            throw new ApApplicationException("Error downloading schemas for " + nodeType, exception);
        }
    }

    private String generateDownloadFile(final String nodeType, final boolean isNodeTypeBlank) {
        final String filePrefix = isNodeTypeBlank ? "AP" : nodeType.toLowerCase(Locale.US);
        final String uniqueFileId = new StringBuilder()
            .append(Calendar.getInstance().getTimeInMillis())
            .append("_")
            .append(filePrefix)
            .append("_SchemasAndSamples.zip")
            .toString();

        final String downloadDir = DirectoryConfiguration.getDownloadDirectory() + File.separator + uniqueFileId;

        final Map<String, byte[]> schemaAndSampleFileContentsByPath = isNodeTypeBlank ? getSchemasAndSamplesForAllNodeTypes()
            : getSchemasAndSamplesForAllNodeVersions(nodeType);

        resourceService.writeContentsToZip(downloadDir, schemaAndSampleFileContentsByPath);
        return uniqueFileId;
    }

    private Map<String, byte[]> getSchemasAndSamplesForAllNodeTypes() {
        final Map<String, byte[]> schemaAndSampleFileContentsByPath = new HashMap<>();
        final List<String> router6KNodeTypes = Stream.of("router6x71", "router6672", "router6675", "router6673", "router6000-2").collect(Collectors.toList());
        final Collection<String> nodeTypes = modelReader.getSupportedNodeTypes();
        for (final String validNodeType : nodeTypes) {
            final List<SchemaData> schemasForAllNodeVersions = schemaService.readSchemas(validNodeType);
            final List<SchemaData> samplesForAllNodeVersions = schemaService.readSamples(validNodeType);
            if (router6KNodeTypes.contains(validNodeType.toLowerCase(Locale.US))) {
                schemaAndSampleFileContentsByPath.putAll(groupSchemasByNodeType("Router6K", schemasForAllNodeVersions));
                schemaAndSampleFileContentsByPath.putAll(groupSamplesByNodeType("Router6K", samplesForAllNodeVersions));
            } else {
                schemaAndSampleFileContentsByPath.putAll(groupSchemasByNodeType(validNodeType, schemasForAllNodeVersions));
                schemaAndSampleFileContentsByPath.putAll(groupSamplesByNodeType(validNodeType, samplesForAllNodeVersions));
            }
        }
        return schemaAndSampleFileContentsByPath;
    }

    private static Map<String, byte[]> groupSchemasByNodeType(final String nodeType, final List<SchemaData> filesForAllNodeVersions) {
        final Map<String, byte[]> fileContentsByPath = new HashMap<>();
        for (final SchemaData schemaData : filesForAllNodeVersions) {
            final String filePath = String.format("%s/schemas/%s/%s.%s", nodeType, schemaData.getIdentifier(), schemaData.getName(),
                schemaData.getExtension());
            fileContentsByPath.put(filePath, schemaData.getData());
        }

        return fileContentsByPath;
    }

    private static Map<String, byte[]> groupSamplesByNodeType(final String nodeType, final List<SchemaData> filesForAllNodeVersions) {
        final Map<String, byte[]> fileContentsByPath = new HashMap<>();

        for (final SchemaData schemaData : filesForAllNodeVersions) {
            final String filePath = String.format("%s/samples/%s.%s", nodeType, schemaData.getName(), schemaData.getExtension());
            fileContentsByPath.put(filePath, schemaData.getData());
        }

        return fileContentsByPath;
    }

    private Map<String, byte[]> getSchemasAndSamplesForAllNodeVersions(final String nodeType) {
        final Map<String, byte[]> schemaAndSampleFileContentsByPath = new HashMap<>();

        final List<SchemaData> schemasForAllNodeVersions = schemaService.readSchemas(nodeType);
        schemaAndSampleFileContentsByPath.putAll(groupFileContentsByPath(schemasForAllNodeVersions));

        final List<SchemaData> samplesForAllNodeVersions = schemaService.readSamples(nodeType);
        schemaAndSampleFileContentsByPath.putAll(groupFileContentsByPath(samplesForAllNodeVersions));

        return schemaAndSampleFileContentsByPath;
    }

    private static Map<String, byte[]> groupFileContentsByPath(final List<SchemaData> filesForAllNodeVersions) {
        final Map<String, byte[]> fileContentsByPath = new HashMap<>();

        for (final SchemaData schemaData : filesForAllNodeVersions) {
            final String filePath = generateFilePath(schemaData);
            fileContentsByPath.put(filePath, schemaData.getData());
        }

        return fileContentsByPath;
    }

    private static String generateFilePath(final SchemaData schemaData) {
        if (SAMPLE_TYPE.equals(schemaData.getType())) {
            return String.format("samples/%s.%s", schemaData.getName(), schemaData.getExtension());
        }
        return String.format("schemas/%s/%s.%s", schemaData.getIdentifier(), schemaData.getName(), schemaData.getExtension());
    }

    private boolean isNodeTypeInModel(final String nodeType) {
        final Collection<String> nodeTypes = modelReader.getSupportedNodeTypes();
        return collectionContains(nodeTypes, nodeType);
    }

    private static boolean collectionContains(final Collection<String> supportedTypes, final String type) {
        for (final String supportedType : supportedTypes) {
            if (supportedType.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }
}
