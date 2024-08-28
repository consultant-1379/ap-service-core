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
 -----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.exception

import com.ericsson.cds.cdi.support.spock.CdiSpecification

/**
 * Unit tests for {@link SWVersionNotMatchException}.
 */
class SWVersionNotMatchExceptionSpec extends CdiSpecification {

    def "Check exception message sets correctly for new exception with message"() {
        when: "Exception created with message"
            Exception e = new SWVersionNotMatchException("Exception message test")

        then: "Exception has expected content"
            e.getMessage() == "Exception message test"
    }

    def "Check exception message and parent exception sets correctly for new exception with message and exception"() {
        given: "Create parent exception for test"
            Exception parentException = new Exception("Parent exception message test");

        when: "Exception created with message and exception"
            Exception e = new SWVersionNotMatchException("Exception message test", parentException)

        then: "Exception has expected content"
            e.getMessage() == "Exception message test"
            e.getCause().getMessage() == "Parent exception message test"
    }
}
