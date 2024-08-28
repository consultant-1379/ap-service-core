/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.view;

import java.util.List;

/**
 * Contains all the Automatic License Request file data values.
 * This Data will be used to to send to SHM.
 */
public class AutomaticLicenseRequestData {

    private String groupId;
    private String hardwareType;
    private List<String> radioAccessTechnologies;
    private String softwareLicenseTargetId;

    /**
     * Constructor for Automatic License Request Data
     */
    public AutomaticLicenseRequestData() {
        // Instantiates object
    }

    public String getGroupId() {
        return groupId;
    }

    public AutomaticLicenseRequestData setGroupId(final String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getHardwareType() {
        return hardwareType;
    }

    public AutomaticLicenseRequestData setHardwareType(final String hardwareType) {
        this.hardwareType = hardwareType;
        return this;
    }

    public List<String> getRadioAccessTechnologies() {
        List<String> ratValues;
        ratValues = radioAccessTechnologies;
        return ratValues;
    }

    public AutomaticLicenseRequestData setRadioAccessTechnologies(final List<String> ratValues) {
        List<String> radioAccessTechnologyValues;
        radioAccessTechnologyValues = ratValues;
        this.radioAccessTechnologies = radioAccessTechnologyValues;
        return this;
    }

    public String getSoftwareLicenseTargetId() {
        return softwareLicenseTargetId;
    }

    public AutomaticLicenseRequestData setSoftwareLicenseTargetId(final String softwareLicenseTargetId) {
        this.softwareLicenseTargetId = softwareLicenseTargetId;
        return this;
    }
}
