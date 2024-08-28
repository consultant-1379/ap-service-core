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
package com.ericsson.oss.services.ap.common.model;

/**
 * An attribute in the <code>NodeArtifact</code> model.
 */
public enum NodeArtifactAttribute {

    DESCRIPTION("description"),
    EXPORTABLE("exportable"),
    ENCRYPTED("encrypted"),
    GEN_LOCATION("generatedLocation"),
    NAME("name"),
    RAW_LOCATION("rawLocation"),
    NODE_ARTIFACT_ID("NodeArtifactId"),
    TYPE("type"),
    IMPORT_PROGRESS("importProgress"),
    IMPORT_ERR_MSG("importErrorMessage"),
    CONFIGURATION_NODE_NAME("configurationNodeName"),
    FILE_FORMAT("fileFormat"),
    IGNORE_ERROR("ignoreError");

    private String attributeName;

    private NodeArtifactAttribute(final String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public String toString() {
        return attributeName;
    }
}
