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

import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ZipBasedValidation;

/**
 * Abstract class to validate reconfiguration of integrated nodes
 */
public abstract class AbstractValidateRule extends ZipBasedValidation {

    private static final String VALIDATION_FAIL_PROJECT_FILE_CONTENT_NOT_LISTED = "validation.project.file.content.not.listed";

    private static final String DIAGRAM_FILE = "diagram.svg";

    @Inject
    private DpsQueries dpsQueries;

    protected static void addNodeValidationFailure(final ValidationContext context, final String validationErrorMessage, final String dirName) {
        context.addNodeValidationError(validationErrorMessage, dirName);
    }

    protected String getContentAsString(final Archive archive, final String artifactName, final String dirName) {

            final ArchiveArtifact archiveArtifact = archive.getArtifactOfNameInDir(dirName, artifactName);
            return archiveArtifact.getContentsAsString();
    }

    protected ManagedObject findMo(final String nodeName, final String moType, final String nameSpace) {
        final Iterator<ManagedObject> existingNodesFoundByName = dpsQueries.findMoByName(nodeName, moType, nameSpace).execute();
        return existingNodesFoundByName.hasNext() ? existingNodesFoundByName.next() : null;
    }

    protected static void allFilesInDirPresentInNodeInfo(final List<String> artifactFilesInNodeInfo,
        final List<String> artifactFilesInDir, final LocalContext localContext, final String dirName) {

        if (artifactFilesInDir.contains(DIAGRAM_FILE)) {
            artifactFilesInNodeInfo.add(DIAGRAM_FILE);
        }
        if (!artifactFilesInNodeInfo.containsAll(artifactFilesInDir)) {
            localContext.recordNodeValidationError(VALIDATION_FAIL_PROJECT_FILE_CONTENT_NOT_LISTED, dirName);
        }
    }

    protected LocalContext createLocalContext(final ValidationContext validationContext) {
        return new LocalContext(validationContext, apMessages);
    }

    protected static final class LocalContext {

        private final ApMessages apMessages;
        private final ValidationContext validationContext;
        private int errorCount = 0;

        private LocalContext(final ValidationContext validationContext, final ApMessages apMessages) {
            this.validationContext = validationContext;
            this.apMessages = apMessages;
        }

        public final void recordNodeValidationError(final String key, final String directory, final String... args) {
            final String validationErrorMessage = apMessages.format(key, (Object[]) args);
            validationContext.addNodeValidationError(validationErrorMessage, directory);
            errorCount++;
        }

        public final boolean isValid() {
            return errorCount == 0;
        }

        ValidationContext getValidationContext() {
            return validationContext;
        }
    }

}
