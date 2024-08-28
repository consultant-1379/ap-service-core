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

import static com.ericsson.oss.services.ap.common.model.CmSyncStatus.SYNCHRONIZED;
import static com.ericsson.oss.services.ap.common.model.MoType.CM_FUNCTION;
import static com.ericsson.oss.services.ap.common.model.MoType.NETWORK_ELEMENT;
import static com.ericsson.oss.services.ap.common.model.Namespace.OSS_NE_DEF;

import java.util.List;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ValidationCrudException;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.model.CmFunctionAttribute;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.usecase.validation.common.AbstractValidateRule;
import com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact;

/**
 * Rule to validate that expansion node is synced in ENM
 */
@Group(name = ValidationRuleGroups.EXPANSION, priority = 6, abortOnFail = true)
@Rule(name = "ValidateNodeIsSynchronized")
public class ValidateNodeIsSynchronized extends AbstractValidateRule {

    private static final String VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE = "failure.general";
    private static final String VALIDATION_FAIL_NODE_MUST_BE_SYNCHRONIZED_IN_ENM = "validation.node.not.synchronized.in.enm.failure";

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        boolean result = true;
        for (final String dirName : directoryNames) {
            result &= validateNodeIsSynchronizedInEnm(context, dirName);
        }
        return result;
    }

    private boolean validateNodeIsSynchronizedInEnm(final ValidationContext context, final String dirName) {
        final String nodeInfoContent = getContentAsString(getArchive(context), ProjectArtifact.NODEINFO.toString(), dirName);
        final String nodeName = new DocumentReader(nodeInfoContent).getElementValue("name");
        try {
            final ManagedObject networkElementMo = findMo(nodeName, NETWORK_ELEMENT.toString(), OSS_NE_DEF.toString());
            final ManagedObject cmFunctionMo = networkElementMo.getChild(CM_FUNCTION.toString() + "=1");

            if (cmFunctionMo == null || !cmFunctionMo.getAttribute(CmFunctionAttribute.SYNC_STATUS.toString()).equals(SYNCHRONIZED.toString())) {
                final String message = apMessages.format(VALIDATION_FAIL_NODE_MUST_BE_SYNCHRONIZED_IN_ENM, nodeName);
                addNodeValidationFailure(context, message, dirName);
                return false;
            }
        } catch (final Exception e) {
            logger.error("Unexpected error while validating that nodeName is synchronized in ENM for node {}", nodeName, e);
            throw new ValidationCrudException(apMessages.get(VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE), e);
        }
        return true;
    }
}
