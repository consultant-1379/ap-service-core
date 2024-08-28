/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.api.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Namespace information for the model.
 */
public class ModelData implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nameSpace;
    private final String version;

    /**
     * Constructs an instance of {@link ModelData}.
     *
     * @param nameSpace
     *            the model namespace - cannot be null or an empty string
     * @param nameSpaceVersion
     *            the version of the namespace - cannot be null or an empty string
     */
    public ModelData(final String nameSpace, final String nameSpaceVersion) {
        if (nameSpace == null || nameSpace.isEmpty()) {
            throw new IllegalArgumentException("nameSpace cannot be null or empty");
        }
        if (nameSpaceVersion == null || nameSpaceVersion.isEmpty()) {
            throw new IllegalArgumentException("nameSpaceVersion cannot be null or empty");
        }
        this.nameSpace = nameSpace;
        version = nameSpaceVersion;
    }

    /**
     * Get the namespace.
     *
     * @return the nameSpace
     */
    public String getNameSpace() {
        return nameSpace;
    }

    /**
     * Get the model version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameSpace, version);
    }

    @Override
    public boolean equals(final Object other) {
        if (other == null || this.getClass() != other.getClass()) {
            return false;
        }
        final ModelData otherModelData = (ModelData) other;
        return other == this || (nameSpace.equals(otherModelData.getNameSpace()) && version.equals(otherModelData.getVersion()));
    }
}
