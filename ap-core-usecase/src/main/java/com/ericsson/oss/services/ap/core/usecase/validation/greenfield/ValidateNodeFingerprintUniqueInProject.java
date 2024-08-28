/*
 ------------------------------------------------------------------------------
  *******************************************************************************
  * COPYRIGHT Ericsson 2020
  *
  * The copyright to the computer program(s) herein is the property of
  * Ericsson Inc. The programs may be used and/or copied only with written
  * permission from Ericsson Inc. or in accordance with the terms and
  * conditions stipulated in the agreement/contract under which the
  * program(s) have been supplied.
  *******************************************************************************
  *----------------------------------------------------------------------------
 */

package com.ericsson.oss.services.ap.core.usecase.validation.greenfield;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;

/**
 * Validate that the Node Fingerprint of a Node is unique within an AP Project
 */
@Group(name = ValidationRuleGroups.ORDER, priority = 10)
@Rule(name = "ValidateNodeFingerprintUniqueInProject")
public class ValidateNodeFingerprintUniqueInProject extends ZipBasedValidation {

    private static final String FINGERPRINT_ATTRIBUTE_NAME = "fingerprint";
    private static final String VALIDATION_FAILURE_FOR_NODE_FINGERPRINT_MESSAGE = "validation.duplicate.node.fingerprint";

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        final Set<String> fingerprints = new HashSet<>();
        for (final String dirName : directoryNames) {
            final String fingerprint = getFingerprint(context, dirName);
            if (isValidFingerprint(fingerprint) && isDuplicateFingerprint(fingerprints, fingerprint)) {
                recordNodeValidationError(context, VALIDATION_FAILURE_FOR_NODE_FINGERPRINT_MESSAGE, dirName, fingerprint);
            }
        }
        return context.getValidationErrors().isEmpty();
    }

    private String getFingerprint(final ValidationContext context, final String dirName) {
        final NodeInfo nodeInfo = getNodeInfo(context, dirName);
        return (String) nodeInfo.getLicenseAttributes().get(FINGERPRINT_ATTRIBUTE_NAME);
    }

    private static boolean isValidFingerprint(final String fingerprint) {
        return !StringUtils.isBlank(fingerprint);
    }

    private static boolean isDuplicateFingerprint(final Set<String> fingerprints, final String fingerprint) {
        return !fingerprints.add(fingerprint);
    }
}
