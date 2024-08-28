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
import com.ericsson.oss.services.ap.api.exception.UnsupportedArtifactTypeException
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse

class UnsupportedArtifactTypeExceptionMapperSpec extends CdiSpecification {

    @ObjectUnderTest
    private UnsupportedArtifactTypeExceptionMapper exceptionMapper

    def "Verify Response messages are correctly parsed from UnsupportedArtifactTypeException"() {

        when: "UnsupportedArtifactTypeException is thrown"
        final ErrorResponse errorResponse = exceptionMapper.toErrorResponse(new UnsupportedArtifactTypeException("Node1", "SITE_BASIC"), "download")
        final Response response = Response.status(errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
        response.getStatus() == 409
        errorResponse.errorTitle == "The file type for file SITE_BASIC is not supported by the upload action."
        errorResponse.errorBody == "Suggested Solution : Rename the configuration file to match the name of an existing configuration that is supported."
    }
}
