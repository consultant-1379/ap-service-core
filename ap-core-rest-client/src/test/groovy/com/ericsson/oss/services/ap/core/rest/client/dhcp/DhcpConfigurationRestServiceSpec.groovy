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
package com.ericsson.oss.services.ap.core.rest.client.dhcp

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.itpf.sdk.security.accesscontrol.EAccessControl
import com.ericsson.oss.itpf.sdk.security.accesscontrol.ESecuritySubject
import com.ericsson.oss.services.ap.api.exception.DhcpRestServiceException
import com.ericsson.oss.services.ap.api.model.DhcpConfiguration
import com.ericsson.oss.services.ap.core.rest.client.RestUrls
import com.ericsson.oss.services.ap.core.rest.client.common.HttpConstants
import org.apache.http.entity.ContentType
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpStatusCode

import static org.mockserver.model.HttpResponse.response

class DhcpConfigurationRestServiceSpec extends CdiSpecification {

    private static final String INTERNAL_URL = "INTERNAL_URL"
    private static ClientAndServer mockServerClient
    private static def configurationDhcpService

    private static final String JSON_REQUEST_EXAMPLE_1 = "{" +
            "\"clientIdentifier\":\"client1\"," +
            "\"hostname\":\"client1.ericsson.net\"," +
            "\"fixedAddress\":\"192.168.1.1/24\"," +
            "\"defaultRouter\":\"192.168.1.10\"" +
            "}"

    private static final String JSON_RESPONSE_EXAMPLE_1 = "{" +
            "\"clientIdentifier\": \"client1\"," +
            "\"hostname\": \"client1.ericsson.net\"," +
            "\"fixedAddress\": \"192.168.1.1/24\"," +
            "\"defaultRouter\": \"192.168.1.10\"" +
            "}"

    private static final String JSON_RESPONSE_CLIENT_ALREADY_EXISTS = "{\n" +
            "\"message\": \"Client already exists\",\n" +
            "\"correctiveAction\": \"Provide client with different identifier\",\n" +
            "\"errorCode\": 12345,\n" +
            "\"additionalInformation\": \"Provide another client identifier\"\n" +
            "}"

    private static final String JSON_RESPONSE_NOT_FOUND_ERROR = "{\n" +
            "\"message\": \"Client does not exist\",\n" +
            "\"correctiveAction\": \"Provide client with different identifier\",\n" +
            "\"errorCode\": 12345,\n" +
            "\"additionalInformation\": \"Provide another client identifier\"\n" +
            "}"

    def setupSpec() {
        System.setProperty(INTERNAL_URL, "http://localhost:1080")
        mockServerClient = ClientAndServer.startClientAndServer(1080)
        def eAccessControl = Mock(EAccessControl)
        eAccessControl.getAuthUserSubject() >> new ESecuritySubject("user1")
        configurationDhcpService = new DhcpConfigurationRestService(eAccessControl)
    }

    def cleanupSpec() {
        mockServerClient.stop()
    }

    def setup() {
        mockServerClient.reset()
    }

    def "DHCP configuration service should send proper POST request when adding configuration"() {

        given:
        mockServerClient
                .when(HttpRequest.request(RestUrls.DHCP_CONFIGURATION_SERVICE.getServiceContext())
                        .withMethod("POST")
                        .withHeader(Header.header(HttpConstants.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(Header.header(HttpConstants.USERNAME_HEADER, "user1"))
                        .withBody(JSON_REQUEST_EXAMPLE_1)
                )
                .respond(response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withHeader(Header.header(HttpConstants.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withBody(JSON_RESPONSE_EXAMPLE_1)
                )

        when:
        def dhcpConfiguration = new DhcpConfiguration()
        dhcpConfiguration.setClientIdentifier("client1")
        dhcpConfiguration.setHostname("client1.ericsson.net")
        dhcpConfiguration.setFixedAddress("192.168.1.1/24")
        dhcpConfiguration.setDefaultRouter("192.168.1.10")
        def clientIdentifier = configurationDhcpService.create(dhcpConfiguration)

        then:
        clientIdentifier == "client1"
    }

    def "DHCP configuration service should send proper PUT request when updating configuration"() {

        given:
        mockServerClient
                .when(HttpRequest.request(RestUrls.DHCP_CONFIGURATION_SERVICE.getServiceContext() + "/client0")
                        .withMethod("PUT")
                        .withHeader(Header.header(HttpConstants.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(Header.header(HttpConstants.USERNAME_HEADER, "user1"))
                        .withBody(JSON_REQUEST_EXAMPLE_1)
                )
                .respond(response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withHeader(Header.header(HttpConstants.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withBody(JSON_RESPONSE_EXAMPLE_1)
                )

        when:
        def dhcpConfiguration = new DhcpConfiguration()
        dhcpConfiguration.setClientIdentifier("client1")
        dhcpConfiguration.setHostname("client1.ericsson.net")
        dhcpConfiguration.setFixedAddress("192.168.1.1/24")
        dhcpConfiguration.setDefaultRouter("192.168.1.10")
        def clientIdentifier = configurationDhcpService.update("client0", dhcpConfiguration)

        then:
        clientIdentifier == "client1"
    }

    def "DHCP configuration service should send proper DELETE request when deleting configuration"() {

        given:
        mockServerClient
                .when(HttpRequest.request(RestUrls.DHCP_CONFIGURATION_SERVICE.getServiceContext() + "/client1")
                        .withMethod("DELETE")
                        .withHeader(Header.header(HttpConstants.USERNAME_HEADER, "user1"))
                )
                .respond(response()
                        .withStatusCode(HttpStatusCode.OK_200.code())
                        .withHeader(Header.header(HttpConstants.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.toString()))
                )

        when:
        def result = configurationDhcpService.delete("client1")

        then:
        result
    }

    def "DHCP configuration service should send proper POST request when adding configuration --"() {

        given:
        mockServerClient
                .when(HttpRequest.request(RestUrls.DHCP_CONFIGURATION_SERVICE.getServiceContext())
                        .withMethod("POST")
                        .withHeader(Header.header(HttpConstants.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(Header.header(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withHeader(Header.header(HttpConstants.USERNAME_HEADER, "user1"))
                        .withBody(JSON_REQUEST_EXAMPLE_1)
                )
                .respond(response()
                        .withStatusCode(HttpStatusCode.CONFLICT_409.code())
                        .withHeader(Header.header(HttpConstants.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withBody(JSON_RESPONSE_CLIENT_ALREADY_EXISTS)
                )

        when:
        def dhcpConfiguration = new DhcpConfiguration()
        dhcpConfiguration.setClientIdentifier("client1")
        dhcpConfiguration.setHostname("client1.ericsson.net")
        dhcpConfiguration.setFixedAddress("192.168.1.1/24")
        dhcpConfiguration.setDefaultRouter("192.168.1.10")
        configurationDhcpService.create(dhcpConfiguration)

        then:
        DhcpRestServiceException ex = thrown(DhcpRestServiceException)
        ex.getMessage() == "Client already exists"
    }

    def "DHCP configuration service should send proper DELETE request when deleting configuration --"() {

        given:
        mockServerClient
                .when(HttpRequest.request(RestUrls.DHCP_CONFIGURATION_SERVICE.getServiceContext() + "/client1")
                        .withMethod("DELETE")
                        .withHeader(Header.header(HttpConstants.USERNAME_HEADER, "user1"))
                )
                .respond(response()
                        .withStatusCode(HttpStatusCode.NOT_FOUND_404.code())
                        .withHeader(Header.header(HttpConstants.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.toString()))
                        .withBody(JSON_RESPONSE_NOT_FOUND_ERROR)
                )

        when:
        configurationDhcpService.delete("client1")

        then:
        DhcpRestServiceException ex = thrown(DhcpRestServiceException)
        ex.getMessage() == "Client does not exist"
    }
}
