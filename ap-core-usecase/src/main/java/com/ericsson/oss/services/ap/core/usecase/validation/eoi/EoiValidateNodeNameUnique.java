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

import com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NAME;

@Group(name = ValidationRuleGroups.EOI, priority = 3, abortOnFail = true)
@Rule(name = "EoiValidateNodeNameUnique")
public class EoiValidateNodeNameUnique extends EoiBasedValidation {

    @Override
    protected boolean validate(final ValidationContext context, final List<Map<String, Object>> networkElements) {
        final Collection<String> nodeNames = new HashSet<>();

        for (final Map<String, Object> networkElement : networkElements) {
            final String nodeName = (String) networkElement.get(ProjectRequestAttributes.NODE_NAME.toString());
            validateUniqueNodeName(context, nodeNames, nodeName);
        }

        return context.getValidationErrors().isEmpty();
    }

    private void validateUniqueNodeName(final ValidationContext context, final Collection<String> nodeNames, final String nodeName) {
        if (StringUtils.isBlank(nodeName)) {
            context.addValidationError(String.format("The value of node %s is null or empty in network element.", NAME));
        } else if (!nodeNames.add(nodeName)) {
            context.addValidationError(String.format("Duplicate node name %s", nodeName));
        } else {
            nodeNames.add(nodeName);
        }
    }
}
