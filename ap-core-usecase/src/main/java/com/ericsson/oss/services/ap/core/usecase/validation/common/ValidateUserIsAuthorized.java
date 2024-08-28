/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.validation.common;

import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;

import com.ericsson.oss.itpf.sdk.security.accesscontrol.EAccessControl;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecurityAction;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecurityResource;
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecuritySubject;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.util.capability.SecurityCapability;
import com.ericsson.oss.services.ap.common.util.capability.ServiceCapabilityModel;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.usecase.validation.greenfield.ZipBasedValidation;

@Group(name = ValidationRuleGroups.ORDER, priority = 12, abortOnFail = true)
@Rule(name = "ValidateUserIsAuthorized")
public class ValidateUserIsAuthorized extends ZipBasedValidation {

    private static final String APPLY_MOS_SCRIPT = "APPLY_AMOS_SCRIPT";
    private static final String BASELINE_ARTIFACT_TAG = "baseline";
    private static final String VALIDATION_USER_NOT_AUTHORIZED_ERROR = "access.control.not.authorized";

    @Inject
    private EAccessControl eAccessControl;

    @Override
    protected boolean validate(final ValidationContext context, final List<String> directoryNames) {
        for (final String dirName : directoryNames) {
            if (isBaselinePresentInNodeInfo(context, dirName) && !isUserAuthorized(context, dirName, APPLY_MOS_SCRIPT)) {
                return false;
            }
        }

        return true;
    }

    private boolean isBaselinePresentInNodeInfo(final ValidationContext context, final String dirName) {
        final List<String> baselineArtifacts = getNodeInfo(context, dirName).getNodeArtifacts().get(BASELINE_ARTIFACT_TAG);
        return CollectionUtils.isNotEmpty(baselineArtifacts);
    }

    private boolean isUserAuthorized(final ValidationContext context, final String dirName, final String useCase) {
        try {
            final ESecuritySubject authUser = getAuthenticatedUser();
            final List<SecurityCapability> securityCapabilities = ServiceCapabilityModel.INSTANCE.getRequiredCapabilities(useCase);

            for (final SecurityCapability securityCapability : securityCapabilities) {
                final String resource = securityCapability.getResource();
                final List<String> operations = securityCapability.getOperations();

                for (final String operation : operations) {
                    if (!eAccessControl.isAuthorized(authUser, new ESecurityResource(resource), new ESecurityAction(operation))) {
                        logger.error("Lack of security access. Resource: {}, Operation: {}.", resource, operation);
                        recordNodeValidationError(context, VALIDATION_USER_NOT_AUTHORIZED_ERROR, dirName);
                        return false;
                    }
                }
            }
        } catch (final Exception e) {
            logger.error("Failed to validate the user authentication.", e);
            recordNodeValidationError(context, VALIDATION_USER_NOT_AUTHORIZED_ERROR, dirName);
            return false;
        }

        return true;
    }

    private ESecuritySubject getAuthenticatedUser() {
        final ESecuritySubject authUserSubject = eAccessControl.getAuthUserSubject();
        return (authUserSubject != null) ? authUserSubject : new ESecuritySubject("invalid_SubjectId");
    }
}