/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ArtifactFileNotFoundException
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse

import javax.ws.rs.core.Response

class ArtifactFileNotFoundExceptionMapperSpec extends CdiSpecification {

    public static final String ERROR_MESSAGE = "No node artifact file found with name: SiteInstallation.xml " +
            "for Project=HardwareReplace_RadioNode_Project,Node=LTE01dg2ERBS00009"
    public static final String ERROR_TITLE = "Error(s) found validating project. "
    public static final String SUGGESTED_SOLUTION = "Suggested Solution : Fix error(s) and try again."

    @ObjectUnderTest
    private ArtifactFileNotFoundExceptionMapper exceptionMapper

    def "Verify Error Response message is correctly returned from ArtifactFileNotFoundException"() {
        when: "ArtifactFileNotFoundException is thrown"
            final ErrorResponse errorResponse = exceptionMapper.toErrorResponse(
                    new ArtifactFileNotFoundException(ERROR_MESSAGE)
                    as ArtifactFileNotFoundException, "")
            final Response response = Response.status(
                    errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
            response.getStatus() == 400
            errorResponse.errorTitle == ERROR_TITLE + ERROR_MESSAGE
            errorResponse.errorBody == SUGGESTED_SOLUTION
    }

    def "Verify Response message is correctly returned from ArtifactFileNotFoundException"() {
        when: "ArtifactFileNotFoundException is thrown"
            final Response response = exceptionMapper.toResponse(
                    new ArtifactFileNotFoundException(ERROR_MESSAGE)
                    as ArtifactFileNotFoundException)

        then: "correct format is output"
            response.getStatus() == 403
            response.getEntity().errorTitle == ERROR_TITLE + ERROR_MESSAGE
            response.getEntity().errorBody == SUGGESTED_SOLUTION
    }
}
