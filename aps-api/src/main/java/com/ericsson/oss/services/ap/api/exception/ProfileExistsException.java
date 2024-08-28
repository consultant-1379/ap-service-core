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
package com.ericsson.oss.services.ap.api.exception;

/**
 * Thrown to indicate that an AP profile under the project already exists on the system
 */
public class ProfileExistsException extends ApApplicationException {

    private static final long serialVersionUID = -4236120756757544114L;

    private final String profileName;
    private final String projectName;

    /**
     * Exception with the name of the profile and project
     *
     * @param projectName AP project name which contains existing profile
     * @param profileName the name of the profile
     */
    public ProfileExistsException(final String projectName, final String profileName) {
        this.projectName = projectName;
        this.profileName = profileName;
    }

    /**
     * Returns the name of AP profile name which exists under the project in the database.
     *
     * @return the name of the profile
     */
    public String getProfileName() {
        return profileName;
    }

    /**
     * Returns the name of AP project name which contains existing profile.
     *
     * @return the name of the project
     */
    public String getProjectName() {
        return projectName;
    }
}
