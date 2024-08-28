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
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse

class UnhandledExceptionMapperSpec extends CdiSpecification {

    @ObjectUnderTest
    private UnhandledExceptionMapper exceptionMapper

    def "Verify Response messages are correctly parsed from Exception"() {

        when: "Exception is thrown"
        final ErrorResponse errorResponse = exceptionMapper.toErrorResponse(new Exception("message"), "CREATE_PROFILE")
        final Response response = Response.status(errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
        response.getStatus() == 500
        errorResponse.errorTitle == "Internal Server Error."
        errorResponse.errorBody == "Suggested Solution : Use Log Viewer for more information."
    }
}
