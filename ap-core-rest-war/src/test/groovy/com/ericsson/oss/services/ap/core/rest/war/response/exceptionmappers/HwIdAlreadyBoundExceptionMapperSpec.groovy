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
import com.ericsson.oss.services.ap.api.exception.HwIdAlreadyBoundException
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse

/**
 * Unit tests for {@link HwIdAlreadyBoundExceptionMapper}.
 */
class HwIdAlreadyBoundExceptionMapperSpec extends CdiSpecification {

    @ObjectUnderTest
    private HwIdAlreadyBoundExceptionMapper hwIdAlreadyBoundExceptionMapper

    def "Verify Response messages are correctly parsed from HwIdAlreadyBoundException"() {

        when: "HwIdAlreadyBoundException is thrown"
        final ErrorResponse errorResponse = hwIdAlreadyBoundExceptionMapper.toErrorResponse(new HwIdAlreadyBoundException(Arrays.asList("The hardware serial number ABC12345678 is already bound to node Node1"), "message"), "ABC12345678" )
        final Response response = Response.status(errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
        response.getStatus() == 409
        errorResponse.errorTitle == "The hardware serial number ABC12345678 is already bound."
        errorResponse.errorBody == "Suggested Solution : Provide a hardware serial number that is not bound to another node."
    }
}
