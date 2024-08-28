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
package com.ericsson.oss.services.ap.common.model;

/**
 * An attribute in the AP <code>ConfigurationProfile</code> model.
 */
public enum ProfileAttribute {

    PROFILE_ID("profileId"),
    PROPERTIES("properties"),
    VERSION("version"),
    OSS_MODEL_IDENTITY("ossModelIdentity"),
    UPGRADE_PACKAGE_NAME("upgradePackageName"),
    PRODUCT_NUMBER("productNumber"),
    PRODUCT_RELEASE("productRelease"),
    GRAPHIC_LOCATION("graphicLocation"),
    GRAPHIC("graphic"),
    PROFILE_CONTENT_LOCATION("profileContentLocation"),
    CONFIGURATIONS("configurations"),
    CIQ("ciq"),
    CIQ_LOCATION("ciqLocation"),
    STATUS("profileStatus"),
    IS_VALID("isValid"),
    PROFILE_DETAILS("profileDetails"),
    DATATYPE("dataType"),
    CONFIG_SNAPSHOT_STATUS("configSnapshotStatus"),
    DUMP_TIMESTAMP("dumpTimeStamp"),
    GET_CONFIG_SCRIPT("getConfigScript"),
    FILTER_LOCATION("filterLocation");

    private String attributeName;

    private ProfileAttribute(final String attributeName) {
        this.attributeName = attributeName;
    }

    @Override
    public String toString() {
        return attributeName;
    }

}
