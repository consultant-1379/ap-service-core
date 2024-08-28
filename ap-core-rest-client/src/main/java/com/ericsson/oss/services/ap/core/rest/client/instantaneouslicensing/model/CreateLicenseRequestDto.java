/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing.model;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for the request data sent to SHM for the request to create a license
 */
public class CreateLicenseRequestDto {

    private String fingerprint;
    private String swltId;
    private String softwarePackageName;
    private String hardwareType;
    private List<String> radioAccessTechnologies;
    private String groupId;

    public CreateLicenseRequestDto(){}

    public CreateLicenseRequestDto(final String fingerprint, final String swltId, final String softwarePackageName,
                                   final String hardwareType, final List<String> radioAccessTechnologies, final String groupId) {
        this.fingerprint = fingerprint;
        this.swltId = swltId;
        this.softwarePackageName = softwarePackageName;
        this.hardwareType = hardwareType;
        this.radioAccessTechnologies = new ArrayList<>(radioAccessTechnologies);
        this.groupId = groupId;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(final String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getSwltId() {
        return swltId;
    }

    public void setSwltId(final String swltId) {
        this.swltId = swltId;
    }

    public String getSoftwarePackageName() {
        return softwarePackageName;
    }

    public void setSoftwarePackageName(final String softwarePackageName) {
        this.softwarePackageName = softwarePackageName;
    }

    public String getHardwareType() {
        return hardwareType;
    }

    public void setHardwareType(final String hardwareType) {
        this.hardwareType = hardwareType;
    }

    public List<String> getRadioAccessTechnologies() {
        return new ArrayList<>(radioAccessTechnologies);
    }

    public void setRadioAccessTechnologies(final List<String> radioAccessTechnologies) {
        this.radioAccessTechnologies =  new ArrayList<>(radioAccessTechnologies);
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(final String groupId) {
        this.groupId = groupId;
    }
}
