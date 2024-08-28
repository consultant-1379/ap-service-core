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

import com.ericsson.oss.services.ap.core.rest.client.common.model.ErrorDetailsEntity
import com.ericsson.oss.services.ap.core.rest.client.common.model.ResponseEntity
import org.apache.commons.io.IOUtils
import org.apache.http.HttpEntity
import org.apache.http.StatusLine
import spock.lang.Specification

import org.apache.http.HttpResponse

class RestResponseSpec extends Specification {

    def "RestResponse - getDefaultResponseHandler test with empty body"() {

        given: "default handler setup"
        def httpEntity = Mock(HttpEntity)
        httpEntity.getContent() >> IOUtils.toInputStream("{}")
        def statusLine = Mock(StatusLine)
        statusLine.getStatusCode() >> 200
        def response = Mock(HttpResponse)
        response.getStatusLine() >> statusLine
        response.getEntity() >> httpEntity
        def defaultHandler = RestResponse.getDefaultResponseHandler(ResponseEntity, ErrorDetailsEntity)

        when: "empty body is apply to default handler"
        def restResponse = defaultHandler.apply(response)

        then: "REST response is valid and clientIdentifier is null"
        notThrown(IllegalArgumentException)
        restResponse != null
        restResponse.isValid()
        restResponse.getData().isPresent()
        restResponse.getData().get().clientIdentifier == null
    }

    def "RestResponse - getDefaultResponseHandler success test"() {

        given: "default handler setup"
        def httpEntity = Mock(HttpEntity)
        httpEntity.getContent() >> IOUtils.toInputStream("{\"clientIdentifier\":\"client123\"}")
        def statusLine = Mock(StatusLine)
        statusLine.getStatusCode() >> 200
        def response = Mock(HttpResponse)
        response.getStatusLine() >> statusLine
        response.getEntity() >> httpEntity
        def defaultHandler = RestResponse.getDefaultResponseHandler(ResponseEntity, ErrorDetailsEntity)

        when: "specific content is apply to default handler"
        def restResponse = defaultHandler.apply(response)

        then: "REST response is valid and clientIdentifier has a expected value"
        notThrown(IllegalArgumentException)
        restResponse != null
        restResponse.isValid()
        restResponse.getData().isPresent()
        restResponse.getData().get().clientIdentifier == "client123"
    }

    def "RestResponse - getDefaultResponseHandler bad request test"() {

        given: "default handler setup"
        def httpEntity = Mock(HttpEntity)
        httpEntity.getContent() >> IOUtils.toInputStream("{\"userMessage\":\"This is test user message\"}")
        def statusLine = Mock(StatusLine)
        statusLine.getStatusCode() >> 400
        def response = Mock(HttpResponse)
        response.getStatusLine() >> statusLine
        response.getEntity() >> httpEntity
        def defaultHandler = RestResponse.getDefaultResponseHandler(ResponseEntity, ErrorDetailsEntity)

        when: "bad request is apply to default handler"
        def restResponse = defaultHandler.apply(response)

        then: "REST response is valid and userMessage has a expected value"
        notThrown(IllegalArgumentException)
        restResponse != null
        !restResponse.isValid()
        !restResponse.getData().isPresent()
        restResponse.getErrorDetails().isPresent()
        restResponse.getErrorDetails().get().userMessage == "This is test user message"
    }

}
