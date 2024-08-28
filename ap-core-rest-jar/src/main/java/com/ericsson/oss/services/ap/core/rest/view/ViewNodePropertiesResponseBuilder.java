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
package com.ericsson.oss.services.ap.core.rest.view;

import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.core.metadata.cli.api.CliMetadataService;
import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewMetadata;
import com.ericsson.oss.services.ap.core.rest.view.properties.data.ViewPropertiesResponse;

/**
 * Builds the {@link ViewPropertiesResponse} for the UI based on view metadata.
 */
public class ViewNodePropertiesResponseBuilder {

    private static final String NODE_METADATA_TYPE = "node";
    private static final String NODETYPE_METADATA_TYPE = "nodeType";

    @Inject
    private CliMetadataService cliMetadataService;

    @Inject
    private ClientView uiViewBuilder;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    /**
     * The view for one node.
     *
     * @param viewMoDataList
     *            the list of target MOs used to build the view with
     * @return the view in the form of a ResponseDto
     */
    public ViewPropertiesResponse buildViewForNode(final List<MoData> viewMoDataList) {
        final ViewMetadata viewMetadata = cliMetadataService.getViewMetadata(getNodeType(viewMoDataList), NODE_METADATA_TYPE);
        return buildViewPropertiesResponse(viewMetadata, viewMoDataList);
    }

    private String getNodeType(final List<MoData> viewMoDataList) {
        final String nodeType = (String) viewMoDataList.get(0).getAttribute(NODETYPE_METADATA_TYPE);
        return nodeTypeMapper.getInternalEjbQualifier(nodeType);
    }

    private ViewPropertiesResponse buildViewPropertiesResponse(final ViewMetadata viewMetadata, final List<MoData> viewMoDataList) {
        return uiViewBuilder.buildViewFromMetadata(viewMetadata, viewMoDataList);
    }
}
