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
package com.ericsson.oss.services.ap.common.validation.rules;

import static com.ericsson.oss.services.ap.common.model.HealthCheckAttribute.HEALTH_CHECK_PROFILE_NAME;
import static com.ericsson.oss.services.ap.common.model.MoType.HEALTH_CHECK;

import com.ericsson.oss.services.ap.api.validation.rules.Groups;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.HealthCheckProfileNotFoundException;
import com.ericsson.oss.services.ap.api.exception.HealthCheckRestServiceException;
import com.ericsson.oss.services.ap.api.validation.ValidationContext;
import com.ericsson.oss.services.ap.api.validation.rules.Group;
import com.ericsson.oss.services.ap.api.validation.rules.Rule;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.validation.ValidationRuleGroups;
import com.ericsson.oss.services.ap.core.rest.client.healthcheck.HealthCheckRestClient;
import com.ericsson.oss.services.ap.core.rest.client.healthcheck.model.ProfileDetails;

/**
 * Validate the existence of a health check profile specified in a NodeInfo file by calling NHC
 */
@Groups(value = {@Group(name = ValidationRuleGroups.ORDER_WORKFLOW, priority = 15),
    @Group(name = ValidationRuleGroups.MIGRATION_WORKFLOW, priority = 15)})
@Rule(name = "ValidateHealthCheckProfile")
public class ValidateHealthCheckProfile extends NodeConfigurationsValidation {

    private static final String RADIO_NODE = "RadioNode";

    private Logger validateHealthCheckProfilelogger = LoggerFactory.getLogger(getClass());

    @Inject
    private DpsOperations dps;

    @Inject
    private HealthCheckRestClient healthCheckRestService;

    @Override
    protected boolean validate(final ValidationContext context) {

        final String nodeFdn = getNodeFdn(context);
        final String healthCheckMoFdn = String.format("%s,%s=1", nodeFdn, HEALTH_CHECK.toString());

        final ManagedObject healthCheckMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(healthCheckMoFdn);

        if (null != healthCheckMo) {
            final String profileName = (String) healthCheckMo.getAllAttributes().get(HEALTH_CHECK_PROFILE_NAME.getAttributeName());
            if (StringUtils.isNotBlank(profileName)) {
                validateProfile(context, profileName);
            }
        }
        return isValidatedWithoutError(context);
    }

    private void validateProfile(final ValidationContext context, final String profileName) {
        try {
            final ProfileDetails profileDetails = healthCheckRestService.getProfileDetails(profileName);
            if (null != profileDetails) {
                if  (!RADIO_NODE.equals(profileDetails.getNodeType())) {
                    context.addValidationError(String.format("Health check profile %s is of nodeType %s", profileName, profileDetails.getNodeType()));
                }
            } else {
                context.addValidationError(String.format("Could not get health check profile details from NHC for profile %s", profileName));
            }
        } catch (final HealthCheckRestServiceException healthCheckRestServiceException) {
            final String errorString = String.format("Could not get health check profile details from NHC for profile %s", profileName);
            validateHealthCheckProfilelogger.error(errorString, healthCheckRestServiceException);
            context.addValidationError(errorString);
        } catch (final HealthCheckProfileNotFoundException healthCheckProfileNotFoundException) {
            final String errorString = String.format("Health check profile %s not found in NHC", profileName);
            validateHealthCheckProfilelogger.error(errorString, healthCheckProfileNotFoundException);
            context.addValidationError(errorString);
        }
    }
}
