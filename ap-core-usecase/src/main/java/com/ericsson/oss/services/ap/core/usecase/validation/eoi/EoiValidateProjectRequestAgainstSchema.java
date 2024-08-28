/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.ap.core.usecase.validation.eoi;


import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.ValidationCrudException;
import com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.api.validation.rules.ValidationRule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.ericsson.oss.services.ap.core.usecase.validation.eoi.EoiProjectTargetKey.REQUEST_CONTENT;

@Group(name = ValidationRuleGroups.EOI, priority = 1, abortOnFail = true)
@Rule(name = "EoiValidateProjectRequestAgainstSchema")
public class EoiValidateProjectRequestAgainstSchema implements ValidationRule {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SCHEMA_PATH = "schemas/eoi-schema.json";
    private static final String VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE = "failure.general";
    private final ApMessages apMessages = new ApMessages();


    @Override
    @SuppressWarnings("unchecked")
    public boolean execute(ValidationContext context) {
        final Map<String, Map<String, Object>> contextTarget = (Map<String, Map<String, Object>>) context.getTarget();
        final Map<String, Object> projectData = contextTarget.get(REQUEST_CONTENT.toString());
        Object requestJson = projectData.get(ProjectRequestAttributes.JSON_PAYLOAD.toString());
        validateRequestAgainstSchema(context, requestJson);
        return context.getValidationErrors().isEmpty();
    }

    private void validateRequestAgainstSchema(final ValidationContext context, final Object requestJson) {
        try {
            String jsonAsString = objectMapper.writeValueAsString(requestJson);
            JsonNode jsonNode = objectMapper.readTree(jsonAsString);
            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema jsonSchema = factory.getSchema(getJsonSchema());
            Set<ValidationMessage> errors = jsonSchema.validate(jsonNode);
            if (!errors.isEmpty()) {
                List<String> schemaErrors = new ArrayList<>();
                for (ValidationMessage vm : errors) {
                    if (vm.getMessage().contains("ipv4") || vm.getMessage().contains("ipv6")) {
                        String[] ipRegex = vm.getMessage().split("pattern");
                        schemaErrors.add(ipRegex[0].replace("$.", "").trim());
                    } else {
                        schemaErrors.add(vm.getMessage().replace("$.", ""));
                    }
                }
                context.addValidationError(String.format("Failed to validate against schema %s", getValidationSchemaErrorString(schemaErrors)));
            }
        } catch (Exception exception) {
            logger.error("Unexpected error while validating schema: {}", exception.getMessage());
            throw new ValidationCrudException(apMessages.get(VALIDATION_TERMINATED_CRUD_EXCEPTION_MESSAGE), exception);

        }
    }

    private InputStream getJsonSchema() {

        try {
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(SCHEMA_PATH);
        } catch (final ApApplicationException exception) {
            logger.error("Error occurred when loading json schema {}", exception.getMessage());
            throw new ApApplicationException(String.format("Failed to load schema from  %s", SCHEMA_PATH));
        }
    }


    private String getValidationSchemaErrorString(List<String> schemaErrors) {

        return StringUtils.join(schemaErrors, ", ");
    }
}
