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
package com.ericsson.oss.services.ap.common.cm.snmp;

import static com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData.NODE_SNMP_INIT_SECURITY;
import static com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData.NODE_SNMP_SECURITY;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.config.ConfigurationEnvironment;
import com.ericsson.oss.itpf.sdk.config.ConfigurationPropertyNotFoundException;
import com.ericsson.oss.itpf.sdk.config.ConfigurationTypeConversionException;

/**
 * This class is responsible to read the values of Security SNMP PIB parameters.
 *
 */
@Stateless
public class SnmpParameterManager {

    @Inject
    private ConfigurationEnvironment configEnvironment;

    @Inject
    private Logger logger;

    /**
     * This method returns the NODE_SNMP_SECURITY fetched from PIB configuration.
     *
     * @return NODE_SNMP_SECURITY
     *
     */
    public String[] getNodeSnmpSecurity() {
        return getPibValuesAsStringArray(NODE_SNMP_SECURITY);
    }

    /**
     * This method returns the NODE_SNMP_INIT_SECURITY fetched from PIB configuration.
     *
     * @return NODE_SNMP_INIT_SECURITY
     *
     */
    public String[] getNodeSnmpInitSecurity() {
        return getPibValuesAsStringArray(NODE_SNMP_INIT_SECURITY);
    }

    private String[] getPibValuesAsStringArray(final String paramName) {
        try {
            final String[] pibValues = (String[]) configEnvironment.getValue(paramName);
            logger.debug("Get Configuration Parameter for {} is {}", paramName, new Object[] { pibValues });
            return pibValues;
        } catch (final ConfigurationTypeConversionException | ConfigurationPropertyNotFoundException e) {
            logger.error("Unable to get value of PIB parameter: {}.", paramName, e);
            throw e;
        }
    }

}
