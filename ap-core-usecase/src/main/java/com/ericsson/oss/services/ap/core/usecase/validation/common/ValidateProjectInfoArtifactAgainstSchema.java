/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.validation.common;

import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.PROJECTINFO;

import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.api.schema.SchemaData;
import com.ericsson.oss.services.ap.api.schema.SchemaService;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.util.xml.XmlValidator;
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaAccessException;
import com.ericsson.oss.services.ap.common.util.xml.exception.SchemaValidationException;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ZipBasedValidation;

/**
 * Rule to validate the projectInfo artifact against its schema.
 */
@Group(name = ValidationRuleGroups.ORDER, priority = 4, abortOnFail = true)
@Rule(name = "ValidateProjectInfoArtifactAgainstSchema")
public class ValidateProjectInfoArtifactAgainstSchema extends ZipBasedValidation {

    private static final String VALIDATION_PROJECTINFO_SCHEMA_ERROR = "validation.artifact.schema.failure";
    private static final String VALIDATION_SCHEMA_ACCESS = "validation.artifact.schema.access";

    @Inject
    private SchemaService schemaService;

    @Inject
    private XmlValidator xmlValidator;

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        final String projectInfoContents = getArtifactOfName(context, PROJECTINFO.toString());
        final List<SchemaData> schemaData = schemaService.readProjectInfoSchemas();
        return validateProjectInfoSchema(context, projectInfoContents, schemaData);
    }

    private boolean validateProjectInfoSchema(final ValidationContext context, final String artifactContent, final List<SchemaData> schemaData) {
        try {
            xmlValidator.validateAgainstSchema(artifactContent, schemaData);
            return true;
        } catch (final SchemaValidationException e) {
            logger.warn("Error validating {} schema: {}", PROJECTINFO, e.getMessage(), e);
            recordValidationError(context, VALIDATION_PROJECTINFO_SCHEMA_ERROR, PROJECTINFO.toString(), e.getValidationError());
        } catch (final SchemaAccessException e) {
            logger.warn("Error accessing {} schema: {}", PROJECTINFO, e.getMessage(), e);
            recordValidationError(context, VALIDATION_SCHEMA_ACCESS, PROJECTINFO.toString());
        }
        return false;
    }
}
