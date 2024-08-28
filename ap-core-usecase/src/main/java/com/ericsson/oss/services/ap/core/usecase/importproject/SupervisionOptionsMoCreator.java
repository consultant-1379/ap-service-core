/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.importproject;

import static com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes.NODE_TYPE;
import static com.ericsson.oss.services.ap.common.model.MoType.SUPERVISION_OPTIONS;

import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import static com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes.SUPERVISION_ATTRIBUTES;
/**
 * Creates the {@link MoType#SUPERVISION_OPTIONS} MO from the input {@link NodeInfo}.
 */
public class SupervisionOptionsMoCreator {

    @Inject
    private DpsOperations dps;

    @Inject
    private ModelReader modelReader;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    /**
     * Write a <code>SupervisionOptions</code> MO to the AP model with attributes read from the nodeInfo.xml.
     *
     * @param nodeMo
     *            the AP <code>Node</code> MO
     * @param nodeInfo
     *            the node data from nodeInfo.xml
     * @return the created <code>SupervisionOptions</code> MO
     */
    public ManagedObject create(final ManagedObject nodeMo, final NodeInfo nodeInfo) {
        final Map<String, Object> nodeSupervisionOptions = nodeInfo.getSupervisionAttributes();
        final String apNamespace = nodeTypeMapper.getNamespace(nodeInfo.getNodeType());
        final ModelData supervisionModelData = modelReader.getLatestPrimaryTypeModel(apNamespace, SUPERVISION_OPTIONS.toString());

        return dps.getDataPersistenceService().getLiveBucket()
            .getMibRootBuilder()
            .parent(nodeMo)
            .namespace(supervisionModelData.getNameSpace())
            .version(supervisionModelData.getVersion())
            .type(SUPERVISION_OPTIONS.toString())
            .name("1")
            .addAttributes(nodeSupervisionOptions)
            .create();
    }

    public ManagedObject eoiCreate(final ManagedObject nodeMo, final Map<String , Object> nodeData) {

        if(nodeData.containsKey(SUPERVISION_ATTRIBUTES.toString())) {
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setSupervisionAttributes((Map<String, Object>) nodeData.get(SUPERVISION_ATTRIBUTES.toString()));
            nodeInfo.setNodeType((String) nodeData.get(NODE_TYPE.toString()));
            return create(nodeMo, nodeInfo);
        }
        return null;
    }

}
