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
package com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration

import javax.inject.Inject

import org.apache.http.entity.ContentType
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Header
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration.util.NodePluginCapability
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ConfigurationFile
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ValidationData
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ValidationResponse
import com.fasterxml.jackson.databind.ObjectMapper

import spock.lang.Subject

class NodePluginRestClientSpec extends CdiSpecification {

    private static final String NODE_FDN = "Project=Project1,Node=Node1"

    @Subject
    @Inject
    NodePluginRestClient nodePluginRestClient

    private NodePluginValidationConfigurationDataProvider validationConfigurationDataProvider = new NodePluginValidationConfigurationDataProvider()

    private static String productNumber = "CXP9024418"
    private static String INTERNAL_URL = "INTERNAL_URL"
    private static String protocol = "http"
    private static String hostname = "localhost"
    private static int port = 1080
    private static String path = "/ect/ai/v1/{nodeType}/validate"
    private static String delta_path = "/ect/ai/v1/{nodeType}/validateDelta"
    private static String pingPath = "/ect/ai/{nodeType}"

    static {
        // Run this block before any constructor
        System.setProperty(INTERNAL_URL, protocol + "://" + hostname + ":" + port)
    }

    def "When all files are validated using a real input then response object is what expected and no exception is thrown"() {
        given: "Validation Data is built with upgrade package data"
        ValidationData validationData = new ValidationData()
        validationData.setProductNumber(productNumber)
        validationData.setNodeType("nodeType")
        validationData.setRevision("revision")

        and: "Configuration Files are built"
        List<ConfigurationFile> configurationFiles = new ArrayList<ConfigurationFile>()

        ConfigurationFile configurationFile1 = new ConfigurationFile()
        configurationFile1.setFileContent("<some>content</some>")
        configurationFile1.setFileName("filename")
        configurationFiles.add(configurationFile1)

        ConfigurationFile configurationFile2 = new ConfigurationFile()
        configurationFile2.setFileContent("<some>content</some>")
        configurationFile2.setFileName("filename")
        configurationFiles.add(configurationFile2)

        validationData.setConfigurationFiles(configurationFiles)

        and: "The expected ValidationResponse object"
        ObjectMapper mapper = new ObjectMapper()
        ValidationResponse expectedValidationResponse = mapper.readValue(validationConfigurationDataProvider.jsonAllFileAreValidated, ValidationResponse.class)

        and: "POST request to validate given files are mocked to respond with status OK and no error"
        ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
        clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(path.replace("{nodeType}", validationData.getNodeType()))
                        .withHeader(Header.header("Content-Type", "multipart/form-data; boundary=.*"))
        ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withHeaders(
                                Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                        )
                        .withBody(
                                validationConfigurationDataProvider.jsonAllFileAreValidated
                        )
        )

        when: "The http request has been sent and the response has been processed"
        ValidationResponse validationResponse = nodePluginRestClient.sendRequest(validationData, NODE_FDN)

        then: "The object mapped from the response is what is expected"
        validationResponse == expectedValidationResponse

        cleanup:
        clientAndServer.stop()
    }

    def "When all files are validated then response object is what expected and no exception is thrown"() {
        given: "Stubbed validation data"
        ValidationData emptyInputObject = Stub(ValidationData)
        emptyInputObject.getNodeType() >> "MSRBS_V1"

        and: "An expected ValidationResponse object"
        ObjectMapper mapper = new ObjectMapper()
        ValidationResponse expectedValidationResponse = mapper.readValue(validationConfigurationDataProvider.jsonAllFileAreValidated, ValidationResponse.class)

        and: "POST request to validate given files are mocked to respond with status OK and no error"
        ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
        clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(path.replace("{nodeType}", emptyInputObject.getNodeType()))
                        .withHeader(Header.header("Content-Type", "multipart/form-data; boundary=.*"))
        ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withHeaders(
                                Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                        )
                        .withBody(
                                validationConfigurationDataProvider.jsonAllFileAreValidated
                        )
        )

        when: "The http request has been sent and the response has been processed"
        ValidationResponse validationResponse = nodePluginRestClient.sendRequest(emptyInputObject, NODE_FDN)

        then: "The object mapped from the response is what is expected"
        validationResponse == expectedValidationResponse

        cleanup:
        if (clientAndServer != null) {
            clientAndServer.stop()
        }
    }

    def "When all files are validated with preconfiguration then response object is what expected and no exception is thrown"() {
        given: "Stubbed validation data"
        ValidationData emptyInputObject = Stub(ValidationData)
        emptyInputObject.getNodeType() >> "RadioNode"
        def preconfigurationFile = new ConfigurationFile()
        preconfigurationFile.setFileName("preconfiguration.xml")
        preconfigurationFile.setFileContent("<ManagedElement>content</ManagedElement>")
        emptyInputObject.getPreconfigurationFile() >> preconfigurationFile
        emptyInputObject.isValidateDelta() >> true

        and: "An expected ValidationResponse object"
        ObjectMapper mapper = new ObjectMapper()
        ValidationResponse expectedValidationResponse = mapper.readValue(validationConfigurationDataProvider.jsonAllFileAreValidated, ValidationResponse.class)

        and: "POST request to validate given files are mocked to respond with status OK and no error"
        ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
        clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(delta_path.replace("{nodeType}", emptyInputObject.getNodeType()))
                        .withHeader(Header.header("Content-Type", "multipart/form-data; boundary=.*"))
        ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withHeaders(
                                Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                        )
                        .withBody(
                                validationConfigurationDataProvider.jsonAllFileAreValidated
                        )
        )

        when: "The http request has been sent and the response has been processed"
        ValidationResponse validationResponse = nodePluginRestClient.sendRequest(emptyInputObject, NODE_FDN)

        then: "The object mapped from the response is what is expected"
        validationResponse == expectedValidationResponse

        cleanup:
        if (clientAndServer != null) {
            clientAndServer.stop()
        }
    }

    def "When not all files are validated then response object is what expected and no exception is thrown"() {
        given: "Stubbed validation data"
        ValidationData emptyInputObject = Stub(ValidationData)
        emptyInputObject.getNodeType() >> "vTIF"
        emptyInputObject.isValidateDelta() >> false

        and: "An expected ValidationResponse object"
        ObjectMapper mapper = new ObjectMapper()
        ValidationResponse expectedValidationResponse = mapper.readValue(validationConfigurationDataProvider.jsonNotAllFileAreValidated, ValidationResponse.class)

        and: "POST request to validate given files are mocked to respond with status OK and no error"
        ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
        clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(path.replace("{nodeType}", emptyInputObject.getNodeType()))
                        .withHeader(Header.header("Content-Type", "multipart/form-data; boundary=.*"))
        ).respond(
                HttpResponse.response()
                        .withStatusCode(200)
                        .withHeaders(
                                Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                        )
                        .withBody(
                                validationConfigurationDataProvider.jsonNotAllFileAreValidated
                        )
        )

        when: "The http request has been sent and the response has been processed"
        ValidationResponse validationResponse = nodePluginRestClient.sendRequest(emptyInputObject, NODE_FDN)

        then: "The object mapped from the response is what is expected"
        validationResponse == expectedValidationResponse

        cleanup:
        clientAndServer.stop()
    }

    def "When Internal Server error performs then the response returns failed but no exception is thrown"() {
        given: "Stubbed validation data"
        ValidationData emptyInputObject = Stub(ValidationData)
        emptyInputObject.getNodeType() >> "RadioNode"

        and: "An expected ValidationResponse object"
        ValidationResponse expectedValidationResponse = new ValidationResponse()
        expectedValidationResponse.setStatus("FAILED")

        and: "POST request to validate given files are mocked to respond with status 500 error"
        ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
        clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(path.replace("{nodeType}", emptyInputObject.getNodeType()))
                        .withHeader(Header.header("Content-Type", "multipart/form-data; boundary=.*"))
        ).respond(
                HttpResponse.response()
                        .withStatusCode(500)
                        .withHeaders(
                                Header.header("Content-Type", ContentType.TEXT_HTML.getMimeType()),
                        )
        )

        when: "The http request has been sent and the response has been processed"
        ValidationResponse validationResponse = nodePluginRestClient.sendRequest(emptyInputObject, NODE_FDN)

        then: "The object mapped from the response is what is expected"
        validationResponse == expectedValidationResponse

        cleanup:
        clientAndServer.stop()
    }

    def "When Internal Server error performs for delta validation then the response returns failed but no exception is thrown"() {
        given: "Stubbed validation data"
        ValidationData emptyInputObject = Stub(ValidationData)
        emptyInputObject.getNodeType() >> "RadioNode"
        def preconfigurationFile = new ConfigurationFile()
        preconfigurationFile.setFileName("preconfiguration.xml")
        preconfigurationFile.setFileContent("<ManagedElement>content</ManagedElement>")
        emptyInputObject.getPreconfigurationFile() >> preconfigurationFile
        emptyInputObject.isValidateDelta() >> true

        and: "An expected ValidationResponse object"
        ValidationResponse expectedValidationResponse = new ValidationResponse()
        expectedValidationResponse.setStatus("FAILED")

        and: "POST request to validate given files are mocked to respond with status 500 error"
        ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
        clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(delta_path.replace("{nodeType}", emptyInputObject.getNodeType()))
                        .withHeader(Header.header("Content-Type", "multipart/form-data; boundary=.*"))
        ).respond(
                HttpResponse.response()
                        .withStatusCode(500)
                        .withHeaders(
                                Header.header("Content-Type", ContentType.TEXT_HTML.getMimeType()),
                        )
        )

        when: "The http request has been sent and the response has been processed"
        ValidationResponse validationResponse = nodePluginRestClient.sendRequest(emptyInputObject, NODE_FDN)

        then: "The object mapped from the response is what is expected"
        validationResponse == expectedValidationResponse

        cleanup:
        clientAndServer.stop()
    }

    def "When Service Unavailable error performs then the response returns failed then exception is thrown"() {
        given: "Stubbed validation data"
        ValidationData emptyInputObject = Stub(ValidationData)
        emptyInputObject.getNodeType() >> "RadioNode"
        emptyInputObject.isValidateDelta() >> false

        and: "An expected ValidationResponse object"
        ValidationResponse expectedValidationResponse = new ValidationResponse()
        expectedValidationResponse.setStatus("FAILED")

        and: "POST request to validate given files are mocked to respond with status 503 error"
        ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
        clientAndServer.when(
                HttpRequest.request()
                        .withMethod("POST")
                        .withPath(path.replace("{nodeType}", emptyInputObject.getNodeType()))
                        .withHeader(Header.header("Content-Type", "multipart/form-data; boundary=.*"))
        ).respond(
                HttpResponse.response()
                        .withStatusCode(503)
                        .withHeaders(
                                Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                        )
        )

        when: "The http request has been sent"
        nodePluginRestClient.sendRequest(emptyInputObject, NODE_FDN)

        then: "ApApplicationException is thrown"
        thrown(ApApplicationException)

        cleanup:
        clientAndServer.stop()
    }

    def "When Gateway Timeout error occurs then the response returns failed then exception is thrown"() {
        given: "Stubbed validation data"
            ValidationData emptyInputObject = Stub(ValidationData)
            emptyInputObject.getNodeType() >> "RadioNode"
            emptyInputObject.isValidateDelta() >> true
            def preconfigurationFile = new ConfigurationFile()
            preconfigurationFile.setFileName("preconfiguration.xml")
            preconfigurationFile.setFileContent("<ManagedElement>content</ManagedElement>")
            emptyInputObject.getPreconfigurationFile() >> preconfigurationFile

        and: "POST request to validate given files are mocked to respond with status 504 error"
            ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
            clientAndServer.when(
                    HttpRequest.request()
                            .withMethod("POST")
                            .withPath(delta_path.replace("{nodeType}", emptyInputObject.getNodeType()))
                            .withHeader(Header.header("Content-Type", "multipart/form-data; boundary=.*"))
            ).respond(
                    HttpResponse.response()
                            .withStatusCode(504)
                            .withHeaders(
                                    Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                            )
            )

        when: "The http request has been sent"
            nodePluginRestClient.sendRequest(emptyInputObject, NODE_FDN)

        then: "ApApplicationException is thrown"
            ApApplicationException exception = thrown()
            exception.getMessage().toString().contains("Failed to validate NETCONF files. Timeout.")

        cleanup:
            clientAndServer.stop()
    }

    def "When send a ping request for Nodeplugin capability for RadioNode then valid response is returned and no exception is thrown"() {
        given: "GET request to validate Nodeplugin capability is mocked to respond with status OK and no error"
            ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
            clientAndServer.when(
                HttpRequest.request()
                           .withMethod("GET")
                           .withPath(pingPath.replace("{nodeType}", "RadioNode"))
                ).respond(
                           HttpResponse.response()
                                        .withStatusCode(200)
                                        .withHeaders(
                                             Header.header("Content-Type", ContentType.APPLICATION_JSON.getMimeType()),
                                        )
                                        .withBody(
                                             validationConfigurationDataProvider.jsonCapabililties
                                        )
                )

        when: "The http request has been sent and the response has been processed"
            List<NodePluginCapability> capabilities = nodePluginRestClient.getCapabilities("RadioNode");

        then: "The object mapped from the response is what is expected"
            compareList(capabilities,buildexpectedECTValidationResponse());

        cleanup:
            clientAndServer.stop()
    }

    def "When send a ping request for Nodeplugin capability for RadioNode then invalid response is returned and no exception is thrown"() {
        given: "GET request to validate Nodeplugin capability is mocked to respond with 404"
            ClientAndServer clientAndServer = ClientAndServer.startClientAndServer(port)
            clientAndServer.when(
                HttpRequest.request()
                           .withMethod("GET")
                           .withPath(pingPath.replace("{nodeType}", "RadioNode"))
                ).respond(
                           HttpResponse.response()
                                        .withStatusCode(404)
                                        .withHeaders(
                                             Header.header("Content-Type", ContentType.TEXT_HTML.getMimeType()),
                                        )
                )

        when: "The http request has been sent and the response has been processed"
            List<NodePluginCapability> capabilities = nodePluginRestClient.getCapabilities("RadioNode");

        then: "Empty list is returned"
            capabilities == Collections.emptyList();

        cleanup:
            clientAndServer.stop()
    }

    List<NodePluginCapability> buildexpectedECTValidationResponse(){
        final List<NodePluginCapability> capabilityList = new ArrayList<>();
        final List<String> capabilityForV1 = [
            "VALIDATEDELTA",
            "VALIDATE",
            "EDIT",
            "GENERATE"
        ];
        final List<String> capabilityForV0 = ["VALIDATE"];
        final NodePluginCapability capability1 = new NodePluginCapability();
        final NodePluginCapability capability2 = new NodePluginCapability();
        capability1.setVersionOfInterface("v0");
        capability1.setCapabilities(capabilityForV0);
        capabilityList.add(capability1);
        capability2.setVersionOfInterface("v1");
        capability2.setCapabilities(capabilityForV1);
        capability2.setApplicationUri("configuration-generator");
        capabilityList.add(capability2);
        return capabilityList;
    }

    boolean compareList(final List<NodePluginCapability> list1,final List<NodePluginCapability> list2){
        if (list1.size() == list2.size()){
            for(int i=0;i<list1.size();i++){
                if(!list1.get(i).toString().equals(list2.get(i).toString())){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
