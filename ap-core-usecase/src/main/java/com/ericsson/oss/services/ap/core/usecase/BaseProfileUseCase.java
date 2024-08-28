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
package com.ericsson.oss.services.ap.core.usecase;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.ProfileNotFoundException;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.ConfigSnapshotStatus;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.ProfileAttribute;
import com.ericsson.oss.services.ap.core.usecase.profile.ProfileStorageHandler;

/**
 * Base UseCase for ConfigurationProfile MO operations
 */
public abstract class BaseProfileUseCase {

    @Inject
    protected DpsQueries dpsQueries;

    @Inject
    protected DpsOperations dpsOperations;

    @Inject
    protected ProfileStorageHandler profileStorageHandler;

    @Inject
    private Logger logger;

    private ResourceService resourceService;

    @PostConstruct
    public void init() {
        resourceService = new ServiceFinderBean().find(ResourceService.class);
    }

    /**
     * Method to get all ConfigurationProfiles that are associated with an AP Project
     *
     * @param projectFdn
     *            The fdn of the project
     *
     * @return {@link Iterator}
     *               profile MO
     * @throws ApApplicationException
     *            if profiles are not found
     */
    protected Iterator<ManagedObject> getAllProfileMOs(final String projectFdn) {
        Iterator<ManagedObject> profileMos;
        try {
            profileMos = dpsQueries.findChildMosOfTypes(projectFdn, MoType.CONFIGURATION_PROFILE.toString()).execute();
        } catch (final Exception e) {
            throw new ApApplicationException("Error retrieving ConfigurationProfile MO's for Project " + projectFdn, e);
        }
        return profileMos;
    }

    /**
     * Checks if a profile exists by its FDN.
     *
     * @param profileFdn
     *            The FDN of the given Project
     * @throws ProfileNotFoundException
     *             if profile was not found
     */
    protected void checkIfProfileExists(final String profileFdn) {
        if (!dpsOperations.existsMoByFdn(profileFdn)) {
            throw new ProfileNotFoundException(
                    String.format("ConfigurationProfile %s does not exist on the system", profileFdn));
        }
    }

    /**
     * Extracts non-file attributes from a ConfigurationProfile MoData to a Map.
     *
     * @param profileData
     *            the profile MoData
     * @return a map of profile attributes
     */
    protected Map<String, Object> extractAttributes(final MoData profileData) {

        final Map<String, Object> attributes = new HashMap<>();
        attributes.put(ProfileAttribute.PROPERTIES.toString(),
                profileData.getAttribute(ProfileAttribute.PROPERTIES.toString()));
        attributes.put(ProfileAttribute.VERSION.toString(),
                profileData.getAttribute(ProfileAttribute.VERSION.toString()));
        attributes.put(ProfileAttribute.STATUS.toString(),
                profileData.getAttribute(ProfileAttribute.STATUS.toString()));
        attributes.put(ProfileAttribute.OSS_MODEL_IDENTITY.toString(),
                profileData.getAttribute(ProfileAttribute.OSS_MODEL_IDENTITY.toString()));
        attributes.put(ProfileAttribute.UPGRADE_PACKAGE_NAME.toString(),
                profileData.getAttribute(ProfileAttribute.UPGRADE_PACKAGE_NAME.toString()));
        attributes.put(ProfileAttribute.DATATYPE.toString(),
                profileData.getAttribute(ProfileAttribute.DATATYPE.toString()));
        attributes.put(ProfileAttribute.CONFIG_SNAPSHOT_STATUS.toString(),
                profileData.getAttribute(ProfileAttribute.CONFIG_SNAPSHOT_STATUS.toString()));
        attributes.put(ProfileAttribute.DUMP_TIMESTAMP.toString(),
                profileData.getAttribute(ProfileAttribute.DUMP_TIMESTAMP.toString()));
        return attributes;
    }

    /**
     * Persists ConfigurationProfile files received on the profileData and
     * returns ONLY file related attributes.
     *
     * @param profileData
     *            the profile MoData
     * @param projectName
     *            the project Id
     * @param profileName
     *            the profile Id
     * @return Map of file related attributes.
     */
    protected Map<String, Object> persistFilesFromProfileData(final MoData profileData, final String projectName,
            final String profileName) {
        final Map<String, Object> attributes = new HashMap<>();

        final Map<String, Object> graphicFileMap = (Map<String, Object>) profileData
                .getAttribute(ProfileAttribute.GRAPHIC.toString());
        final String graphicLocation = profileStorageHandler.saveGraphicFile(projectName, profileName, graphicFileMap);
        attributes.put(ProfileAttribute.GRAPHIC_LOCATION.toString(), graphicLocation);

        final Map<String, Object> ciqFileMap = (Map<String, Object>) profileData
                .getAttribute(ProfileAttribute.CIQ.toString());
        final String ciqLocation = profileStorageHandler.saveCiqFile(projectName, profileName, ciqFileMap);

        final Map<String, Object> ciq = new HashMap<>();
        ciq.put(ProfileAttribute.CIQ_LOCATION.toString(), ciqLocation);
        attributes.put(ProfileAttribute.CIQ.toString(), ciq);

        final List<Map<String, Object>> configurations = (List<Map<String, Object>>) profileData
                .getAttribute(ProfileAttribute.CONFIGURATIONS.toString());
        final String profileContentLocation = profileStorageHandler.saveConfigurationFiles(projectName, profileName,
                configurations);
        attributes.put(ProfileAttribute.PROFILE_CONTENT_LOCATION.toString(), profileContentLocation);

        final Map<String, Object> getConfigScriptFileMap = (Map<String, Object>) profileData
            .getAttribute(ProfileAttribute.GET_CONFIG_SCRIPT.toString());
        final String filterLocation = profileStorageHandler.saveGetConfigScriptFile(projectName, profileName, getConfigScriptFileMap);
        attributes.put(ProfileAttribute.FILTER_LOCATION.toString(), filterLocation);

        return attributes;
    }

    /**
     * Set node configurtion snapshot status.
     *
     * @param status
     *            the status needs to set
     * @param profileFdn
     *            the profile fdn
     */
    protected void setNodeConfigSnapshotStatus(final ConfigSnapshotStatus status, final String profileFdn) {
        final ManagedObject profileMo = getProfileMo(profileFdn);
        if (!profileMo.getAttribute(ProfileAttribute.CONFIG_SNAPSHOT_STATUS.toString()).equals(status.toString())) {
            profileMo.setAttribute(ProfileAttribute.CONFIG_SNAPSHOT_STATUS.toString(), status.toString());
        }
    }

    /**
     * Reset snapshot Status to NOT_STARTED and delete snapshot file
     *
     * @param projectName
     *            the project name
     * @param profileFdn
     *            the profile fdn
     * @param snapshotPath
     *            the path of snapshot file
     */
    protected void snapshotStatusCleanUp(final String projectName, final String profileFdn, final String snapshotPath) {
         if (deleteFileAndDirectory(snapshotPath)) {
             setNodeConfigSnapshotStatus(ConfigSnapshotStatus.NOT_STARTED, profileFdn);
         } else {
             logger.warn("Could not delete the directory {} for {}", snapshotPath, profileFdn);
             setNodeConfigSnapshotStatus(ConfigSnapshotStatus.FAILED, profileFdn);
         }
    }

    protected boolean deleteFileAndDirectory(final String filePath) {
        try {
            return (StringUtils.isNotEmpty(filePath) &&
                    doesFileExist(filePath) &&
                    isFileDeleted(filePath) &&
                    deleteDirectoryIfEmpty(filePath));
        } catch (final Exception e) {
            logger.warn("Could not delete the directory {}", filePath, e);
            return false;
        }
    }

    protected int writeFile(final String filePath, final byte[] content, final boolean append) {
        return resourceService.write(filePath, content, append);
    }

    protected boolean doesFileExist(final String filePath) {
        return resourceService.exists(filePath);
    }

    protected boolean isFileDeleted(final String filePath) {
        return resourceService.delete(filePath);
    }

    protected String getConfigurationSnapshotPath(final String nodeName, final String fileDir) {
        return String.format("%s/%s/%s_SNAPSHOT.xml", fileDir, nodeName, nodeName);
    }

    protected String getFilterLocation(final String profileFdn) {
        final ManagedObject profileMo = getProfileMo(profileFdn);
        return profileMo.getAttribute(ProfileAttribute.FILTER_LOCATION.toString());
    }

    private boolean deleteDirectoryIfEmpty(final String filePath) {
        final String parentDir = new File(filePath).getParent();

        return ((resourceService.isDirectoryExists(parentDir)) && resourceService.deleteDirectoryIfEmpty(parentDir));
    }

    protected ManagedObject getProfileMo(final String profileFdn) {
        final ManagedObject profileMo = dpsOperations.getDataPersistenceService().getLiveBucket().findMoByFdn(profileFdn);
        if (profileMo == null) {
            throw new ProfileNotFoundException(profileFdn);
        }
        return profileMo;
    }
}
