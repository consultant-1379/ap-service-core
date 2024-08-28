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
package com.ericsson.oss.services.ap.core.rest.client.nodeplugin.configuration.util

import javax.inject.Inject

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.core.rest.client.nodeplugin.model.ValidationResponse
import com.fasterxml.jackson.databind.ObjectMapper

import spock.lang.Subject

class NodePluginResponseHandlerSpec extends CdiSpecification {

    @Subject
    @Inject
    NodePluginResponseHandler nodePluginResponseHandler

    @Inject
    NodePluginMockResponseDataProvider dataProvider

    @Inject
    ObjectMapper mapper

    private static final String EMPTY_STRING = ""
    private static final String NODE_PLUGIN_VALIDATION_FAILURE_MESSAGE = "Unable to perform additional validation on NETCONF files";
    private static final String NODE_PLUGIN_VALIDATION_UNKNOWN_STATUS = "Failure to validate the configuration files (NETCONF). " +
            "Please check the logs for additional information. Validation status received : INVALID"
    private static final String NODE_PLUGIN_VALIDATION_ERROR_STATUS = "No matching Mom found for request."
    private static final String PRECONFIGURATION_FILE_NAME = "preconfiguration_LTE01dg2ERBS00001.xml"

    def "When the response object is given with SUCCESS status then the appropriate message for Additional Info is created and no exception is thrown"() {
        given: "The ValidationResponse object"
        ValidationResponse validationResponse = mapper.readValue(dataProvider.jsonAllFileAreValidatedWithSuccess, ValidationResponse.class)

        and: "The expected Additional Info message"
        String expectedAdditionalInfoMessage = EMPTY_STRING

        when: "The validationResponse object is provided for the appropriate creation of the message"
        String additionalInfoMessage = nodePluginResponseHandler.createMessage(validationResponse, null)

        then: "The additional info is what is expected"
        expectedAdditionalInfoMessage.equals(additionalInfoMessage)
    }

    def "When the response object is given with WARNING status then the appropriate message for Additional Info is created and no exception is thrown"() {
        given: "The ValidationResponse object"
        ValidationResponse validationResponse = mapper.readValue(dataProvider.jsonNotAllFileAreValidatedWithWarning, ValidationResponse.class)

        and: "The expected Additional Info message"
        String expectedAdditionalInfoMessage = "File: siteEquipment.xml, Validation Result: WARNING, Errors :"

        when: "The validationResponse object is provided for the appropriate creation of the message"
        String additionalInfoMessage = nodePluginResponseHandler.createMessage(validationResponse, null)

        then: "The additional info is what is expected"
        additionalInfoMessage.contains(expectedAdditionalInfoMessage)
    }

    def "When the response object is given with FAILED status then the appropriate message for Additional Info is created and exception is thrown"() {
        given: "The ValidationResponse object"
        ValidationResponse validationResponse = mapper.readValue(dataProvider.jsonNotAllFileAreValidatedWithFailure, ValidationResponse.class)

        and: "The expected Additional Info message"
        String expectedAdditionalInfoMessage = "File: siteEquipment.xml, Validation Result: WARNING, Errors : \n" +
                "Some validation warning here \n" +
                " ***  File: Radio.xml, Validation Result: FAILED, Errors : \n" +
                "some xml syntax error in line 5; \n" +
                "some xml syntax error in line 15 \n"

        when: "The validationResponse object is provided for the appropriate creation of the message"
        nodePluginResponseHandler.createMessage(validationResponse, null)

        then: "Exception is thrown"
        ApApplicationException ex = thrown()

        and: " The exception message is as expected"
        ex.getMessage().equals(expectedAdditionalInfoMessage)

    }

    def "When the response object is given with an UNKNOWN status an exception is thrown with a specific message"() {
        given: "The ValidationResponse object"
        ValidationResponse validationResponse = mapper.readValue(dataProvider.jsonWithUnknownStatus, ValidationResponse.class)

        and: "The expected Additional Info message"
        String expectedAdditionalInfoMessage = NODE_PLUGIN_VALIDATION_UNKNOWN_STATUS

        when: "The validationResponse object is provided for the appropriate creation of the message"
        nodePluginResponseHandler.createMessage(validationResponse, null)

        then: "Exception is thrown"
        ApApplicationException ex = thrown()

        and: " The exception message is as expected"
        ex.getMessage().equals(expectedAdditionalInfoMessage)
    }

    def "When the response object is constructed with an ERROR status an exception is thrown with a specific message"() {
        given: "The ValidationResponse object is constructed through ErrorResponse object"
        ValidationResponse validationResponse = new ValidationResponse()
        String responseString = dataProvider.jsonErrorResponse
        nodePluginResponseHandler.validationResponseConvert(validationResponse, responseString)

        and: "The expected Additional Info message is the content of  Error JSON response."
        String expectedAdditionalInfoMessage = NODE_PLUGIN_VALIDATION_ERROR_STATUS

        when: "The validationResponse object is provided for the appropriate creation of the message"
        nodePluginResponseHandler.createMessage(validationResponse, null)

        then: "Exception is thrown"
        ApApplicationException ex = thrown()

        and: " The exception message is as expected"
        ex.getMessage().equals(expectedAdditionalInfoMessage)
    }

    def "When the response object is given with multiple messages then the appropriate message for Additional Info is created and no exception is thrown"() {
        given: "The ValidationResponse object"
        ValidationResponse validationResponse = mapper.readValue(dataProvider.jsonNotAllFileAreValidatedWithMultipleWarnings, ValidationResponse.class)

        and: "The expected Additional Info message"
        String expectedAdditionalInfoMessage = "File: siteEquipment.xml, Validation Result: WARNING, Errors : \n" +
                "Some validation warning here; \n" +
                "Other validation warning here \n"

        when: "The validationResponse object is provided for the appropriate creation of the message"
        String additionalInfoMessage = nodePluginResponseHandler.createMessage(validationResponse, null)

        then: "The additional info is what is expected"
        additionalInfoMessage.equals(expectedAdditionalInfoMessage)
    }

    def "When the response object is given with empty validation results an exception is thrown with a specific message"() {
        given: "The ValidationResponse object"
        ValidationResponse validationResponse = mapper.readValue(dataProvider.jsonNotAllFileAreValidatedWithNoDetails, ValidationResponse.class)

        and: "The expected Additional Info message"
        String expectedAdditionalInfoMessage = NODE_PLUGIN_VALIDATION_FAILURE_MESSAGE

        when: "The validationResponse object is provided for the appropriate creation of the message"
        nodePluginResponseHandler.createMessage(validationResponse, null)

        then: "Exception is thrown"
        ApApplicationException ex = thrown()

        and: " The exception message is as expected"
        ex.getMessage().equals(expectedAdditionalInfoMessage)
    }

    def "When the response object is given with WARNING status for preconfiguration then the appropriate message for Additional Info is created and no exception is thrown"() {
        given: "The ValidationResponse object"
        ValidationResponse validationResponse = mapper.readValue(dataProvider.jsonPreconfigurationIsValidatedWithWarning, ValidationResponse.class)

        and: "The expected Additional Info message"
        String expectedAdditionalInfoMessage = EMPTY_STRING

        when: "The validationResponse object is provided for the appropriate creation of the message"
        String additionalInfoMessage = nodePluginResponseHandler.createMessage(validationResponse, PRECONFIGURATION_FILE_NAME)

        then: "The additional info is what is expected"
        expectedAdditionalInfoMessage.equals(additionalInfoMessage)
    }

    def "When the response object is given with WARNING status for all files then the appropriate message for Additional Info is created and no exception is thrown"() {
        given: "The ValidationResponse object"
        ValidationResponse validationResponse = mapper.readValue(dataProvider.jsonAllFileAreValidatedWithWarning, ValidationResponse.class)

        and: "The expected Additional Info message"
        String expectedAdditionalInfoMessage = "File: radio.xml, Validation Result: WARNING, Errors : \n" +
                 "Some validation warning here \n"

        when: "The validationResponse object is provided for the appropriate creation of the message"
        String additionalInfoMessage = nodePluginResponseHandler.createMessage(validationResponse, PRECONFIGURATION_FILE_NAME)

        then: "The additional info is what is expected"
        expectedAdditionalInfoMessage.equals(additionalInfoMessage)
    }
}
