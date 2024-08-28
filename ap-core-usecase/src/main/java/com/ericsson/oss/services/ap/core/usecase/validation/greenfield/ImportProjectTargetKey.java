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
package com.ericsson.oss.services.ap.core.usecase.validation.greenfield;

/**
 * Key available in <code>ValidationContext</code> target map for validation of a project archive.
 */
public enum ImportProjectTargetKey {

    FILENAME("fileName"),
    FILE_CONTENT("fileContent"),
    DIRECTORY_LIST("directoryList");

    private String key;

    private ImportProjectTargetKey(final String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return key;
    }
}
