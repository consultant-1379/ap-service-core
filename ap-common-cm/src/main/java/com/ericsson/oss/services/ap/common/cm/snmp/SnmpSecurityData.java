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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;

import com.ericsson.nms.security.nscs.api.enums.SnmpSecurityLevel;

/**
 * This POJO stores the values of SNMP security settings.
 */
public class SnmpSecurityData {
    public static final String NODE_SNMP_INIT_SECURITY = "NODE_SNMP_INIT_SECURITY";
    public static final String NODE_SNMP_SECURITY = "NODE_SNMP_SECURITY";

    private static final String SECURITY_LEVEL_KEY = "securityLevel";
    private static final String AUTH_PROTOCOL_KEY = "authProtocol";
    private static final String AUTH_PSD_KEY = "authPassword";
    private static final String PRIV_PROTOCOL_KEY = "privProtocol";
    private static final String PRIV_PSD_KEY = "privPassword";
    private static final String USER_KEY = "user";

    private final Map<String, String> snmpSecurityDataMap = new HashMap<>();
    private String paramName;
    private String paramUser;

    /**
     * This Constructor composes SNMP security setting with default NO_AUTH_NO_PRIV security level.
     *
     * @param paramName
     *            PIB parameter name for this security setting
     */
    protected SnmpSecurityData(final String paramName) {
        snmpSecurityDataMap.put(SECURITY_LEVEL_KEY, SnmpSecurityLevel.NO_AUTH_NO_PRIV.getSnmpSecurityLevel());
        snmpSecurityDataMap.put(AUTH_PROTOCOL_KEY, "NONE");
        snmpSecurityDataMap.put(AUTH_PSD_KEY, "");
        snmpSecurityDataMap.put(PRIV_PROTOCOL_KEY, "NONE");
        snmpSecurityDataMap.put(PRIV_PSD_KEY, "");
        snmpSecurityDataMap.put(USER_KEY, "");
        this.paramName = paramName;
    }

    /**
     * This Constructor composes SNMP security setting from PIB configuration.
     *
     * @param snmpdata
     *            a String[] snmpdata from PIB configuration that contains details for SNMP security setting
     * @param paramName
     *            PIB parameter name for this security setting
     */
    protected SnmpSecurityData(final String[] snmpdata, final String paramName) {
        this.paramName = paramName;
        for (final String snmpSecurityParm : snmpdata) {
            if (StringUtils.isNotBlank(snmpSecurityParm) && snmpSecurityParm.contains(":")) {
                final String[] fieldPairs = snmpSecurityParm.split(":", 2);
                this.snmpSecurityDataMap.put(fieldPairs[0], fieldPairs[1]);
            }
        }
        paramUser = snmpSecurityDataMap.get(USER_KEY);
    }

    /**
     * This method returns the securityLevel.
     *
     * @return securityLevel
     */
    public String getSecurityLevel() {
        return snmpSecurityDataMap.get(SECURITY_LEVEL_KEY);
    }

    /**
     * This method returns the authProtocol.
     *
     * @return authProtocol
     */
    public String getAuthProtocol() {
        return snmpSecurityDataMap.get(AUTH_PROTOCOL_KEY);
    }

    /**
     * This method returns the authPassword.
     *
     * @return authPassword
     */
    public String getAuthPassword() {
        return snmpSecurityDataMap.get(AUTH_PSD_KEY);
    }

    /**
     * This method sets the authPassword.
     *
     * @param password the authentication password
     */
    public void setAuthPassword(final String password) {
        snmpSecurityDataMap.put(AUTH_PSD_KEY, password);
    }

    /**
     * This method returns the privProtocol.
     *
     * @return privProtocol
     */
    public String getPrivProtocol() {
        return snmpSecurityDataMap.get(PRIV_PROTOCOL_KEY);
    }

    /**
     * This method returns the privPassword.
     *
     * @return the password
     */
    public String getPrivPassword() {
        return snmpSecurityDataMap.get(PRIV_PSD_KEY);
    }

    /**
     * This method sets the PrivPasswor.
     *
     * @param password the authentication password
     */
    public void setPrivPassword(final String password) {
        snmpSecurityDataMap.put(PRIV_PSD_KEY, password);
    }

    /**
     * This method returns the user identifier.
     *
     * @return user identifier
     */
    public String getUser() {
        return snmpSecurityDataMap.get(USER_KEY);
    }

    /**
     * This method sets the user identifier.
     *
     * @param username A unique user identifier
     */
    public void setUser(final String username) {
        snmpSecurityDataMap.put(USER_KEY, username);
    }

    /**
     * This method returns the PIB parameter name for this security setting.
     *
     * @return parameter name
     */
    public String getParamName() {
        return paramName;
    }

    /**
     * This method returns the user in PIB parameter.
     *
     * @return the paramUser
     */
    public String getParamUser() {
        return paramUser;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSecurityLevel(), getAuthProtocol(), getAuthPassword(), getPrivProtocol(), getPrivPassword(), getUser(), paramName);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SnmpSecurityData that = (SnmpSecurityData) o;
        return Objects.equals(paramName, that.paramName)
                && Objects.equals(getUser(), that.getUser())
                && Objects.equals(getSecurityLevel(), that.getSecurityLevel())
                && Objects.equals(getAuthProtocol(), that.getAuthProtocol())
                && Objects.equals(getAuthPassword(), that.getAuthPassword())
                && Objects.equals(getPrivProtocol(), that.getPrivProtocol())
                && Objects.equals(getPrivPassword(), that.getPrivPassword());
    }

    @Override
    public String toString() {
        return paramName +
                ": {securityLevel=" + getSecurityLevel() +
                ", authProtocol=" + getAuthProtocol() +
                ", privProtocol=" + getPrivProtocol() +
                ", user=" + getUser() +
                '}';
    }
}
