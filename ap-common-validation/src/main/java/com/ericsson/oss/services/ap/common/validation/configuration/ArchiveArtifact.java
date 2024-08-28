/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.validation.configuration;

import java.nio.charset.StandardCharsets;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * This class is a data container to represent an artifact retrieved or stored in an archive.
 */
public class ArchiveArtifact {

    private static final char FOLDER_SEPARATOR = '/';

    private final String name;
    private final String absolutePathName;
    private final byte[] byteContents;
    private final String stringContents;

    public ArchiveArtifact(final String absolutePathName, final byte[] byteContents) {
        this.absolutePathName = absolutePathName;
        name = formatName(absolutePathName);
        this.byteContents = byteContents.clone();
        stringContents = new String(byteContents, StandardCharsets.UTF_8);
    }

    public ArchiveArtifact(final String absolutePathName, final String stringContents) {
        this.absolutePathName = absolutePathName;
        name = formatName(absolutePathName);
        this.stringContents = stringContents;
        byteContents = null;
    }

    /**
     * Get the contents of the artifact as a byte array
     *
     * @return artifact contents as a byte array
     */
    public byte[] getContentsAsBytes() {
        return byteContents.clone();
    }

    /**
     * Get the contents of the artifact as a string
     *
     * @return artifact contents as a string
     */
    public String getContentsAsString() {
        return stringContents;
    }

    /**
     * Get the name of the artifact, excluding any directories. E.g. for "node-001/nodeInfo.xml", it will return "nodeInfo.xml"
     *
     * @return the artifact name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the name including directory, e.g. E.g. "node-001/nodeInfo.xml"
     *
     * @return the artifact name with directory
     */
    public String getAbsoluteName() {
        return absolutePathName;
    }

    @Override
    public String toString() {
        return absolutePathName;
    }

    private static String formatName(final String absolutePathName) {
        final int index = absolutePathName.lastIndexOf(FOLDER_SEPARATOR);
        if (index > 0) {
            return absolutePathName.substring(index + 1);
        }

        return absolutePathName;
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }

        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }

        final ArchiveArtifact that = (ArchiveArtifact) other;
        return new EqualsBuilder()
                .append(absolutePathName, that.absolutePathName)
                .append(name, that.name)
                .append(stringContents, that.stringContents)
                .append(byteContents, that.byteContents)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(absolutePathName)
                .append(name)
                .append(stringContents)
                .append(byteContents)
                .toHashCode();
    }
}
