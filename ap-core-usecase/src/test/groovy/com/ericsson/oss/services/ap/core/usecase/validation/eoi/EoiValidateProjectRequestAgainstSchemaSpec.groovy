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

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ValidationCrudException
import com.ericsson.oss.services.ap.api.validation.ValidationContext
import groovy.json.JsonSlurper

class EoiValidateProjectRequestAgainstSchemaSpec extends CdiSpecification {
    @ObjectUnderTest
    private EoiValidateProjectRequestAgainstSchema eoiValidateProjectRequestAgainstSchema
    private ValidationContext validationContext
    private static final Map<String, Object> attributes = new HashMap<>()
    final Map<String, Map<String, Object>> validationTarget = new HashMap<>()


    def "Validation passes with valid schema file"() {
        given: "Json Schema is pre-defined"
        def jsonSlurper = new JsonSlurper()
        def object = jsonSlurper.parseText('{ "name": "LTE01", "creator" : "admin","networkUsecaseType" : "ADD_TO_ENM","networkElements":[{"nodeName":"eoi","neType":"Shared-CNF","cnfType":"","userName":"Shared CNF","password":"temp","ossPrefix":"prefix","ipAddress":"0.0.0.0","modelVersion":"V23 09","supervision":{"pm":true,"cm":true,"fm":true}}]}')

        attributes.put("jsonPayload", object)
        validationTarget.put(EoiProjectTargetKey.REQUEST_CONTENT.toString(), attributes)
        validationContext = new ValidationContext("Import", validationTarget)


        when: "Validate against pre-defined schema with provided json input"
        boolean isValidationSuccess = eoiValidateProjectRequestAgainstSchema.execute(validationContext)

        then: "Validation Success"
        isValidationSuccess == true
    }

    def "Validation fails with json schema when any input has invalid data"() {
        given: "Json Schema is pre-defined"
        def jsonSlurper = new JsonSlurper()
        def object = jsonSlurper.parseText('{ "name": "LTE01", "creator" : "admin","networkUsecaseType" : "","eoiNetworkElements":[{"nodeName":"eoi","neType":"Shared-CNF","ossPrefix":"prefix","ipAddress":"198.175.1","supervision":{"pm":true,"cm":true,"fm":true}}]}')
        attributes.put("jsonPayload", object)
        validationTarget.put(EoiProjectTargetKey.REQUEST_CONTENT.toString(), attributes)
        validationContext = new ValidationContext("Import", validationTarget)


        when: "Validate against pre-defined schema with provided json input"
        boolean isValid = eoiValidateProjectRequestAgainstSchema.execute(validationContext)

        then: "Validation fails"
        isValid == false

    }

    def "ValidationCURDException thrown while any unexpected issue "() {
        given: "Json Schema is pre-defined"
        attributes.put("jsonPayload", _ as Object)
        validationTarget.put(EoiProjectTargetKey.REQUEST_CONTENT.toString(), attributes)
        validationContext = new ValidationContext("Import", validationTarget)


        when: "Any unexpected error while validating"

        def message = ""
        try {
            eoiValidateProjectRequestAgainstSchema.execute(validationContext)
        } catch (ValidationCrudException e) {
            message = e.getMessage()
        }
        then: "Throws ValidationCrudException"

        assert (message == "Internal Server Error")

    }

}


