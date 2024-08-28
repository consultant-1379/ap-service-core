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

import static com.ericsson.oss.services.ap.api.model.eoi.ProjectRequestAttributes.NODE_TYPE;
import static com.ericsson.oss.services.ap.common.model.MoType.SECURITY;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.HierarchicalPrimaryTypeSpecification;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;

/**
 * Creates the {@link MoType#SECURITY} MO.
 */
public class SecurityMoCreator {

    @Inject
    private DpsOperations dps;

    @Inject
    private ModelReader modelReader;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    /**
     * If security attributes exist in the nodeInfo
     * then write Security MO as a child of the AP Node MO,
     * otherwise do not create the security child MO
     *
     * @param nodeMo
     * the node MO
     * @param nodeInfo
     * the node information from nodeInfo.xml
     * @return the created security MO
     */

    private static final String FH6000_NODE = "FRONTHAUL-6000";
    private static final String ENROLLMENT_MODE = "enrollmentMode";
    private static final String CMPV2_INITIAL = "CMPv2_INITIAL";
    private static final String IPSEC_LEVEL = "ipSecLevel";
    private static final String OAM = "OAM";

    public ManagedObject create(final ManagedObject nodeMo, final NodeInfo nodeInfo) {
        if (nodeInfo.getSecurityAttributes().isEmpty()) {
            return null;
        }
        return createSecurityMo(nodeMo, nodeInfo);
    }

    /**
     * Write Security MO as a child of the AP Node MO.
     *
     * @param nodeMo   the node MO
     * @param nodeInfo the node information from nodeInfo.xml
     * @return the created security MO
     */
    public ManagedObject createSecurityMo(final ManagedObject nodeMo, final NodeInfo nodeInfo) {
        final String apNamespace = nodeTypeMapper.getNamespace(nodeInfo.getNodeType());

        final HierarchicalPrimaryTypeSpecification securityModel = modelReader.getLatestPrimaryTypeSpecification(apNamespace, SECURITY.toString());
        final ModelInfo securityModelInfo = securityModel.getModelInfo();

        final Map<String, Object> securityAttributes = nodeInfo.getSecurityAttributes();
        if (nodeInfo.getNodeType() != null && !nodeInfo.getNodeType().isEmpty() && nodeInfo.getNodeType().equals(FH6000_NODE)) {
            securityAttributes.put(ENROLLMENT_MODE, CMPV2_INITIAL);
        }
        return dps.getDataPersistenceService().getLiveBucket()
            .getMibRootBuilder()
            .parent(nodeMo)
            .namespace(securityModelInfo.getNamespace())
            .version(securityModelInfo.getVersion().toString())
            .type(SECURITY.toString())
            .name("1")
            .addAttributes(securityAttributes)
            .create();
    }


    public ManagedObject eoiCreate(final ManagedObject nodeMo, final Map<String, Object> nodeData) {
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setNodeType((String) nodeData.get(NODE_TYPE.toString()));
            final Map<String, Object> securityAttributes = new HashMap<>();
            securityAttributes.put(IPSEC_LEVEL, OAM);
            nodeInfo.setSecurityAttributes(securityAttributes);
            return createSecurityMo(nodeMo, nodeInfo);
    }
}
