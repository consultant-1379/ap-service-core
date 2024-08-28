/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.util.alarm;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.eventbus.model.EventSender;
import com.ericsson.oss.itpf.sdk.eventbus.model.classic.EventSenderBean;
import com.ericsson.oss.mediation.translator.model.Constants;
import com.ericsson.oss.mediation.translator.model.EventNotification;
import com.ericsson.oss.mediation.translator.model.EventNotificationBatch;
import com.ericsson.oss.services.fm.service.util.EventNotificationConverter;

/**
 * Sends alarms to FM as modelled events. Alarms will be visible in the FM Alarm Monitoring GUI.
 */
public class AlarmSender {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Sends an alarm to FM. The alarm will be visible in the FM Alarm Monitoring GUI as an alarm record type.
     *
     * @param eventNotification
     *            notification to be sent
     */
    public void sendAlarm(final EventNotification eventNotification) {
        logger.info("Sending alarm to FM {}", eventNotification);
        eventNotification.setRecordType(Constants.NOTIF_TYPE_ALARM);
        send(eventNotification);
    }

    /**
     * Sends an alarm to FM. The alarm will be visible in the FM Alarm Monitoring GUI as an error record type.
     *
     * @param eventNotification
     *            notification to be sent
     */
    public void sendError(final EventNotification eventNotification) {
        logger.info("Sending error to FM {}", eventNotification);
        eventNotification.setRecordType(Constants.NOTIF_TYPE_ERROR);
        send(eventNotification);
    }

    private static void send(final EventNotification eventNotification) {
        final List<EventNotification> eventNotifications = new ArrayList<>(1);
        eventNotifications.add(eventNotification);
        final EventSender<EventNotificationBatch> modeledEventSender = new EventSenderBean<>(EventNotificationBatch.class);

        modeledEventSender.send(EventNotificationConverter.serializeObject(eventNotifications));
    }
}
