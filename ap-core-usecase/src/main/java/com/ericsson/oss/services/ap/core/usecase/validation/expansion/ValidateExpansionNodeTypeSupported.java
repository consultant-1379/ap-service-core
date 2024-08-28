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
package com.ericsson.oss.services.ap.core.usecase.validation.expansion;

import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.usecase.validation.common.ValidateNodeTypeSupported;

/**
 * Rule to validate that all expansion nodes in an AP project have supported node type.
 */
@Group(name = ValidationRuleGroups.EXPANSION, priority = 6, abortOnFail = true)
@Rule(name = "ValidateExpansionNodeTypeSupportedInAp")
public class ValidateExpansionNodeTypeSupported extends ValidateNodeTypeSupported {

    static {
        SUPPORTED_NODE_TYPES.add("RadioNode");
        SUPPORTED_NODE_TYPES.add("Controller6610");
        validationFailNodeTypeNotSupportedError = "validation.expansion.node.type.not.supported";
    }
}
