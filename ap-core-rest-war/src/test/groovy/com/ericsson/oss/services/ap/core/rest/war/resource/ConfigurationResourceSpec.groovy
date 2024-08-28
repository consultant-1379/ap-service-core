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
package com.ericsson.oss.services.ap.core.rest.war.resource

import javax.inject.Inject

import org.slf4j.Logger

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.NotificationService
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.common.workflow.messages.GetNodeConfigurationMessage

class ConfigurationResourceSpec extends CdiSpecification {

    private static final String NODE_ID = "TestNode"
    private static GetNodeConfigurationMessage input
    private static final Map<String, Object> additionalInfo = ['result':true, 'additionalInfo':'/mnt/test']
    private static final String GET_CONFIG_MESSAGE = GetNodeConfigurationMessage.getMessageKey()

    @ObjectUnderTest
    ConfigurationResource configurationResource

    @MockedImplementation
    private NotificationService notificationService

    @Inject
    private Logger logger

    def setupSpec() {

        input = new GetNodeConfigurationMessage()
    }

    def "Receive client message and then call notificationService"() {

        when: "the message has correct parameters"
        input.setAdditionalInfo("/mnt/test")
        input.setResult(true)
        def response = configurationResource.putConfiguration(input, NODE_ID)

        then: "notficationService is called"
        1 * notificationService.sendNotification(NODE_ID, GET_CONFIG_MESSAGE , additionalInfo)

        and: "the HTTP Status should be 200 OK"
        response.status == 200

        and: "the response entity should not be returned"
        response.entity == null
    }

    def "Return failure when exception is thrown"() {
        given: "throws exception when notificationService is called"
        notificationService.sendNotification(_ as String, _ as String, _ as Map) >>
        {throw new ApApplicationException("Invalid request")}

        when: "The endpoint is called"
        input.setAdditionalInfo("/mnt/test")
        input.setResult(true)
        def response = configurationResource.putConfiguration(input, NODE_ID)

        then: "bad request is returned"
        1 * logger.warn(_ as String, _ as String, _ as String, _ as Exception)
        notThrown(Exception)
        response.status != 200
    }
}
