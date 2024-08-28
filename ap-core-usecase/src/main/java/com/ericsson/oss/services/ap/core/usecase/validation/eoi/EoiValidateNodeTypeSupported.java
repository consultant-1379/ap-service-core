/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.ap.core.usecase.validation.eoi;

import com.ericsson.oss.itpf.modeling.modelservice.typed.core.edt.EnumDataTypeSpecification;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.ericsson.oss.itpf.modeling.schema.util.SchemaConstants.OSS_EDT;
import static com.ericsson.oss.services.ap.common.model.EnumType.NODETYPE;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_TYPE;

@Group(name = ValidationRuleGroups.EOI, priority = 2, abortOnFail = true)
@Rule(name = "EoiValidateNodeTypeSupported")
public class EoiValidateNodeTypeSupported extends EoiBasedValidation {

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    @Inject
    private ModelReader modelReader;

    @Override
    protected boolean validate(final ValidationContext context, final List<Map<String, Object>> networkElements) {
        for (final Map<String, Object> networkElement : networkElements) {
            final String nodeType = (String) networkElement.get(ProjectRequestAttributes.NODE_TYPE.toString());
            validateNodeType(context, nodeType);
        }

        return isValidatedWithoutError(context);
    }

    private boolean validateNodeType(final ValidationContext context, final String nodeType) {
        if (!isNodeTypeValueDefined(context, nodeType)) {
            return false;
        }
        return isSupportedNodeType(context, nodeType);
    }

    private boolean isNodeTypeValueDefined(final ValidationContext context, final String nodeType) {
        if (StringUtils.isBlank(nodeType)) {
            context.addValidationError(String.format("The value of node attribute %s is not set in network element.", NODE_TYPE.toString()));
            return false;
        }
        return true;
    }

    private boolean isSupportedNodeType(final ValidationContext context, final String nodeType) {
        final EnumDataTypeSpecification nodeTypes = modelReader.getLatestEnumDataTypeSpecification(OSS_EDT, AP.toString(), NODETYPE.toString());
        if (!isNodeTypeInModel(nodeTypeMapper.toApRepresentation(nodeType), nodeTypes)) {
            context.addValidationError(String.format("Unsupported node type %s in network element. Valid node types are: [%s]", nodeType, getValidNodeTypesString()));
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
