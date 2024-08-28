/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing.model

import com.ericsson.cds.cdi.support.spock.CdiSpecification

class CreateLicenseResponseSpec extends CdiSpecification {

    private static final String REQUEST_ID = "requestId_value"
    private static final String ADDITIONAL_INFO = "additionalInfo_value"

    def "Test Create License Response constructor with params works as expected"() {
        when: "Create object with params passed into the constructor"
            CreateLicenseResponseDto response = new CreateLicenseResponseDto(REQUEST_ID, ADDITIONAL_INFO)

        then: "Getters return expected values"
            response.getRequestId() == REQUEST_ID
            response.getAdditionalInfo() == ADDITIONAL_INFO
    }

    def "Test Create License Response constructor works as expected"() {
        when: "Create object"
            CreateLicenseResponseDto response = new CreateLicenseResponseDto()

        then: "Object is created as expected"
            response.class == CreateLicenseResponseDto.class
    }
}
