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

import java.util.concurrent.TimeUnit;
import java.util.Iterator;
import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommand;
import com.ericsson.oss.itpf.sdk.core.retry.RetriableCommandException;
import com.ericsson.oss.itpf.sdk.core.retry.RetryContext;
import com.ericsson.oss.itpf.sdk.core.retry.RetryManager;
import com.ericsson.oss.itpf.sdk.core.retry.RetryPolicy;
import com.ericsson.oss.itpf.sdk.eventbus.model.EventSender;
import com.ericsson.oss.itpf.sdk.eventbus.model.annotation.Modeled;
import com.ericsson.oss.mediation.core.events.OperationType;
import com.ericsson.oss.mediation.sdk.event.MediationTaskRequest;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.ProfileNotFoundException;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.ProfileAttribute;
import com.ericsson.oss.services.ap.common.model.ConfigSnapshotStatus;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;
import com.ericsson.oss.services.ap.model.event.NetconfConfigurationEvent;

/**
 * Fetch netconf node configuration snapshot
 */
@UseCase(name = UseCaseName.DUMP_SNAPSHOT)
public class DumpSnapshotUseCase extends BaseProfileUseCase {

    private static final String AP_PROJECT_LOCATION = "/ericsson/autoprovisioning/projects/";
    private static final String ROOT_AP_SFS_DIRECTORY = "/ericsson/autoprovisioning/";
    private static final String GENERATED_DIRECTORY = ROOT_AP_SFS_DIRECTORY + "artifacts/generated";
    private static final String CONFIGURATION_FILTER_FILE_NAME = "get-node-config-snapshot.xml";

    @Inject
    @Modeled
    private EventSender<MediationTaskRequest> eventSender;

    @Inject
    private Logger logger;

    @Inject
    private RetryManager retryManager;

    /**
     * @param projectId The id of the given Project
     * @param profileId  The id of the Profile
     * @param apNodeName  The name of the node
     * @param profileFdn the FDN of the profile
     * @throws ProfileNotFoundException if no Profile found.
     * @throws ApApplicationException if error fetching node configuration snapshot
     */
    public void execute(final String projectId, final String profileId, final String apNodeName, final String profileFdn) {
        try {
            final String snapshotPath = getConfigurationSnapshotPath(apNodeName, GENERATED_DIRECTORY);
            setNodeConfigSnapshotStatus(ConfigSnapshotStatus.IN_PROGRESS, profileFdn);
            checkAndDeleteSnapshotFile(snapshotPath);
            createTimeStamp(profileFdn);
            sendNetconfSnapshotEvent(projectId, profileId, apNodeName, profileFdn);
        } catch (final ProfileNotFoundException exception) {
            logger.error("Profile is not found for dumping snapshot.", exception);
            throw exception;
        } catch (final ApApplicationException exception) {
            logger.error("Fail to trigger snapshot dumping", exception);
            setNodeConfigSnapshotStatus(ConfigSnapshotStatus.FAILED, profileFdn);
            throw exception;
        } catch (final Exception exception) {
            setNodeConfigSnapshotStatus(ConfigSnapshotStatus.FAILED, profileFdn);
            throw new ApApplicationException(String.format("Error fetching netconf configuration snapshot for profile %s", profileId),
                exception);
        }
    }

    private void sendNetconfSnapshotEvent(final String projectName, final String profileId, final String apNodeName, final String profileFdn) {
       final String configurationFilterLocation = String.format("%s/%s", getFilterLocation(profileFdn), CONFIGURATION_FILTER_FILE_NAME);
       final NetconfConfigurationEvent event = new NetconfConfigurationEvent();

       event.setNodeAddress(getManagedElementFdn(apNodeName));
       event.setProtocolInfo(OperationType.AP.toString());
       event.setNetconfFileContents(configurationFilterLocation);
       event.setProfileFdn(profileFdn);

       eventSender.send(event);
    }

    private String getManagedElementFdn(final String nodeName) {
        final Iterator<ManagedObject> managedElementMos = dpsQueries.findMoByName(nodeName, MoType.MANAGEDELEMENT.toString(), "ECIM_Top").execute();

        if ((managedElementMos != null)&&(managedElementMos.hasNext())) {
            return managedElementMos.next().getFdn();
        }

        throw new ApApplicationException(String.format("Could not find ManagedElement MO from node name %s.", nodeName));
    }

    private void createTimeStamp(final String profileFdn) {
        final ManagedObject profileMo = getProfileMo(profileFdn);
        if (profileMo.getAttribute(ProfileAttribute.DUMP_TIMESTAMP.toString()) != null) {
            profileMo.setAttribute(ProfileAttribute.DUMP_TIMESTAMP.toString(), Long.valueOf(System.currentTimeMillis()));
        } else {
            setNodeConfigSnapshotStatus(ConfigSnapshotStatus.FAILED, profileFdn);
            throw new ApApplicationException(String.format("Error setting timestamp for profile %s", profileFdn));
        }
    }

    protected void checkAndDeleteSnapshotFile(final String snapshotFilePath) {
        try {
            if (deleteFileAndDirectory(snapshotFilePath)) {
                final RetryPolicy policy = RetryPolicy.builder()
                        .attempts(2)
                        .waitInterval(2, TimeUnit.SECONDS)
                        .retryOn(ApApplicationException.class)
                        .build();

                retryManager.executeCommand(policy, new RetriableCommand<Void>() {
                    @Override
                    public Void execute(final RetryContext retryContext) throws Exception {
                        logger.debug("Executing checkAndDeleteSnapshotFile for {} for {} time.", snapshotFilePath, retryContext.getCurrentAttempt());
                        if (doesFileExist(snapshotFilePath)) {
                            throw new ApApplicationException("Retry on deleting snaoshot file");
                        }
                        return null;
                    }
                });
            }
        } catch (final RetriableCommandException e) {
            logger.debug("Fail to delete snapshot file ", e);
            throw new ApApplicationException("Fail to delete snapshot file");
        }
    }
}
