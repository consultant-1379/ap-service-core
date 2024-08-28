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
import com.ericsson.oss.services.ap.api.exception.IllegalDownloadArtifactException
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse

class IllegalDownloadArtifactExceptionMapperSpec extends CdiSpecification {

    @ObjectUnderTest
    private IllegalDownloadArtifactExceptionMapper exceptionMapper

    def "Verify Response messages are correctly parsed from IllegalDownloadArtifactException"() {

        when: "IllegalDownloadArtifactException is thrown"
        final ErrorResponse errorResponse = exceptionMapper.toErrorResponse(new IllegalDownloadArtifactException("Exception Message"), "DOWNLOAD")
        final Response response = Response.status(errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
        response.getStatus() == 403
        errorResponse.errorTitle == "Node is not in correct state to execute the requested operation. See AP Online help for more information."
        errorResponse.errorBody == null
    }
}
