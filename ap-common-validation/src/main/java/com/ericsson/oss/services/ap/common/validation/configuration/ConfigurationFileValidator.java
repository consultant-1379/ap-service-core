/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.validation.configuration;

import static com.ericsson.oss.services.ap.common.model.CmSyncStatus.SYNCHRONIZED;
import static com.ericsson.oss.services.ap.common.model.MoType.CM_FUNCTION;
import static com.ericsson.oss.services.ap.common.model.MoType.NETWORK_ELEMENT;
import static com.ericsson.oss.services.ap.common.model.Namespace.OSS_NE_DEF;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.model.CmFunctionAttribute;
import com.ericsson.oss.services.cm.bulkimport.api.ImportService;
import com.ericsson.oss.services.cm.bulkimport.dto.ImportValidationSpecification;
import com.ericsson.oss.services.cm.bulkimport.fileformat.FileFormat;
import com.ericsson.oss.services.cm.bulkimport.response.dto.BulkImportServiceErrorDetails;
import com.ericsson.oss.services.cm.bulkimport.response.dto.ImportServiceValidationError;
import com.ericsson.oss.services.cm.bulkimport.response.ImportServiceValidationResponse;

/**
 * Validate the configuration files supplied for each node in a project using {@link ImportService}.
 */
public class ConfigurationFileValidator {

    @Inject
    private BulkCmValidator bulkCmValidator;

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private ArtifactResourceOperations artifactResourceOperations;

    /**
     * Validates a supplied {@link ArchiveArtifact} of a bulk import configuration file, using
     * {@link ImportService#validate(ImportValidationSpecification)}.
     * <p>
     * Since the {@link ImportService} requires the file to be validated to exist on the filesystem, the {@link ArchiveArtifact} will be written to
     * the temporary directory on the SFS, and then removed after validation is complete.
     *
     * @param projectName
     *            the name of the project, used to determine the location of the temporary file
     * @param nodeName
     *            the name of the node, used to determine the location of the temporary file
     * @param nodeIdentifier
     *            the OSS model identity of the node that the configuration file will be used against
     * @param configurationFile
     *            the file to be validated
     * @return a list of all validation errors/failures for the file
     * @see DirectoryConfiguration#getTemporaryDirectory()
     */
    public List<String> validateFile(final String projectName, final String nodeName, final String nodeIdentifier,
        final ArchiveArtifact configurationFile) {

        final String temporaryArtifactFilePath = createTemporaryFile(projectName, nodeName, configurationFile); // File must exist on FS for ImportService to read and validate it
        final ImportValidationSpecification importSpecification = getImportSpecification(nodeName, nodeIdentifier, temporaryArtifactFilePath);
        final Object response = bulkCmValidator.validateBulkCmFile(importSpecification, configurationFile);
        deleteTemporaryFile(temporaryArtifactFilePath);

        if (response instanceof ImportServiceValidationResponse) { // Temporary workaround until TORF-199020 is addressed
            return getErrorMessages(configurationFile, (ImportServiceValidationResponse) response);
        } else {
            final List<String> errorMessages = new ArrayList<>(1);
            errorMessages.add((String) response);
            return errorMessages;
        }
    }

    private String createTemporaryFile(final String projectName, final String nodeName, final ArchiveArtifact configurationFile) {
        final String temporaryArtifactFilePath = generateTemporaryArtifactFilePath(projectName, nodeName, configurationFile);
        artifactResourceOperations.writeArtifact(temporaryArtifactFilePath, configurationFile.getContentsAsString().getBytes(StandardCharsets.UTF_8));
        return temporaryArtifactFilePath;
    }

    private void deleteTemporaryFile(final String temporaryArtifactFilePath) {
        artifactResourceOperations.deleteFile(temporaryArtifactFilePath);
    }

    private String generateTemporaryArtifactFilePath(final String projectName, final String nodeName,
        final ArchiveArtifact configurationFile) {
        final String tempDirectoryPath = DirectoryConfiguration.getTemporaryDirectory() + File.separator + projectName + File.separator + nodeName;
        return tempDirectoryPath + File.separator + configurationFile.getName();
    }

    private ImportValidationSpecification getImportSpecification(final String nodeName, final String nodeIdentifier,
        final String temporaryArtifactFilePath) {
        final Map<String, String> networkElementIdToOssModelIdentity = new HashMap<>(1);
        networkElementIdToOssModelIdentity.put(nodeName, nodeIdentifier);
        final Boolean syncStatus = isNodeSynchronized(nodeName);
        return ImportValidationSpecification.builder()
            .setConfiguration("LIVE")
            .setFileFormat(FileFormat.THREE_GPP)
            .setFilePath(temporaryArtifactFilePath)
            .setNetworkElementIdOssModelIdentity(networkElementIdToOssModelIdentity)
            .setValidateInstances(syncStatus)
            .build();
    }

    private Boolean isNodeSynchronized(final String nodeName) {
        final Iterator<ManagedObject> existingNodesFoundByName =
            dpsQueries.findMoByName(nodeName, NETWORK_ELEMENT.toString(), OSS_NE_DEF.toString()).execute();
        final ManagedObject networkElementMo = (existingNodesFoundByName.hasNext() ? existingNodesFoundByName.next() : null);
        if(networkElementMo == null) {
            return false;
        }
        final ManagedObject cmFunctionMo = networkElementMo.getChild(CM_FUNCTION.toString() + "=1");
        return cmFunctionMo != null && cmFunctionMo.getAttribute(CmFunctionAttribute.SYNC_STATUS.toString()).equals(SYNCHRONIZED.toString());
    }

    private List<String> getErrorMessages(final ArchiveArtifact configurationFile, final ImportServiceValidationResponse response) {
        final List<BulkImportServiceErrorDetails> errors = response.getErrors();
        final List<ImportServiceValidationError> validationErrors = response.getValidationErrors();

        final List<String> errorMessages = new ArrayList<>(errors.size() + validationErrors.size());

        for (final BulkImportServiceErrorDetails error : errors) {
            final String rawErrorMessage = error.getErrorMessage();
            final String validationErrorMessage = removeErrorCodeFromErrorMessage(rawErrorMessage);
            errorMessages.add(validationErrorMessage);
        }

        for (final ImportServiceValidationError validationError : validationErrors) {
            final String validationErrorMessage = String.format("%s (MO operation on line %s): %s", configurationFile.getName(),
                validationError.getLineNumber(), validationError.getFailureReason());
            errorMessages.add(validationErrorMessage);
        }

        return errorMessages;
    }

    /**
     * Error messages from {@link ImportServiceValidationResponse#getErrors()} can often be prefixed with an error code ("Error 7xxx :
     * {@literal <}errorMessage{@literal >}). This method will remove that prefix if it exists.
     *
     * @param rawErrorMessage
     *            the raw error message to be parsed
     * @return the error message without the error code
     */
    public String removeErrorCodeFromErrorMessage(final String rawErrorMessage) {
        final boolean startsWithErrorCode = doesErrorMesssageStartWithErrorCode(rawErrorMessage);
        return startsWithErrorCode ? rawErrorMessage.split(":", 2)[1].trim() : rawErrorMessage;
    }

    private boolean doesErrorMesssageStartWithErrorCode(final String rawErrorMessage) {
        final Pattern pattern = Pattern.compile("(Error)\\s*\\d+\\s*:"); // "Error" literal | any # of spaces | at least 1 digit | any # of spaces | ":" literal
        final Matcher matcher = pattern.matcher(rawErrorMessage);
        return matcher.find();
    }
}
