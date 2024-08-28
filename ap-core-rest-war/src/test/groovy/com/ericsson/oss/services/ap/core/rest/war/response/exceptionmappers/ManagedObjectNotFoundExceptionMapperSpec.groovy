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
import com.ericsson.oss.services.ap.api.exception.ManagedObjectNotFoundException
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse

class ManagedObjectNotFoundExceptionMapperSpec extends CdiSpecification {

    @ObjectUnderTest
    private ManagedObjectNotFoundExceptionMapper exceptionMapper

    def "Verify Response messages are correctly parsed from ManagedObjectNotFoundException"() {

        when: "ManagedObjectNotFoundException is thrown"
             final ErrorResponse errorResponse = exceptionMapper.toErrorResponse(new ManagedObjectNotFoundException("ConnectivityInformation", "Node1"), "")
             final Response response = Response.status(errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
             response.getStatus() == 404
             errorResponse.errorTitle == "No ConnectivityInformation MO exists in the database for node Node1."
             errorResponse.errorBody == "Suggested Solution : No solution is available as critical data is missing from the database for this node."
    }
}
