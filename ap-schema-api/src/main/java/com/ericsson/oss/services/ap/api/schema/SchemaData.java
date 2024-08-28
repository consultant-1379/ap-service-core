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
package com.ericsson.oss.services.ap.api.schema;

import java.io.Serializable;

/**
 * File data for an AutoProvisioning sample or schema file.
 */
public class SchemaData implements Serializable {

    private static final long serialVersionUID = -6095970518940253258L;

    private final String name;
    private final String extension;
    private final String type;
    private final String identifier;
    private final byte[] data;
    private final String artifactLocation;

    /**
     * Construct file data.
     *
     * @param name
     *            the name of the file including file extension - cannot be null or empty
     * @param type
     *            type of the file
     * @param identifier
     *            identifier of the file
     * @param data
     *            the byte data representing the file contents - cannot be null or empty
     * @param artifactLocation
     *            the location of the artifact
     */
    public SchemaData(final String name, final String type, final String identifier, final byte[] data, final String artifactLocation) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("'Name' must not be null or empty");
        }
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("'Data' must not be null or empty");
        }

        this.name = getFilenameWithoutExtension(name);
        extension = getFileExtension(name);
        this.type = type;
        this.identifier = identifier;
        this.data = data.clone();
        this.artifactLocation = artifactLocation;
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
     * Gets the file extension.
     *
     * @return the file extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Gets the file type. Can be:
     * <ul>
     * <li>SCHEMA</li>
     * <li>SAMPLE</li>
     * </ul>
     *
     * @return the file type
     */
    public String getType() {
        return type;
    }

    /**
     * Get the byte[] representation of the file contents.
     *
     * @return file contents as byte[]
     */
    public byte[] getData() {
        return data.clone();
    }

    /**
     * Get the identifier of the file.
     *
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get the location of the artifact.
     *
     * @return the artifactLocation
     */
    public String getArtifactLocation() {
        return artifactLocation;
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
