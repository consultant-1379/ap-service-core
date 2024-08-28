/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2012
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase;

import static com.ericsson.oss.services.ap.common.model.MoType.NODE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.api.exception.ApServiceException;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;
import com.ericsson.oss.services.ap.core.usecase.view.MoDataComparator;

/**
 * Gets the details of a single project in the AP model.
 */
@UseCase(name = UseCaseName.VIEW_PROJECT)
public class ViewProjectUseCase {

    @Inject
    private DpsOperations dps;

    @Inject
    private Logger logger;

    /**
     * Reads the model for an imported AP project.
     *
     * @param projectFdn
     *            the FDN of the project in AP model
     * @return a {@link List} of {@link MoData} containing project attributes
     */
    public List<MoData> execute(final String projectFdn) {
        logger.info("Viewing project {}", projectFdn);

        try {
            return readProjectAndNodes(projectFdn);
        } catch (final ApServiceException e) {
            throw e;
        } catch (final Exception e) {
            throw new ApApplicationException("Error viewing project " + projectFdn, e);
        }
    }

    private List<MoData> readProjectAndNodes(final String projectFdn) {
        final ManagedObject projectMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(projectFdn);
        final List<MoData> allNodeData = readAllNodeDataSortedByName(projectMo);

        final MoData projectMoData = new MoData(projectMo.getFdn(), projectMo.getAllAttributes(), projectMo.getType(),
            new ModelData(projectMo.getNamespace(), projectMo.getVersion()));
        addExtraProjectAtttributes(projectMo.getName(), projectMoData, allNodeData.size());

        final List<MoData> projectData = new ArrayList<>(allNodeData.size() + 1);
        projectData.add(projectMoData);
        projectData.addAll(allNodeData);
        return projectData;
    }

    private static List<MoData> readAllNodeDataSortedByName(final ManagedObject projectMo) {
        final Collection<ManagedObject> childrenMos = projectMo.getChildren();
        final Collection<ManagedObject> nodeMos = new ArrayList<>();

        for (final ManagedObject childMo : childrenMos) {
            if (childMo.getType().equals(NODE.toString())) {
                nodeMos.add(childMo);
            }
        }

        final List<MoData> allNodeData = new ArrayList<>(nodeMos.size());
        for (final ManagedObject nodeMo : nodeMos) {
            final MoData nodeMoData = new MoData(nodeMo.getFdn(), nodeMo.getAllAttributes(), nodeMo.getType(), new ModelData(nodeMo.getNamespace(),
                nodeMo.getVersion()));
            addExtraNodeAttributes(nodeMoData);
            allNodeData.add(nodeMoData);
        }

        MoDataComparator.sortByName(allNodeData);
        return allNodeData;
    }

    private static void addExtraProjectAtttributes(final String projectName, final MoData projectMo, final int nodeQuantity) {
        final Map<String, Object> projectAttributes = projectMo.getAttributes();
        projectAttributes.put("projectName", projectName);
        projectAttributes.put("nodeQuantity", String.valueOf(nodeQuantity));
    }

    private static void addExtraNodeAttributes(final MoData nodeMo) {
        final String nodeName = FDN.get(nodeMo.getFdn()).getRdnValue();
        final Map<String, Object> attributes = nodeMo.getAttributes();
        attributes.put("nodeName", nodeName);
    }
}
