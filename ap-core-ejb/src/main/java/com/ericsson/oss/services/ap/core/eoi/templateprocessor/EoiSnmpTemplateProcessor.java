/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.eoi.templateprocessor;

import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class EoiSnmpTemplateProcessor {

    private static final String SNMP_TEMPLATE = "/templates/generatedEoiJson/EoiSnmpTemplate.json";
    private static final String SNMP_CONFIGURATION_TAG = "\"%EOI_SNMP_CONFIGURATION%\"";
    private static final String EOI_SNMP_SECURITY_LEVEL_TAG = "%EOI_SNMP_SECURITY_LEVEL%";
    private static final String EOI_SNMP_AUTH_PROTOCOL_TAG = "%EOI_SNMP_AUTH_PROTOCOL%";
    private static final String EOI_SNMP_AUTH_PASSWORD_TAG = "%EOI_SNMP_AUTH_PASSWORD%";
    private static final String EOI_SNMP_PRIV_PROTOCOL_TAG = "%EOI_SNMP_PRIV_PROTOCOL%";
    private static final String EOI_SNMP_PRIV_PASSWORD_TAG = "%EOI_SNMP_PRIV_PASSWORD%";
    private static final String EOI_SNMP_USER_TAG = "%EOI_SNMP_USER%";


    public String processTemplate(final SnmpSecurityData snmpSecurityData, final String jsonContent) {

        try (final InputStream initialTemplate = getClass().getResourceAsStream(SNMP_TEMPLATE)) {
            String generatedDay0Template = convertInputStreamToString(initialTemplate);
            String updatedLdapTemplate = updateTemplate(generatedDay0Template, snmpSecurityData);
            return jsonContent.replace(SNMP_CONFIGURATION_TAG, updatedLdapTemplate);
        } catch (final Exception e) {
            throw new ApApplicationException(e.getMessage(), e);
        }
    }

    private String updateTemplate(final String rawTemplate, final SnmpSecurityData snmpSecurityData)
    {
        return rawTemplate.replace(EOI_SNMP_AUTH_PASSWORD_TAG, snmpSecurityData.getAuthPassword())
            .replace(EOI_SNMP_AUTH_PROTOCOL_TAG, snmpSecurityData.getAuthProtocol())
            .replace(EOI_SNMP_SECURITY_LEVEL_TAG, snmpSecurityData.getSecurityLevel())
            .replace(EOI_SNMP_PRIV_PROTOCOL_TAG, snmpSecurityData.getPrivProtocol())
            .replace(EOI_SNMP_PRIV_PASSWORD_TAG, snmpSecurityData.getPrivPassword())
            .replace(EOI_SNMP_USER_TAG, snmpSecurityData.getUser());
    }

    private static String convertInputStreamToString(final InputStream input) throws IOException {
        return IOUtils.toString(input, StandardCharsets.UTF_8);
    }

}
