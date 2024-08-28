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
import com.ericsson.oss.services.ap.api.exception.IllegalCancelOperationException
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse

class IllegalCancelOperationExceptionMapperSpec extends CdiSpecification {

    @ObjectUnderTest
    private IllegalCancelOperationExceptionMapper exceptionMapper

    def "Verify Response messages are correctly parsed from IllegalCancelOperationException"() {

        when: "IllegalCancelOperationException is thrown"
        final ErrorResponse errorResponse = exceptionMapper.toErrorResponse(new IllegalCancelOperationException(new Exception()), "CANCEL")
        final Response response = Response.status(errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
        response.getStatus() == 403
        errorResponse.errorTitle == "Node is not waiting for resume or cancel."
        errorResponse.errorBody == "Suggested Solution : Action can only be used on node waiting for resume or cancel."
    }
}
