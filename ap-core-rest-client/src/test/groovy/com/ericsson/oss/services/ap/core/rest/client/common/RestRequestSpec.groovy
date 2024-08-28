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
package com.ericsson.oss.services.ap.core.rest.client.common

import spock.lang.Specification

class RestRequestSpec extends Specification {

    def "RestRequest Builder - success test"() {

        given: "RestRequest setup"
        def restBuilder = RestRequest.Builder.of("http://example.com")

        when: "RestRequest is built"
        def restRequest = restBuilder.build()

        then: "RestRequest is not null"
        restRequest != null
    }

    def "RestRequest Builder - missing 'url' test"() {

        given: "RestRequest setup"
        def restBuilder = RestRequest.Builder.of(null)

        when: "RestRequest is built"
        restBuilder.build()

        then: "Builder throws exception"
        thrown(IllegalArgumentException)
    }

    def "RestRequest Builder - empty 'url' test"() {

        given: "RestRequest setup"
        def restBuilder = RestRequest.Builder.of("")

        when: "RestRequest is built"
        restBuilder.build()

        then: "Builder throws exception"
        thrown(IllegalArgumentException)
    }

}
