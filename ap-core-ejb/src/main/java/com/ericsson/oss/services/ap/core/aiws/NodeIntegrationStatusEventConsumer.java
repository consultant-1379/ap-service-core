/*------------------------------------------------------------------------------
 ********************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.aiws;

import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.model.MoType.NODE;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.eventbus.model.annotation.Modeled;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.model.autoprovisioning.NodeIntegrationStatusEvent;

import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

/**
 * This class consumes messages sent from the Auto-Integration Web Service (AIWS) when a node
 * contacts the web service to update its integration progress.
 */
@Singleton
@LocalBean
public class NodeIntegrationStatusEventConsumer {

    private static final String NODE_INTEGRATION_STATUS_EVENT_URN = "//global/NodeIntegrationStatusEventChannel/1.0.0";

    @Inject
    protected DpsQueries dpsQueries;

    @Inject
    private NodeIntegrationStatusEventProcessor statusEventProcessor;

    @Inject
    private Logger logger;

    /**
     * Consumes a notification from the AIWS for Node Integration Status Events
     *
     * @param nodeIntegrationStatusEvent
     *            a notification from AIWS that contains details for Node Integration Status Events
     */
    @Lock(LockType.READ)
    @Asynchronous
    public void listenToAiwsNodeIntegrationNotifications(@Observes @Modeled(eventUrn = NODE_INTEGRATION_STATUS_EVENT_URN) final NodeIntegrationStatusEvent nodeIntegrationStatusEvent) {
        final ManagedObject apNodeMo = findApNodeMo(nodeIntegrationStatusEvent);

        if (apNodeMo != null) {
            statusEventProcessor.processNodeIntegrationStatusEvent(apNodeMo, nodeIntegrationStatusEvent);
        }
    }

    private ManagedObject findApNodeMo(final NodeIntegrationStatusEvent nodeIntegrationStatusEvent) {
        final String nodeName = nodeIntegrationStatusEvent.getNodeIdentifier().getName();
        if (StringUtils.isNotBlank(nodeName)) {
            return findMoByNodeName(nodeName);
        }

        final String hardwareSerialNumber = nodeIntegrationStatusEvent.getNodeIdentifier().getSerialNumber();
        if (StringUtils.isNotBlank(hardwareSerialNumber)) {
            return findMoByHardwareSerialNumber(hardwareSerialNumber);
        }
        logger.debug("Ignoring node integration status event notification. No node name or serial number found in event notification");
        return null;
    }

    private ManagedObject findMoByNodeName(final String nodeName) {
        logger.debug("Node integration status event received for node {}", nodeName);
        final Iterator<ManagedObject> apNodeMos = dpsQueries.findMoByName(nodeName, NODE.toString(), AP.toString()).execute();
        return apNodeMos.hasNext() ? apNodeMos.next() : returnNoApModelNodeFound(nodeName);
    }

    private ManagedObject findMoByHardwareSerialNumber(final String serialNumber) {
        logger.debug("Node integration status event received for node with serialNumber {}", serialNumber);
        final Iterator<ManagedObject> apNodeMos = dpsQueries
            .findMosWithAttributeValue(NodeAttribute.HARDWARE_SERIAL_NUMBER.toString(), serialNumber,
                Namespace.AP.toString(), NODE.toString())
            .execute();
        return apNodeMos.hasNext() ? apNodeMos.next() : returnNoApModelNodeFound(serialNumber);
    }

    private ManagedObject returnNoApModelNodeFound(final String nodeAttribute) {
        logger.debug("Ignoring node integration status event notification, {} is not in AP model", nodeAttribute);
        return null;
    }
}
