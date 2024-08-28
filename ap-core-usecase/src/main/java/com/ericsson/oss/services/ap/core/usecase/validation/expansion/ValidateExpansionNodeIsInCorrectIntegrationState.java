/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.validation.expansion;

import static com.ericsson.oss.services.ap.common.model.MoType.NODE;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;

import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ValidationCrudException;
import com.ericsson.oss.services.ap.api.status.State;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NodeStatusAttribute;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.cli.response.exceptionmappers.ValidStatesForEventMapper;
import com.ericsson.oss.services.ap.core.usecase.validation.common.AbstractValidateRule;
import com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact;

/**
 * Rule to validate expansion node is in correct state for expansion i.e. not in an active workflow
 */
@Group(name = ValidationRuleGroups.EXPANSION, priority = 6, abortOnFail = true)
@Rule(name = "ValidateExpansionNodeIsInCorrectIntegrationState")
public class ValidateExpansionNodeIsInCorrectIntegrationState extends AbstractValidateRule {

    private static final String VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE = "failure.general";
    private static final String VALIDATION_FAIL_NODE_IS_IN_ACTIVE_WORKFLOW = "validation.expansion.node.in.active.workflow";

    @Inject
    private ValidStatesForEventMapper validStatesForEventMapper;

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        boolean result = true;
        for (final String dirName : directoryNames) {
            result &= validateExpansionNodeIntegrationState(context, dirName);
        }
        return result;
    }

    private boolean validateExpansionNodeIntegrationState(final ValidationContext context, final String dirName) {
        final String nodeInfoContent = getContentAsString(getArchive(context), ProjectArtifact.NODEINFO.toString(), dirName);
        final String fileNodeName = new DocumentReader(nodeInfoContent).getElementValue("name");

        try {
            final ManagedObject nodeMo = findMo(fileNodeName, NODE.toString(), AP.toString());
            if (nodeMo == null) {
                return true;
            }
            return isNodeInValidExpansionIntegrationState(context, nodeMo, dirName, fileNodeName);
        } catch (final Exception e) {
            logger.error("Unexpected error while validating node {} is in correct state for expansion", fileNodeName, e);
            throw new ValidationCrudException(apMessages.get(VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE), e);
        }
    }

    private boolean isNodeInValidExpansionIntegrationState(final ValidationContext context, final ManagedObject nodeMo, final String dirName, final String nodeName) {
        final State state = getExpansionNodeIntegrationState(nodeMo);
        if (!getValidExpansionIntegrationStates().contains(state.getDisplayName())) {
            final String message = apMessages.format(VALIDATION_FAIL_NODE_IS_IN_ACTIVE_WORKFLOW, nodeName, state.getDisplayName(), getValidExpansionIntegrationStates());
            addNodeValidationFailure(context, message, dirName);
            return false;
        }
        return true;
    }

    private String getValidExpansionIntegrationStates() {
        return validStatesForEventMapper.getValidStates("expansion");
    }

    private static State getExpansionNodeIntegrationState(final ManagedObject nodeMo) {
        final ManagedObject nodeStatusMo = nodeMo.getChild(MoType.NODE_STATUS.toString() + "=1");
        final String state = nodeStatusMo.getAttribute(NodeStatusAttribute.STATE.toString());

        return State.getState(state);
    }
}
