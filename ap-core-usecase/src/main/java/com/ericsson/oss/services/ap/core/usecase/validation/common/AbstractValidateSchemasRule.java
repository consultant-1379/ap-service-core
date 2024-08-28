/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.validation.common;

import static com.ericsson.oss.services.ap.common.model.MoType.NETWORK_ELEMENT;
import static com.ericsson.oss.services.ap.common.model.Namespace.OSS_NE_DEF;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NAME;
import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.NODEINFO;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ArtifactDataNotFoundException;
import com.ericsson.oss.services.ap.api.schema.SchemaData;
import com.ericsson.oss.services.ap.api.schema.SchemaService;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.common.model.NetworkElementAttribute;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.util.xml.XmlValidator;
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaAccessException;
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaValidationException;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader;

/**
 * Abstract class to validate schemas of integrated nodes
 */
public abstract class AbstractValidateSchemasRule extends AbstractValidateRule {

    @Inject
    private SchemaService schemaService;

    @Inject
    private XmlValidator xmlValidator;

    @Inject
    private NodeInfoReader nodeInfoReader;

    private static final String VALIDATION_NODEINFO_SCHEMA_ERROR = "validation.project.zip.file.schema.node.failure";
    private static final String VALIDATION_SCHEMA_ACCESS = "validation.artifact.schema.access";
    private static final String VALIDATION_NODE_ARTIFACT_SCHEMA_ERROR = "validation.project.zip.file.schema.node.failure";

    private final Map<String, SchemaData> schemaByVersionAndArtifactType = new HashMap<>();
    protected final Map<String, List<SchemaData>> nodeInfoSchemasByVersion = new HashMap<>();

    protected boolean isValidNodeInfoArtifact(final ValidationContext context, final String directoryName, final String schemaType) {
        final ArchiveArtifact nodeInfo = getArtifactOfNameInDir(context, directoryName, NODEINFO.toString());
        final DocumentReader nodeInfoDoc = new DocumentReader(nodeInfo.getContentsAsString());
        try {
            final List<SchemaData> nodeInfoSchemas = readNodeInfoSchemas(nodeInfoDoc, schemaType);
            xmlValidator.validateAgainstSchema(nodeInfo.getContentsAsString(), nodeInfoSchemas);
            return true;
        } catch (final SchemaValidationException e) {
            logger.warn("Error validating {} schema: {}", schemaType, e.getMessage(), e);
            recordNodeValidationError(context, VALIDATION_NODEINFO_SCHEMA_ERROR, directoryName, NODEINFO.toString(), e.getValidationError());
        } catch (final SchemaAccessException e) {
            logger.warn("Error accessing {} schema: {}", schemaType, e.getMessage(), e);
            recordNodeValidationError(context, VALIDATION_SCHEMA_ACCESS, directoryName, NODEINFO.toString());
        }
        return false;
    }

    private List<SchemaData> readNodeInfoSchemas(final DocumentReader nodeInfoDoc, final String schemaType) {
        final String nodeName = nodeInfoDoc.getElementValue(NAME.toString());

        final ManagedObject networkElementMo = findMo(nodeName, NETWORK_ELEMENT.toString(), OSS_NE_DEF.toString());
        if (networkElementMo != null) {
            final String nodeIdentifier = networkElementMo.getAttribute(NetworkElementAttribute.OSS_MODEL_IDENTITY.toString());
            final String nodeType = networkElementMo.getAttribute(NetworkElementAttribute.NE_TYPE.toString());
            if (!nodeInfoSchemasByVersion.containsKey(nodeIdentifier)) {
                final List<SchemaData> schemas = schemaService.readSchemas(nodeType, nodeIdentifier, schemaType);
                nodeInfoSchemasByVersion.put(nodeIdentifier, schemas);
            }
            return nodeInfoSchemasByVersion.get(nodeIdentifier);
        }
        return Collections.emptyList();
    }

    protected boolean validateArtifactsForSingleNode(final ValidationContext context, final String directoryName) {
        final NodeInfo nodeInfo = nodeInfoReader.read(getArchive(context), directoryName);
        final Map<String, List<String>> nodeArtifactNamesByType = nodeInfo.getNodeArtifacts();

        for (final Entry<String, List<String>> artifactEntry : nodeArtifactNamesByType.entrySet()) {
            final SchemaData schema = readArtifactSchema(nodeInfo, artifactEntry.getKey());
            if (schema != null) {
                validateArtifactsOfSingleType(context, artifactEntry.getValue(), directoryName, schema);
            }
        }
        return isValidatedWithoutError(context);
    }

    private void validateArtifactsOfSingleType(final ValidationContext context, final List<String> artifactFileNames, final String directoryName,
            final SchemaData schema) {
        for (final String artifactFileName : artifactFileNames) {
            final ArchiveArtifact nodeArtifactFile = getArtifactOfNameInDir(context, directoryName, artifactFileName);
            if (nodeArtifactFile != null) {
                validateArtifactAgainstXsdSchema(context, nodeArtifactFile, schema, directoryName);
            }
        }
    }

    private void validateArtifactAgainstXsdSchema(final ValidationContext context, final ArchiveArtifact nodeArtifactFile, final SchemaData schema,
            final String directoryName) {
        final String artifactFileName = nodeArtifactFile.getName();
        final String artifactXmlContent = nodeArtifactFile.getContentsAsString();

        try {
            xmlValidator.validateAgainstSchema(artifactXmlContent, schema.getData());
        } catch (final SchemaValidationException e) {
            logger.warn("Error validating {} XSD schema: {}", artifactFileName, e.getMessage(), e);
            recordNodeValidationError(context, VALIDATION_NODE_ARTIFACT_SCHEMA_ERROR, directoryName, artifactFileName, e.getValidationError());
        } catch (final SchemaAccessException e) {
            logger.warn("Error accessing {} XSD schema: {}", artifactFileName, e.getMessage(), e);
            recordNodeValidationError(context, VALIDATION_SCHEMA_ACCESS, directoryName, artifactFileName);
        }
    }

    private SchemaData readArtifactSchema(final NodeInfo nodeInfo, final String artifactType) {
        final String nodeIdentifier = nodeInfo.getNodeIdentifier();
        final String nodeType = nodeInfo.getNodeType();
        final String schemaKey = nodeIdentifier + artifactType;

        try {
            if (!schemaByVersionAndArtifactType.containsKey(schemaKey)) {
                final SchemaData schemas = schemaService.readSchema(nodeType, nodeIdentifier, artifactType);
                schemaByVersionAndArtifactType.put(schemaKey, schemas);
            }
        } catch (final ArtifactDataNotFoundException e) {
            logger.debug("Error reading schema -> artifactType={}, nodeType{}, nodeIdentifier={}", artifactType, nodeType, nodeIdentifier, e);
        }
        return schemaByVersionAndArtifactType.get(schemaKey);
    }


}
