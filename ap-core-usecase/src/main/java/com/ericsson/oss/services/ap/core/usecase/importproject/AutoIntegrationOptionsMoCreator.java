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

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;

/**
 * Creates the <code>AutoIntegrationOptions</code> MO from the input {@link NodeInfo}.
 */
public class AutoIntegrationOptionsMoCreator {

    @Inject
    private DpsOperations dpsOperations;

    @Inject
    private ModelReader modelReader;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    /**
     * Creates an {@link MoType#AI_OPTIONS} MO to AP model with attributes read from the supplied {@link NodeInfo}.
     *
     * @param nodeMo
     *            the AP node MO
     * @param nodeInfo
     *            the node data from nodeInfo.xml
     * @return the created AutoIntegrationOptions MO
     */
    public ManagedObject create(final ManagedObject nodeMo, final NodeInfo nodeInfo) {
        final String apNamespace = nodeTypeMapper.getNamespace(nodeInfo.getNodeType());
        final ModelData aiOptionsModelData = modelReader.getLatestPrimaryTypeModel(apNamespace, MoType.AI_OPTIONS.toString());

        return dpsOperations.getDataPersistenceService().getLiveBucket()
            .getMibRootBuilder()
            .parent(nodeMo)
            .namespace(apNamespace)
            .version(aiOptionsModelData.getVersion())
            .type(MoType.AI_OPTIONS.toString())
            .name("1")
            .addAttributes(nodeInfo.getIntegrationAttributes())
            .create();
    }
}
