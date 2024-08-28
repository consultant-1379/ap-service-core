package com.ericsson.oss.services.ap.core.eoi.templateprocessor

import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.api.model.eoi.LdapConfigurationResponse
import org.apache.commons.io.IOUtils

import java.nio.charset.StandardCharsets

class EoiLdapTemplateProcessorSpec extends CdiSpecification {

    @ObjectUnderTest
    EoiLdapTemplateProcessor eoiLdapTemplateProcessor;


    private String template;

    private static final String FULL_PROTOCOL_TEMPLATE = "/templates/generatedEoiJson/EoiEnrollmentTemplate.json";
    private static final String PRECONFIGURATION_FILE_CONTENT = "  \"NetworkElement\": {\n" +
            "    \"NodeName\": \"%EOI_NODE_NAME%\",\n" +
            "    \"Enrollment\": {\n" +
            "      \"domain\": {\n" +
            "        \"domainName\": \"%EOI_DOMAIN_NAME%\",\n" +
            "        \"enrollmentCmpConfig\": \"%EOI_ENROLLMENT_CONFIGURATION%\" ,\n" +
            "        \"trustedCertificates\": \"%EOI_TRUSTED_CERTIFICATES%\"   ,\n" +
            "        \"trustCategories\": \"%EOI_TRUSTED_CATEGORIES%\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"LDAP\":   \"%EOI_LDAP_CONFIGURATION%\" ,\n" +
            "    \"FMVIP\":  \"%EOI_FM_VIP_CONFIGURATION%\" ,\n" +
            "    \"SNMP\":  \"%EOI_SNMP_CONFIGURATION%\"\n" +
            "  }";

    LdapConfigurationResponse ldapConfigurationResponse = new LdapConfigurationResponse();

    final String fmVipAddress = "192.168.100.111";
    def setup() throws IOException {
        final InputStream initialTemplate = getClass().getResourceAsStream(FULL_PROTOCOL_TEMPLATE);
        template = IOUtils.toString(initialTemplate, StandardCharsets.UTF_8);

        ldapConfigurationResponse.setTlsPort("tlsport")
        ldapConfigurationResponse.getTlsPort()
        ldapConfigurationResponse.setLdapsPort("ldapport")
        ldapConfigurationResponse.getLdapsPort()
        ldapConfigurationResponse.setLdapIpAddress("1.1.1.1")
        ldapConfigurationResponse.getLdapIpAddress()
        ldapConfigurationResponse.setFallbackLdapIpAddress("0.0.0.0")
        ldapConfigurationResponse.getFallbackLdapIpAddress()
        ldapConfigurationResponse.setBindDn("binddn")
        ldapConfigurationResponse.getBindDn()
        ldapConfigurationResponse.setBindPassword("bindPass")
        ldapConfigurationResponse.getBindPassword()
        ldapConfigurationResponse.setBaseDn("basedn")
        ldapConfigurationResponse.getBaseDn()
    }

    def "whenLdapTemplateProcessorIsCalledSuccessfully"()
    {
        given:
        Object response = ldapConfigurationResponse;

        when:
        final String updateLdapTemplate = eoiLdapTemplateProcessor.processTemplate(response, PRECONFIGURATION_FILE_CONTENT, fmVipAddress);

        then:
        updateLdapTemplate.contains("\"tlsPort\": \"tlsport\"") == true;
    }

    def "whenLdapTemplateProcessorThrowsException"()
    {
        when:
        eoiLdapTemplateProcessor.processTemplate(null, PRECONFIGURATION_FILE_CONTENT, fmVipAddress);

        then:
        thrown(ApApplicationException)
    }

    def "whenFmVipTemplateProcessorThrowsException"()
    {
        given:
        Object response = ldapConfigurationResponse;

        when:
        eoiLdapTemplateProcessor.processTemplate(response, PRECONFIGURATION_FILE_CONTENT, null);

        then:
        thrown(ApApplicationException)
    }

}
