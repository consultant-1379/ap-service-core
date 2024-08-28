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
import com.ericsson.oss.services.ap.core.rest.client.common.model.RequestEntity
import com.ericsson.oss.services.ap.core.rest.client.common.model.ResponseEntity
import com.ericsson.oss.services.ap.core.rest.client.common.utils.SomeException
import com.ericsson.oss.services.ap.core.rest.client.common.utils.SpyHelper
import org.apache.http.entity.ContentType
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import spock.lang.Specification

import java.util.function.Consumer

import static com.ericsson.oss.services.ap.core.rest.client.common.HttpMethods.POST
import static com.ericsson.oss.services.ap.core.rest.client.common.RestResponse.getDefaultResponseHandler
import static com.ericsson.oss.services.ap.core.rest.client.common.utils.JSONData.JSON_BROKEN_RESPONSE_EXAMPLE_2
import static com.ericsson.oss.services.ap.core.rest.client.common.utils.JSONData.JSON_REQUEST_EXAMPLE_1
import static com.ericsson.oss.services.ap.core.rest.client.common.utils.JSONData.JSON_REQUEST_EXAMPLE_2
import static com.ericsson.oss.services.ap.core.rest.client.common.utils.JSONData.JSON_RESPONSE_EXAMPLE_1
import static com.ericsson.oss.services.ap.core.rest.client.common.utils.JSONData.JSON_RESPONSE_NOT_FOUND_ERROR
import static org.mockserver.model.HttpResponse.response

class RestClientSpec extends Specification {

    private static ClientAndServer mockServerClient
    private SpyHelper spyHelper = Mock()

    def setupSpec() {
        mockServerClient = ClientAndServer.startClientAndServer(1080)
    }

    def cleanupSpec() {
        mockServerClient.stop()
    }

    def setup() {
        mockServerClient.reset()
    }

    def prepareMockServer(final String method,
                          final Integer statusCode,
                          final String jsonRequest = "{}",
                          final String jsonResponse = "{}"
    ) {
        mockServerClient
                .when(HttpRequest.request("/test-endpoint")
                        .withMethod(method)
                        .withHeader(Header.header(HttpConstants.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(Header.header(HttpConstants.USERNAME_HEADER, "user1"))
                        .withBody(jsonRequest)
                )
                .respond(response()
                        .withStatusCode(statusCode)
                        .withHeader(Header.header(HttpConstants.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withBody(jsonResponse)
                )
    }

    def "REST client - proper JSON request and response"() {

        given:
        prepareMockServer("POST", 200, JSON_REQUEST_EXAMPLE_1, JSON_RESPONSE_EXAMPLE_1)
        def entity = new RequestEntity("client1", "client1.ericsson.net", "192.168.1.1/24", "192.168.1.10")

        when:
        def clientIdentifier = RestRequest.Builder.of("http://localhost:1080/test-endpoint")
                .setMethod(POST)
                .setEntity(entity)
                .setAuthorization("user1")
                .build()
                .send(getDefaultResponseHandler(ResponseEntity, ErrorDetailsEntity))
                .ifSuccess({ data -> spyHelper.success() } as Consumer<Optional<ResponseEntity>>)
                .ifFailure({ errorDetails -> spyHelper.failure() } as Consumer<Optional<ErrorDetailsEntity>>)
                .getData()
                .map({ it -> it.getClientIdentifier() })
                .orElseThrow({ new SomeException("Problem with parsing JSON") })

        then:
        clientIdentifier == "client1"
        1 * spyHelper.success()
        0 * spyHelper.failure()
    }

    def "REST client - proper JSON request and broken response"() {

        given:
        prepareMockServer("POST", 200, JSON_REQUEST_EXAMPLE_2, JSON_BROKEN_RESPONSE_EXAMPLE_2)
        def entity = new RequestEntity("client1", "client1.ericsson.net", "192.168.1.1/24", "192.168.1.10")

        when:
        RestRequest.Builder.of("http://localhost:1080/test-endpoint")
                .setMethod(POST)
                .setEntity(entity)
                .setAuthorization("user1")
                .build()
                .send(getDefaultResponseHandler(ResponseEntity, ErrorDetailsEntity))
                .ifSuccess({ data -> spyHelper.success() } as Consumer<Optional<ResponseEntity>>)
                .ifFailure({ errorDetails -> spyHelper.failure() } as Consumer<Optional<ErrorDetailsEntity>>)
                .getData()
                .map({ it -> it.getClientIdentifier() })
                .orElseThrow({ new SomeException("Problem with parsing JSON") })

        then:
        thrown(SomeException)
        1 * spyHelper.success()
        0 * spyHelper.failure()
    }

    @SuppressWarnings("squid:S00112")
    def "REST client - proper JSON request and failure response"() {

        given:
        prepareMockServer("POST", 400, JSON_REQUEST_EXAMPLE_2, JSON_RESPONSE_NOT_FOUND_ERROR)
        def entity = new RequestEntity("client1", "client1.ericsson.net", "192.168.1.1/24", "192.168.1.10")

        when:
        RestRequest.Builder.of("http://localhost:1080/test-endpoint")
                .setMethod(POST)
                .setEntity(entity)
                .setAuthorization("user1")
                .build()
                .send(getDefaultResponseHandler(ResponseEntity, ErrorDetailsEntity))
                .ifFailure({ errorDetails -> throw new SomeException(errorDetails.get().getUserMessage()) } as Consumer<Optional<ErrorDetailsEntity>>)
                .getData()
                .get()

        then:
        SomeException exception = thrown()
        exception.getMessage() == "Client does not exist"
    }

}
