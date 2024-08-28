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

import static com.ericsson.oss.services.ap.common.model.MoType.NODE_ARTIFACT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.cm.DpsQueries;
import com.ericsson.oss.services.ap.common.model.NodeArtifactAttribute;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.ericsson.oss.services.ap.core.usecase.qualifier.UseCase;

/**
 * View the details of an imported node.
 */
@UseCase(name = UseCaseName.VIEW_NODE)
public class ViewNodeUseCase {

    private static final String NODE_NAME_LABEL = "nodeName";
    private static final String PROJECT_NAME_LABEL = "projectName";

    @Inject
    private DpsOperations dps;

    @Inject
    private DpsQueries dpsQueries;

    @Inject
    private Logger logger;

    /**
     * Reads the model for an imported AP node
     *
     * @param nodeFdn
     *            the FDN of the node in AP model
     * @return a {@link List} of {@link MoData} containing node and child attribute data
     */
    public List<MoData> execute(final String nodeFdn) {
        logger.info("Viewing node {}", nodeFdn);

        final MoData nodeMo = findNodeMo(nodeFdn);
        addExtraNodeAttributes(nodeMo.getAttributes(), nodeMo);

        final List<MoData> allMoChildrenExcludingNodeArtifactMosWithNoRawArtifacts = readMoChildrenExcludingNodeArtifactMosWithNoRawArtifacts(nodeMo
                .getFdn());

        final List<MoData> moDataList = new ArrayList<>();
        moDataList.add(nodeMo);
        moDataList.addAll(allMoChildrenExcludingNodeArtifactMosWithNoRawArtifacts);

        return moDataList;
    }

    private static void addExtraNodeAttributes(final Map<String, Object> attributes, final MoData nodeMo) {
        final FDN nodeFdnWrapper = FDN.get(nodeMo.getFdn());
        attributes.put(NODE_NAME_LABEL, nodeFdnWrapper.getRdnValue());
        attributes.put(PROJECT_NAME_LABEL, FDN.get(nodeFdnWrapper.getParent()).getRdnValue());
    }

    private MoData findNodeMo(final String nodeFdn) {
        final ManagedObject nodeMo = dps.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
        return new MoData(nodeMo.getFdn(), nodeMo.getAllAttributes(), nodeMo.getType(), new ModelData(nodeMo.getNamespace(), nodeMo.getVersion()));
    }

    private List<MoData> readMoChildrenExcludingNodeArtifactMosWithNoRawArtifacts(final String nodeFdn) {
        try {
            final List<MoData> nodeMoChildren = readAllMoChildren(nodeFdn);
            return removeNodeArtifactMosWithNoRawArtifacts(nodeMoChildren);
        } catch (final Exception e) {
            logger.warn("Error reading children for node {}: {}", nodeFdn, e.getMessage(), e);
        }
        return Collections.<MoData> emptyList();
    }

    private List<MoData> readAllMoChildren(final String nodeFdn) {
        final List<MoData> nodeChildrenMoData = new ArrayList<>();
        final Iterator<ManagedObject> nodeMoChildren = dpsQueries.findAllChildMos(nodeFdn).execute();
        while (nodeMoChildren.hasNext()) {
            final ManagedObject childMo = nodeMoChildren.next();
            final ModelData childModelData = new ModelData(childMo.getNamespace(), childMo.getVersion());
            nodeChildrenMoData.add(new MoData(childMo.getFdn(), childMo.getAllAttributes(), childMo.getType(), childModelData));

        }
        return nodeChildrenMoData;
    }

    private static List<MoData> removeNodeArtifactMosWithNoRawArtifacts(final List<MoData> nodeMoChildren) {
        final Iterator<MoData> moDataIterator = nodeMoChildren.iterator();

        while (moDataIterator.hasNext()) {
            final MoData nodeMoChild = moDataIterator.next();
            if (nodeMoChild.getType().equals(NODE_ARTIFACT.toString())
                    && nodeMoChild.getAttribute(NodeArtifactAttribute.RAW_LOCATION.toString()) == null) {
                moDataIterator.remove();
            }
        }
        return nodeMoChildren;
    }
}
