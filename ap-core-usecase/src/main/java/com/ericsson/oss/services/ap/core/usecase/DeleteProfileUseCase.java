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
package com.ericsson.oss.services.ap.core.usecase;

import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * Deletes ConfigurationProfile MO and files
 */
@UseCase(name = UseCaseName.DELETE_PROFILE)
public class DeleteProfileUseCase extends BaseProfileUseCase {
    /**
     * Deletes the ConfigurationProfile MO in database and it's files.
     *
     * @param projectId The id of the given Project
     * @param profileId The id of the given ConfigurationProfile
     */
    public void execute(final String projectId, final String profileId) {
        final String profileFdn = String.format("Project=%s,ConfigurationProfile=%s", projectId, profileId);
        this.checkIfProfileExists(profileFdn);

        if (profileStorageHandler.profileHasFiles(projectId, profileId)) {
            profileStorageHandler.deleteProfileDirectory(projectId, profileId);
        }

        if (profileStorageHandler.isProfilesRootFolderEmpty(projectId)) {
            profileStorageHandler.deleteProfileRootDirectory(projectId);
        }

        dpsOperations.deleteMo(profileFdn);
    }
}
