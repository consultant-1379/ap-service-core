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
package com.ericsson.oss.services.ap.core.usecase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.eventbus.model.EventSender;
import com.ericsson.oss.itpf.sdk.eventbus.model.annotation.Modeled;
import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.itpf.sdk.resources.Resources;
import com.ericsson.oss.mediation.sdk.event.MediationTaskRequest;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.ProfileNotFoundException;
import com.ericsson.oss.services.ap.common.model.ConfigSnapshotStatus;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * Fetch netconf node configuration snapshot
 */
@UseCase(name = UseCaseName.GET_SNAPSHOT)
public class GetSnapshotUseCase extends BaseProfileUseCase {

    private static final String ROOT_AP_SFS_DIRECTORY = "/ericsson/autoprovisioning/";
    private static final String GENERATED_DIRECTORY = ROOT_AP_SFS_DIRECTORY + "artifacts/generated";
    private static final String DATA_SYNC_FILE_USAGE_MSG = "This file is used for polling file system to detect the new generated config snapshot.";

    @Inject
    @Modeled
    private EventSender<MediationTaskRequest> eventSender;

    @Inject
    private Logger logger;

    /**
     * @param projectId The name of the given Project
     * @param profileFdn  The fdn of the Profile
     * @param apNodeName the name of the AP node
     * @return snapshot content
     * @throws ApApplicationException if error fetching node configuration snapshot
     */
    public String execute(final String projectId, final String profileFdn, final String apNodeName) {
        try {
            final String snapshotFilePath = getConfigurationSnapshotPath(apNodeName, GENERATED_DIRECTORY);
            final String response = readFile(snapshotFilePath);
            if (StringUtils.isNotBlank(response)) {
                snapshotStatusCleanUp(projectId,profileFdn,snapshotFilePath);
            }
            return response;
        } catch (final ProfileNotFoundException exception) {
            throw exception;
        } catch (final ApApplicationException exception) {
            setNodeConfigSnapshotStatus(ConfigSnapshotStatus.FAILED, profileFdn);
            throw exception;
        } catch (final Exception exception) {
            setNodeConfigSnapshotStatus(ConfigSnapshotStatus.FAILED, profileFdn);
            throw new ApApplicationException(String.format("Error fetching netconf configuration snapshot for profile %s", profileFdn),
                exception);
        }
    }

    private String readFile(final String filePath) throws IOException {
        final String dataSyncFilePath = String.format("%s/%s_%s", GENERATED_DIRECTORY, "DataSync", System.currentTimeMillis());
        if (writeFile(dataSyncFilePath, DATA_SYNC_FILE_USAGE_MSG.getBytes("UTF-8"), false) > 0) {
            final Resource resource = Resources.getFileSystemResource(filePath);
            if ((resource != null) && resource.exists()) {
                final long length = resource.size();
                final long maxFileSizeByte = 10485760;
                if (length > maxFileSizeByte) {
                  throw new ApApplicationException(String.format("The node snapshot size %s bytes exceeds the maxmium supported size %s bytes.", length, maxFileSizeByte));
                }

                final StringBuilder stringBuilder = new StringBuilder();
                final InputStreamReader isReader = new InputStreamReader(resource.getInputStream());
                final BufferedReader bufferedReader = new BufferedReader(isReader);
                String line = bufferedReader.readLine();
                while (line != null) {
                    stringBuilder.append(line.trim() + " ");
                    line = bufferedReader.readLine();
                }
                final byte[] newLineBytes = stringBuilder.toString().getBytes("UTF-8");
                final String newLineBase64 = Base64.getEncoder().encodeToString(newLineBytes);
                isReader.close();
                if (!isFileDeleted(dataSyncFilePath)) {
                    logger.warn("Data sync file {} could not be deleted from file system. Please check server log for more details.", dataSyncFilePath);
                }
                return newLineBase64;
            }
            if (!isFileDeleted(dataSyncFilePath)) {
                logger.warn("Data sync file {} could not be deleted from file system. Please check server log for more details.", dataSyncFilePath);
            }
        } else {
            logger.warn("Fail to write data sync file to file system!");
        }
        logger.warn("Fail to read config snapshot contents from file system!");
        return null;
    }
}
