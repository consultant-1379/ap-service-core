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

package com.ericsson.oss.services.ap.core.usecase.validation.eoi

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ValidationCrudException
import com.ericsson.oss.services.ap.api.validation.ValidationContext
import com.ericsson.oss.services.nedo.ipaddress.validator.DuplicateIpAddressValidationResult
import com.ericsson.oss.services.nedo.ipaddress.validator.DuplicateIpAddressValidator

class EoiValidateIpAddressUniqueSpec extends CdiSpecification {

    private static final String FAILURE_FDN = "Project=Test,Node=Node00001"
    private static final String TEST_IP = "1.1.1.1"

    @ObjectUnderTest
    private EoiValidateIpAddressUnique eoiValidateIpAddressUnique


    @MockedImplementation
    private DuplicateIpAddressValidator duplicateIpAddressValidator


    private ValidationContext validationContext

    private final Map<String, Object> projectDataContentTarget = new HashMap<>()

    private static final successfulValidationResult = new DuplicateIpAddressValidationResult()
    private static final failureValidationResult = new DuplicateIpAddressValidationResult(FAILURE_FDN, TEST_IP)

    private static final invalidValidationResult = new DuplicateIpAddressValidationResult("Invalid IP", new Exception())


    def setup() {
        Map<String, Object> attributes = [

                nodeName : "LTE01",
                neType   : "ERBS",
                cnfType  : "cnftype",
                ipAddress: TEST_IP

        ]

        final List list = new ArrayList()
        list.add(attributes)
        final Map<String, Object> elements = new HashMap<>()
        elements.put("networkelements", list)

        projectDataContentTarget.put(EoiProjectTargetKey.REQUEST_CONTENT.toString(), elements)
        validationContext = new ValidationContext("import", projectDataContentTarget)

    }

    def "Validation passes when ip is unique accross the system in project "() {
        given: "Project and test ip is NOT in the system"
        duplicateIpAddressValidator.checkForValidIpAddress(_ as List<String>, false) >> successfulValidationResult

        when: "There is a node in the project file with the test ip"
        boolean isValid = eoiValidateIpAddressUnique.execute(validationContext)

        then: "Validation passed"
        isValid
    }

    def "Validation fails when ip is NOT unique across the system in project "() {
        given: "Project and test ip IS in the system"
        duplicateIpAddressValidator.checkForValidIpAddress(_ as List<String>, false) >> failureValidationResult

        when: "There is a node in the project  with the test ip"
        boolean isValid = eoiValidateIpAddressUnique.execute(validationContext)

        def errors = validationContext.validationErrors
        boolean hasIpAddressValidationError = false
        for (final String error : errors) {
            if (error.contains(FAILURE_FDN)) {
                hasIpAddressValidationError = true
            }
        }

        then: "Validation failed and validation error is in context"
        !isValid
        hasIpAddressValidationError
    }


    def "Validation passes when invalid ip is provided"() {
        given: "Project and invalid ip address"
        duplicateIpAddressValidator.checkForValidIpAddress(_ as List<String>, false) >> invalidValidationResult

        when: "There is a node in the project file with an invalid ip"
        boolean isValid = eoiValidateIpAddressUnique.execute(validationContext)

        then: "Validation passed"
        isValid
    }

    def "Validation exception throw when unexpected error occurred"() {
        given: "Project file"
        duplicateIpAddressValidator.checkForValidIpAddress(_ as List<String>, false) >> { throw new ValidationCrudException("Internal Server Error") }

        when: "Any unexpected error while validating"
        def message = ""
        try {
            eoiValidateIpAddressUnique.execute(validationContext)
        } catch (ValidationCrudException e) {
            message = e.getMessage()
        }

        then: "Throws ValidationCurdException"
        assert (message == "Internal Server Error")
    }

}
