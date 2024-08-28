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

import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

/**
 * Unit tests for {@link ShmDetailsRetriever}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ShmDetailsRetrieverTest {

    private static final String DELIMETER = "/";

    private static final String VALID_BASIC_PACKAGE_NAME = "basicPackage";
    private static final String VALID_UPGRADE_PACKAGE_NAME = "upgradePackage";

    private static final String SMRS_ROOT_DIRECTORY = "/home/smrs/smrsroot";
    private static final String BASIC_PACKAGE_RELATIVE_FILE_PATH = "shm/basicPackages/" + VALID_BASIC_PACKAGE_NAME;
    private static final String LICENSE_KEY_FILE_RELATIVE_FILE_PATH = "shm/licenseKeyFiles/" + NODE_NAME;
    private static final String UPGRADE_PACKAGE_RELATIVEFILE_PATH = "shm/upgradePackages/" + VALID_UPGRADE_PACKAGE_NAME;

    private static final String BASIC_PACKAGE_ABSOLUTE_FILE_PATH = SMRS_ROOT_DIRECTORY + DELIMETER + BASIC_PACKAGE_RELATIVE_FILE_PATH;
    private static final String LICENSE_KEY_FILE_ABSOLUTE_FILE_PATH = SMRS_ROOT_DIRECTORY + DELIMETER + LICENSE_KEY_FILE_RELATIVE_FILE_PATH;
    private static final String UPGRADE_PACKAGE_ABSOLUTE_FILE_PATH = SMRS_ROOT_DIRECTORY + DELIMETER + UPGRADE_PACKAGE_RELATIVEFILE_PATH;

    private static final String BACKUP_NAME = "Backup.zip";
    private static final String BACKUP_RELATIVE_FILE_PATH = "/smrsroot/backup/radionode/" + NODE_NAME + DELIMETER + BACKUP_NAME;

    private static final String PRODUCT_NUMBER = "CXP9031596";
    private static final String PRODUCT_REVISION = "R-1024";
    private static final String NODE_TYPE = "ERBS";
    private static final String UPGRADE_PACKAGE_NAME = "AX-2030";

    @Mock
    private LicenseFileManagerService licenseFileManagerService;

    @Mock
    private RemoteSoftwarePackageService softwarePackageService;

    @Mock
    private BackupProviderRemoteService shmBackupProvider;

    @Mock
    private SoftwarePackageQueryService softwarePackageQueryService;

    @Mock
    private SoftwarePackage softwarePackage;

    @Mock
    private ProductDetails productDetails;

    @Mock
    private UpgradePackageProductDetails upgradePackageProductDetails;

    @Mock
    private Logger logger;

    @InjectMocks
    private ShmDetailsRetriever shmDetailsRetriever;

    @Before
    public void setUp() {
        final List<String> upgradePackagesFilePaths = new ArrayList<>();
        upgradePackagesFilePaths.add(UPGRADE_PACKAGE_ABSOLUTE_FILE_PATH);

        final BasicPackageDetails basicPackageDetails = new BasicPackageDetails(BASIC_PACKAGE_ABSOLUTE_FILE_PATH,
            Collections.<String, List<String>> emptyMap());
        final BackupResponse backupResponse = new BackupResponse(NODE_NAME, NODE_NAME, SMRS_ROOT_DIRECTORY + BACKUP_RELATIVE_FILE_PATH,
            BACKUP_RELATIVE_FILE_PATH, BackupLocation.ENM, new Date());

        when(licenseFileManagerService.getLicenseKeyFilePathByFingerprint(NODE_NAME)).thenReturn(LICENSE_KEY_FILE_ABSOLUTE_FILE_PATH);
        when(softwarePackageService.getUpgradePackageDetails(VALID_UPGRADE_PACKAGE_NAME)).thenReturn(upgradePackagesFilePaths);
        when(softwarePackageService.getBasicPackageDetails(VALID_BASIC_PACKAGE_NAME)).thenReturn(basicPackageDetails);
        when(shmBackupProvider.getLatestBackup(NODE_NAME, BackupLocation.ENM)).thenReturn(backupResponse);
        when(shmBackupProvider.getBackup(NODE_NAME, BACKUP_NAME)).thenReturn(backupResponse);
        when(productDetails.getProductNumber()).thenReturn(PRODUCT_NUMBER);
        when(productDetails.getProductRevision()).thenReturn(PRODUCT_REVISION);
        when(softwarePackage.getSoftwarePackageProductDetailsList()).thenReturn(populateSoftwarePackageProductDetailsList());
        when(softwarePackageQueryService.getSoftwarePackagesBasedOnPackageName(populateUpgradePackageMap())).thenReturn(prepareSoftwarePackageMap());

    }

    @Test
    public void whenGetUpgradePackageFilePathThenRelativePathIsReturnedSuccessfully() {
        final String result = shmDetailsRetriever.getUpgradePackageFilePath(VALID_UPGRADE_PACKAGE_NAME, SMRS_ROOT_DIRECTORY);
        assertEquals(UPGRADE_PACKAGE_RELATIVEFILE_PATH, result);
    }

    @Test
    public void whenGetBasicPackageFilePathThenRelativePathIsReturnedSuccessfully() {
        final String result = shmDetailsRetriever.getBasicPackageFilePath(VALID_BASIC_PACKAGE_NAME, SMRS_ROOT_DIRECTORY);
        assertEquals(BASIC_PACKAGE_RELATIVE_FILE_PATH, result);
    }

    @Test
    public void whenGetLicenseKeyFileFilePathThenRelativePathIsReturnedSuccessfully() {
        final String result = shmDetailsRetriever.getLicenseKeyFilePath(NODE_NAME, SMRS_ROOT_DIRECTORY);
        assertEquals(LICENSE_KEY_FILE_RELATIVE_FILE_PATH, result);
    }

    @Test
    public void whenGetUpgradePackageFilePathAndPackageDoesNotExistThenApApplicationExceptionIsThrownWithPackageNameInMessage() {
        final String invalidUpgradePackageName = "invalidUpgradePackage";
        boolean exceptionCaught = false;

        try {
            shmDetailsRetriever.getUpgradePackageFilePath(invalidUpgradePackageName, SMRS_ROOT_DIRECTORY);
        } catch (final ApApplicationException e) {
            assertTrue("Exception message does not contain the upgrade package name", e.getMessage().contains(invalidUpgradePackageName));
            exceptionCaught = true;
        }

        assertTrue("No exception was thrown", exceptionCaught);
    }

    @Test
    public void whenGetBasicPackageFilePathAndPackageDoesNotExistThenApApplicationExceptionIsThrownWithPackageNameInMessage() {
        final String invalidBasicPackageName = "invalidBasicPackage";
        boolean exceptionCaught = false;

        try {
            shmDetailsRetriever.getBasicPackageFilePath(invalidBasicPackageName, SMRS_ROOT_DIRECTORY);
        } catch (final ApApplicationException e) {
            assertTrue("Exception message does not contain the basic package name", e.getMessage().contains(invalidBasicPackageName));
            exceptionCaught = true;
        }

        assertTrue("No exception was thrown", exceptionCaught);
    }

    @Test
    public void whenGetLicenseKeyFileFilePathAndFileDoesNotExistThenApApplicationExceptionIsThrownWithNodeNameInMessage() {
        final String invalidNodeName = "invalidNodeName";
        boolean exceptionCaught = false;

        try {
            shmDetailsRetriever.getLicenseKeyFilePath(invalidNodeName, SMRS_ROOT_DIRECTORY);
        } catch (final ApApplicationException e) {
            assertTrue("Exception message does not contain the node name", e.getMessage().contains(invalidNodeName));
            exceptionCaught = true;
        }

        assertTrue("No exception was thrown", exceptionCaught);
    }

    @Test(expected = ApApplicationException.class)
    public void whenGetUpgradePackageFilePathAndExceptionIsThrownThenApApplicationExceptionIsThrown() {
        doThrow(Exception.class).when(softwarePackageService).getUpgradePackageDetails(anyString());
        shmDetailsRetriever.getUpgradePackageFilePath(VALID_UPGRADE_PACKAGE_NAME, SMRS_ROOT_DIRECTORY);
    }

    @Test(expected = ApApplicationException.class)
    public void whenGetBasicPackageFilePathAndExceptionIsThrownThenApApplicationExceptionIsThrown() {
        doThrow(Exception.class).when(softwarePackageService).getBasicPackageDetails(anyString());
        shmDetailsRetriever.getBasicPackageFilePath(VALID_BASIC_PACKAGE_NAME, SMRS_ROOT_DIRECTORY);

    }

    @Test(expected = ApApplicationException.class)
    public void whenGetLicenseKeyFileFilePathAndExceptionIsThrownThenApApplicationExceptionIsThrown() {
        doThrow(Exception.class).when(licenseFileManagerService).getLicenseKeyFilePathByFingerprint(anyString());
        shmDetailsRetriever.getLicenseKeyFilePath(NODE_NAME, SMRS_ROOT_DIRECTORY);
    }

    @Test
    public void whenGetRelativeBackupFilePathThenRelativePathIsReturnedSuccessfully() {
        final String result = shmDetailsRetriever.getRelativeBackupFilePath(NODE_NAME);
        assertEquals(BACKUP_RELATIVE_FILE_PATH, result);
    }

    @Test(expected = ApApplicationException.class)
    public void whenGetRelativeBackupFilePathAndExceptionIsThrownThenApApplicationExceptionIsThrown() {
        doThrow(Exception.class).when(shmBackupProvider).getLatestBackup(anyString(), any(BackupLocation.class));
        shmDetailsRetriever.getRelativeBackupFilePath(NODE_NAME);
    }

    @Test
    public void whenGetRelativeBackupFilePathWithNamedBackupThenRelativePathIsReturnedSuccessfully() {
        final String result = shmDetailsRetriever.getRelativeBackupFilePath(NODE_NAME, BACKUP_NAME);
        assertEquals(BACKUP_RELATIVE_FILE_PATH, result);
    }

    @Test(expected = ApApplicationException.class)
    public void whenGetRelativeBackupFilePathWithNamedBackupAndExceptionIsThrownThenApApplicationExceptionIsThrown() {
        doThrow(Exception.class).when(shmBackupProvider).getBackup(anyString(), anyString());
        shmDetailsRetriever.getRelativeBackupFilePath(NODE_NAME, BACKUP_NAME);
    }

    @Test
    public void whenUpgradePackageNameAndNodeTypeSuppliedThenProductNumberAndRevisionIsReturnedSuccessfully() {
        upgradePackageProductDetails = shmDetailsRetriever.getUpgradePackageProductDetails(UPGRADE_PACKAGE_NAME, NODE_TYPE);
        assertEquals(PRODUCT_NUMBER, upgradePackageProductDetails.getProductNumber());
        assertEquals(PRODUCT_REVISION, upgradePackageProductDetails.getProductRevision());
    }

    @Test(expected = ApApplicationException.class)
    public void whenUpgradePackageNameAndNodeTypeNotSuppliedThenProductNumberAndRevisionIsReturnedSuccessfully() {
        upgradePackageProductDetails = shmDetailsRetriever.getUpgradePackageProductDetails("", "");
        doThrow(Exception.class).when(upgradePackageProductDetails.getProductNumber());
        doThrow(Exception.class).when(upgradePackageProductDetails.getProductRevision());
    }

    private List<ProductDetails> populateSoftwarePackageProductDetailsList() {
        final List<ProductDetails> softwarePackageProductDetailsList = new ArrayList();
        softwarePackageProductDetailsList.add(productDetails);
        return softwarePackageProductDetailsList;
    }

    private Map<String, SoftwarePackage> prepareSoftwarePackageMap() {
        final Map<String, SoftwarePackage> softwarePackageMap = new HashMap<>();
        softwarePackageMap.put(NODE_TYPE, softwarePackage);
        return softwarePackageMap;
    }

    private Map<String, List<String>> populateUpgradePackageMap() {
        final Map<String, List<String>> upgradePackageMap = new HashMap<>();
        final List<String> swpackageList = new ArrayList<>();
        swpackageList.add(UPGRADE_PACKAGE_NAME);
        upgradePackageMap.put(NODE_TYPE, swpackageList);
        return upgradePackageMap;
    }

}
