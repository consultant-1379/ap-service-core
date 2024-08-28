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
package com.ericsson.oss.services.ap.core.usecase.validation.greenfield;

import static com.ericsson.oss.services.ap.common.model.MoType.NODE;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;

import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;

/**
 * Validate node name specified in nodeInfo.xml is not a duplicate of any AP <code>Node</code> MO already existing in ENM.
 */
@Group(name = ValidationRuleGroups.ORDER, priority = 10)
@Rule(name = "ValidateNodeNameIsUniqueInAp")
public class ValidateNodeNameIsUniqueInAp extends AbstractValidateNodeNameIsUnique {

    private static final String FAIL_NODE_ALREADY_EXISTS = "validation.node.exists.failure";

    @Override
    protected String getNamespace() {
        return AP.toString();
    }

    @Override
    protected String getMoType() {
        return NODE.toString();
    }

    @Override
    protected String getValidationFailureMessage(final FDN nodeFdn) {
        final FDN projectFdn = FDN.get(nodeFdn.getParent());
        return apMessages.format(FAIL_NODE_ALREADY_EXISTS, nodeFdn.getRdnValue(), projectFdn.getRdnValue());
    }
}