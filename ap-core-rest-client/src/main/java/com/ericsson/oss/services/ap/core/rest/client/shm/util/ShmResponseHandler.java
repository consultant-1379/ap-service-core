/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2022
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client.shm.util;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.core.rest.client.shm.backupsoftwareversion.model.BackupSoftwareVersionDetail;
import com.ericsson.oss.services.ap.core.rest.client.shm.backupsoftwareversion.model.BackupSoftwareVersionResponse;
import com.ericsson.oss.services.ap.core.rest.client.shm.upgradepackage.model.ProductDetail;
import com.ericsson.oss.services.ap.core.rest.client.shm.upgradepackage.model.ShmResponse;
import com.ericsson.oss.services.ap.core.rest.client.shm.upgradepackage.model.SoftwarePackageDetail;
import com.ericsson.oss.services.ap.core.rest.client.shm.upgradepackage.model.SupportedPackageDetail;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ShmResponseHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static ObjectMapper objectMapper = new ObjectMapper();

    private static final String INVALID_SW_VERSION = "Unable to retreive Software Version details";

    /**
     * Retrieves the Upgrade Package Name from the response based on the software version.
     *
     * @param HttpResponse
     *            the response of the request
     * @param nodeName
     *            the name of the AP node
     * @param softwareVersion
     *            the software version
     * @param nodeTypeIdForPackageName
     *            the type of the AP node
     * @return the Upgrade Package name
     */
    public String handlePackageNameResponse(final HttpResponse httpResponse, final String nodeName, final String softwareVersion,
                                            final String nodeTypeIdForPackageName) {
        final List<SupportedPackageDetail> supportedPackageDetails = processUpgradePackageNameHttpResponse(httpResponse);
        if (supportedPackageDetails.isEmpty()) {
            throw new ApApplicationException(String.format("No upgrade package is retrieved from SHM for %s", nodeTypeIdForPackageName));
        }

        final String packageName = findPackagNameForSpecificSoftwareVersion(supportedPackageDetails, softwareVersion, nodeTypeIdForPackageName);

        if (packageName == null) {
            logger.error("Unable to retrieve upgrade package details for {} {} from SHM", nodeTypeIdForPackageName, softwareVersion);
            throw new ApApplicationException(String.format("Failed to get upgrade package %s", softwareVersion));
        }
        return packageName;
    }

    /**
     * Retrieves backup software version from the response.
     *
     * @param httpResponse
     *            the response of the request
     * @param nodeName
     *            the name of the AP node
     * @param backupId
     *            the backup id
     * @return the backup software version
     */
    public String handleBackupSoftwareVersionResponse(final HttpResponse httpResponse, final String nodeName, final String backupId) {
        final String backupSoftwareVersion = processBackupSoftwareVersionHttpResponse(httpResponse, backupId);
        if (!isValidBackupSoftwareVersion(backupSoftwareVersion)) {
            logger.error("Unable to retrieve valid backup software version for node {} backupid {} from SHM.", nodeName, backupId);
            throw new ApApplicationException(String.format("Invalid backup software version : %s", backupSoftwareVersion));
        }
        return backupSoftwareVersion;
    }

    private List<SupportedPackageDetail> processUpgradePackageNameHttpResponse(final HttpResponse httpResponse) {
        if (httpResponse != null) {
            try {
                final String responseString = EntityUtils.toString(httpResponse.getEntity());
                final ShmResponse packageResponse = objectMapper.readValue(responseString, ShmResponse.class);
                return packageResponse.getSupportedPackageDetails();
            } catch (ParseException | IOException e1) {
                throw new ApApplicationException("Error in parsing httpResponse.", e1);
            }
        }
        return Collections.emptyList();
    }

    private String processBackupSoftwareVersionHttpResponse(final HttpResponse httpResponse,  final String backupId) {
        String backupSoftwareVersion = null;
        try {
            final String responseString = EntityUtils.toString(httpResponse.getEntity());

            final BackupSoftwareVersionResponse validationResponse = objectMapper.readValue(responseString, BackupSoftwareVersionResponse.class);
            final List<BackupSoftwareVersionDetail> spls = validationResponse.getBackupSwVersionDetails();
            if (spls.isEmpty()){
                throw new ApApplicationException(String.format("No backup software version for %s", backupId));
            }
            if (spls.size() > 1) {
                logger.error("Only one backup software versions is expected but found {}!", spls.size());
                throw new ApApplicationException(String.format("More than one backup software version for %s", backupId));
            }
            backupSoftwareVersion = spls.get(0).getSwVersion();
        } catch (ParseException | IOException e1) {
            throw new ApApplicationException("Error in parsing httpResponse for ", e1);
        }
        return backupSoftwareVersion;
    }

    @SuppressWarnings("squid:S3776")
    private String findPackagNameForSpecificSoftwareVersion(final List<SupportedPackageDetail> supportedPackageDetails, final String softwareVsersion,
                                                            final String nodeTypeIdForPackageName) {
        boolean checkforDuplicatePackageName = false;
        String packageName = null;
        for (final SupportedPackageDetail supportedPackageDetail : supportedPackageDetails) {
            if (supportedPackageDetail.getNeType().equalsIgnoreCase(nodeTypeIdForPackageName)) {
                for (final SoftwarePackageDetail softwarePackageDetail : supportedPackageDetail.getSoftwarePackageDetails()) {
                    for (final ProductDetail productDetail : softwarePackageDetail.getProductDetails()) {
                        if (softwareVsersion.equalsIgnoreCase(productDetail.getProductNumber() + "_" + productDetail.getProductRevision())) {
                            if (!checkforDuplicatePackageName) {
                                checkforDuplicatePackageName = true;
                                packageName = softwarePackageDetail.getPackageName();
                            } else {
                                throw new ApApplicationException(
                                        String.format("Multiple upgrade packages of version %s exist, unable to identify which UP to use %s",
                                                softwareVsersion, nodeTypeIdForPackageName));
                            }
                        }
                    }
                }
            }
        }
        return packageName;
    }

    private static boolean isValidBackupSoftwareVersion(final String backupSoftwareVersion) {
        return !(backupSoftwareVersion == null || backupSoftwareVersion.equalsIgnoreCase(INVALID_SW_VERSION) || backupSoftwareVersion.isEmpty());
    }
}
