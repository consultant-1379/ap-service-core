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
package com.ericsson.oss.services.ap.core.usecase.validation.common;

import static com.ericsson.oss.services.ap.common.model.MoType.NETWORK_ELEMENT;
import static com.ericsson.oss.services.ap.common.model.Namespace.OSS_NE_DEF;

import java.util.List;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ValidationCrudException;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Groups;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;

/**
 * Rule to validate node specified in nodeInfo.xml has a <code>NetworkElement</code> MO already existing in ENM ready for expansion or hardware
 * replace or migration.
 */
@Groups(value = { @Group(name = ValidationRuleGroups.EXPANSION, priority = 5, abortOnFail = true),
    @Group(name = ValidationRuleGroups.HARDWARE_REPLACE, priority = 5, abortOnFail = true),
    @Group(name = ValidationRuleGroups.MIGRATION, priority = 5, abortOnFail = true) })
@Rule(name = "ValidateNodeNameExistsInNrm")
public class ValidateNodeExistsInNrm extends AbstractValidateRule {

    private static final String VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE = "failure.general"; 
    private static final String VALIDATION_FAIL_NODE_MUST_EXIST_IN_ENM = "validation.node.does.not.exist.failure";

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        boolean result = true;
        for (final String dirName : directoryNames) {
            result &= validateNodeNameExistsInEnm(context, dirName);
        }
        return result;
    }

    private boolean validateNodeNameExistsInEnm(final ValidationContext context, final String dirName) {

        final String nodeInfoContent = getContentAsString(getArchive(context), ProjectArtifact.NODEINFO.toString(), dirName);
        final String fileNodeName = new DocumentReader(nodeInfoContent).getElementValue("name");

        try {
            final ManagedObject enmNodeNameMo = findMo(fileNodeName, NETWORK_ELEMENT.toString(), OSS_NE_DEF.toString());
            if (enmNodeNameMo == null) {
                final String message = apMessages.format(VALIDATION_FAIL_NODE_MUST_EXIST_IN_ENM, fileNodeName);
                addNodeValidationFailure(context, message, dirName);
                return false;
            }
        } catch (final Exception e) {
            logger.error("Unexpected error while validating that a nodeName exists in ENM for node {}", fileNodeName, e);
            throw new ValidationCrudException(apMessages.get(VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE), e);
        }
        return true;
    }
}
