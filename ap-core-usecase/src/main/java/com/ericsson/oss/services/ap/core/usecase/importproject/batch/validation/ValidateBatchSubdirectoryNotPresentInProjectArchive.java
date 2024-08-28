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
package com.ericsson.oss.services.ap.core.usecase.importproject.batch.validation;

import java.util.List;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;

/**
 * Validate that the {@literal <}batch_project{@literal >}.zip file contains no sub-directories (i.e: is a flat structure).
 */
@Group(name = ValidationRuleGroups.ORDER_BATCH, priority = 1, abortOnFail = true)
@Rule(name = "ValidateBatchSubdirectoryNotPresentInProjectArchive")
public class ValidateBatchSubdirectoryNotPresentInProjectArchive extends AbstractValidateBatchImport {

    @Override
    protected boolean validate(final ValidationContext context, final String fileName, final List<String> directoryNames) {
        boolean isSubdirectoryInBatchProjectArchive = true;

        if (!directoryNames.isEmpty()) {
            isSubdirectoryInBatchProjectArchive = false;
            recordValidationError(context, "validation.batch.subdirectory.found", fileName);
        }

        return isSubdirectoryInBatchProjectArchive;
    }
}