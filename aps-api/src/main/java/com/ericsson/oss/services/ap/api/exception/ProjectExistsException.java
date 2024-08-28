/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2017
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
 * Thrown to indicate that an AP project already exists on the system
 */
public class ProjectExistsException extends ApApplicationException {

    private static final long serialVersionUID = 8598524563622764196L;

    private final String projectName;

    /**
     * Exception with only the name of the project.
     *
     * @param projectName the name of the project
     */
    public ProjectExistsException(final String projectName) {
        this.projectName = projectName;
    }

    /**
     * Returns the name of AP project name which exists in the database.
     *
     * @return the name of the project
     */
    public String getProjectName() {
        return projectName;
    }
}
