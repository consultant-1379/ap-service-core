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
package com.ericsson.oss.services.ap.core.rest.model.profile;

/**
 * POJO model for Version object in {@link Profile}.
 */
public class Version {

    private String productNumber;
    private String productRelease;

    public Version(final String productNumber, final String productRelease) {
        this.productNumber = productNumber;
        this.productRelease = productRelease;
    }

    public Version() {
    }

    public String getProductNumber() {
        return productNumber;
    }

    public String getProductRelease() {
        return productRelease;
    }

    public void setProductNumber(final String productNumber) {
        this.productNumber = productNumber;
    }

    public void setProductRelease(final String productRelease) {
        this.productRelease = productRelease;
    }
}
