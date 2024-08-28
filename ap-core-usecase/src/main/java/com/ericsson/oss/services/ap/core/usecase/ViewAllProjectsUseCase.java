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
package com.ericsson.oss.services.ap.core.usecase;

import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.model.ProjectAttribute.CREATION_DATE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;
import com.ericsson.oss.services.ap.core.usecase.view.MoDataComparator;
import com.ericsson.oss.services.ap.core.usecase.view.ProjectData;

/**
 * View all the Projects.
 */
@UseCase(name = UseCaseName.VIEW_ALL_PROJECTS)
public class ViewAllProjectsUseCase {

    @Inject
    private DpsQueries dpsQueries;

    /**
     * Gets summary details for each project in AP model.
     *
     * @return a {@link List} of {@link MoData} containing attributes for each project
     * @throws ApApplicationException if there is an error reading the project data
     */
    public List<MoData> execute() {
        final List<MoData> viewProjectsData;

        try {
            viewProjectsData = readAllProjects();
        } catch (final Exception e) {
            throw new ApApplicationException("Error reading MOs for projects", e);
        }
        return viewProjectsData;
    }

    private List<MoData> readAllProjects() {
        final List<MoData> viewProjectsData = new ArrayList<>();
        final Iterator<ManagedObject> allProjectMos = dpsQueries.findMosByType(MoType.PROJECT.toString(), AP.toString())
            .execute();

        while (allProjectMos.hasNext()) {
            final MoData projectMo = ProjectData.createProjectData(allProjectMos.next());
            viewProjectsData.add(projectMo);
        }
        MoDataComparator.sortByAttribute(viewProjectsData, CREATION_DATE);

        return viewProjectsData;
    }
}
