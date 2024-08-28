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

package com.ericsson.oss.services.ap.core.usecase.importproject;

import static com.ericsson.oss.services.ap.common.model.MoType.NOTIFICATION;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.modeling.modelservice.typed.persistence.primarytype.HierarchicalPrimaryTypeSpecification;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.modeling.common.info.ModelInfo;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.access.ModelReader;

/**
 * Creates the {@link MoType#NOTIFICATION} MO.
 */
public class NotificationMoCreator {

    private DataPersistenceService dps;

    @Inject
    private ModelReader modelReader;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    @PostConstruct
    public void init() {
        dps = new ServiceFinderBean().find(DataPersistenceService.class);
    }

    /**
     * Write notification child MO under the AP node MO.
     *
     * @param nodeMo
     *            the node MO
     * @param nodeInfo
     *            the node information from nodeInfo.xml
     * @return the created notification MO
     */
    public ManagedObject create(final ManagedObject nodeMo, final NodeInfo nodeInfo) {
        if (nodeInfo.getNotifications().isEmpty()) {
            return null;
        }
        return createNotificationMo(nodeMo, nodeInfo);
    }

    private ManagedObject createNotificationMo(final ManagedObject nodeMo, final NodeInfo nodeInfo) {
        final String apNamespace = nodeTypeMapper.getNamespace(nodeInfo.getNodeType());
        final HierarchicalPrimaryTypeSpecification notificationModel = modelReader.getLatestPrimaryTypeSpecification(apNamespace,
            NOTIFICATION.toString());
        final ModelInfo notificationModelInfo = notificationModel.getModelInfo();

        return dps.getLiveBucket()
            .getMibRootBuilder()
            .parent(nodeMo)
            .namespace(notificationModelInfo.getNamespace())
            .version(notificationModelInfo.getVersion().toString())
            .type(NOTIFICATION.toString())
            .name("1")
            .addAttributes(nodeInfo.getNotifications())
            .create();
    }
}
