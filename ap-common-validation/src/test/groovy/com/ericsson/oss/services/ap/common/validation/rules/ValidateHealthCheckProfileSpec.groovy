/*
 *------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *------------------------------------------------------------------------------
 */
package com.ericsson.oss.services.ap.common.validation.rules

import com.fasterxml.jackson.databind.ObjectMapper
import org.mockito.internal.util.reflection.Whitebox

import spock.lang.Subject
import javax.inject.Inject

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.datalayer.dps.stub.RuntimeConfigurableDps
import com.ericsson.oss.services.ap.api.exception.HealthCheckProfileNotFoundException
import com.ericsson.oss.services.ap.api.exception.HealthCheckRestServiceException
import com.ericsson.oss.services.ap.api.validation.ValidationContext
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.services.ap.common.test.util.setup.MoCreatorSpec
import com.ericsson.oss.services.ap.core.rest.client.healthcheck.HealthCheckRestClient
import com.ericsson.oss.services.ap.core.rest.client.healthcheck.model.ProfileDetails

/**
 * ValidateHealthCheckProfileSpec is a test class for {@link ValidateHealthCheckProfile}
 */
class ValidateHealthCheckProfileSpec extends CdiSpecification {

    @Subject
    @Inject
    private ValidateHealthCheckProfile validateHealthCheckProfile

    @Inject
    private DpsOperations dpsOperations

    @MockedImplementation
    private HealthCheckRestClient healthCheckRestService;

    private ValidationContext context

    private RuntimeConfigurableDps dps

    private static final String PROJECT_FDN = "Project=Project1"
    private static final String NODE_FDN = PROJECT_FDN + ",Node=Node1"
    private static final String PROFILE_NAME = "ApProfile"

    private ManagedObject projectMo
    private ManagedObject apNodeMo

    final String profileDetailsWithRadioNodeProfile = "{\"name\":\"ApProfile\",\"softwareVersion\":\"CXP9024418/6_R2CXS2\"," +
            "\"creationTime\":1607379541626,\"createdBy\":\"administrator\",\"userLabel\":[],\"nodeType\":\"RadioNode\"}"

    final String profileDetailsWithErbsProfile = "{\"name\":\"ApProfile\",\"softwareVersion\":\"CXP9024418/6_R2CXS2\"," +
            "\"creationTime\":1607379541626,\"createdBy\":\"administrator\",\"userLabel\":[],\"nodeType\":\"ERBS\"}"

    final ObjectMapper objectMapper = new ObjectMapper()
    final ProfileDetails radioNodeProfileDetails = objectMapper.readValue(profileDetailsWithRadioNodeProfile, ProfileDetails.class)
    final ProfileDetails erbsProfileDetails = objectMapper.readValue(profileDetailsWithErbsProfile, ProfileDetails.class)

    void setup() {
        final Map<String, Object> contextTarget = new HashMap<>();
        contextTarget.put("nodeFdn", NODE_FDN);
        context = new ValidationContext("", contextTarget);
        dps = cdiInjectorRule.getService(RuntimeConfigurableDps)
        Whitebox.setInternalState(dpsOperations, "dps", dps.build())
        MoCreatorSpec.setDps(dps)
        projectMo = MoCreatorSpec.createProjectMo(PROJECT_FDN)
        apNodeMo = MoCreatorSpec.createNodeMo(NODE_FDN, projectMo)
    }

    def "Validate health check profile is not executed if HealthCheck MO does not exist"() {

        given: "HealthCheck MO is not created and mock get profile details call"
        healthCheckRestService.getProfileDetails(PROFILE_NAME) >> radioNodeProfileDetails

        when: "Validate method is called"
        validateHealthCheckProfile.execute(context)

        then: "Get profile details rest call is not executed"
        0 * healthCheckRestService.getProfileDetails(PROFILE_NAME)
    }

    def "Validate health check profile is not executed if profile name does not exist"() {

        given: "HealthCheck MO is created with empty profile name and mock get profile details call"
        MoCreatorSpec.createHealthCheckMo(NODE_FDN, apNodeMo, "")
        healthCheckRestService.getProfileDetails(PROFILE_NAME) >> radioNodeProfileDetails

        when: "Validate method is called"
        validateHealthCheckProfile.execute(context)

        then: "Get profile details rest call is not executed"
        0 * healthCheckRestService.getProfileDetails(PROFILE_NAME)
    }

    def "Validate health check profile passes without any validation error when a ProfileDetails object is returned from NHC with node type as RadioNode"() {

        given: "HealthCheck MO is created and mock get profile details call"
        MoCreatorSpec.createHealthCheckMo(NODE_FDN, apNodeMo, PROFILE_NAME)
        healthCheckRestService.getProfileDetails(PROFILE_NAME) >> radioNodeProfileDetails

        when: "Validate method is called"
        final boolean result = validateHealthCheckProfile.execute(context)

        then: "Validation is successful without any errors"
        result == true
        context.getValidationErrors().isEmpty() == true
    }

    def "Validate health check profile fails when a ProfileDetails object is returned from NHC with node type other than RadioNode"() {

        given: "HealthCheck MO is created and mock get profile details call"
        MoCreatorSpec.createHealthCheckMo(NODE_FDN, apNodeMo, PROFILE_NAME)
        healthCheckRestService.getProfileDetails(PROFILE_NAME) >> erbsProfileDetails

        when: "Validate method is called"
        final boolean result = validateHealthCheckProfile.execute(context)

        then: "Validation fails with proper error"
        result == false
        context.getValidationErrors().get(0) == "Health check profile " + PROFILE_NAME + " is of nodeType ERBS"
    }

    def "Validate health check profile fails when a HealthCheckRestServiceException is thrown when get profile details called"() {

        given: "HealthCheck MO is created and mock get profile details call"
        MoCreatorSpec.createHealthCheckMo(NODE_FDN, apNodeMo, PROFILE_NAME)
        healthCheckRestService.getProfileDetails(PROFILE_NAME) >> {throw new HealthCheckRestServiceException(_ as String)}

        when: "Validate method is called"
        final boolean result = validateHealthCheckProfile.execute(context)

        then: "Validation fails with proper error"
        result == false
        context.getValidationErrors().get(0) == "Could not get health check profile details from NHC for profile " + PROFILE_NAME
    }

    def "Validate health check profile fails when a HealthCheckProfileNotFoundException is thrown when get profile details called"() {

        given: "HealthCheck MO is created and mock get profile details call"
        MoCreatorSpec.createHealthCheckMo(NODE_FDN, apNodeMo, PROFILE_NAME)
        healthCheckRestService.getProfileDetails(PROFILE_NAME) >> {throw new HealthCheckProfileNotFoundException(_ as String)}

        when: "Validate method is called"
        final boolean result = validateHealthCheckProfile.execute(context)

        then: "Validation fails with proper error"
        result == false
        context.getValidationErrors().get(0) == "Health check profile " + PROFILE_NAME + " not found in NHC"
    }

    def "Validate health check profile fails when null is returned from NHC when getting profile details"() {

        given: "HealthCheck MO is created and mock get profile details call"
        MoCreatorSpec.createHealthCheckMo(NODE_FDN, apNodeMo, PROFILE_NAME)
        healthCheckRestService.getProfileDetails(PROFILE_NAME) >> null

        when: "Validate method is called"
        final boolean result = validateHealthCheckProfile.execute(context)

        then: "Validation fails with proper error"
        result == false
        context.getValidationErrors().get(0) == "Could not get health check profile details from NHC for profile " + PROFILE_NAME
    }
}
