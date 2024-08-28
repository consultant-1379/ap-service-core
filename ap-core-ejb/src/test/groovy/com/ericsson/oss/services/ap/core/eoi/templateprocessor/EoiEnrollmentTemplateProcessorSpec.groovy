package com.ericsson.oss.services.ap.core.eoi.templateprocessor

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ApApplicationException
import com.ericsson.oss.services.ap.api.model.eoi.Crl
import com.ericsson.oss.services.ap.api.model.eoi.Domain
import com.ericsson.oss.services.ap.api.model.eoi.EnrollmentCmpConfig
import com.ericsson.oss.services.ap.api.model.eoi.EnrollmentConfigurationResponse
import com.ericsson.oss.services.ap.api.model.eoi.TrustCategory
import com.ericsson.oss.services.ap.api.model.eoi.TrustedCertificate
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations
import org.apache.commons.io.IOUtils

import java.nio.charset.StandardCharsets

class EoiEnrollmentTemplateProcessorSpec extends CdiSpecification {

    @ObjectUnderTest
    EoiEnrollmentTemplateProcessor enrollmentTemplateProcessor;

    @MockedImplementation
    ArtifactResourceOperations artifactResourceOperations;

    @MockedImplementation
    EnrollmentCmpConfig enrollmentCmpConfig;

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

    EnrollmentCmpConfig enrollmentCmpConfig1 = new EnrollmentCmpConfig();
    Domain domain=new  Domain();

    def setup() throws IOException {
        final InputStream initialTemplate = getClass().getResourceAsStream(FULL_PROTOCOL_TEMPLATE);
            template = IOUtils.toString(initialTemplate, StandardCharsets.UTF_8);

        Crl crl = new Crl();
        List<Crl> crlList = new ArrayList<>();

        crl.setId("trustcrlid");
        crl.setCdpsUri("trustcdpsuri");
        crlList.add(crl);

        TrustedCertificate trustedCertificate = new TrustedCertificate();
        trustedCertificate.setId("trustiD");
        trustedCertificate.setCaFingerprint("trustFingerprint");
        trustedCertificate.setCaPem("trustedCaPem");
        trustedCertificate.setCaSubjectName("trustCaSubjectName");
        trustedCertificate.setTdpsUri("trustTdpsUri");
        trustedCertificate.setCrls(crlList);

        List<String> certificates = new ArrayList<>();
        certificates.add("certificate1");
        certificates.add("certificate2");

        TrustCategory trustCategory = new TrustCategory();
        trustCategory.setId("categoryId");
        trustCategory.setCertificates(certificates);

        List<TrustedCertificate> trustedCertificates = new ArrayList<>();
        List<TrustCategory> trustCategories = new ArrayList<>();
        enrollmentCmpConfig1.setAlgorithm ( "algorithm" );
        enrollmentCmpConfig1.setCertificateId ( "certid" );
        enrollmentCmpConfig1.setChallengePassword ( "password" );
        enrollmentCmpConfig1.setCmpTrustCategoryId ( "id" );
        enrollmentCmpConfig1.setEnrollmentAuthorityId ( "id" );
        enrollmentCmpConfig1.setEnrollmentAuthorityType ( "type" );
        enrollmentCmpConfig1.setEnrollmentAuthorityName ( "name" );
        enrollmentCmpConfig1.setSubjectName ( "subName" );
        domain.setDomainName ( "domainName" );
        enrollmentCmpConfig1.setEnrollmentServerGroupId ( "grpid" );
        enrollmentCmpConfig1.setUrl ( "url" );
        enrollmentCmpConfig1.setTrustCategoryId ( "id" );
        enrollmentCmpConfig1.setEnrollmentServerId ( "id" );

        trustedCertificates.add(trustedCertificate);
        trustCategories.add(trustCategory);
        domain.setTrustedCertificates(trustedCertificates);
        domain.setTrustCategories(trustCategories);
    }

    def "whenEnrollmentTemplateProcessorIsCalledSuccessfully"()
    {
        given:
        EnrollmentConfigurationResponse enrollmentConfigurationResponse =new EnrollmentConfigurationResponse();
        domain.setEnrollmentCmpConfig(enrollmentCmpConfig1);
        enrollmentConfigurationResponse.setDomain(domain);
        Object response = enrollmentConfigurationResponse;

        when:
        final String updatedEnrollmentTemplate = enrollmentTemplateProcessor.processTemplate(response, PRECONFIGURATION_FILE_CONTENT);

        then:
        updatedEnrollmentTemplate.contains("\"enrollmentAuthorityId\": \"id\"") == true;
    }

    def "whenEnrollmentTemplateProcessorThrowsException"()
    {
        given:
        EnrollmentConfigurationResponse enrollmentConfigurationResponse =new EnrollmentConfigurationResponse();
        enrollmentConfigurationResponse.setDomain(domain);
        Object response = enrollmentConfigurationResponse;

        when:
        enrollmentTemplateProcessor.processTemplate(response, PRECONFIGURATION_FILE_CONTENT);

        then:
        thrown(ApApplicationException)

    }

}
