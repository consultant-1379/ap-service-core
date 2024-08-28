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

import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.exception.general.AlreadyDefinedException;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ProjectExistsException;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.usecase.importproject.ProjectMoCreator;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;
import com.ericsson.oss.services.ap.core.usecase.view.ProjectData;

/**
 * Creates project MO containing no nodes.
 */
@UseCase(name = UseCaseName.CREATE_PROJECT)
public class CreateProjectUseCase {

    @Inject
    private Logger logger;

    @Inject
    private ProjectMoCreator projectMoCreator;

    /**
     * Creates project MO in database containing no nodes
     *
     * @param name        Project name
     * @param creator     Logged in user that creates project
     * @param description Project description
     * @return {@link MoData} containing created project MO
     */
    public MoData execute(final String name, final String creator, final String description) {
        try {
            final ManagedObject projectMo = projectMoCreator.create(name, creator, description);
            return ProjectData.createProjectData(projectMo);
        } catch (final AlreadyDefinedException e) {
            logger.error(name, "Project with name {0} already exists", e);
            throw new ProjectExistsException(name);
        }

    }

}
