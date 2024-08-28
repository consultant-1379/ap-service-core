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
import com.ericsson.oss.services.ap.api.exception.ProjectExistsException
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse

class ProjectExistsExceptionMapperSpec extends CdiSpecification {

    @ObjectUnderTest
    private ProjectExistsExceptionMapper exceptionMapper

    def "Verify Response messages are correctly parsed from ProjectExistsException"() {

        when: "ProjectExistsException is thrown"
        final ErrorResponse errorResponse = exceptionMapper.toErrorResponse(new ProjectExistsException("Project1"), "CREATE_PROJECT")
        final Response response = Response.status(errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
        response.getStatus() == 409
        errorResponse.errorTitle == "Project \"Project1\" already exists."
        errorResponse.errorBody == "Suggested Solution : Please choose a different project name"
    }
}
