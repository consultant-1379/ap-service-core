/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2018
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.reader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;

import com.ericsson.oss.itpf.modeling.modelservice.ModelService;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.TargetTypeInformation;
import com.ericsson.oss.itpf.modeling.modelservice.typed.core.target.TargetTypeVersionInformation;
import com.ericsson.oss.services.ap.core.rest.model.NodeModelData;

/**
 * Reader class for model information in ENM and AP
 *
 */
public class ModelInformationReader {

    @Inject
    private ModelService modelService;

    @Inject
    private Logger logger;

    /**
     * Retrieve model information based on the nodeType and required data
     * @param nodeType
     *     the node type to read information about
     * @param isRetrieveOssModelIds
     *     true if oss model identities should be retrieved
     * @return
     *     the required model information
     */
    public NodeModelData createNodeModelData(final String nodeType, final boolean isRetrieveOssModelIds) {
        final NodeModelData nodeModelData = new NodeModelData();
        if (isRetrieveOssModelIds) {
            nodeModelData.setOssModelIds(getOssModelIdentities(nodeType));
        }
        return nodeModelData;
    }

    private List<String> getOssModelIdentities(final String nodeType) {
        try {
            final TargetTypeVersionInformation targetTypeVersionInformation = getTargetTypeVersionInformation(nodeType);
            final List<String> ossModelIdList = new ArrayList<>();
            for (final String ossModelId : targetTypeVersionInformation.getTargetModelIdentities()) {
                ossModelIdList.add(ossModelId);
            }
            ossModelIdList.sort(Collections.reverseOrder(String.CASE_INSENSITIVE_ORDER));
            return ossModelIdList;
        } catch (final Exception exception) {
            logger.error("Retrieve oss model identities failed", exception);
            return Collections.emptyList();
        }
    }

    private TargetTypeVersionInformation getTargetTypeVersionInformation(final String nodeType) {
        final TargetTypeInformation targetTypeInformation = modelService.getTypedAccess().getModelInformation(TargetTypeInformation.class);
        return targetTypeInformation.getTargetTypeVersionInformation(TargetTypeInformation.CATEGORY_NODE, nodeType);
    }
}
