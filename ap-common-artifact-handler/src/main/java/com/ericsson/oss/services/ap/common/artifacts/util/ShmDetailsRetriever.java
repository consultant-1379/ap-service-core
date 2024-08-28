/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.artifacts.util;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.common.artifacts.UpgradePackageProductDetails;
import com.ericsson.oss.services.shm.filestore.swpackage.api.ProductDetails;
import com.ericsson.oss.services.shm.filestore.swpackage.api.SoftwarePackage;
import com.ericsson.oss.services.shm.filestore.swpackage.remote.api.BasicPackageDetails;
import com.ericsson.oss.services.shm.filestore.swpackage.remote.api.RemoteSoftwarePackageService;
import com.ericsson.oss.services.shm.inventory.remote.backup.api.BackupLocation;
import com.ericsson.oss.services.shm.inventory.remote.backup.api.BackupProviderRemoteService;
import com.ericsson.oss.services.shm.inventory.remote.backup.api.BackupResponse;
import com.ericsson.oss.services.shm.licenseservice.remoteapi.LicenseFileManagerService;
import com.ericsson.oss.services.shm.swpackage.query.api.SoftwarePackageQueryService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

/**
 * Class used to retrieve details for SHM packages. This can include:
 * <ul>
 * <li>Basic Packages</li>
 * <li>License Key Files</li>
 * <li>Upgrade Packages</li>
 * <li>Upgrade Package Product Details</li>
 * <li>Backups</li>
 * </ul>
 */
public class ShmDetailsRetriever {

    private LicenseFileManagerService licenseFileManagerService;

    private RemoteSoftwarePackageService softwarePackageService;

    private BackupProviderRemoteService shmBackupProvider;

    private SoftwarePackageQueryService softwarePackageQueryService;

    @Inject
    private Logger logger;

    @PostConstruct
    public void init() {
        shmBackupProvider = new ServiceFinderBean().find(BackupProviderRemoteService.class);
        licenseFileManagerService = new ServiceFinderBean().find(LicenseFileManagerService.class);
        softwarePackageService = new ServiceFinderBean().find(RemoteSoftwarePackageService.class);
        softwarePackageQueryService = new ServiceFinderBean().find(SoftwarePackageQueryService.class);
    }

    /**
     * Retrieves the file path of the upgrade package in SHM. The returned filepath will be relative to the root directory of SMRS.
     * <p>
     * Retrieves the package using the upgrade package name through {@link RemoteSoftwarePackageService}.
     *
     * @param upgradePackageName the name of the upgrade package
     * @param smrsRootDir        the root SMRS directory
     * @return the relative file path from SMRS root to the upgrade package
     */
    public String getUpgradePackageFilePath(final String upgradePackageName, final String smrsRootDir) {
        try {
            final String absoluteUpgradePackageFilePath = getUpgradePackageAbsolutePath(upgradePackageName);
            return getPathRelativeToSmrsRootDir(absoluteUpgradePackageFilePath, smrsRootDir);
        } catch (final ApApplicationException e) {
            throw e;
        } catch (final Exception e) {
            throw new ApApplicationException("Error retrieving upgrade package details for " + upgradePackageName, e);
        }
    }

    /**
     * Retrieves the absolute file path of the upgrade package in SHM.
     * <p>
     * Retrieves the package using the upgrade package name through {@link RemoteSoftwarePackageService}.
     *
     * @param upgradePackageName
     *            the name of the upgrade package
     * @return the absolute the upgrade package file path
     */
    public String getUpgradePackageAbsolutePath(final String upgradePackageName) {
        final List<String> upgradePackageDetails = softwarePackageService.getUpgradePackageDetails(upgradePackageName);
        if (upgradePackageDetails.isEmpty()) {
            throw new ApApplicationException(String.format("Upgrade package %s does not exist", upgradePackageName));
        }
        return upgradePackageDetails.get(0);
    }

    /**
     * Retrieves the file path of the basic package in SHM. The returned filepath will be relative to the root directory of SMRS.
     * <p>
     * Retrieves the package using the basic package name through {@link RemoteSoftwarePackageService}.
     *
     * @param basicPackageName the name of the basic package
     * @param smrsRootDir      the root SMRS directory
     * @return the relative file path from SMRS root to the basic package
     */
    public String getBasicPackageFilePath(final String basicPackageName, final String smrsRootDir) {
        try {
            final String absoluteBasicPackageFilePath = getBasicPackageAbsolutePath(basicPackageName);
            return getPathRelativeToSmrsRootDir(absoluteBasicPackageFilePath, smrsRootDir);
        } catch (final Exception e) {
            throw new ApApplicationException("Error retrieving basic package details for " + basicPackageName, e);
        }
    }

    private String getBasicPackageAbsolutePath(final String basicPackageName) {
        final BasicPackageDetails basicPackageDetails = softwarePackageService.getBasicPackageDetails(basicPackageName);
        return basicPackageDetails.getBasicPackageFilePath();
    }

    /**
     * Retrieves the file path of the license key file in SHM. The returned filepath will be relative to the root directory of SMRS.
     * <p>
     * Retrieves the file path using {@link LicenseFileManagerService} and the given fingerprint value
     *
     * @param fingerprint the fingerprint of the LKF
     * @param smrsRootDir the root SMRS directory
     * @return the relative file path from SMRS root to the upgrade package
     */
    public String getLicenseKeyFilePath(final String fingerprint, final String smrsRootDir) {
        try {
            final String absoluteLicenseKeyFilePath = getLicenseKeyAbsolutePath(fingerprint);
            return getPathRelativeToSmrsRootDir(absoluteLicenseKeyFilePath, smrsRootDir);
        } catch (final ApApplicationException e) {
            throw e;
        } catch (final Exception e) {
            throw new ApApplicationException("Error retrieving license key file details for " + fingerprint, e);
        }
    }

    private String getLicenseKeyAbsolutePath(final String fingerprint) {
        final String licenseKeyFilePath = licenseFileManagerService.getLicenseKeyFilePathByFingerprint(fingerprint);
        if (StringUtils.isEmpty(licenseKeyFilePath)) {
            throw new ApApplicationException("Unable to retrieve License Key file for " + fingerprint + " from SHM");
        }
        return licenseKeyFilePath;
    }

    private static String getPathRelativeToSmrsRootDir(final String absolutePath, final String smrsRootDir) {
        return absolutePath.substring(smrsRootDir.length() + 1);
    }

    /**
     * Retrieves the Backup file path from shm relative to the SMRS root directory.
     *
     * @param nodeName name of the node to retrieve the backup for
     * @return the file path relative to SMRS root directory
     */
    public String getRelativeBackupFilePath(final String nodeName) {
        try {
            final BackupResponse backupResponse = shmBackupProvider.getLatestBackup(nodeName, BackupLocation.ENM);
            return backupResponse.getBackupFileRelativePath();
        } catch (final Exception e) {
            throw new ApApplicationException("Failed to retrieve Backup Path for " + nodeName, e);
        }
    }

    /**
     * Retrieves the named Backup file path from SHM relative to the SMRS root directory.
     *
     * @param nodeName name of the node to retrieve the backup for
     * @param backupName name the backup to retrieve
     * @throws ApApplicationException if the named backup cannot be retrieved from SHM
     * @return the file path relative to SMRS root directory
     */
    public String getRelativeBackupFilePath(final String nodeName, final String backupName) {
        try {
            logger.debug("Attempting to retrieve Backup Path for {} from SHM. Referenced Backup is: {}", nodeName, backupName);
            final BackupResponse backupResponse = shmBackupProvider.getBackup(nodeName, backupName);
            return backupResponse.getBackupFileRelativePath();
        } catch (final Exception e) {
            throw new ApApplicationException("Failed to retrieve Backup Path for " + nodeName + " from SHM. Check that referenced Backup \"" + backupName + "\" is uploaded to ENM.", e);
        }
    }

    /**
     * Returns UpgradePackageProductDetails with Software Upgrade Package Information from upgradePackageName and nodeType.
     *
     * @param upgradePackageName
     * @param nodeType
     * @return ApApplicationException
     */
    public UpgradePackageProductDetails getUpgradePackageProductDetails(final String upgradePackageName, final String nodeType) {
        try {
            final Map<String, SoftwarePackage> softwarePackageMap = softwarePackageQueryService.getSoftwarePackagesBasedOnPackageName(
                    getSoftwarePackageMap(upgradePackageName, nodeType));
            return createUpgradePackageProductDetails(softwarePackageMap);
        } catch (final Exception e) {
            throw new ApApplicationException(String.format("Error retrieving Software Package %s, package does not exist or is invalid.", upgradePackageName), e);
        }
    }

    private UpgradePackageProductDetails createUpgradePackageProductDetails(final Map<String, SoftwarePackage> softwarePackageMap) {
        final UpgradePackageProductDetails upgradePackageProductDetails = new UpgradePackageProductDetails();
        final Map.Entry<String, SoftwarePackage> softwarePackageMapEntry = softwarePackageMap.entrySet().iterator().next();
        final List<ProductDetails> productDetails = softwarePackageMapEntry.getValue().getSoftwarePackageProductDetailsList();
        upgradePackageProductDetails.setProductNumber(productDetails.get(0).getProductNumber());
        upgradePackageProductDetails.setProductRevision(productDetails.get(0).getProductRevision());
        return upgradePackageProductDetails;
    }

    private Map<String, List<String>> getSoftwarePackageMap(final String upgradePackageName, final String nodeType) {
        final Map<String, List<String>> softwarePackagesMap = new HashMap<>();
        final List<String> swpackageList = new ArrayList<>();
        swpackageList.add(upgradePackageName);
        softwarePackagesMap.put(nodeType, swpackageList);
        return softwarePackagesMap;
    }


}
