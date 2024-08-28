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
package com.ericsson.oss.services.ap.core;

import java.util.Map;

import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.ap.api.NotificationService;
import com.ericsson.oss.services.ap.common.workflow.BusinessKeyGenerator;
import com.ericsson.oss.services.wfs.jee.api.WorkflowInstanceServiceLocal;

/**
 * Provides implementation for the methods on the {@link NotificationService} interface.
 */
@Stateless
@Local
@EService
@Asynchronous
public class NotificationServiceBean implements NotificationService {

    @Inject
    private Logger logger;

    @EServiceRef
    private WorkflowInstanceServiceLocal wfsInstanceService;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void sendNotification(final String nodeName, final String messageType, final Map<String, Object> workflowMessageVariables) {
        sendNotificationToWorkflow(nodeName, messageType, workflowMessageVariables);
        logger.info("Send {} notification, nodeName is {} and workflowMessageVariables is {}", messageType, nodeName, workflowMessageVariables);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void sendNotification(final String nodeName, final String messageType) {
        sendNotificationToWorkflow(nodeName, messageType);
        logger.info("Send {} notification, nodeName is {}", messageType, nodeName);
    }

    /**
     * Send notification to workflow.
     *
     * @param nodeName
     *            the node to receive the notification
     * @param messageType
     *            the message type of the notification
     * @param workflowMessageVariables
     *            the workflowMessageVariables
     */
    private void sendNotificationToWorkflow(final String nodeName, final String messageType, final Map<String, Object> workflowMessageVariables) {
        try {
            final String businessKey = BusinessKeyGenerator.generateBusinessKeyFromNodeName(nodeName);
            wfsInstanceService.correlateMessage(messageType, businessKey, workflowMessageVariables);
        } catch (final Exception e) {
            logger.warn("Error correlating the {} notification message for node {} with workflowMessageVariables {}: {}", messageType, nodeName,
                    workflowMessageVariables, e.getMessage(), e);
        }
    }

    /**
     * Send notification to workflow.
     *
     * @param nodeName
     *            the node to receive the notification
     * @param messageType
     *            the message type of the notification
     */
    private void sendNotificationToWorkflow(final String nodeName, final String messageType) {
        try {
            final String businessKey = BusinessKeyGenerator.generateBusinessKeyFromNodeName(nodeName);
            wfsInstanceService.correlateMessage(messageType, businessKey);
        } catch (final Exception e) {
            logger.warn("Error correlating the {} notification message for node {}: {}", messageType, nodeName, e.getMessage(), e);
        }
    }
}
