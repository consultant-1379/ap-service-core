/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ValidationCrudException;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Groups;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;

/**
 * Rule to validate <code>NetworkElement</code> MO(s) with the name specified in ControllingNodes element exist in ENM.
 */
@Groups(value = { @Group(name = ValidationRuleGroups.ORDER, priority = 10), @Group(name = ValidationRuleGroups.EXPANSION, priority = 10)})
@Rule(name = "ValidateControllingNodesExist")
public class ValidateControllingNodesExist extends AbstractValidateRule {

    private static final String VALIDATION_FAIL_CONTROLLING_NODE_MUST_EXIST_IN_ENM = "validation.controlling.node.does.not.exist.failure";
    private static final String VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE = "failure.general";

    @Override
    protected boolean validate(ValidationContext context, List<String> directoryNames) {
        return directoryNames.stream()
                .map(dirName -> validateControllingNodesExistPerNodeInfo(context, dirName))
                .collect(Collectors.toList())
                .stream()
                .allMatch(validateResult -> validateResult);
    }

    private boolean validateControllingNodesExistPerNodeInfo(final ValidationContext context, final String dirName) {
        final NodeInfo nodeInfo = getNodeInfo(context, dirName);
        final Collection<Object> controllingNodes = nodeInfo.getControllingNodesAttributes().values();

        return controllingNodes.stream()
                .map(controllingNode -> validateControllingNodeExist(context, controllingNode.toString(), dirName))
                .collect(Collectors.toList())
                .stream()
                .allMatch(validateResult -> validateResult);
    }

    private boolean validateControllingNodeExist(final ValidationContext context, final String controllingNodeName, final String dirName) {
        try {
            final ManagedObject networkElementMo = findMo(controllingNodeName, NETWORK_ELEMENT.toString(), OSS_NE_DEF.toString());
            if (networkElementMo == null) {
                final String message = apMessages.format(VALIDATION_FAIL_CONTROLLING_NODE_MUST_EXIST_IN_ENM, controllingNodeName);
                addNodeValidationFailure(context, message, dirName);
                return false;
            }
        } catch (final Exception e) {
            logger.error("Unexpected error while validating the existence for controlling node {}", controllingNodeName, e);
            throw new ValidationCrudException(apMessages.get(VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE), e);
        }

        return true;
    }
}
