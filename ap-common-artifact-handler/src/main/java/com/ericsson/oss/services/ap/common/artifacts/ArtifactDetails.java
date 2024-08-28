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
package com.ericsson.oss.services.ap.common.artifacts;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Details of raw or generated artifact.
 */
public final class ArtifactDetails implements Serializable {

    private static final long serialVersionUID = 1L;

    private String apNodeFdn;
    private String name;
    private String extension;
    private String type;
    private String location;
    private boolean exportable;
    private boolean encrypted;
    private byte[] artifactContent;
    private ArtifactImportProgress importProgress;
    private List<String> importErrorMessage;
    private String configurationNodeName;
    private ArtifactFileFormat fileFormat;
    private boolean ignoreError;

    private ArtifactDetails() {

    }

    /**
     * Get the node FDN.
     *
     * @return the apNodeFdn
     */
    public String getApNodeFdn() {
        return apNodeFdn;
    }

    /**
     * Get the name of the file without extension.
     *
     * @return the name of the file
     */
    public String getName() {
        return name;
    }

    /**
     * Get the name of the file including the file extension.
     *
     * @return the name of the file including the file extension
     */
    public String getNameWithExtension() {
        return extension == null ? name : name + "." + extension;
    }

    /**
     * Gets the file extension.
     *
     * @return the file extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Gets the file type, e.g SCHEMA or SAMPLE.
     *
     * @return the file type
     */
    public String getType() {
        return type;
    }

    /**
     * Get the location of the artifact on the file system.
     *
     * @return the artifact location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Get the configuration nodename of the artifact.
     *
     * @return the artifact configuration nodename
     */
    public String getConfigurationNodeName() {
        return configurationNodeName;
    }

    /**
     * Get the file format of the artifact.
     *
     * @return the artifact file format
     */
    public ArtifactFileFormat getFileFormat() {
        return fileFormat == null ? ArtifactFileFormat.UNKNOWN : fileFormat;
    }

    /**
     * Is the file allowed to be exported from the system.
     *
     * @return true if export is allowed
     */
    public boolean isExportable() {
        return exportable;
    }

    /**
     * Is the import errors in this artifact ignored.
     *
     * @return true if error is ignored.
     */
    public boolean isIgnoreError() {
        return ignoreError;
    }

    /**
     * Will this file be encrypted when created on filesystem.
     *
     * @return true if file will be encrypted when created on filesystem
     */
    public boolean isEncrypted() {
        return encrypted;
    }

    /**
     * Is the artifact file content not empty
     *
     * @return true if the artifact file content not empty
     */
    public boolean isNotEmptyContent() {
        return artifactContent != null;
    }

    /**
     * Get the contents of the artifact as a String.
     *
     * @return representation of the artifact contents
     */
    public String getArtifactContent() {
        return artifactContent == null ? null : new String(artifactContent);
    }

    /**
     * Get the contents of the artifact as a byte array.
     *
     * @return representation of the artifact contents
     */
    public byte[] getArtifactContentAsBytes() {
        return artifactContent == null ? null : artifactContent.clone();
    }

    /**
     * Get the import progress of the artifact.
     *
     * @return the import progress
     */
    public ArtifactImportProgress getImportProgress() {
        return importProgress;
    }

    /**
     * Get the import error message of the artifact.
     *
     * @return the import error message
     */
    public List<String> getImportErrorMessage() {
        return importErrorMessage;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }

        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }

        final ArtifactDetails that = (ArtifactDetails) other;
        return new EqualsBuilder()
                .append(apNodeFdn, that.apNodeFdn)
                .append(name, that.name)
                .append(extension, that.extension)
                .append(type, that.type)
                .append(location, that.location)
                .append(exportable, that.exportable)
                .append(encrypted, that.encrypted)
                .append(artifactContent, that.artifactContent)
                .append(importProgress, that.importProgress)
                .append(importErrorMessage, that.importErrorMessage)
                .append(configurationNodeName, that.configurationNodeName)
                .append(fileFormat, that.fileFormat)
                .append(ignoreError, that.ignoreError)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(apNodeFdn)
                .append(name)
                .append(extension)
                .append(type)
                .append(location)
                .append(exportable)
                .append(encrypted)
                .append(artifactContent)
                .append(importProgress)
                .append(importErrorMessage)
                .append(configurationNodeName)
                .append(fileFormat)
                .append(ignoreError)
                .toHashCode();
    }
    /**
     * Build an artifact.
     */
    public static class ArtifactBuilder {

        final ArtifactDetails artifact = new ArtifactDetails();

        /**
         * Set the fdn of the node to which the artifact belongs. Required if creating a new raw or generated artifact.
         *
         * @param apNodeFdn
         *            the <code>apNodeFdn</code> to be set
         * @return <code>ArtifactBuilder</code>
         */
        public ArtifactBuilder apNodeFdn(final String apNodeFdn) {
            artifact.apNodeFdn = apNodeFdn;
            return this;
        }

        /**
         * Sets the name of the artifact. Name should include the file extension.
         *
         * @param name
         *            the artifact name
         * @return <code>ArtifactBuilder</code>
         */
        public ArtifactBuilder name(final String name) {
            artifact.name = getFilenameWithoutExtension(name);
            artifact.extension = getFileExtension(name);
            return this;
        }

        /**
         * Sets the type of the artifact
         *
         * @param type
         *            the artifact type
         * @return <code>ArtifactBuilder</code>
         */
        public ArtifactBuilder type(final String type) {
            artifact.type = type;
            return this;
        }

        /**
         * Sets the absolute path to the artifact on the filesystem. Not required if creating the artifact.
         *
         * @param location
         *            the <code>location</code> to be set
         * @return <code>ArtifactBuilder</code>
         */
        public ArtifactBuilder location(final String location) {
            artifact.location = location;
            return this;
        }

        /**
         * Sets this artifact to be encrypted when created on filesystem.
         *
         * @param encrypted
         *            whether the file should be encrypted
         * @return <code>ArtifactBuilder</code>
         */
        public ArtifactBuilder encrypted(final boolean encrypted) {
            artifact.encrypted = encrypted;
            return this;
        }

        /**
         * Sets if the artifact can be exported.
         *
         * @param exportable
         *            whether the file should be exportable
         * @return <code>ArtifactBuilder</code>
         */
        public ArtifactBuilder exportable(final boolean exportable) {
            artifact.exportable = exportable;
            return this;
        }

        /**
         * Sets the contents of the artifact.
         *
         * @param artifactContent
         *            the <code>artifactContent</code> to be set
         * @return <code>ArtifactBuilder</code>
         */
        public ArtifactBuilder artifactContent(final String artifactContent) {
            if (artifactContent != null) {
                artifact.artifactContent = artifactContent.getBytes();
            }
            return this;
        }

        /**
         * Sets the contents of the artifact.
         *
         * @param artifactContent
         *            the <code>artifactContent</code> to be set
         * @return <code>ArtifactBuilder</code>
         */
        public ArtifactBuilder artifactContent(final byte[] artifactContent) {
            artifact.artifactContent = artifactContent;
            return this;
        }

        /**
         * Sets the import progress of the artifact.
         *
         * @param importProgress
         *            the <code>importProgress</code> to be set
         * @return <code>ArtifactBuilder</code>
         */
        public ArtifactBuilder importProgress(final ArtifactImportProgress importProgress) {
            artifact.importProgress = importProgress;
            return this;
        }

        /**
         * Sets the import error message of the artifact.
         *
         * @param importErrorMessage
         *            the <code>importErrorMessage</code> to be set
         * @return <code>ArtifactBuilder</code>
         */
        public ArtifactBuilder importErrorMsg(final List<String> importErrorMessage) {
            artifact.importErrorMessage = importErrorMessage;
            return this;
        }

        /**
         * Sets the configuration nodename to the artifact.
         *
         * @param configurationNodeName
         *            the <code>configurationNodeName</code> to be set
         * @return <code>ArtifactBuilder</code>
         */
        public ArtifactBuilder configurationNodeName(final String configurationNodeName) {
            artifact.configurationNodeName = configurationNodeName;
            return this;
        }
        /**
         * Sets the ignoreError to the artifact.
         *
         * @param ignoreError
         *            the <code>ignoreError</code> to be set
         * @return <code>ArtifactBuilder</code>
         */
        public ArtifactBuilder ignoreError(final boolean ignoreError) {
            artifact.ignoreError = ignoreError;
            return this;
        }
        /**
         * Sets the file format to the artifact.
         *
         * @param fileFormat
         *            the <code>fileFormat</code> to be set
         * @return <code>ArtifactBuilder</code>
         */
        public ArtifactBuilder fileFormat(final ArtifactFileFormat fileFormat) {
            artifact.fileFormat = fileFormat;
            return this;
        }

        public ArtifactDetails build() {
            return artifact;
        }

        private static String getFilenameWithoutExtension(final String name) {
            if (name.contains(".")) {
                return name.substring(0, name.lastIndexOf('.'));
            }
            return name;
        }

        private static String getFileExtension(final String name) {
            if (name.contains(".")) {
                return name.substring(name.lastIndexOf('.') + 1);
            }
            return null;
        }
    }
}
