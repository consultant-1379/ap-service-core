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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.ericsson.oss.services.shm.filestore.swpackage.api.ActivityDetailsDto;
import com.ericsson.oss.services.shm.filestore.swpackage.api.ManagedElementTypeDto;
import com.ericsson.oss.services.shm.filestore.swpackage.api.ParameterDto;
import com.ericsson.oss.services.shm.filestore.swpackage.api.ProductDetailsDto;
import com.ericsson.oss.services.shm.filestore.swpackage.api.SoftwarePackageDto;

/**
 * A concrete implementation of {@link SoftwarePackageDto} intended to be used by tests that require that the {@code List<ProductDetailsDto>} returned
 * by {@link SoftwarePackageDto#getProductDetails()} contains a single {@link ProductDetailsDto} whose {@link ProductDetailsDto#getProductRevision()}
 * and {@link ProductDetailsDto#getProductNumber()} return values are set at initialization time.
 * <p>
 * {@link ProductDetailsDto#getProductNumber()} and {@link ProductDetailsDto#getProductRevision()} return the CXP Number and R-State extracted from
 * the {@code String} argument of {@link NodeIdentifierSoftwarePackageDto#getInstance(String)} according to the following mapping:
 * {@code upgradePackage: XXX_YYY_ZZZ => productNumber: XXX/YYY, productRevision: ZZZ}.
 * </p>
 */
public class NodeIdentifierSoftwarePackageDto implements SoftwarePackageDto {

    private final String productNumber;
    private final String productRevision;

    public static SoftwarePackageDto getInstance(final String upgradePackage) {
        final String[] upgradePackageSplits = upgradePackage.split("_");
        final String productNumber = upgradePackageSplits[0] + "/" + upgradePackageSplits[1];
        final String productRevision = upgradePackageSplits[2];
        return new NodeIdentifierSoftwarePackageDto(productNumber, productRevision);
    }

    private NodeIdentifierSoftwarePackageDto(final String productNumber, final String productRevision) {
        this.productNumber = productNumber;
        this.productRevision = productRevision;
    }

    @Override
    public String getPackageName() {
        return "";
    }

    @Override
    public String getNodePlatform() {
        return "";
    }

    @Override
    public String getImportedBy() {
        return "";
    }

    @Override
    public Date getImportDate() {
        return new Date();
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getNEType() {
        return "";
    }

    @Override
    public String getType() {
        return "";
    }

    @Override
    public List<ProductDetailsDto> getProductDetails() {
        final List<ProductDetailsDto> productDetailsDtos = new ArrayList<>();
        productDetailsDtos.add(NodeIdentifierProductDetailsDto.getInstance(productNumber, productRevision));
        return productDetailsDtos;
    }

    @Override
    public List<ParameterDto> getJobParams() {
        return Collections.emptyList();
    }

    @Override
    public List<ActivityDetailsDto> getActivities() {
        return Collections.emptyList();
    }

    @Override
    public String getFilePath() {
        return "";
    }

    @Override
    public List<ProductDetailsDto> getAssociatedUpgradePackages() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, List<ManagedElementTypeDto>> getManagedElementTypes() {
        return Collections.emptyMap();
    }
}
