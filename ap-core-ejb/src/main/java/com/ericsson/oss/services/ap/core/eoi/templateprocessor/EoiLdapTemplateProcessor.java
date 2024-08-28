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
import com.ericsson.oss.services.ap.api.model.eoi.LdapConfigurationResponse;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class EoiLdapTemplateProcessor {

    @Inject
    EoiFmVipTemplateProcessor eoiFmVipTemplateProcessor;

    private static final String FULL_PROTOCOL_TEMPLATE = "/templates/generatedEoiJson/EoiLdapTemplate.json";

    private static final String LDAP_TEMPLATE_TAG = "\"%EOI_LDAP_CONFIGURATION%\"";
    private static final String EOI_TLS_PORT_TAG = "%EOI_TLS_PORT%";
    private static final String EOI_LDAP_PORT_TAG = "%EOI_LDAP_PORT%";
    private static final String EOI_LDAP_IPADDRESS_TAG = "%EOI_LDAP_IPADDRESS%";
    private static final String EOI_FALLBACK_LDAP_IPADDRESS_TAG = "%EOI_FALLBACK_LDAP_IPADDRESS%";
    private static final String EOI_BIND_DN_TAG = "%EOI_BIND_DN%";
    private static final String EOI_BIND_PASSWORD_TAG = "%EOI_BIND_PASSWORD%";
    private static final String EOI_BASE_DN_TAG = "%EOI_BASE_DN%";

    public String processTemplate(final Object response, String jsonContent, final String fmVipAddress) {

        LdapConfigurationResponse ldapConfigurationResponse = (LdapConfigurationResponse) response;

        jsonContent = eoiFmVipTemplateProcessor.processTemplate(jsonContent, fmVipAddress);

        try (final InputStream initialTemplate = getClass().getResourceAsStream(FULL_PROTOCOL_TEMPLATE)) {
            String generatedDay0Template = convertInputStreamToString(initialTemplate);

            final String updatedLdapTemplate = updateTemplate(generatedDay0Template, ldapConfigurationResponse);
            jsonContent = jsonContent.replace(LDAP_TEMPLATE_TAG, updatedLdapTemplate);
            return jsonContent;

        } catch (final Exception e) {
            throw new ApApplicationException(e.getMessage(), e);
        }
    }

    private String updateTemplate(String rawTemplate, LdapConfigurationResponse ldapConfigurationResponse) {
        return rawTemplate.replace(EOI_TLS_PORT_TAG, ldapConfigurationResponse.getTlsPort())
            .replace(EOI_LDAP_PORT_TAG, ldapConfigurationResponse.getLdapsPort())
            .replace(EOI_LDAP_IPADDRESS_TAG, ldapConfigurationResponse.getLdapIpAddress())
            .replace(EOI_FALLBACK_LDAP_IPADDRESS_TAG, ldapConfigurationResponse.getFallbackLdapIpAddress())
            .replace(EOI_BIND_DN_TAG, ldapConfigurationResponse.getBindDn())
            .replace(EOI_BIND_PASSWORD_TAG, ldapConfigurationResponse.getBindPassword())
            .replace(EOI_BASE_DN_TAG, ldapConfigurationResponse.getBaseDn());
    }

    public static String convertInputStreamToString(final InputStream input) throws IOException {
        return IOUtils.toString(input, StandardCharsets.UTF_8);
    }

}
