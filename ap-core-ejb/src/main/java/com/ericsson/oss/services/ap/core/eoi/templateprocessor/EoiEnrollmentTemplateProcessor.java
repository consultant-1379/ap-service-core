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
import com.ericsson.oss.services.ap.api.model.eoi.Crl;
import com.ericsson.oss.services.ap.api.model.eoi.EnrollmentConfigurationResponse;
import com.ericsson.oss.services.ap.api.model.eoi.TrustCategory;
import com.ericsson.oss.services.ap.api.model.eoi.TrustedCertificate;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class EoiEnrollmentTemplateProcessor {


    private static final String FULL_PROTOCOL_TEMPLATE = "/templates/generatedEoiJson/EoiEnrollmentTemplate.json";

    private static final String DOMAIN_NAME_TAG = "%EOI_DOMAIN_NAME%";
    private static final String ENROLLMENT_TAG_NAME = "\"%EOI_ENROLLMENT_CONFIGURATION%\"";
    private static final String ENROLLMENT_AUTHORITY_ID_TAG = "%ENROLLMENT_AUTHORITY_ID%";
    private static final String ENROLLMENT_AUTHORITY_NAME_TAG = "%ENROLLMENT_AUTHORITY_NAME%";
    private static final String ENROLLMENT_AUTHORITY_TYPE_TAG = "%ENROLLMENT_AUTHORITY_TYPE%";
    private static final String ENROLLMENT_SERVER_GROUP_ID = "%ENROLLMENT_SERVER_GROUP_ID%";
    private static final String ENROLLMENT_SERVER_ID_TAG = "%ENROLLMENT_SERVER_ID%";
    private static final String CMP_TRUST_CATEGORY_ID_TAG = "%CMP_TRUST_CATEGORY_ID%";
    private static final String ENROLLMENT_CMP_URL_TAG = "%ENROLLMENT_CMP_URL%";
    private static final String CERTIFICATE_ID_TAG = "%CERTIFICATE_ID%";
    private static final String ENROLLMENT_CMP_ALGORITHM = "%ENROLLMENT_CMP_ALGORITHM%";
    private static final String ENROLLMENT_CMP_SUBJECTNAME_TAG = "%ENROLLMENT_CMP_SUBJECTNAME%";
    private static final String ENROLLMENT_CMP_CHALLANGE_PASSWORD_TAG = "%ENROLLMENT_CMP_CHALLANGE_PASSWORD%";
    private static final String ENROLLMENT_CMP_TRUST_CATEGORY_ID_TAG = "%ENROLLMENT_CMP_TRUST_CATEGORY_ID%";

    private static final String EOI_TRUSTED_CERTIFICATES_TAG_NAME = "\"%EOI_TRUSTED_CERTIFICATES%\"";
    private static final String EOI_TRUST_CERT_ID_TAG = "%EOI_TRUST_CERT_ID%";
    private static final String TRUST_CERT_CA_SUBJECT_NAME_TAG = "%TRUST_CERT_CA_SUBJECT_NAME%";
    private static final String TRUST_CERT_CA_FINGERPRINT_TAG = "%TRUST_CERT_CA_FINGERPRINT%";
    private static final String TRUST_CERT_TDPS_URI_TAG = "%TRUST_CERT_TDPS_URI%";
    private static final String TRUST_CERT_CA_PEM_TAG = "%TRUST_CERT_CA_PEM%";
    private static final String TRUST_CERT_CRLS_TAG = "\"%TRUST_CERT_CRLS%\"";
    private static final String TRUSTED_CERTIFICATES_TEMPLATE = "/templates/generatedEoiJson/EoiTrustCertificateTemplate.json";

    private static final String CRL_CDPS_URI_TAG = "%CRL_CDPS_URI%";
    private static final String CRL_ID_TAG = "%CRL_ID%";
    private static final String TRUSTED_CRL_TEMPLATE = "/templates/generatedEoiJson/EoiTrustCertsCrlsTemplate.json";

    private static final String EOI_TRUSTED_CATEGORIES_TAG_NAME = "\"%EOI_TRUSTED_CATEGORIES%\"";
    private static final String TRUST_CATEGORY_CERTIFICATES_TAG = "%TRUST_CATEGORY_CERTIFICATES%";
    private static final String EOI_TRUST_CATEGORY_ID_TAG = "%EOI_TRUST_CATEGORY_ID%";
    private static final String TRUSTED_CATEGORIES_TEMPLATE = "/templates/generatedEoiJson/EoiTrustCategoriesTemplate.json";

    public String processTemplate(final Object response, String jsonContent) {

        EnrollmentConfigurationResponse enrollmentConfigurationResponse = (EnrollmentConfigurationResponse) response;

        return extractTrustCertsAndCat(replaceDomainTag(jsonContent), enrollmentConfigurationResponse);
    }

    private static String replaceDomainTag(String jsonContent) {
        return jsonContent.replace(DOMAIN_NAME_TAG, "OAM");
    }

    private String extractTrustCertsAndCat(String jsonContent, EnrollmentConfigurationResponse enrollmentConfigurationResponse) {
        try (final InputStream initialTemplate = getClass().getResourceAsStream(FULL_PROTOCOL_TEMPLATE)) {
            String generatedDay0Template = convertInputStreamToString(initialTemplate);
            String updatedEnrollmentTemplate = updateTemplate(generatedDay0Template, enrollmentConfigurationResponse);
            return jsonContent.replace(ENROLLMENT_TAG_NAME, updatedEnrollmentTemplate)
                .replace(EOI_TRUSTED_CERTIFICATES_TAG_NAME, updateTrustDetails(enrollmentConfigurationResponse))
                .replace(EOI_TRUSTED_CATEGORIES_TAG_NAME, updateTrustCategories(enrollmentConfigurationResponse));
        } catch (final Exception e) {
            throw new ApApplicationException(e.getMessage(), e);
        }
    }

    private String updateTemplate(final String rawTemplate, EnrollmentConfigurationResponse enrollmentConfigurationResponse) {
        return rawTemplate.replace(ENROLLMENT_AUTHORITY_ID_TAG, enrollmentConfigurationResponse.getDomain().getEnrollmentCmpConfig().getEnrollmentAuthorityId())
            .replace(ENROLLMENT_AUTHORITY_NAME_TAG, enrollmentConfigurationResponse.getDomain().getEnrollmentCmpConfig().getEnrollmentAuthorityName())
            .replace(ENROLLMENT_AUTHORITY_TYPE_TAG, enrollmentConfigurationResponse.getDomain().getEnrollmentCmpConfig().getEnrollmentAuthorityType())
            .replace(ENROLLMENT_SERVER_GROUP_ID, enrollmentConfigurationResponse.getDomain().getEnrollmentCmpConfig().getEnrollmentServerGroupId())
            .replace(ENROLLMENT_SERVER_ID_TAG, enrollmentConfigurationResponse.getDomain().getEnrollmentCmpConfig().getEnrollmentServerId())
            .replace(CMP_TRUST_CATEGORY_ID_TAG, enrollmentConfigurationResponse.getDomain().getEnrollmentCmpConfig().getCmpTrustCategoryId())
            .replace(ENROLLMENT_CMP_URL_TAG, enrollmentConfigurationResponse.getDomain().getEnrollmentCmpConfig().getUrl())
            .replace(CERTIFICATE_ID_TAG, enrollmentConfigurationResponse.getDomain().getEnrollmentCmpConfig().getCertificateId())
            .replace(ENROLLMENT_CMP_ALGORITHM, enrollmentConfigurationResponse.getDomain().getEnrollmentCmpConfig().getAlgorithm())
            .replace(ENROLLMENT_CMP_SUBJECTNAME_TAG, enrollmentConfigurationResponse.getDomain().getEnrollmentCmpConfig().getSubjectName())
            .replace(ENROLLMENT_CMP_CHALLANGE_PASSWORD_TAG, enrollmentConfigurationResponse.getDomain().getEnrollmentCmpConfig().getChallengePassword())
            .replace(ENROLLMENT_CMP_TRUST_CATEGORY_ID_TAG, enrollmentConfigurationResponse.getDomain().getEnrollmentCmpConfig().getCmpTrustCategoryId());
    }

    private String updateTrustDetails(EnrollmentConfigurationResponse enrollmentConfigurationResponse) {
        // Updating Trust Certificates

        final StringBuilder certificateList = new StringBuilder();
        for (final TrustedCertificate trustedCertificate : enrollmentConfigurationResponse.getDomain().getTrustedCertificates()) {
            String trustedCertificatesTemplate = configureTrustedCertificates(trustedCertificate);

            StringBuilder crlBuilder = new StringBuilder();
            for (final Crl crl : trustedCertificate.getCrls()) {
                crlConfiguration(crl, crlBuilder);
            }
            if(!trustedCertificate.getCrls().isEmpty()) {
                crlBuilder.deleteCharAt(crlBuilder.length() - 1);
            }
            certificateList.append(extractTrustCertCrlTag(trustedCertificatesTemplate, crlBuilder)).append(",");
        }
        if(!enrollmentConfigurationResponse.getDomain().getTrustedCertificates().isEmpty()) {
            certificateList.deleteCharAt(certificateList.length() - 1);
        }
        return certificateList.toString();
    }

    private String extractTrustCertCrlTag(final String trustedCertificatesTemplate, StringBuilder crlBuilder) {
        return trustedCertificatesTemplate.replace(TRUST_CERT_CRLS_TAG, crlBuilder.toString());
    }

    private String configureTrustedCertificates(TrustedCertificate trustedCertificate) {
        String trustedCertificatesTemplate = readTemplate(TRUSTED_CERTIFICATES_TEMPLATE);

        return trustedCertificatesTemplate.replace(EOI_TRUST_CERT_ID_TAG, trustedCertificate.getId())
            .replace(TRUST_CERT_CA_SUBJECT_NAME_TAG, trustedCertificate.getCaSubjectName())
            .replace(TRUST_CERT_CA_FINGERPRINT_TAG, trustedCertificate.getCaFingerprint())
            .replace(TRUST_CERT_TDPS_URI_TAG, trustedCertificate.getTdpsUri())
            .replace(TRUST_CERT_CA_PEM_TAG, trustedCertificate.getCaPem());

    }

    private void crlConfiguration(Crl crl, StringBuilder crlBuilder) {
        crlBuilder.append(replaceCrlTags(crl)).append(",");
    }

    private String replaceCrlTags(final Crl crl) {
        final String trustedCrlTemplate = readTemplate(TRUSTED_CRL_TEMPLATE);
        return trustedCrlTemplate.replace(CRL_ID_TAG, crl.getId()).replace(CRL_CDPS_URI_TAG, crl.getCdpsUri());
    }

    //    Updating Trust Categories
    private String updateTrustCategories( EnrollmentConfigurationResponse enrollmentConfigurationResponse) {
        final StringBuilder trustCertificateList = new StringBuilder();

        for (final TrustCategory trustCategory : enrollmentConfigurationResponse.getDomain().getTrustCategories()) {
            String trustedCategoryTemplate = readTemplate(TRUSTED_CATEGORIES_TEMPLATE)
                .replace(EOI_TRUST_CATEGORY_ID_TAG, trustCategory.getId());

            final StringBuilder trustCertificates = new StringBuilder();
            for (final String certificate : trustCategory.getCertificates()) {
                trustCertificates.append(certificate).append(",");
            }
            if(!trustCategory.getCertificates().isEmpty()) {
                trustCertificates.deleteCharAt(trustCertificates.length() - 1);
            }
            trustCertificateList.append(replaceTrustCatCertTag(trustedCategoryTemplate, trustCertificates)).append(",");
        }
        trustCertificateList.deleteCharAt(trustCertificateList.length() - 1);
        return trustCertificateList.toString();
    }

    private String replaceTrustCatCertTag(String trustedCategoryTemplate, StringBuilder trustCertificates) {
        return  trustedCategoryTemplate.replace(TRUST_CATEGORY_CERTIFICATES_TAG, trustCertificates);
    }


    public static String convertInputStreamToString(final InputStream input) throws IOException {
        return IOUtils.toString(input, StandardCharsets.UTF_8);
    }

    private String readTemplate(final String templateName) {
        try (final InputStream trustedCertEnrollmentInputStream = getClass().getResourceAsStream(templateName)) {
            return convertInputStreamToString(trustedCertEnrollmentInputStream);
        } catch (final IOException e) {
            throw new ApApplicationException(e);
        }
    }

}
