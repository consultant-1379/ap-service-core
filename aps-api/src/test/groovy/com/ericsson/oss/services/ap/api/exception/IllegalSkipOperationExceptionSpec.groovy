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
 * Unit tests for {@link IllegalSkipOperationException}.
 */
class IllegalSkipOperationExceptionSpec extends CdiSpecification {

    def "Check exception message sets correctly for new exception with message"() {
        given: "Create parent exception for test"
            Exception parentException = new Exception("Parent exception message test")

        when: "Exception created with exception"
            Exception e = new IllegalSkipOperationException(parentException)

        then: "Exception has expected content"
            e.getCause().getMessage() == "Parent exception message test"
    }
}
