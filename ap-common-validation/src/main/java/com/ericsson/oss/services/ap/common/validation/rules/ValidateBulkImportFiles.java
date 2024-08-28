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
package com.ericsson.oss.services.ap.common.validation.rules;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Groups;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.common.validation.configuration.ConfigurationFileValidator;

/**
 * Validate the configuration files supplied for each node in a project.
 */
@Groups(value = {@Group(name = ValidationRuleGroups.ORDER_WORKFLOW, priority = 13),
    @Group(name = ValidationRuleGroups.MIGRATION_WORKFLOW, priority = 13)})
@Rule(name = "ValidateBulkImportFiles")
public class ValidateBulkImportFiles extends NodeConfigurationsValidation {

    private ResourceService resourceService;

    @Inject
    private ConfigurationFileValidator configurationFileValidator;

    @Inject
    private DpsOperations dps;

    @PostConstruct
    public void init() {
        resourceService = new ServiceFinderBean().find(ResourceService.class);
    }

    @Override
    protected boolean validate(final ValidationContext context) {
        validateConfigurationFiles(context);
        return isValidatedWithoutError(context);
    }

    private void validateConfigurationFiles(final ValidationContext context) {
        final String nodeFdn = getNodeFdn(context);
        final List<String> configFileLocations = getConfigFilesLocation(context);

        logger.info("Validating configuration files for node {}", FDN.get(nodeFdn).getRdnValue());

        for (final String fileConfig : configFileLocations) {
            final String fileContent = resourceService.getAsText(fileConfig);
            final ArchiveArtifact configurationFile = new ArchiveArtifact(fileConfig, fileContent);
            validateSingleConfigurationFile(context, nodeFdn, configurationFile);
        }
    }

    private void validateSingleConfigurationFile(final ValidationContext context, final String nodeFdn,
            final ArchiveArtifact configurationFileArtifact) {

        final List<String> validationErrors = validateConfigurationFile(nodeFdn, configurationFileArtifact);

        if (!validationErrors.isEmpty()) {
            logger.warn("Error validating file {} for node {}", configurationFileArtifact.getName(), nodeFdn);

            for (final String validationErrorMessage : validationErrors) {
                context.addValidationError(validationErrorMessage);
            }
        }
    }

    private List<String> validateConfigurationFile(final String nodeFdn,
            final ArchiveArtifact configurationFileArtifact) {

        final String nodeName = FDN.get(nodeFdn).getRdnValue();
        final ManagedObject nodeMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
        final ManagedObject projectMo = nodeMo.getParent();
        final String projectName = projectMo.getName();

        final String nodeIdentifier = nodeMo.getAttribute(NodeAttribute.NODE_IDENTIFIER.toString());

        return configurationFileValidator.validateFile(projectName, nodeName, nodeIdentifier, configurationFileArtifact);
    }
}
