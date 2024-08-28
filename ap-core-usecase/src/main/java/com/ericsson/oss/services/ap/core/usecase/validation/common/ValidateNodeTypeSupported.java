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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ValidationCrudException;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.common.model.NetworkElementAttribute;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;

/**
 * Rule to validate that all nodes in an AP project have supported node type.
 */
public class ValidateNodeTypeSupported extends AbstractValidateRule{

    private static final String VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE = "failure.general";
    protected static final Collection<String> SUPPORTED_NODE_TYPES = new ArrayList<>();
    protected static String validationFailNodeTypeNotSupportedError = "validation.node.type.not.supported";

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        for (final String directoryName : directoryNames) {
            validateNodeTypeSupported(context, directoryName);
        }
        return isValidatedWithoutError(context);
    }

    private boolean validateNodeTypeSupported(final ValidationContext context, final String dirName) {
        final String nodeInfoContent = getContentAsString(getArchive(context), ProjectArtifact.NODEINFO.toString(), dirName);
        final String fileNodeName = new DocumentReader(nodeInfoContent).getElementValue("name");

        try{
            final ManagedObject networkElementMo = findMo(fileNodeName, NETWORK_ELEMENT.toString(), OSS_NE_DEF.toString());
            final String nodeType = networkElementMo.getAttribute(NetworkElementAttribute.NE_TYPE.toString());
            return isNodeTypeSupported(context, nodeType, dirName, fileNodeName);
        } catch (final Exception e) {
            logger.error("Unexpected error for node: {}, when validating if the node type is supported", fileNodeName, e);
            throw new ValidationCrudException(apMessages.get(VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE), e);
        }
    }

    private String getSupportedNodeTypes(final Collection<String> nodeTypes) {
        return StringUtils.join(nodeTypes, ", ");
    }

    private static boolean isSpecificNodeTypeExpansionSupported(final String nodeType) {
        return SUPPORTED_NODE_TYPES.contains(nodeType);
    }

    private boolean isNodeTypeSupported(final ValidationContext context, final String nodeType, final String dirName, final String nodeName) {
        if (!isSpecificNodeTypeExpansionSupported(nodeType)) {
            final String message = apMessages.format(validationFailNodeTypeNotSupportedError, nodeType, nodeName, getSupportedNodeTypes(SUPPORTED_NODE_TYPES));
            addNodeValidationFailure(context, message, dirName);
            return false;
        }
        return true;
    }
}
