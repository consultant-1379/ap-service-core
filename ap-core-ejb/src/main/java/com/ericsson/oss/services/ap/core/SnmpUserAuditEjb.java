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
package com.ericsson.oss.services.ap.core;

import static com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData.NODE_SNMP_INIT_SECURITY;
import static com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData.NODE_SNMP_SECURITY;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.nms.security.nscs.api.enums.SnmpSecurityLevel;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpDataManager;
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpParameterManager;
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.core.rest.client.snmp.SnmpRestClient;

/**
 * Startup bean that handles the creation of a list of SNMP connection ids used to notify node discovery which secure connections can be closed.
 * It adds snmpUsers from all Node Mos which are not UNDEFINED or ECIMUser to the list, and all SnmpTargetV3 Mo users (Users are used to identify
 * connections in node discovery).
 */
@EService
@Startup
@Singleton
public class SnmpUserAuditEjb {

    private static final String SNMPUSER = "snmpUser";
    private static final String USER = "user";
    private static final String ECIMUSER = "ECIMUser";

    private List<String> users;

    @Resource
    private TimerService timerService;

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private SnmpRestClient restClient;

    @Inject
    private Logger logger;

    @Inject
    private SnmpDataManager snmpDataManager;

    @Inject
    private SnmpParameterManager snmpParameterManager;

    /**
     * Initialise the timer service
     */
    @PostConstruct
    public void init() {
        timerService.createCalendarTimer(scheduleTimer());
    }

    /**
     * Method executed on the Timer Service schedule.
     */
    @Timeout
    public void start() {
        logger.info("Executing snmp user audit. Creating a list of active snmp connection ids.");
        createActiveConnectionIdsList();
    }

    /**
     * Creates a schedule set to 02:30 every day for the timer service
     *
     * @return the schedule expression for the timer service
     */
    private ScheduleExpression scheduleTimer() {
        final ScheduleExpression scheduleExpression = new ScheduleExpression();
        scheduleExpression.hour("2");
        scheduleExpression.minute("30");
        scheduleExpression.second("0");
        return scheduleExpression;
    }

    /**
     * Creates a list of all the active connection ids by joining the lists of Node and SnmpTargetV3=1 users.
     * The current NODE_SNMP_INIT_SECURITY and NODE_SNMP_SECURITY users are also added.
     *
     * @return list of active connection ids
     */
    private void createActiveConnectionIdsList() {
        users  = new ArrayList<>();
        users.add(getCurrentNodeSnmpInitSecurityUser());
        users.add(getCurrentNodeSnmpSecurityUser());
        getNodeUsers();
        getSnmpTargetV3Users();

        restClient.sendActiveConnectionIdsToNodeDiscovery(users);
    }

    /**
     * Gets the snmpUsers from all Node MOs and adds them to the list to be sent for audit, excluding users that are UNDEFINED.
     */
    private void getNodeUsers() {
        final String UNDEFINED = "UNDEFINED";
        try {
            final Iterator<ManagedObject> nodeUsers = dpsQueries
                .findMosWithAttribute(SNMPUSER, Namespace.AP.toString(), MoType.NODE.toString()).execute();
            nodeUsers.forEachRemaining(user -> {
                final String userName = user.getAttribute(SNMPUSER).toString();
                if (!userName.equals(UNDEFINED) && !users.contains(userName)) {
                    users.add(userName);
                }
            });
        } catch (final Exception e) {
            logger.warn("Could not retrieve any Node Mo with the snmpUser attribute: {}", e.getMessage());
        }
    }

    /**
     * Gets the users for all SnmpTargetV3=1 MOs and adds them to the list to be sent for audit.
     */
    private void getSnmpTargetV3Users() {
        final String namespace = "RcsSnmp";
        final String moType = "SnmpTargetV3";
        try {
            final Iterator<ManagedObject> moUsers = dpsQueries
                .findMosWithAttribute(USER, namespace, moType).execute();
            moUsers.forEachRemaining(user -> {
                final String userName = user.getAttribute(USER).toString();
                if(!users.contains(userName)) {
                    users.add(userName);
                }
            });
        } catch (final Exception e) {
            logger.warn("Could not retrieve any SnmpTargetV3=1 Mo with the user attribute: {}", e.getMessage());
        }
    }

    /**
     * Get the current NODE_SNMP_INIT_SECURITY user
     *
     * @return the current NODE_SNMP_INIT_SECURITY user
     */
    private String getCurrentNodeSnmpInitSecurityUser() {
        SnmpSecurityData snmpData;
        try{
            snmpData = snmpDataManager.buildSystemParameter(snmpParameterManager.getNodeSnmpInitSecurity(), NODE_SNMP_INIT_SECURITY);
        } catch (final Exception e){
            logger.warn("Could not retrieve NODE_SNMP_INIT_SECURITY parameter: {}", e.getMessage());
            snmpData = snmpDataManager.getDefaultData(NODE_SNMP_INIT_SECURITY);
        }
        return snmpData.getSecurityLevel().equals(SnmpSecurityLevel.NO_AUTH_NO_PRIV.getSnmpSecurityLevel()) ? ECIMUSER : snmpData.getUser();
    }

    /**
     * Get the current NODE_SNMP_SECURITY user
     *
     * @return the current NODE_SNMP_SECURITY user
     */
    private String getCurrentNodeSnmpSecurityUser() {
        SnmpSecurityData snmpData;
        try{
            snmpData = snmpDataManager.buildSystemParameter(snmpParameterManager.getNodeSnmpSecurity(), NODE_SNMP_SECURITY);
        } catch (final Exception e){
            logger.warn("Could not retrieve NODE_SNMP_SECURITY parameter: {}", e.getMessage());
            snmpData = snmpDataManager.getDefaultData(NODE_SNMP_SECURITY);
        }
        return snmpData.getSecurityLevel().equals(SnmpSecurityLevel.NO_AUTH_NO_PRIV.getSnmpSecurityLevel()) ? ECIMUSER : snmpData.getUser();
    }
}
