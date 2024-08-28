/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.importproject;

import java.util.ArrayList;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.model.ModelData;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.common.model.HealthCheckAttribute;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;
import com.ericsson.oss.services.ap.common.util.log.MRDefinition;
import com.ericsson.oss.services.ap.common.util.log.MRExecutionRecorder;

/**
 * Creates or update the {@code HealthCheck} MO from the input {@link NodeInfo}.
 */
public class HealthCheckMoCreator {

    @Inject
    private ModelReader modelReader;

    private DataPersistenceService dps;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    @Inject
    private MRExecutionRecorder mrExecutionRecorder;

    @PostConstruct
    public void init() {
        dps = new ServiceFinderBean().find(DataPersistenceService.class);
    }

    /**
     * Creates an HEALTH_CHECK MO and logs the MRid to DDP.
     *
     * @param nodeMo
     *            the node MO
     * @param nodeInfo
     *            the node information from nodeInfo.xml
     * @return the created Health Check Profile MO
     */
    public ManagedObject create(final ManagedObject nodeMo, final NodeInfo nodeInfo) {
        if (nodeInfo.getHealthCheckAttributes().isEmpty()) {
            return null;
        }
        final ManagedObject healthCheckProfileMo = createHealthCheckProfileMo(nodeMo, nodeInfo);
        mrExecutionRecorder.recordMRExecution(MRDefinition.AP_EXPANSION_HEALTHCHECK);
        return healthCheckProfileMo;
    }

    private ManagedObject createHealthCheckProfileMo(final ManagedObject nodeMo, final NodeInfo nodeInfo) {
        final String apNamespace = nodeTypeMapper.getNamespace(nodeInfo.getNodeType());
        final ModelData healthCheckModelData = modelReader.getLatestPrimaryTypeModel(apNamespace, MoType.HEALTH_CHECK.toString());
        final Map<String, Object> healthCheckAttributes = nodeInfo.getHealthCheckAttributes();
        healthCheckAttributes.put(HealthCheckAttribute.PRE_REPORT_IDS.toString(), new ArrayList<String>());
        healthCheckAttributes.put(HealthCheckAttribute.POST_REPORT_IDS.toString(), new ArrayList<String>());

        return dps.getLiveBucket()
            .getMibRootBuilder()
            .parent(nodeMo)
            .namespace(apNamespace)
            .version(healthCheckModelData.getVersion())
            .type(MoType.HEALTH_CHECK.toString())
            .name("1")
            .addAttributes(healthCheckAttributes)
            .create();
    }
}
