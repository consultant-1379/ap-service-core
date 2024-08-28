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
import com.ericsson.oss.services.ap.common.usecase.CommandLogName;
import com.ericsson.oss.services.ap.common.util.log.DdpTimer;

import java.util.Map;

/**
 * Creates information for all nodes defined in the project archive to the AP model.
 * <p>
 * Reads the data from each nodeInfo.xml along with all artifacts defined in the node directory of the project archive.
 */
public class ModelCreator {

    @Inject
    private AutoIntegrationOptionsMoCreator autoIntegrationMoCreator;

    @Inject
    private LicenseOptionsMoCreator licenseOptionsMoCreator;

    @Inject
    private DdpTimer ddpTimer;

    @Inject
    private NodeMoCreator nodeMoCreator;

    @Inject
    private NodeUserCredentialsMoCreator nodeUserCredentialsMoCreator;

    @Inject
    private SecurityMoCreator securityMoCreator;

    @Inject
    private NotificationMoCreator notificationMoCreator;

    @Inject
    private SupervisionOptionsMoCreator supervisionMoCreator;

    @Inject
    private NodeStatusMoCreator nodeStatusMoCreator;

    @Inject
    private NodeDhcpMoCreator nodeDhcpMoCreator;

    @Inject
    private ControllingNodesMoCreator controllingNodesMoCreator;

    @Inject
    private HealthCheckMoCreator healthCheckMoCreator;

    /**
     * Create node and its child MOs from the node element data.
     *
     * @param projectFdn
     *            the project fdn
     * @param nodeData
     *            node element data
     * @return the node fdn
     */
    public String create(final String projectFdn, final NodeInfo nodeData) {
        final ManagedObject nodeMo = nodeMoCreator.create(projectFdn, nodeData);
        ddpTimer.start(CommandLogName.CREATE_NODE_CHILDREN_MOS.toString());
        securityMoCreator.create(nodeMo, nodeData);
        notificationMoCreator.create(nodeMo, nodeData);
        autoIntegrationMoCreator.create(nodeMo, nodeData);
        licenseOptionsMoCreator.create(nodeMo, nodeData);
        nodeUserCredentialsMoCreator.create(nodeMo, nodeData);
        supervisionMoCreator.create(nodeMo, nodeData);
        nodeStatusMoCreator.create(nodeMo, nodeData);
        nodeDhcpMoCreator.create(nodeMo, nodeData);
        controllingNodesMoCreator.create(nodeMo, nodeData);
        healthCheckMoCreator.create(nodeMo, nodeData);
        ddpTimer.end(nodeMo.getFdn());
        return nodeMo.getFdn();
    }

    /**
     * Create node and its child MOs from the node element data.
     *
     * @param projectFdn
     *            the project fdn
     * @param nodeData
     *            node element data
     * @return the node fdn
     */
    public String eoiCreate(final String projectFdn, final Map<String,Object> nodeData) {
        final ManagedObject nodeMo = nodeMoCreator.eoiCreate(projectFdn, nodeData);
        securityMoCreator.eoiCreate(nodeMo, nodeData);
        nodeUserCredentialsMoCreator.eoiCreate(nodeMo, nodeData);
        supervisionMoCreator.eoiCreate(nodeMo, nodeData);
        nodeStatusMoCreator.eoiCreate(nodeMo);
        return nodeMo.getFdn();
    }

}
