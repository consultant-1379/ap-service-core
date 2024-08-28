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
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse

/**
 * {@link ApApplicationExceptionMapper} builds error data using {@link ApServiceExceptionMapper} so both are covered by this test
 */
class ApApplicationExceptionMapperSpec extends CdiSpecification {

    @ObjectUnderTest
    private ApApplicationExceptionMapper exceptionMapper

    def "Verify Response messages are correctly parsed from ApApplicationException"() {

        when: "ApApplicationException is thrown"
        final ErrorResponse errorResponse = exceptionMapper.toErrorResponse(new ApApplicationException("Exception message details"), "CREATE_PROFILE")
        final Response response = Response.status(errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
        response.getStatus() == 400
        errorResponse.errorTitle == "Exception message details"
        errorResponse.errorBody == "Suggested Solution : Use Log Viewer for more information."
    }
}
