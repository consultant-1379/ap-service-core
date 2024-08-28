/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.response.exceptionmappers

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ApServiceException
import com.ericsson.oss.services.ap.api.exception.NodeNotFoundException
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto

class ApServiceExceptionMapperSpec extends CdiSpecification {

    private static final String EXCEPTION_MESSAGE = "Exception Message"

    @ObjectUnderTest
    private ApServiceExceptionMapper apServiceExceptionMapper

    def "ApServiceException thrown with message should have expected params returned with correct status code" () {
        given: "ApServiceException with message"
            final Exception e = new ApServiceException(EXCEPTION_MESSAGE)

        when: "Exception Mapper is called with valid params"
            final CommandResponseDto dto = apServiceExceptionMapper.toCommandResponse("ap bind -n Node", e)

        then: "Params in the Response are as expected"
            dto.getCommand() == "ap bind -n Node"
            dto.getErrorCode() == CliErrorCodes.SERVICE_ERROR_CODE
            dto.getSolution() == "Use Log Viewer for more information"
            dto.getStatusMessage() == "Exception Message"
    }

    def "ApServiceException wrapping another exception should have expected params returned with correct status code" () {
        given: "ApServiceException with message"
            final Exception excption = new ApServiceException(new NodeNotFoundException("Node not found"))

        when: "Exception Mapper is called with valid params"
            final CommandResponseDto dto = apServiceExceptionMapper.toCommandResponse("ap view -n Node",  excption)

        then: "Params in the Response are as expected"
            dto.getCommand() == "ap view -n Node"
            dto.getErrorCode() == CliErrorCodes.SERVICE_ERROR_CODE
            dto.getSolution() == "Use Log Viewer for more information"
            dto.getStatusMessage() == "Node not found"
    }
}
