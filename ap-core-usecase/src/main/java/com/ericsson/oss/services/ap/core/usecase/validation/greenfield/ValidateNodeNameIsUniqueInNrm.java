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

import static com.ericsson.oss.services.ap.common.model.MoType.NETWORK_ELEMENT;
import static com.ericsson.oss.services.ap.common.model.Namespace.OSS_NE_DEF;

import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
/**
 * Validate node name specified in nodeInfo.xml is not a duplicate of any <code>NetworkElement</code> MO already existing in ENM.
 */
@Group(name = ValidationRuleGroups.ORDER, priority = 10)
@Rule(name = "ValidateNodeNameIsUniqueInNrm")
public class ValidateNodeNameIsUniqueInNrm extends AbstractValidateNodeNameIsUnique {

    private static final String FAIL_NODE_ALREADY_EXISTS = "validation.nodename.unique.failure";

    @Override
    protected String getNamespace() {
        return OSS_NE_DEF.toString();
    }

    @Override
    protected String getMoType() {
        return NETWORK_ELEMENT.toString();
    }

    @Override
    protected String getValidationFailureMessage(final FDN existingNodeFdn) {
        return apMessages.format(FAIL_NODE_ALREADY_EXISTS, existingNodeFdn.getRdnValue());
    }
}
