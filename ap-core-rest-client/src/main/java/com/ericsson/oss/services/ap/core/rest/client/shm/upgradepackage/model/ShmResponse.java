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



/**
 * Class containing response of the SHM upgrade package search service.
 * The message contains available supported packages with pakage number and their respective product number and revision
 */
package com.ericsson.oss.services.ap.core.rest.client.shm.upgradepackage.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class ShmResponse {

    public final List<SupportedPackageDetail> supportedPackageDetails;
    public final List<UnSupportedPackageDetail> unSupportedPackageDetails;
    public final List<ExcludedPackageDetail> excludedPackageDetails;

    @JsonCreator
    public ShmResponse(@JsonProperty("supportedPackageDetails") List<SupportedPackageDetail> supportedPackageDetails, @JsonProperty("unSupportedPackageDetails") List<UnSupportedPackageDetail> unSupportedPackageDetails, @JsonProperty("excludedPackageDetails") List<ExcludedPackageDetail> excludedPackageDetails){
        this.supportedPackageDetails = supportedPackageDetails;
        this.unSupportedPackageDetails = unSupportedPackageDetails;
        this.excludedPackageDetails = excludedPackageDetails;
    }

    public List<SupportedPackageDetail> getSupportedPackageDetails() {
        return supportedPackageDetails;
    }
}
