/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
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
import com.ericsson.oss.services.ap.api.exception.IllegalSkipOperationException
import com.ericsson.oss.services.ap.core.cli.CliErrorCodes
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages
import com.ericsson.oss.services.scriptengine.spi.dtos.CommandResponseDto

/**
 * Unit tests for {@link IllegalSkipOperationExceptionMapper}.
 */
class IllegalSkipOperationExceptionMapperSpec extends CdiSpecification {

    private static final String ERROR_MESSAGE_KEY = "not.waiting.for.skip"
    private static final String SOLUTION_MESSAGE_KEY = "not.waiting.for.skip.solution"
    private final ApMessages apMessages = new ApMessages()

    @ObjectUnderTest
    private IllegalSkipOperationExceptionMapper illegalSkipOperationExceptionMapper

    def "ApServiceException thrown with message should have expected params returned with correct status code" () {
        given: "ApServiceException with message"
            final IllegalSkipOperationException illegalSkipOperationException = new IllegalSkipOperationException(null)

        when: "Exception Mapper is called with valid params"
            final CommandResponseDto dto = illegalSkipOperationExceptionMapper.toCommandResponse("ap skip -n Node", illegalSkipOperationException)

        then: "Params in the Response are as expected"
            dto.getCommand() == "ap skip -n Node"
            dto.getErrorCode() == CliErrorCodes.ILLEGAL_OPERATION_ERROR_CODE
            dto.getSolution() == apMessages.get(SOLUTION_MESSAGE_KEY)
            dto.getStatusMessage() == apMessages.get(ERROR_MESSAGE_KEY)
    }
}
