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
package com.ericsson.oss.services.ap.core.usecase.validation.greenfield;

import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_IDENTIFIER;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_TYPE;
import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.NODEINFO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.api.schema.SchemaData;
import com.ericsson.oss.services.ap.api.schema.SchemaService;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Groups;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.util.xml.XmlValidator;
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaAccessException;
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaValidationException;
import com.ericsson.oss.services.ap.common.util.xml.exception.XmlException;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;

/**
 * Rule to validate the nodeInfo artifact against its schema.
 */
@Groups(value = { @Group(name = ValidationRuleGroups.ORDER, priority = 7, abortOnFail = true),
        @Group(name = ValidationRuleGroups.ORDER_BATCH, priority = 7, abortOnFail = true) })
@Rule(name = "ValidateNodeInfoArtifactAgainstSchema")
public class ValidateNodeInfoArtifactAgainstSchema extends ZipBasedValidation {

    private static final String NODE_INFO_SCHEMA_TYPE = "NodeInfo";
    private static final String VALIDATION_NODEINFO_FORMAT_ERROR = "validation.artifact.format.failure";
    private static final String VALIDATION_NODEINFO_SCHEMA_ERROR = "validation.project.zip.file.schema.node.failure";
    private static final String VALIDATION_SCHEMA_ACCESS = "validation.artifact.schema.access";

    @Inject
    private SchemaService schemaService;

    @Inject
    private XmlValidator xmlValidator;

    private final Map<String, List<SchemaData>> nodeInfoSchemasByVersion = new HashMap<>();

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        boolean allNodeInfoArtifactsValid = true;

        for (final String directoryName : directoryNames) {
            if (!isValidNodeInfoArtifact(context, directoryName)) {
                allNodeInfoArtifactsValid = false;
            }
        }

        return allNodeInfoArtifactsValid;
    }

    private boolean isValidNodeInfoArtifact(final ValidationContext context, final String directoryName) {
        final ArchiveArtifact nodeInfo = getArtifactOfNameInDir(context, directoryName, NODEINFO.toString());

        try {
            final DocumentReader nodeInfoDoc = new DocumentReader(nodeInfo.getContentsAsString());
            final List<SchemaData> nodeInfoSchemas = readNodeInfoSchemas(nodeInfoDoc);
            xmlValidator.validateAgainstSchema(nodeInfo.getContentsAsString(), nodeInfoSchemas);
            return true;
        } catch (final XmlException e) {
            logger.warn("Error validating {} schema: {}", NODE_INFO_SCHEMA_TYPE, e.getMessage(), e);
            recordNodeValidationError(context, VALIDATION_NODEINFO_FORMAT_ERROR, directoryName, NODEINFO.toString());
        } catch (final SchemaValidationException e) {
            logger.warn("Error validating {} schema: {}", NODE_INFO_SCHEMA_TYPE, e.getMessage(), e);
            recordNodeValidationError(context, VALIDATION_NODEINFO_SCHEMA_ERROR, directoryName, NODEINFO.toString(), e.getValidationError());
        } catch (final SchemaAccessException e) {
            logger.warn("Error accessing {} schema: {}", NODE_INFO_SCHEMA_TYPE, e.getMessage(), e);
            recordNodeValidationError(context, VALIDATION_SCHEMA_ACCESS, directoryName, NODEINFO.toString());
        } catch (final Exception e) {
            logger.warn("Error accessing {} schema: {}", NODE_INFO_SCHEMA_TYPE, e.getMessage(), e);
            recordNodeValidationError(context, VALIDATION_NODEINFO_SCHEMA_ERROR, directoryName, NODEINFO.toString(), e.getMessage());
        }
        return false;
    }

    private List<SchemaData> readNodeInfoSchemas(final DocumentReader nodeInfoDoc) {
        final String nodeIdentifier = nodeInfoDoc.getElementValue(NODE_IDENTIFIER.toString());
        final String nodeType = nodeInfoDoc.getElementValue(NODE_TYPE.toString());

        if (!nodeInfoSchemasByVersion.containsKey(nodeIdentifier)) {
            final List<SchemaData> schemas = schemaService.readSchemas(nodeType, nodeIdentifier, NODE_INFO_SCHEMA_TYPE);
            nodeInfoSchemasByVersion.put(nodeIdentifier, schemas);
        }
        return nodeInfoSchemasByVersion.get(nodeIdentifier);
    }
}
