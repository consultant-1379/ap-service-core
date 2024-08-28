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

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ValidationCrudException;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;

/**
 * Rule to validate a node does not already exist in any existing AP project
 */
abstract class AbstractValidateNodeNameIsUnique extends ZipBasedValidation {

    private static final String VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE = "failure.general";

    @Inject
    private DpsQueries dpsQueries;

    @Override
    protected boolean validate(final ValidationContext context, final List<String> dirNames) {
        boolean result = true;
        for (final String dirName : dirNames) {
            result &= validateNodeNameIsUnique(context, dirName);
        }

        return result;
    }

    private boolean validateNodeNameIsUnique(final ValidationContext context, final String dirName) {
        final NodeInfo nodeInfo = getNodeInfo(context, dirName);
        final String nodeName = nodeInfo.getName();

        try {
            final ManagedObject existingNodeWithSameName = findNodeWithName(nodeName);
            if (existingNodeWithSameName != null) {
                final FDN existingNodeFdn = FDN.get(existingNodeWithSameName.getFdn());
                addNodeValidationFailure(context, getValidationFailureMessage(existingNodeFdn), dirName);
                return false;
            }
        } catch (final Exception e) {
            logger.error("Unexpected error while validating the name uniqueness for node {}", nodeName, e);
            throw new ValidationCrudException(apMessages.get(VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE), e);
        }

        return true;
    }

    protected abstract String getValidationFailureMessage(final FDN existingNodeFdn);

    protected abstract String getNamespace();

    protected abstract String getMoType();

    private ManagedObject findNodeWithName(final String nodeName) {
        final Iterator<ManagedObject> existingNodesFoundByName = dpsQueries.findMoByName(nodeName, getMoType(), getNamespace()).execute();
        return existingNodesFoundByName.hasNext() ? existingNodesFoundByName.next() : null;
    }

    private static void addNodeValidationFailure(final ValidationContext context, final String validationErrorMessage, final String dirName) {
        context.addNodeValidationError(validationErrorMessage, dirName);
    }
}