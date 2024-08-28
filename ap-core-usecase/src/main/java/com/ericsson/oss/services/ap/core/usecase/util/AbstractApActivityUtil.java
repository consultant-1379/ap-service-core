/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.util;

import java.util.Collection;

import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.datalayer.dps.persistence.PersistenceObject;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NetworkElementAttribute;
import com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfo;

/**
 * Utility class used to create the NetworkElementMo.
 */
public class AbstractApActivityUtil {

    @Inject
    private DpsOperations dps;

    protected String getNodeIpAddress(final ManagedObject networkElementMo) {
        return findConnectivityInformationMo(networkElementMo).getAttribute(NetworkElementAttribute.IP_ADDRESS.toString());
    }

    protected ManagedObject getNetworkElementMo(final NodeInfo nodeData) {
        final String nodeName = nodeData.getName();
        final DataBucket liveBucket = dps.getDataPersistenceService().getLiveBucket();
        final String nodeFdn = MoType.NETWORK_ELEMENT.toString() + "=" + nodeName;
        return liveBucket.findMoByFdn(nodeFdn);
    }

    protected PersistenceObject findConnectivityInformationMo(final ManagedObject networkElementMo) {
        final PersistenceObject target = networkElementMo.getTarget();
        final Collection<PersistenceObject> ciAssociations = target.getAssociations("ciRef");
        return ciAssociations.iterator().next();
    }

}
