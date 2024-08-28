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

class GetLicenseStatusResponseSpec extends CdiSpecification {

    private static final String REQUESTID = "requestId_value"
    private static final String FINGERPRINT = "fingerprint_value"
    private static final String RESULT = "result_value"
    private static final String STATE = "state_value"
    private static final String ADDITIONALINFO = "additionalInfo_value"

    def "Test Create License Request constructor works as expected"() {
        when: "Create object with params passed into the constructor"
            RetrieveLicenseStatusResponseDto request = new RetrieveLicenseStatusResponseDto(REQUESTID, FINGERPRINT, RESULT, STATE, ADDITIONALINFO)

        then: "Getters return expected values"
            request.getRequestId() == REQUESTID
            request.getFingerprint() == FINGERPRINT
            request.getResult() == RESULT
            request.getState() == STATE
            request.getAdditionalInfo() == ADDITIONALINFO
    }

    def "Test get license status response constructor"() {
        when: "Create object"
            RetrieveLicenseStatusResponseDto request = new RetrieveLicenseStatusResponseDto()

        then: "Object is created as expected"
            request.class == RetrieveLicenseStatusResponseDto.class
    }
}
