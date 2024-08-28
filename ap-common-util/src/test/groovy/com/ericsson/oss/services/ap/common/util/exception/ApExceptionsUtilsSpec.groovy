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
package com.ericsson.oss.services.ap.common.util.exception

import com.ericsson.cds.cdi.support.spock.CdiSpecification

class ApExceptionsUtilsSpec extends CdiSpecification {

    def "when throwable has no cause, throwable message is returned"() {
        given:
        final Throwable throwable = new Exception('Expected message')

        when:
        final String rootCause = ApExceptionUtils.getRootCause(throwable)

        then:
        rootCause == 'Expected message'
    }

    def "when no message, Exception name is returned"() {
        given:
        final Throwable throwable = new Exception()

        when:
        final String rootCause = ApExceptionUtils.getRootCause(throwable)

        then:
        rootCause == 'Exception'
    }

    def "when throwable has null cause, throwable message is returned"() {
        given:
        final Throwable throwable = new Exception('Expected message', null)

        when:
        final String rootCause = ApExceptionUtils.getRootCause(throwable)

        then:
        rootCause == 'Expected message'
    }

    def "when throwable has non null cause with message, rooted message is returned"() {
        given:
        final Throwable rootThrowable = new Exception('root message')
        final Throwable throwable = new Exception(rootThrowable)

        when:
        final String rootCause = ApExceptionUtils.getRootCause(throwable)

        then:
        rootCause == 'root message'
    }

    def "when throwable has non null cause with null message, firt throwable message is returned"() {
        given:
        final Throwable rootThrowable = new Exception()
        final Throwable throwable = new Exception('first message', rootThrowable)

        when:
        final String rootCause = ApExceptionUtils.getRootCause(throwable)

        then:
        rootCause == 'first message'
    }

    def "when large stacktrace, last cause with non null message returned"() {
        given:
        final Throwable rootThrowable = new Exception()
        final Throwable interThrowable = new Exception("inter message", rootThrowable)
        final Throwable throwable = new Exception('first message', interThrowable)

        when:
        final String rootCause = ApExceptionUtils.getRootCause(throwable)

        then:
        rootCause == 'inter message'
    }
}
