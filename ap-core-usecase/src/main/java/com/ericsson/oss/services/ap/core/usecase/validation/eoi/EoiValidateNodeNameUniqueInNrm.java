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

import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;

import static com.ericsson.oss.services.ap.common.model.MoType.NETWORK_ELEMENT;
import static com.ericsson.oss.services.ap.common.model.Namespace.OSS_NE_DEF;

@Group(name = ValidationRuleGroups.EOI, priority = 4, abortOnFail = true)
@Rule(name = "EoiValidateNodeNameUniqueInNrm")
public class EoiValidateNodeNameUniqueInNrm extends AbstractEoiValidateNodeNameIsUnique {

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
