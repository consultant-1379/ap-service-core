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
import com.ericsson.oss.services.ap.api.exception.UnsupportedCommandException
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse

class UnsupportedCommandExceptionMapperSpec extends CdiSpecification {

    @ObjectUnderTest
    private UnsupportedCommandExceptionMapper exceptionMapper

    def "Verify Response messages are correctly parsed from UnsupportedCommandException"() {

        when: "UnsupportedCommandException is thrown"
        final ErrorResponse errorResponse = exceptionMapper.toErrorResponse(new UnsupportedCommandException("Node1"), "resume")
        final Response response = Response.status(errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
        response.getStatus() == 403
        errorResponse.errorTitle == "Unsupported action for this node type."
        errorResponse.errorBody == null
    }
}
