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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class SoftwarePackageDetail {

    public final String packageName;

    @JsonIgnore
    public final UcfFilesList[] ucfFilesList;

    @JsonIgnore
    public final String defaultUcf;

    @JsonIgnore
    public final String message;

    public final List<ProductDetail> productDetails;

    @JsonIgnore
    public final String importedBy;

    @JsonIgnore
    public final long importedDate;

    @JsonCreator
    public SoftwarePackageDetail(
        @JsonProperty("packageName")
            String packageName,
        @JsonProperty("ucfFilesList")
            UcfFilesList[] ucfFilesList,
        @JsonProperty("defaultUcf")
            String defaultUcf,
        @JsonProperty("productDetails")
            List<ProductDetail> productDetails,
        @JsonProperty("importedBy")
            String importedBy,
        @JsonProperty("importedDate")
            long importedDate,
        @JsonProperty("message")
            String message) {
        this.packageName = packageName;
        this.message = message;
        this.ucfFilesList = ucfFilesList;
        this.defaultUcf = defaultUcf;
        this.productDetails = productDetails;
        this.importedBy = importedBy;
        this.importedDate = importedDate;
    }

    public String getPackageName() {
        return packageName;
    }

    public UcfFilesList[] getUcfFilesList() {
        return ucfFilesList;
    }

    public String getDefaultUcf() {
        return defaultUcf;
    }

    public String getMessage() {
        return message;
    }

    public List<ProductDetail> getProductDetails() {
        return productDetails;
    }

    public String getImportedBy() {
        return importedBy;
    }

    public long getImportedDate() {
        return importedDate;
    }

    @JsonIgnoreProperties
    public static final class UcfFilesList {

        @JsonCreator
        public UcfFilesList() {
            //Do nothing
        }
    }


}
