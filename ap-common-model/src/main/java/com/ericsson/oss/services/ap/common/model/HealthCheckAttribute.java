/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
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
 * Attribute names in the {@code HealthCheck} MO.
 */
public enum HealthCheckAttribute {

    HEALTH_CHECK_PROFILE_NAME("healthCheckProfileName"),
    PRE_REPORT_IDS("preReportIds"),
    POST_REPORT_IDS("postReportIds");

    private String attributeName;
    private String tagName;

    private HealthCheckAttribute(final String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeName() {
        return this.attributeName;
    }

    public String getTagName() {
        return this.tagName;
    }

    @Override
    public String toString() {
        return attributeName;
    }
}
