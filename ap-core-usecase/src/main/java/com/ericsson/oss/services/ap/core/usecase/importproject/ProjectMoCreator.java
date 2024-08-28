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
package com.ericsson.oss.services.ap.core.usecase.importproject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.Namespace;
import com.ericsson.oss.services.ap.common.model.ProjectAttribute;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.cdi.Transactional;
import com.ericsson.oss.services.ap.common.util.cdi.Transactional.TxType;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;

/**
 * Creates the {@link MoType#PROJECT} MO.
 */
public class ProjectMoCreator {

    @Inject
    private DpsOperations dps;

    @Inject
    private DdpTimer ddpTimer;

    @Inject
    private ModelReader modelReader;

    /**
     * Creates overall project information MO in AP model in a separate transaction. DPS will block the database until the root MO (project MO)
     * transaction commits. So it can import multiple projects in parallel.
     *
     * @param projectInfo the project archive information
     * @return the created project's FDN
     */
    @Transactional(txType = TxType.REQUIRES)
    public String create(final ProjectInfo projectInfo) {
        final String projectName = projectInfo.getName();
        final Map<String, Object> projectAttributes = getProjectAttributes(projectInfo);

        ddpTimer.start(CommandLogName.CREATE_PROJECT_MO.toString());
        final String projectFdn = createProjectMo(projectName, projectAttributes).getFdn();
        ddpTimer.end(projectFdn, projectInfo.getNodeQuantity());
        return projectFdn;
    }

    /**
     * Creates project MO while adding creation date
     *
     * @param name        project name
     * @param creator     username of person who created project
     * @param description project description
     * @return {@link ManagedObject} contains newly created project MO
     */
    @Transactional(txType = TxType.REQUIRES)
    public ManagedObject create(final String name, final String creator, final String description) {
        final Map<String, Object> projectAttributes = new HashMap<>();
        projectAttributes.put(ProjectAttribute.DESCRIPTION.toString(), description);
        projectAttributes.put(ProjectAttribute.CREATOR.toString(), creator);
        projectAttributes.put(ProjectAttribute.GENERATED_BY.toString(), "ECT");
        addCreationDate(projectAttributes);
        ddpTimer.start(CommandLogName.CREATE_PROJECT_MO.toString());
        final ManagedObject managedObject = createProjectMo(name, projectAttributes);
        final String fdn = managedObject.getFdn();
        ddpTimer.end(fdn, managedObject.getChildrenSize());
        return managedObject;
    }

    private static Map<String, Object> getProjectAttributes(final ProjectInfo projectInfo) {
        final Map<String, Object> projectAttributes = projectInfo.getProjectAttributes();
        addCreationDate(projectAttributes);
        return projectAttributes;
    }

    private static void addCreationDate(final Map<String, Object> projectAttributes) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        projectAttributes.put(ProjectAttribute.CREATION_DATE.toString(), dateFormat.format(new Date()));
    }

    private ManagedObject createProjectMo(final String projectName, final Map<String, Object> projectAttributes) {
        final ModelData apModelData = modelReader.getLatestPrimaryTypeModel(Namespace.AP.toString(), MoType.PROJECT.toString());
        return dps.getDataPersistenceService().getLiveBucket()
            .getMibRootBuilder()
            .namespace(apModelData.getNameSpace())
            .version(apModelData.getVersion())
            .type(MoType.PROJECT.toString())
            .name(projectName)
            .addAttributes(projectAttributes)
            .create();
    }
}
