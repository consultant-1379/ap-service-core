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
package com.ericsson.oss.services.ap.api.model.eoi;

import java.util.Collections;
import java.util.List;

public class Domain {
    private String domainName;
    private EnrollmentCmpConfig enrollmentCmpConfig;
    private List<TrustedCertificate> trustedCertificates;
    private List<TrustCategory> trustCategories;

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public EnrollmentCmpConfig getEnrollmentCmpConfig() {
        return enrollmentCmpConfig;
    }

    public void setEnrollmentCmpConfig(EnrollmentCmpConfig enrollmentCmpConfig) {
        this.enrollmentCmpConfig = enrollmentCmpConfig;
    }

    public List<TrustedCertificate> getTrustedCertificates()
    {
        return  Collections.unmodifiableList(trustedCertificates);
    }

    public void setTrustedCertificates(List<TrustedCertificate> trustedCertificates) {
        this.trustedCertificates = Collections.unmodifiableList(trustedCertificates);
    }

    public List<TrustCategory> getTrustCategories() {
        return Collections.unmodifiableList(trustCategories);
    }

    public void setTrustCategories(List<TrustCategory> trustCategories)
    {
        this.trustCategories = Collections.unmodifiableList(trustCategories);
    }

    @Override
    public String toString() {
        return "Domain{" +
            "domainName='" + domainName + '\'' +
            ", enrollmentCmpConfig=" + enrollmentCmpConfig +
            ", trustedCertificates=" + trustedCertificates +
            ", trustCategories=" + trustCategories +
            '}';
    }
}
