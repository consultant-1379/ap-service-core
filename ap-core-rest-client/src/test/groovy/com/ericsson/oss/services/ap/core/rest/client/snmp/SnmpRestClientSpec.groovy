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
package com.ericsson.oss.services.ap.core.rest.client.snmp

import org.mockserver.integration.ClientAndServer
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse
import org.slf4j.Logger

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * SnmpRestClientSpec is the test class for the {@link SnmpRestClient}
 */
class SnmpRestClientSpec extends CdiSpecification{

    private static ClientAndServer mockEndpoint
    private static final int PORT = 8106
    private static final String URL = "/nodediscovery/v1/snmp/connections/audit"

    private static final List<String> POST_STATUS_OK = Arrays.asList(
        "snmpUser1",
        "ECIMUser",
        "snmpUser2",
        "snmpUser3",
        "snmpUser4",
        "snmpUser5",
        "snmpUser6",
        "snmpUser7",
        "snmpUser8")

    private static Map<String, Object> auditConnectionRequest = new HashMap<>()

    @ObjectUnderTest
    private final SnmpRestClient restClient

    @MockedImplementation
    private Logger logger

    def setupSpec() {
        System.setProperty("INTERNAL_URL","http://localhost:" + PORT + URL)
        mockEndpoint = ClientAndServer.startClientAndServer(PORT)
        auditConnectionRequest.put("connectionIds", POST_STATUS_OK)
    }

    def setup() {
        restClient.logger = logger
        mockEndpoint.reset()
    }

    def cleanupSpec() {
        mockEndpoint.stop()
    }

    def "When the rest client is called with a correct list of connection ids a call is made to node discovery" () {
        given: "A mocked REST endpoint to receive the POST request, mocked to respond with status 200 OK"
            ObjectMapper objectMapper = new ObjectMapper()
            mockEndpoint.when(
                HttpRequest.request(URL)
                .withMethod("POST")
                .withBody(objectMapper.writeValueAsString(auditConnectionRequest))
                ).respond(
                HttpResponse.response()
                .withStatusCode(200)
                )
        when: "The rest client receives a correct list of connection ids"
            restClient.sendActiveConnectionIdsToNodeDiscovery(POST_STATUS_OK)

        then: "Mocked status 200 response is received and logged correctly"
            1 * logger.info("Active connection ids successfully sent to node discovery for audit: {}", 200)
    }

        def "When the rest client receives an empty list of connection ids, it does not call Node Discovery" () {
            given: "A mocked REST endpoint to receive the POST request, mocked to respond with status 400 BAD REQUEST"
                ObjectMapper objectMapper = new ObjectMapper()
                mockEndpoint.when(
                        HttpRequest.request(URL)
                                .withMethod("POST")
                                .withBody(objectMapper.writeValueAsString(new ArrayList<String>()))
                ).respond(
                        HttpResponse.response()
                                .withStatusCode(400)
                )
            when: "The rest client receives an empty list of connection ids"
                restClient.sendActiveConnectionIdsToNodeDiscovery(new ArrayList<String>())
            then: "Mocked status 400 response is not logged, no ids were sent"
                0 * logger.error("Invalid connection ids where sent to node discovery for audit: {}", 400)
                1 * logger.info("No connection ids to send to Node Discovery for audit")
        }

        def "When the rest client receives an empty list of connection ids, and does call Node Discovery" () {
            given: "A mocked REST endpoint to receive the POST request, mocked to respond with status 400 BAD REQUEST"
                ObjectMapper objectMapper = new ObjectMapper()
                mockEndpoint.when(
                        HttpRequest.request(URL)
                                .withMethod("POST")
                                .withBody(objectMapper.writeValueAsString(auditConnectionRequest))
                ).respond(
                        HttpResponse.response()
                                .withStatusCode(400)
                )
            when: "The rest client is called with an array of users"
                restClient.sendActiveConnectionIdsToNodeDiscovery(POST_STATUS_OK)
            then: "Mocked status 400 response is logged correctly"
                1 * logger.error("Invalid connection ids were sent to node discovery for audit: {}", 400)
                0 * logger.info("No connection ids to send to Node Discovery for audit")
        }

        def "When a call is made to node discovery and an internal server error occurs" () {
            given: "A mocked REST endpoint to receive the POST request, mocked to respond with status 500 Server Error"
                ObjectMapper objectMapper = new ObjectMapper()
                mockEndpoint.when(
                        HttpRequest.request(URL)
                                .withMethod("POST")
                                .withBody(objectMapper.writeValueAsString(auditConnectionRequest))
                ).respond(
                        HttpResponse.response()
                                .withStatusCode(500)
                )
            when: "The rest client is called with an array of users"
                restClient.sendActiveConnectionIdsToNodeDiscovery(POST_STATUS_OK)
            then: "Mocked status 500 response is logged correctly"
                1 * logger.error("Node Discovery internal error occurred during audit: {}", 500)
        }
}
