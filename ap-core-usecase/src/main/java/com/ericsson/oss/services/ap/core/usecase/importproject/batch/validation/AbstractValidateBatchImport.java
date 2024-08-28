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
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ZipBasedValidation;

/**
 * Basis for validation of any batch project archive.
 */
abstract class AbstractValidateBatchImport extends ZipBasedValidation {

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        return this.validate(context, getZipFileName(context), directoryNames);
    }

    protected abstract boolean validate(final ValidationContext context, final String fileName, final List<String> directoryNames);

    protected List<ArchiveArtifact> getFilesInBatchArchive(final ValidationContext context, final String fileName) {
        return getArtifactsOfName(context, fileName);
    }

    protected List<ArchiveArtifact> getFilesInBatchArchiveByPattern(final ValidationContext context, final String pattern) {
        return getArchive(context).getArtifactsByPattern(pattern);
    }
}
