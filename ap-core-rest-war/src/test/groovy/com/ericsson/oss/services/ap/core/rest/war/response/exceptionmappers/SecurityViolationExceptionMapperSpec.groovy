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
import com.ericsson.oss.itpf.sdk.security.accesscontrol.SecurityViolationException
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse

class SecurityViolationExceptionMapperSpec extends CdiSpecification {

    @ObjectUnderTest
    private SecurityViolationExceptionMapper exceptionMapper

    def "Verify Response messages are correctly parsed from SecurityViolationException"() {

        when: "SecurityViolationException is thrown"
        final ErrorResponse errorResponse = exceptionMapper.toErrorResponse(new SecurityViolationException("message"), "CREATE_PROFILE")
        final Response response = Response.status(errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
        response.getStatus() == 401
        errorResponse.errorTitle == "Insufficient access rights to execute the action."
        errorResponse.errorBody == "Suggested Solution : Contact the system administrator to update the user profile."
    }
}
