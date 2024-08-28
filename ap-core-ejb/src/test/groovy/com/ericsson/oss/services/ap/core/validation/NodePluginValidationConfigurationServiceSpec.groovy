/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.validation

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration.NodePluginRestClient
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration.util.NodePluginResponseHandler
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.factory.ValidationDataFactory
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ConfigurationFile
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ValidationData
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ValidationResponse

class NodePluginValidationConfigurationServiceSpec extends CdiSpecification {

    private static final String AP_NODE_FDN = "Project=RadioNodeECTValidSEValidSB,Node=LTE01dg2ERBS00001"
    private static final String NODE_TYPE = "RadioNode"

    @ObjectUnderTest
    private NodePluginValidationConfigurationService validationConfigurationService

    @MockedImplementation
    private ValidationDataFactory mockDataFactory

    @MockedImplementation
    private NodePluginRestClient mockNodePluginRestClient

    @MockedImplementation
    private NodePluginResponseHandler mockResponseHandler

    def "When validate delta request is sent and 200 OK is received then no additional message is created"() {
        given: "Validation data is built successfully"
            def validationData = new ValidationData()
            def preconfigurationFile = new ConfigurationFile()
            preconfigurationFile.setFileName("preconfiguration.xml")
            validationData.setPreconfigurationFile(preconfigurationFile)
            mockDataFactory.createDeltaValidationData(AP_NODE_FDN, NODE_TYPE) >> validationData

        and: "Node plugin validates delta configuration passed and responds 200 OK"
            def validationResponse = new ValidationResponse()
            validationResponse.setStatusCode(200)
            validationResponse.setStatus("SUCCESS")
            mockNodePluginRestClient.sendRequest(validationData, AP_NODE_FDN) >> validationResponse

        and: "Validation response is handled as per success case"
            mockResponseHandler.createMessage(validationResponse, "preconfiguration.xml") >> ""

        when: "Execute validateDeltaConfiguration method"
            def message = validationConfigurationService.validateDeltaConfiguration(AP_NODE_FDN, NODE_TYPE)

        then: "Verify no additional message is created, no exception is thrown"
            message.isEmpty()
            notThrown(Exception)
    }

    def "When validation request is sent and Internal Server Error is received followed by a 200 OK then no exception is thrown"() {
        given: "Validation data is built successfully"
            def validationData = new ValidationData()
            validationData.setPreconfigurationFile(null)
            mockDataFactory.createValidationData(AP_NODE_FDN, NODE_TYPE) >> validationData

        and: "Node plugin responds with 500 Internal Server Error first"
            def validationResponse1 = new ValidationResponse()
            validationResponse1.setStatusCode(500)
            validationResponse1.setStatus("ERROR")
        and: "Node plugin validates configuration passed and responds 200 OK for a retry"
            def validationResponse2 = new ValidationResponse()
            validationResponse2.setStatusCode(200)
            validationResponse2.setStatus("SUCCESS")
            mockNodePluginRestClient.sendRequest(validationData, AP_NODE_FDN) >>> [validationResponse1, validationResponse2]

        and: "Validation response is handled as per success case"
            mockResponseHandler.createMessage(validationResponse2, null) >> ""

        when: "Execute validateConfiguration method"
            def message = validationConfigurationService.validateConfiguration(AP_NODE_FDN, NODE_TYPE)

        then: "Verify no additional message is created, no exception is thrown"
            message.isEmpty()
            notThrown(Exception)
    }
}
