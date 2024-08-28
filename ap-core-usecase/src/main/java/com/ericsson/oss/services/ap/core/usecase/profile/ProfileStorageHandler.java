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
package com.ericsson.oss.services.ap.core.usecase.profile;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;

import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.itpf.sdk.resources.Resource;
import com.ericsson.oss.services.ap.api.resource.ResourceService;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.util.file.File;

/**
 * Handler for all ConfigurationProfile File related operations.
 */
@Dependent
public class ProfileStorageHandler {

    @Inject
    private Logger logger;

    private ResourceService resourceService;

    @Inject
    private ArtifactResourceOperations artifactResourceOperations;

    private static final String BASE_PROJECT_DIRECTORY_LOCATION = DirectoryConfiguration.getProfileDirectory();

    private static final String PROFILE_BASE_PATH = BASE_PROJECT_DIRECTORY_LOCATION + "/%s/profiles";

    private static final String PROFILE_FILES_PATH = PROFILE_BASE_PATH + "/%s";

    private static final String CIQ_BASE_PATH = BASE_PROJECT_DIRECTORY_LOCATION + "/%s/profiles/%s/ciq";

    private static final String GRAPHIC_BASE_PATH = BASE_PROJECT_DIRECTORY_LOCATION + "/%s/profiles/%s/graphic";

    private static final String PROFILE_CONTENT_BASE_PATH = BASE_PROJECT_DIRECTORY_LOCATION + "/%s/profiles/%s/configuration";

    private static final String GET_CONFIG_SCRIPT_BASE_PATH = BASE_PROJECT_DIRECTORY_LOCATION + "/%s/profiles/%s/getConfigScript";

    @PostConstruct
    public void init() {
        resourceService = new ServiceFinderBean().find(ResourceService.class);
    }

    /**
     * Saves a graphic file to the storage.
     *
     * @param projectName    project name
     * @param profileName    profile name
     * @param graphicFileMap map of name,content
     * @return the full path where the file was stored
     */
    public String saveGraphicFile(final String projectName, final String profileName, final Map<String, Object> graphicFileMap) {
        if (isFilePresent(graphicFileMap)) {
            final String graphicBasePath = String.format(GRAPHIC_BASE_PATH,
                projectName,
                profileName);

            saveFile(graphicBasePath, graphicFileMap);

            return graphicBasePath;
        }
        return null;
    }

    /**
     * Saves a CIQ file to the storage.
     *
     * @param projectName project name
     * @param profileName profile name
     * @param ciqFileMap  map of name,content
     * @return the full path where the file was stored
     */
    public String saveCiqFile(final String projectName, final String profileName, final Map<String, Object> ciqFileMap) {
        if (isFilePresent(ciqFileMap)) {
            final String ciqLocationBasePath = String.format(CIQ_BASE_PATH,
                projectName,
                profileName);

            saveFile(ciqLocationBasePath, ciqFileMap);

            return ciqLocationBasePath;
        }

        return null;
    }

    /**
     * Saves all configuration files to the storage.
     *
     * @param projectName    project name
     * @param profileName    profile name
     * @param configurations list of map of name, content
     * @return the full path where the file was stored
     */
    public String saveConfigurationFiles(final String projectName, final String profileName, final List<Map<String, Object>> configurations) {
        if (CollectionUtils.isNotEmpty(configurations)) {
            final String profileContentBaseFilePath = String.format(PROFILE_CONTENT_BASE_PATH, projectName,
                profileName);

            saveConfigurationFiles(configurations, profileContentBaseFilePath);

            return profileContentBaseFilePath;
        }
        return null;
    }

    /**
     * Checks if the file map is not, empty and if the format is valid.
     *
     * @param fileMap map of name,content
     * @return true if fileMap is valid, false otherwise
     */
    public boolean isFilePresent(final Map<String, Object> fileMap) {
        return MapUtils.isNotEmpty(fileMap) && fileMap.containsKey("name") && fileMap.containsKey("content");
    }

    /**
     * Saves a file to the store on the given baseStoragePath.
     *
     * @param baseStoragePath base path where the file should be stored
     * @param fileMap         map of name,content
     */
    public void saveFile(final String baseStoragePath, final Map<String, Object> fileMap) {
        final String fileName = (String) fileMap.get("name");
        final String fileContent = (String) fileMap.get("content");
        final String profileConfigurationFilePath = (String.format("%s/%s", baseStoragePath, fileName));

        final byte[] decodedFileContent = File.decodeBase64FileContent(fileContent);
        artifactResourceOperations.writeArtifact(profileConfigurationFilePath, decodedFileContent);

        logger.info("File {} saved.", profileConfigurationFilePath);
    }

    private void saveConfigurationFiles(final List<Map<String, Object>> configurationFiles, final String basePath) {
        configurationFiles.stream().filter(this::isFilePresent).forEach(map -> saveFile(basePath, map));
    }

    /**
     * Checks if ConfigurationProfile has any files associated with it.
     *
     * @param projectId project name
     * @param profileId profile name
     * @return true if there's at least one file stored, false otherwise
     */
    public boolean profileHasFiles(final String projectId, final String profileId) {
        final String profileDirectoryLocation = String.format(PROFILE_FILES_PATH, projectId, profileId);
        return artifactResourceOperations.directoryExists(profileDirectoryLocation);
    }

    /**
     * Checks if the ConfigurationProfile root folder is empty.
     *
     * @param projectId project name
     * @return true if empty, false otherwise.
     */
    public boolean isProfilesRootFolderEmpty(final String projectId) {
        final String profileRootDirectoryLocation = String.format(PROFILE_BASE_PATH, projectId);
        return !artifactResourceOperations.directoryExistAndNotEmpty(profileRootDirectoryLocation);
    }

    /**
     * Deletes a ConfigurationProfile file directory.
     *
     * @param projectId project name
     * @param profileId profile name
     */
    public void deleteProfileDirectory(final String projectId, final String profileId) {
        final String profileDirectoryLocation = String.format(PROFILE_FILES_PATH, projectId, profileId);
        deleteDirectory(profileDirectoryLocation);
    }

    /**
     * Deletes the ConfigurationProfile root file directory.
     *
     * @param projectId project name
     */
    public void deleteProfileRootDirectory(final String projectId) {
        final String profileRootDirectoryLocation = String.format(PROFILE_BASE_PATH, projectId);
        deleteDirectory(profileRootDirectoryLocation);
    }

    /**
     * Returns a list of map (name,content) containing all the files in the location.
     *
     * @param location folder path where the files should be fetched.
     * @return list of map of name,content
     */
    public List<Map<String, Object>> getFilePathsInLocation(final String location) {
        if (location == null) {
            return Collections.emptyList();
        }

        // Don't call getBytes from the Resource directly because it leads to connection leak.
        final Collection<Resource> resourceFilePaths = resourceService.listFiles(location);

        if (resourceFilePaths != null && !resourceFilePaths.isEmpty()) {
            return resourceFilePaths.stream()
                .map(Resource::getName)
                .map(name -> File.toBase64EncodedMap(location, name))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private void deleteDirectory(final String directoryLocation) {
        artifactResourceOperations.deleteDirectory(directoryLocation);
        logger.info("Deleted directory {}", directoryLocation);
    }

    /**
     * Saves a getConfigScript file to the storage.
     *
     * @param projectName project name
     * @param profileName profile name
     * @param getConfigScriptFileMap  map of name,content
     * @return the full path where the file was stored
     */
    public String saveGetConfigScriptFile(final String projectName, final String profileName, final Map<String, Object> getConfigScriptFileMap) {
        if (isFilePresent(getConfigScriptFileMap)) {
            final String getConfigScriptLocationBasePath = String.format(GET_CONFIG_SCRIPT_BASE_PATH,
                projectName,
                profileName);

            saveFile(getConfigScriptLocationBasePath, getConfigScriptFileMap);

            return getConfigScriptLocationBasePath;
        }

        return null;
    }
}
