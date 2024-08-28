/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2019
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client.shm.upgradepackage.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SupportedPackageDetail {
    public final String neType;
    public String getNeType() {
        return neType;
    }

    public List<SoftwarePackageDetail> getSoftwarePackageDetails() {
        return softwarePackageDetails;
    }

    public final List<SoftwarePackageDetail> softwarePackageDetails;

    @JsonCreator
    public SupportedPackageDetail(@JsonProperty("neType") String neType, @JsonProperty("softwarePackageDetails") List<SoftwarePackageDetail> softwarePackageDetails){
        this.neType = neType;
        this.softwarePackageDetails = softwarePackageDetails;
    }
}
