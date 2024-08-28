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
import com.ericsson.oss.services.ap.api.exception.InvalidNodeStateException
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse

class InvalidNodeStateExceptionMapperSpec extends CdiSpecification {

    @ObjectUnderTest
    private InvalidNodeStateExceptionMapper exceptionMapper

    def "Verify Response messages are correctly parsed from InvalidNodeStateException"() {

        when: "InvalidNodeStateException is thrown"
        final ErrorResponse errorResponse = exceptionMapper.toErrorResponse(new InvalidNodeStateException("Node1", "ORDER_COMPLETED"), "order")
        final Response response = Response.status(errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
        response.getStatus() == 403
        errorResponse.errorTitle == "Node is not in the correct state to perform the operation [Order Completed]."
        errorResponse.errorBody == "Suggested Solution : Ensure node is in correct state before performing the action. Valid state(s) are [Order Failed, Order Cancelled]."
    }
}
