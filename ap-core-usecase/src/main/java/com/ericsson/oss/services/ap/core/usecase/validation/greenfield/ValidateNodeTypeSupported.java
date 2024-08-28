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
package com.ericsson.oss.services.ap.core.usecase.validation.greenfield;

import static com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants.OSS_EDT;
import static com.ericsson.oss.services.ap.common.model.EnumType.NODETYPE;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_TYPE;
import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.NODEINFO;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.itpf.modeling.modelservice.typed.core.edt.EnumDataTypeSpecification;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;

/**
 * Rule to validate that all nodes in an AP project have supported node type.
 */
@Group(name = ValidationRuleGroups.ORDER, priority = 6, abortOnFail = true)
@Rule(name = "ValidateNodeTypeSupported")
public class ValidateNodeTypeSupported extends ZipBasedValidation {

    private static final String VALIDATION_NODEINFO_ATTRIBUTE_NOT_SET_ERROR = "validation.project.zip.file.nodeinfo.attribute.not.set";
    private static final String VALIDATION_NODE_TYPE_NOT_SUPPORTED_ERROR = "validation.project.zip.file.node.type.not.supported";

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    @Inject
    private ModelReader modelReader;

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        for (final String directoryName : directoryNames) {
            final String nodeType = getNodeType(context, directoryName);
            validateNodeType(context, nodeType, directoryName);
        }

        return isValidatedWithoutError(context);
    }

    private String getNodeType(final ValidationContext context, final String directoryName) {
        final ArchiveArtifact nodeInfo = getArtifactOfNameInDir(context, directoryName, NODEINFO.toString());
        final DocumentReader nodeInfoDoc = new DocumentReader(nodeInfo.getContentsAsString());
        return nodeInfoDoc.getElementValue(NODE_TYPE.toString());
    }

    private boolean validateNodeType(final ValidationContext context, final String nodeType, final String dirName) {
        if (!isNodeTypeValueDefined(context, nodeType, dirName)) {
            return false;
        }
        return isSupportedNodeType(context, nodeType, dirName);
    }

    private boolean isNodeTypeValueDefined(final ValidationContext context, final String nodeType, final String dirName) {
        if (StringUtils.isBlank(nodeType)) {
            recordNodeValidationError(context, VALIDATION_NODEINFO_ATTRIBUTE_NOT_SET_ERROR, dirName, NODE_TYPE.toString());
            return false;
        }
        return true;
    }

    private boolean isSupportedNodeType(final ValidationContext context, final String nodeType, final String dirName) {
        final EnumDataTypeSpecification nodeTypes = modelReader.getLatestEnumDataTypeSpecification(OSS_EDT, AP.toString(), NODETYPE.toString());
        if (!isNodeTypeInModel(nodeTypeMapper.toApRepresentation(nodeType), nodeTypes)) {
            recordNodeValidationError(context, VALIDATION_NODE_TYPE_NOT_SUPPORTED_ERROR, dirName, nodeType, getValidNodeTypesString());
            return false;
        }
        return true;
    }

    private static boolean isNodeTypeInModel(final String nodeType, final EnumDataTypeSpecification nodeTypes) {
        final Collection<String> listOfNodeTypes = nodeTypes.getMemberNames();
        return listOfNodeTypes.contains(nodeType);
    }

    private String getValidNodeTypesString() {
        return StringUtils.join(modelReader.getSupportedNodeTypes(), ", ");
    }
}
