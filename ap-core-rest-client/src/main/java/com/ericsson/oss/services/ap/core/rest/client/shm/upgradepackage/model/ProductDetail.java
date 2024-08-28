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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductDetail {

    public final String productName;
    public final String productNumber;
    public final String productRevision;

    public String getProductName() {
        return productName;
    }

    public String getProductNumber() {
        return productNumber;
    }

    public String getProductRevision() {
        return productRevision;
    }

    @JsonCreator
    public ProductDetail(
        @JsonProperty("productName")
            String productName,
        @JsonProperty("productNumber")
            String productNumber,
        @JsonProperty("productRevision")
            String productRevision) {
        this.productName = productName;
        this.productNumber = productNumber;
        this.productRevision = productRevision;
    }
}
