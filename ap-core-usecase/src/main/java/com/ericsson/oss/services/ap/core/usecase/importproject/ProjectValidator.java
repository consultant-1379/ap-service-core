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
package com.ericsson.oss.services.ap.core.usecase.importproject;

import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.NODEINFO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.services.ap.api.exception.ValidationException;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.ValidationEngine;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.util.xml.exception.XmlException;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact;
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ImportProjectTargetKey;

/**
 * Validate project for both batch project and standard project.
 */
public class ProjectValidator {

    private static final String VALIDATION_NODEINFO_NOT_FOUND = "validation.nodeinfo.missing";

    private static final String EXPANSION_NODE_INFO_XSD = "ExpansionNodeInfo.xsd";
    private static final String HARDWARE_REPLACE_NODE_INFO_XSD = "HardwareReplaceNodeInfo.xsd";
    private static final String MIGRATION_NODE_INFO_XSD = "MigrationNodeInfo.xsd";

    private final ApMessages apMessages = new ApMessages();

    @Inject
    private Logger logger;

    @Inject
    private DdpTimer ddpTimer;

    @Inject
    private ValidationEngine validationEngine;

    @Inject
    private NodeSchemaProcessor nodeSchemaProcessor;

    /**
     * Validate the batch project archive before import project. ValidationException will throw if fails the validation.
     *
     * @param projectFileName
     *            the name of the project archive
     * @param batchArchive
     *            the project archive
     */
    public void validateBatchProject(final String projectFileName, final Archive batchArchive) {
        ddpTimer.start(CommandLogName.VALIDATE_PROJECT.toString());
        validateArchive(projectFileName, batchArchive, ValidationRuleGroups.ORDER_BATCH);
    }

    /**
     * Validate the standard project archive before import project. ValidationException will throw if fails the validation.
     *
     * @param projectFileName
     *            the name of the project archive
     * @param projectArchive
     *            the project archive
     */
    public void validateStandardProject(final String projectFileName, final Archive projectArchive) {
        ddpTimer.start(CommandLogName.VALIDATE_PROJECT.toString());

        final List<String> greenfieldNodes = new ArrayList<>();
        final List<String> expansionNodes = new ArrayList<>();
        final List<String> replaceNodes = new ArrayList<>();
        final List<String> migrationNodes = new ArrayList<>();
        final List<String> validationErrors = new ArrayList<>();

        for (final String nodeDirectory : projectArchive.getAllDirectoryNames()) {
            try {
                if (nodeSchemaProcessor.existsNodeInfoFile(projectArchive, nodeDirectory)) {
                    final String ruleGroup = getRuleGroup(projectArchive, nodeDirectory);
                    switch (ruleGroup) {
                        case ValidationRuleGroups.ORDER:
                            greenfieldNodes.add(nodeDirectory);
                            break;
                        case ValidationRuleGroups.EXPANSION:
                            expansionNodes.add(nodeDirectory);
                            break;
                        case ValidationRuleGroups.HARDWARE_REPLACE:
                            replaceNodes.add(nodeDirectory);
                            break;
                        case ValidationRuleGroups.MIGRATION:
                            migrationNodes.add(nodeDirectory);
                            break;
                        default:
                            break;
                    }
                } else {
                    validationErrors.add(apMessages.get(VALIDATION_NODEINFO_NOT_FOUND));
                }
            } catch (final XmlException e) {
                logger.warn("Error reading file: {}", NODEINFO, e);
            }
        }
        validationErrors.addAll(getErrorsOnValidationIfAny(projectFileName, projectArchive, ValidationRuleGroups.ORDER, greenfieldNodes));
        validationErrors.addAll(getErrorsOnValidationIfAny(projectFileName, projectArchive, ValidationRuleGroups.EXPANSION, expansionNodes));
        validationErrors.addAll(getErrorsOnValidationIfAny(projectFileName, projectArchive, ValidationRuleGroups.HARDWARE_REPLACE, replaceNodes));
        validationErrors.addAll(getErrorsOnValidationIfAny(projectFileName, projectArchive, ValidationRuleGroups.MIGRATION, migrationNodes));

        if (!validationErrors.isEmpty()) {
            final String allValidationErrors = mergeAllValidationErrors(validationErrors);
            throw new ValidationException(validationErrors, apMessages.get("validation.project.error") + ": " + allValidationErrors);
        }

        final String projectFdn = getProjectFdnFromValidatedArchive(projectArchive);
        ddpTimer.end(projectFdn, projectArchive.getAllDirectoryNames().size());
    }

    /**
     * Validates a project archive. Can be standard or batch archive.
     *
     * @param projectFileName
     *            the name of the project archive
     * @param archive
     *            the project archive
     * @param ruleGroup
     *            the validation rule group to execute
     */
    public void validateArchive(final String projectFileName, final Archive archive, final String ruleGroup) {
        final List<String> validationErrors = getErrorsOnValidationIfAny(projectFileName, archive, ruleGroup, archive.getAllDirectoryNames());
        if (!validationErrors.isEmpty()) {
            final String allValidationErrors = mergeAllValidationErrors(validationErrors);
            throw new ValidationException(validationErrors, apMessages.get("validation.project.error") + ": " + allValidationErrors);
        }
    }

    private String getProjectFdnFromValidatedArchive(final Archive archive) {
        final String projectInfoContent = archive.getArtifactContentAsString(ProjectArtifact.PROJECTINFO.toString());
        final DocumentReader projectInfoDoc = new DocumentReader(projectInfoContent);
        return MoType.PROJECT.toString() + "=" + projectInfoDoc.getElementValue("name");
    }

    private List<String> getErrorsOnValidationIfAny(final String projectFileName, final Archive archive, final String ruleGroup,
        final List<String> nodeDirectories) {
        final Map<String, Object> validationTarget = new HashMap<>();
        validationTarget.put(ImportProjectTargetKey.FILENAME.toString(), projectFileName);
        validationTarget.put(ImportProjectTargetKey.FILE_CONTENT.toString(), archive);
        validationTarget.put(ImportProjectTargetKey.DIRECTORY_LIST.toString(), nodeDirectories);

        final ValidationContext context = new ValidationContext(ruleGroup, validationTarget);

        final boolean validProject = validationEngine.validate(context);
        if (!validProject) {
            return context.getValidationErrors();
        } else {
            return Collections.<String> emptyList();
        }
    }

    private String getRuleGroup(final Archive archive, final String nodeDirectory) {
        final String attributeValue = nodeSchemaProcessor.getNoNamespaceSchemaLocation(archive, nodeDirectory);
        if (EXPANSION_NODE_INFO_XSD.equals(attributeValue)) {
            return ValidationRuleGroups.EXPANSION;
        } else if (HARDWARE_REPLACE_NODE_INFO_XSD.equals(attributeValue)) {
            return ValidationRuleGroups.HARDWARE_REPLACE;
        } else if (MIGRATION_NODE_INFO_XSD.equals(attributeValue)) {
            return ValidationRuleGroups.MIGRATION;
        } else {
            return ValidationRuleGroups.ORDER;
        }
    }

    private static String mergeAllValidationErrors(final List<String> validationErrors) {
        final StringBuilder fullValidationErrorMessage = new StringBuilder();
        for (final String validationError : validationErrors) {
            if (fullValidationErrorMessage.length() != 0) {
                fullValidationErrorMessage.append(" ,");
            }
            fullValidationErrorMessage.append(validationError);
        }
        return fullValidationErrorMessage.toString();
    }
}
