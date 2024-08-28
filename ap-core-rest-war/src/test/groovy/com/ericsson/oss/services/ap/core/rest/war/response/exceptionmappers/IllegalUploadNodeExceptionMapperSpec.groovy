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
package com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers

import javax.ws.rs.core.Response

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.IllegalUploadNodeStateException
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse

class IllegalUploadNodeExceptionMapperSpec extends CdiSpecification {

    @ObjectUnderTest
    private IllegalUploadNodeStateExceptionMapper exceptionMapper

    def "Verify Response messages are correctly parsed from IllegalUploadNodeStateException"() {

        given: "map of valid node states for upload"
        List<String> validStates = new ArrayList<>()
        validStates.add("ORDER_STARTED")
        validStates.add("INTEGRATION_STARTED")

        when: "IllegalUploadNodeStateException is thrown"
        final ErrorResponse errorResponse = exceptionMapper.toErrorResponse(new IllegalUploadNodeStateException("Exception message", "INTEGRATION_COMPLETED", validStates), "upload")
        final Response response = Response.status(errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
        response.getStatus() == 406
        errorResponse.errorTitle == "Node is not in the correct state to perform the operation [upload]."
        errorResponse.errorBody == "Suggested Solution : Ensure node is in correct state before performing the action. Valid state(s) are [Order Started, Integration Started]."
    }
}
