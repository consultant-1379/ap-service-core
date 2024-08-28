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

public class EnrollmentCmpConfig{
    private String enrollmentAuthorityId;
    private String enrollmentAuthorityName;
    private String enrollmentAuthorityType;
    private String enrollmentServerGroupId;
    private String enrollmentServerId;
    private String cmpTrustCategoryId;
    private String url;
    private String certificateId;
    private String algorithm;
    private String subjectName;
    private String challengePassword;
    private String trustCategoryId;

    public String getEnrollmentAuthorityId() {
        return enrollmentAuthorityId;
    }

    public void setEnrollmentAuthorityId(String enrollmentAuthorityId) {
        this.enrollmentAuthorityId = enrollmentAuthorityId;
    }

    public String getEnrollmentAuthorityName() {
        return enrollmentAuthorityName;
    }

    public void setEnrollmentAuthorityName(String enrollmentAuthorityName) {
        this.enrollmentAuthorityName = enrollmentAuthorityName;
    }

    public String getEnrollmentAuthorityType() {
        return enrollmentAuthorityType;
    }

    public void setEnrollmentAuthorityType(String enrollmentAuthorityType) {
        this.enrollmentAuthorityType = enrollmentAuthorityType;
    }

    public String getEnrollmentServerGroupId() {
        return enrollmentServerGroupId;
    }

    public void setEnrollmentServerGroupId(String enrollmentServerGroupId) {
        this.enrollmentServerGroupId = enrollmentServerGroupId;
    }

    public String getEnrollmentServerId() {
        return enrollmentServerId;
    }

    public void setEnrollmentServerId(String enrollmentServerId) {
        this.enrollmentServerId = enrollmentServerId;
    }

    public String getCmpTrustCategoryId() {
        return cmpTrustCategoryId;
    }

    public void setCmpTrustCategoryId(String cmpTrustCategoryId) {
        this.cmpTrustCategoryId = cmpTrustCategoryId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getChallengePassword() {
        return challengePassword;
    }

    public void setChallengePassword(String challengePassword) {
        this.challengePassword = challengePassword;
    }

    public String getTrustCategoryId() {
        return trustCategoryId;
    }

    public void setTrustCategoryId(String trustCategoryId) {
        this.trustCategoryId = trustCategoryId;
    }

    @Override
    public String toString() {
        return "EnrollmentCmpConfig{" +
            "enrollmentAuthorityId='" + enrollmentAuthorityId + '\'' +
            ", enrollmentAuthorityName='" + enrollmentAuthorityName + '\'' +
            ", enrollmentAuthorityType='" + enrollmentAuthorityType + '\'' +
            ", enrollmentServerGroupId='" + enrollmentServerGroupId + '\'' +
            ", enrollmentServerId='" + enrollmentServerId + '\'' +
            ", cmpTrustCategoryId='" + cmpTrustCategoryId + '\'' +
            ", url='" + url + '\'' +
            ", certificateId='" + certificateId + '\'' +
            ", algorithm='" + algorithm + '\'' +
            ", subjectName='" + subjectName + '\'' +
            ", challengePassword='" + challengePassword + '\'' +
            ", trustCategoryId='" + trustCategoryId + '\'' +
            '}';
    }
}
