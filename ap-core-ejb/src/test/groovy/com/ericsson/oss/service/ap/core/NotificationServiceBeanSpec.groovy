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
package com.ericsson.oss.service.ap.core

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.core.NotificationServiceBean
import com.ericsson.oss.services.wfs.api.WorkflowMessageCorrelationException
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal

class NotificationServiceBeanSpec extends CdiSpecification {

    private static final String NODE_ID = "TestNode"
    private static final String PREFIX_NODE_ID = "AP_Node="
    private static final Map<String, Object> workflowMessageVariables = ['filePath':'/mnt/test', 'status':true]

    @ObjectUnderTest
    private NotificationServiceBean notificationService

    @MockedImplementation
    private WorkflowInstanceServiceLocal wfsInstanceService;

    def "Receive a notification with workflowMessageVariables and then send correlateMessage to workflow"  () {

        when: "a notification is received"
        notificationService.sendNotification(NODE_ID, _ as String , workflowMessageVariables)

        then: "a correlateMessage is sent to workflow"
        1 * wfsInstanceService.correlateMessage(_ as String, PREFIX_NODE_ID+NODE_ID, workflowMessageVariables)
    }

    def "Receive a notification and then send correlateMessage to workflow"  () {

        when: "a notification is received"
        notificationService.sendNotification(NODE_ID, _ as String)

        then: "a correlateMessage is sent to workflow"
        1 * wfsInstanceService.correlateMessage(_ as String, PREFIX_NODE_ID+NODE_ID)
    }

    def "When error correlating message with workflowMessageVariables, then exception not propagated" () {

        given: "correlateMessage call throws WorkflowMessageCorrelationException"
        wfsInstanceService.correlateMessage(_ as String, _ as String, _ as Map) >> { throw new WorkflowMessageCorrelationException() }

        when: "a notification is received"
        notificationService.sendNotification(NODE_ID, _ as String , workflowMessageVariables)

        then: "No exception is propagated"
        notThrown(Exception)
    }

    def "When error correlating message, then exception not propagated" () {

        given: "correlateMessage call throws WorkflowMessageCorrelationException"
        wfsInstanceService.correlateMessage(_ as String, _ as String) >> { throw new WorkflowMessageCorrelationException() }

        when: "a notification is received"
        notificationService.sendNotification(NODE_ID, _ as String)

        then: "No exception is propagated"
        notThrown(Exception)
    }
}
