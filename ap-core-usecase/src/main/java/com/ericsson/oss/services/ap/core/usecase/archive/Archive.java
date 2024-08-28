/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.archive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;

/**
 * Class that provides access to an AP Project Archive (e.g zip, batch). An archive has one or more directories that contain files or artifacts. All
 * methods are case insensitive.
 */
public class Archive {

    private final Map<String, ArchiveArtifact> artifactMap;
    private final List<String> directoryNames;
    private final List<ArchiveArtifact> artifactList;

    public Archive(final Map<String, ArchiveArtifact> map) {
        artifactMap = map;
        directoryNames = extractDirectoryNames(map);
        artifactList = extractArtifactList(map);
    }

    /**
     * Get all the contents of a specified directory. This method is case insensitive.
     *
     * @param dirName
     *            the case insensitive directory name.
     * @return the ArchiveArtifacts in the specified directory.
     */
    public List<ArchiveArtifact> getArtifactsInDirectory(final String dirName) {
        final String trimmedDirName = dirName.trim();
        if (!trimmedDirName.endsWith("/")) {
            return getArtifactsInDirectory(trimmedDirName + "/");
        }

        return getArtifactsByPattern(trimmedDirName + ".+");
    }

    /**
     * Retrieves the artifact content for the named artifact from the archive. This method is case insensitive.
     * <p>
     * Archives can contain subdirectories so a full path is required to pull out an artifact by name if it is located in a subdirectory.
     *
     * <pre>
     *     getArtifactContent("projectInfo.xml")
     *     getArtifactContent("node01/site.xml")
     * </pre>
     *
     *
     * @param artifactName
     *            the case insensitive name of the artifact to retrieve. This must not be <code>null</code>
     * @return the contents of the named artifact as a String if the artifact exists or <code>null</code> if the artifact does not exist
     */
    public String getArtifactContentAsString(final String artifactName) {
        for (final Entry<String, ArchiveArtifact> artifactEntry : artifactMap.entrySet()) {
            if (artifactEntry.getKey().equalsIgnoreCase(artifactName)) {
                final ArchiveArtifact artifact = artifactEntry.getValue();

                if (artifact != null) {
                    return artifact.getContentsAsString();
                }
            }
        }
        return null;
    }

    /**
     * Retrieves the artifact content for the named artifact from the archive. This method is case insensitive.
     * <p>
     * Archives can contain subdirectories so a full path is required to pull out an artifact by name if it is located in a subdirectory.
     *
     * <pre>
     *     getArtifactContent("projectInfo.xml")
     *     getArtifactContent("node01/site.xml")
     * </pre>
     *
     *
     * @param artifactName
     *            the case insensitive name of the artifact to retrieve. This must not be <code>null</code>
     * @return the contents of the named artifact as a byte array if the artifact exists or <code>null</code> if the artifact does not exist
     */
    public byte[] getArtifactContentAsBytes(final String artifactName) {
        for (final Entry<String, ArchiveArtifact> artifactEntry : artifactMap.entrySet()) {
            if (artifactEntry.getKey().equalsIgnoreCase(artifactName)) {
                final ArchiveArtifact artifact = artifactEntry.getValue();

                if (artifact != null) {
                    return artifact.getContentsAsBytes();
                }
            }
        }
        return new byte[0];
    }

    /**
     * Retrieves a list of artifacts that have the specified name. Multiple files can have the same name as they can exist in different directories.
     * This can, for example, return all the "SiteBasic.xml" files. This method is case insensitive.
     *
     * @param artifactName
     *            the case insensitive artifact name
     * @return list of ArchiveArtifact, empty list if none exist
     */
    public List<ArchiveArtifact> getArtifactsOfName(final String artifactName) {
        final List<ArchiveArtifact> list = new ArrayList<>();
        for (final ArchiveArtifact artifact : artifactMap.values()) {
            if (artifact.getName().equalsIgnoreCase(artifactName)) {
                list.add(artifact);
            }
        }
        return list;
    }

    /**
     * Retrieves an artifact with the specified name and in the specified directory. This method is case insensitive.
     *
     * @param dirName
     *            the case insensitive directory name.
     * @param artifactName
     *            the case insensitive artifact name
     * @return the requested artifact or null.
     */
    public ArchiveArtifact getArtifactOfNameInDir(final String dirName, final String artifactName) {
        final String absoluteName = dirName + "/" + artifactName;
        for (final ArchiveArtifact artifact : artifactMap.values()) {
            if (artifact.getAbsoluteName().equalsIgnoreCase(absoluteName)) {
                return artifact;
            }
        }
        return null;
    }

    /**
     * Retrieves a list of artifacts that match the supplied regular expression. Java regular expression syntax is supported. This method is case
     * insensitive and ignores the case of the characters in the search expression.
     * <p>
     * The artifacts are returned in the same order that they were retrieved from the archive.
     *
     * @param theArchivePattern
     *            A case insensitive regular expression defining the artifacts to retrieve
     * @return a list of artifacts whose name matches the regular expression. This list is never <code>null</code> but may be empty.
     */
    public List<ArchiveArtifact> getArtifactsByPattern(final String theArchivePattern) {
        final List<ArchiveArtifact> list = new ArrayList<>();
        final Pattern pattern = Pattern.compile(theArchivePattern, Pattern.CASE_INSENSITIVE);
        for (final ArchiveArtifact artifact : artifactList) {
            if (pattern.matcher(artifact.getAbsoluteName()).matches()) {
                list.add(artifact);
            }
        }

        return list;
    }

    /**
     * Get the total number of artifacts in the archive
     *
     * @return the number of artifacts
     */
    public int getNumberOfArtifacts() {
        return artifactList.size();
    }

    /**
     * Gets all artifacts in the zip archive
     *
     * @return all artifacts
     */
    public List<ArchiveArtifact> getAllArtifacts() {
        return Collections.unmodifiableList(artifactList);
    }

    /**
     * Get all the directory names in the archive.
     *
     * @return collection of directory name.
     */
    public List<String> getAllDirectoryNames() {
        return Collections.unmodifiableList(directoryNames);
    }

    private static List<ArchiveArtifact> extractArtifactList(final Map<String, ArchiveArtifact> map) {
        final List<ArchiveArtifact> result = new ArrayList<>();
        for (final Entry<String, ArchiveArtifact> entry : map.entrySet()) {
            result.add(entry.getValue());
        }

        return result;
    }

    private static List<String> extractDirectoryNames(final Map<String, ArchiveArtifact> map) {
        final List<String> result = new ArrayList<>();
        for (final Entry<String, ArchiveArtifact> entry : map.entrySet()) {
            addAllFolders(entry.getKey(), result);
        }

        return result;
    }

    private static void addAllFolders(final String folder, final List<String> folders) {
        int index = -1;
        while ((index = folder.indexOf('/', index + 1)) > 0) {
            final String subFolder = folder.substring(0, index);
            if (!folders.contains(subFolder)) {
                folders.add(subFolder);
            }
        }
    }
}
