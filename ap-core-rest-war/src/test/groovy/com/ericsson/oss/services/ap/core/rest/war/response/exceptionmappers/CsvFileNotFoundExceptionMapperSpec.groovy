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
import com.ericsson.oss.services.ap.api.exception.CsvFileNotFoundException
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse

class CsvFileNotFoundExceptionMapperSpec extends CdiSpecification {

    @ObjectUnderTest
    private CsvFileNotFoundExceptionMapper exceptionMapper

    def "Verify Response messages are correctly parsed from ApNodeExistsException"() {

        when: "CsvFileNotFoundException is thrown"
        final ErrorResponse errorResponse = exceptionMapper.toErrorResponse(new CsvFileNotFoundException("Node1", "ErrorCsv.csv"), "ORDER_PROJECT")
        final Response response = Response.status(errorResponse.getHttpResponseStatus()).entity(errorResponse).build()

        then: "correct format is output"
        response.getStatus() == 500
        errorResponse.errorTitle == "csv substitution file is not found in project."
        errorResponse.errorBody == "Suggested Solution : Please include a csv file and retry."
    }
}
