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
import com.ericsson.oss.services.ap.api.exception.ProfileNotFoundException
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse

class ProfileNotFoundExceptionMapperSpec extends CdiSpecification {

    @ObjectUnderTest
    private ProfileNotFoundExceptionMapper exceptionMapper

    def "Verify Response messages are correctly parsed from ProfileNotFoundException"() {

        when: "ProfileNotFoundException is thrown"
        final ErrorResponse errorResponse = exceptionMapper.toErrorResponse(new ProfileNotFoundException("Node1"), "EDIT_PROFILE")
        final Response response = Response.status(errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
        response.getStatus() == 404
        errorResponse.errorTitle == "Profile does not exist."
        errorResponse.errorBody == "Suggested Solution : Perform action with a valid profile."
    }
}
