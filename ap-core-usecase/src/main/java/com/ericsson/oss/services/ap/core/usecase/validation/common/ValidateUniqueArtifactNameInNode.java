/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.validation.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Groups;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader;

/**
 * Rule to validate the duplicate artifact file in nodeinfo
 */
@Groups(value = {@Group(name = ValidationRuleGroups.ORDER, priority = 12, abortOnFail = true),
        @Group(name = ValidationRuleGroups.EXPANSION, priority = 9, abortOnFail = true)})
@Rule(name = "ValidateUniqueArtifactNameInNode")
public class ValidateUniqueArtifactNameInNode extends AbstractValidateRule {

    private static final String VALIDATION_FILE_DUPLICATED_ERROR = "validation.artifact.file.duplicate.failure";
    private static final String BASELINE_CONFIGURATION_TAG = "baseline";
    private static final String REMOTE_NODE_CONFIGURATION_TAG = "remoteNodeConfiguration";

    @Inject
    private NodeInfoReader nodeInfoReader;

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        final Archive archive = getArchive(context);
        for (final String directoryName : directoryNames) {
            final NodeInfo nodeInfo = nodeInfoReader.read(archive, directoryName);
            final Map<String, List<String>> configurations = nodeInfo.getConfigurations();
            validateDuplicateArtifactConfigurations(context, directoryName, configurations, BASELINE_CONFIGURATION_TAG);
            validateDuplicateArtifactConfigurations(context, directoryName, configurations, REMOTE_NODE_CONFIGURATION_TAG);
            if (context.getGroup().equals(ValidationRuleGroups.EXPANSION)) {
                validateReservedName(context, directoryName, configurations, nodeInfo);
            }
        }

        return isValidatedWithoutError(context);
    }

    private void validateDuplicateArtifactConfigurations(final ValidationContext context, final String directoryName,
                                                         final Map<String, List<String>> configurations, final String configurationTag) {
        if (configurations != null) {
            final Set<String> validatedFileNames = new HashSet<>();
            final Set<String> duplicateFileNames = new HashSet<>();
            final List<String> fileNames = configurations.get(configurationTag);
            if (CollectionUtils.isNotEmpty(fileNames)) {
                fileNames.forEach(fileName -> {
                    if (validatedFileNames.contains(fileName)) {
                        duplicateFileNames.add(fileName);
                    } else {
                        validatedFileNames.add(fileName);
                    }
                });
                duplicateFileNames.forEach(fileName -> {
                    final String message = apMessages.format(VALIDATION_FILE_DUPLICATED_ERROR, fileName, configurationTag);
                    addNodeValidationFailure(context, message, directoryName);
                });
            }
        }
    }

    private void validateReservedName(final ValidationContext context, final String directoryName,
                                      final Map<String, List<String>> configurations, final NodeInfo nodeInfo) {
        final String nodeName = nodeInfo.getName();
        final List<String> fileNames = new ArrayList<>();
        for (final Entry<String, List<String>> configuration : configurations.entrySet()) {
            fileNames.addAll(configuration.getValue());
        }

        final String reservedArtifactName = String.format("preconfiguration_%s.xml", nodeName);
        for (final String fileName : fileNames) {
            if (reservedArtifactName.equalsIgnoreCase(fileName)) {
                final String message = apMessages.format("validation.artifact.name.reserved", fileName, reservedArtifactName);
                addNodeValidationFailure(context, message, directoryName);
            }
        }
    }
}
