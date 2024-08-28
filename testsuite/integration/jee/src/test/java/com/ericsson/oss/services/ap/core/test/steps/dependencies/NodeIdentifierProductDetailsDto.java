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
package com.ericsson.oss.services.ap.core.test.steps.dependencies;

import com.ericsson.oss.services.shm.filestore.swpackage.api.ProductDetailsDto;

/**
 * A concrete implementation of {@link ProductDetailsDto} intended to be used by tests that require that the {@link String} values
 * returned by {@link ProductDetailsDto#getProductNumber()} and {@link ProductDetailsDto#getProductRevision()} can be set at
 * initialization time. All other methods return null objects.
 * <p>
 * {@link ProductDetailsDto#getProductRevision()} and {@link ProductDetailsDto#getProductNumber()} return the parameters of
 * {@link NodeIdentifierProductDetailsDto#getInstance(String, String)}.
 * </p>
 */
public class NodeIdentifierProductDetailsDto implements ProductDetailsDto {

    private final String productNumber;
    private final String productRevision;

    public static ProductDetailsDto getInstance(final String productNumber, final String productRevision) {
        return new NodeIdentifierProductDetailsDto(productNumber, productRevision);
    }

    private NodeIdentifierProductDetailsDto(final String productNumber, final String productRevision) {
        this.productNumber = productNumber;
        this.productRevision = productRevision;
    }

    @Override
    public String getProductName() {
        return "";
    }

    @Override
    public String getProductNumber() {
        return productNumber;
    }

    @Override
    public String getProductRevision() {
        return productRevision;
    }

    @Override
    public String getReleaseDate() {
        return "";
    }

    @Override
    public String getType() {
        return "";
    }

    @Override
    public String getProductDescription() {
        return "";
    }

    @Override
    public String getTechnology() {
        return "";
    }
}
