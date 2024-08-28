/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.ap.core.rest.model.request

import static org.junit.Assert.assertEquals

import com.ericsson.cds.cdi.support.spock.CdiSpecification

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

import spock.lang.Unroll

class DeleteProjectRequestSpec extends CdiSpecification {

    private static ValidatorFactory validatorFactory
    private static Validator validator

    def setupSpec() {
        validatorFactory = Validation.buildDefaultValidatorFactory()
        validator = validatorFactory.getValidator()
    }

    def "no validation error should occur when model is valid"() {

        given: "a valid delete model"
        DeleteProjectRequest deleteProjectRequest = new DeleteProjectRequest(ignoreNetworkElement: false, projectIds: ["Project01"])

        when: "validation constraints are set"
        Set<ConstraintViolation<DeleteProjectRequest>> constraintViolations = validator.validate(deleteProjectRequest)

        then: "no validation error should occur"
        assertEquals(constraintViolations.size(), 0)
    }

    @Unroll
    def "validation error should occur when model has #description ID list"() {

        given: "an invalid delete model with #description ID list"
        DeleteProjectRequest deleteProjectRequest = new DeleteProjectRequest(ignoreNetworkElement: false, projectIds: ids)

        when: "validation constraints are set"
        Set<ConstraintViolation<DeleteProjectRequest>> constraintViolations = validator.validate(deleteProjectRequest)

        then: "validation exception should occur with the message: #message"
        assertEquals(constraintViolations.size(), 1)
        ConstraintViolation<DeleteProjectRequest> violation = constraintViolations.iterator().next()
        assertEquals(message, violation.getMessage())

        where:
        description | ids  | message
        "empty"     | []   | "You need to provide at least one project ID."
        "null"      | null | "may not be null"
    }
}
