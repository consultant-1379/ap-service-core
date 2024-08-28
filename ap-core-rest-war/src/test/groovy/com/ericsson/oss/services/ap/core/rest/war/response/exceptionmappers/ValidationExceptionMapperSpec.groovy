/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/

package com.ericsson.oss.services.ap.core.rest.war.response.exceptionmappers

import com.ericsson.oss.services.ap.api.exception.ValidationException

import javax.ws.rs.core.Response
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse

/**
 * Unit tests for {@link ValidationExceptionMapper}.
 */
class ValidationExceptionMapperSpec extends CdiSpecification {

    @ObjectUnderTest
    private ValidationExceptionMapper validationExceptionMapper

    def "Verify Response has Node Validation Error with correct responseCode and content"() {

        given: "A single node with a single error"
        List<String> errorSingleNode = Arrays.asList("Node 1 - Err1")

        when: "Node Validation Errors need to be built"
        final ErrorResponse errorResponse = validationExceptionMapper.toErrorResponse(new ValidationException(errorSingleNode, "Exception message"), "ORDER_PROJECT")
        final Response response = Response.status(errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
        response.getStatus() == 417
        errorResponse.errorTitle == "Error(s) found validating project."
        errorResponse.errorBody == "Suggested Solution : Fix error(s) and try again."
        errorResponse.errorDetails.size() == 1
        errorResponse.errorDetails.get(0).contains("Node 1 - Err1")
    }

    def "Verify Response has multiple Node Validation Error with correct responseCode and content"() {

        given: "Node Validation Errors for Three nodes with mixed errors each"
        List<String> errorsThreeNodes = Arrays.asList("Node-1 - Err1",
                "Node-2 - Node2-Err1", "Node-2 - Node2-Err2",
                "Node3 - Node3_Err1", "Node3 - Node3_Err2", "Node3 - Node3_Err3")

        when: "Node Validation Errors need to be built"
        final ErrorResponse errorResponse = validationExceptionMapper.toErrorResponse(new ValidationException(errorsThreeNodes, "Exception message"), "ORDER_PROJECT")
        final Response response = Response.status(errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
        response.getStatus() == 417
        errorResponse.errorDetails.size() == 6
        errorResponse.errorDetails.get(0).contains("Node-1 - Err1")
        errorResponse.errorDetails.get(1).contains("Node-2 - Node2-Err1")
        errorResponse.errorDetails.get(2).contains("Node-2 - Node2-Err2")
        errorResponse.errorDetails.get(3).contains("Node3 - Node3_Err1")
        errorResponse.errorDetails.get(4).contains("Node3 - Node3_Err2")
        errorResponse.errorDetails.get(5).contains("Node3 - Node3_Err3")
    }

    def "Verify Response has Project Validation Error with correct responseCode and content"() {

        given: "Validation Error for invalid Project content"
        List<String> errorsTwoNodes = Arrays.asList("Project Validation Error");

        when: "Project Validation Errors need to be built"
        final ErrorResponse errorResponse = validationExceptionMapper.toErrorResponse(new ValidationException(errorsTwoNodes, "Exception message"), "ORDER_PROJECT")
        final Response response = Response.status(errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
        response.getStatus() == 417
        errorResponse.errorDetails.size() == 1
        errorResponse.errorDetails.get(0).contains("Project Validation Error")
    }

    def "Verify Response is valid when usecase is not order"() {

        when: "Validation Errors need to be built"
        final ErrorResponse errorResponse = validationExceptionMapper.toErrorResponse(new ValidationException(new ArrayList<String>(), "The file type for file siteInstallation is not supported"), "UPLOAD_ARTIFACT")
        final Response response = Response.status(errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
        response.getStatus() == 417
        errorResponse.errorDetails.size() == 0
        errorResponse.errorTitle.contains("The file type for file siteInstallation is not supported")
        errorResponse.errorBody.contains("Suggested Solution")
        errorResponse.errorBody.contains("Fix error(s) and try again")
    }
}
