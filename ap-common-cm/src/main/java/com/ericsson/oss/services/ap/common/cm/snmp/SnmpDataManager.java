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
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;

import com.ericsson.oss.itpf.security.cryptography.CryptographyService;

/**
 * This class helps to build the SNMP configuration setting from PIB parameter.
 */
public class SnmpDataManager {

    @Inject
    private CryptographyService cryptographyService;

    /**
     * This method transforms the security parameter data from PIB into {@link SnmpSecurityData}
     *
     * @param securitySettings
     *            read from PIB NODE_SNMP_INIT_SECURITY or NODE_SNMP_SECURITY
     * @param paramName
     *            PIB parameter name for this security setting
     *
     * @return {@link SnmpSecurityData} with settings read from PIB parameter
     */
    public SnmpSecurityData buildSystemParameter(final String[] securitySettings, final String paramName) {
        final SnmpSecurityData securityData = new SnmpSecurityData(securitySettings, paramName);

        final String authPassword = securityData.getAuthPassword();
        final String privPassword = securityData.getPrivPassword();
        if (StringUtils.isNotEmpty(authPassword)) {
            securityData.setAuthPassword(decrypt(authPassword));
        }

        if (StringUtils.isNotEmpty(privPassword)) {
            securityData.setPrivPassword(decrypt(privPassword));
        }

        securityData.setUser(generateUsername(securityData));

        return securityData;
    }

    /**
     * This method returns the default SNMP security settings - NO_AUTH_NO_PRIV.
     *
     * @param paramName
     *            PIB parameter name for this security setting
     *
     * @return {@link SnmpSecurityData} with default settings
     *
     */
    public SnmpSecurityData getDefaultData(final String paramName) {
        return new SnmpSecurityData(paramName);
    }

    private String decrypt(final String encryptedStr) {
        final byte[] encodedData = DatatypeConverter.parseBase64Binary(encryptedStr);
        return new String(cryptographyService.decrypt(encodedData));
    }

    private String generateUsername(final SnmpSecurityData securityData) {
        return securityData.getUser() + Integer.toHexString(securityData.hashCode());
    }
}
