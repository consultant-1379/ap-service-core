package com.ericsson.oss.services.ap.core.eoi.templateprocessor

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData
import org.apache.commons.io.IOUtils

import java.nio.charset.StandardCharsets

class EoiSnmpTemplateProcessorSpec extends CdiSpecification{

    @ObjectUnderTest
    EoiSnmpTemplateProcessor eoiSnmpTemplateProcessor;

    @MockedImplementation
    private SnmpSecurityData snmpSecurityData;


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

    def setup() throws IOException {
        final InputStream initialTemplate = getClass().getResourceAsStream(FULL_PROTOCOL_TEMPLATE);
        template = IOUtils.toString(initialTemplate, StandardCharsets.UTF_8);

        snmpSecurityData.getSecurityLevel() >> com.ericsson.nms.security.nscs.api.enums.SnmpSecurityLevel.AUTH_PRIV.getSnmpSecurityLevel()
        snmpSecurityData.getAuthProtocol() >> "NONE";
        snmpSecurityData.getPrivProtocol() >> "NONE";
        snmpSecurityData.getAuthPassword() >> "";
        snmpSecurityData.getPrivPassword() >> "";
        snmpSecurityData.getUser() >> "user";

    }
    def "whenSnmpTemplateProcessorIsCalledSuccessfully"()
    {
        when:
        final String updateSnmpTemplate = eoiSnmpTemplateProcessor.processTemplate(snmpSecurityData, PRECONFIGURATION_FILE_CONTENT);

        then:
        updateSnmpTemplate.contains("\"securitylevel\": \"AUTH_PRIV\"") == true;
    }

    def "whenSnmpTemplateProcessorThrowsException"() {
        when:
        eoiSnmpTemplateProcessor.processTemplate(null, PRECONFIGURATION_FILE_CONTENT);

        then:
        thrown(ApApplicationException)
    }
}
