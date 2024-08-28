/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.artifacts;



/**
 * Java Bean to store Upgrade Package Product Details
 */
public class UpgradePackageProductDetails {

    private String productNumber;
    private String productRevision;

    public void setProductNumber(final String productNumber) {
        this.productNumber = productNumber;
    }

    public void setProductRevision(final String productRevision) {
        this.productRevision = productRevision;
    }

    /**
     * Gets the Upgrade Package Product (CXP) Details
     *
     * @return productNumber
     */
    public String getProductNumber() {
        return productNumber;
    }

    /**
     * Gets the Upgdade Package Product Revision Number (R-State)
     *
     * @return productRevision
     */
    public String getProductRevision() {
        return productRevision;
    }


}
