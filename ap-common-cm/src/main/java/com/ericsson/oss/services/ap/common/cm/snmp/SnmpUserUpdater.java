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
package com.ericsson.oss.services.ap.common.cm.snmp;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.nms.security.nscs.api.enums.SnmpSecurityLevel;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;

/**
 * SnmpUserUpdater is used to update the snmpUser attributes of Node Mos to the appropriate user during the auto integration flow.
 */
public class SnmpUserUpdater {

    private static final String SNMPUSER = "snmpUser";
    private static final String UNDEFINED = "UNDEFINED";
    private static final String ECIMUSER = "ECIMUser";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private DpsOperations dps;

    /**
     * Sets the snmpUser attribute of a node to undefined for the given node fdn
     *
     * @param nodeFdn
     *         the node fdn for the node to be updated
     */
    public void setNodeSnmpUserToUndefined(final String nodeFdn) {
        if(apNodeExists(nodeFdn)) {
            getDpsOperations().updateMo(nodeFdn, SNMPUSER, UNDEFINED);
            logger.info("Successfully updated snmpUser attribute for node {}", nodeFdn);
        }
    }

    /**
     * Sets the snmpUser attribute of a node to the NODE_SNMP_INIT_SECURITY user, using the given node fdn and Security data.
     *
     * @param nodeFdn
     *          the node fdn for the node to be updated
     * @param snmpData
     *          the node snmp init security data
     */
    public void setNodeSnmpUserToNodeSnmpInitSecurity(final String nodeFdn, final SnmpSecurityData snmpData) {
        if(apNodeExists(nodeFdn)) {
            final String user = snmpData.getSecurityLevel().equals(SnmpSecurityLevel.NO_AUTH_NO_PRIV.getSnmpSecurityLevel()) ? ECIMUSER : snmpData.getUser();
            try {
                getDpsOperations().updateMo(nodeFdn, SNMPUSER, user);
                logger.info("Successfully updated snmpUser attribute for node {} to {}", nodeFdn, user);
            } catch (final Exception e) {
                logger.warn("Failed to update attribute for Node {} . Node snmpUser not set: {} ", nodeFdn, e.getMessage());
            }
        }
    }

    /**
     * Checks if a node exists with the given fdn
     *
     * @param apNodeFdn
     *          the fdn to check
     * @return true is the node exists and false if not
     */
    private boolean apNodeExists(final String apNodeFdn) {
        if (!getDpsOperations().existsMoByFdn(apNodeFdn)) {
            logger.warn("Cannot find the Node MO for {} in the database.", apNodeFdn);
            return false;
        }
        return true;
    }

    private DpsOperations getDpsOperations() {
        if (dps == null) {
            dps = new DpsOperations();
        }
        return dps;
    }
}
