/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.workflow;

import java.util.stream.Stream;

import com.ericsson.oss.services.ap.api.exception.InvalidArgumentsException;

/**
 * Define the valid activity types for AP workflow.
 */
public enum ActivityType {
    EXPANSION_ACTIVITY("expansion"),
    GREENFIELD_ACTIVITY("greenfield"),
    HARDWARE_REPLACE_ACTIVITY("hardwareReplace"),
    MIGRATION_ACTIVITY("migration"),
    EOI_INTEGRATION_ACTIVITY("eoiIntegration");

    private final String activityName;

    private ActivityType(final String activityName) {
        this.activityName = activityName;
    }

    /**
     * Returns the {@link ActivityType} in the String format.
     *
     * @return the name of the {@link ActivityType}
     */
    public String getActivityName() {
        return activityName;
    }

    /**
     * Returns the {@link ActivityType} for input string.
     *
     * @param activity
     * @return the corresponding {@link ActivityType} value
     *          and throw InvalidArgumentsException if not found
     */
    public static ActivityType from(final String activity) {
        return Stream.of(ActivityType.values())
                .filter(enumActivity -> enumActivity.getActivityName().equals(activity))
                .findFirst()
                .orElseThrow(() -> new InvalidArgumentsException(String.format("Invalid input activity type : %s", activity)));
    }
}
