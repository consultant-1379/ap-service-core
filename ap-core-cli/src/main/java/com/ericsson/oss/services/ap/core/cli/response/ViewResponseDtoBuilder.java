/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2013
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.cli.response;

import java.util.List;

import javax.inject.Inject;

import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.core.cli.view.CliView;
import com.ericsson.oss.services.ap.core.metadata.cli.api.CliMetadataService;
import com.ericsson.oss.services.ap.core.metadata.cli.api.ViewMetadata;
import com.ericsson.oss.services.scriptengine.spi.dtos.ResponseDto;

/**
 * Builds the {@link ResponseDto} for the CLI based on view metadata.
 */
public class ViewResponseDtoBuilder {

    private static final int FIRST_NODE_INDEX = 1;
    private static final String NODE_METADATA_TYPE = "node";
    private static final String NODETYPE_METADATA_TYPE = "nodeType";
    private static final String PROJECT_METADATA_TYPE = "project";
    private static final String PROJECTS_METADATA_TYPE = "projects";

    @Inject
    private CliMetadataService cliMetadataService;

    @Inject
    private CliView cliViewBuilder;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    /**
     * The view for one node.
     *
     * @param viewMoDataList
     *            the list of target MOs used to build the view with
     * @return the view in the form of a ResponseDto
     */
    public ResponseDto buildViewForNode(final List<MoData> viewMoDataList) {
        final ViewMetadata viewMetadata = cliMetadataService.getViewMetadata(getNodeType(viewMoDataList), NODE_METADATA_TYPE);
        return buildViewResponseDto(viewMetadata, viewMoDataList);
    }

    private String getNodeType(final List<MoData> viewMoDataList) {
        final String nodeType = (String) viewMoDataList.get(0).getAttribute(NODETYPE_METADATA_TYPE);
        return nodeTypeMapper.getInternalEjbQualifier(nodeType);
    }

    /**
     * The view for all projects.
     *
     * @param viewMoDataList
     *            the list of target MOs used to build the view with
     * @return the view in the form of a ResponseDto
     */
    public ResponseDto buildViewForAllProjects(final List<MoData> viewMoDataList) {
        final ViewMetadata viewMetadata = cliMetadataService.getViewMetadata(PROJECTS_METADATA_TYPE);
        return buildViewResponseDto(viewMetadata, viewMoDataList);
    }

    /**
     * The view for one project.
     *
     * @param viewMoDataList
     *            the list of target MOs used to build the view with
     * @return the view in the form of a ResponseDto
     */
    public ResponseDto buildViewForProject(final List<MoData> viewMoDataList) {
        final ViewMetadata viewMetadata = getValidViewMetadata(viewMoDataList);
        return buildViewResponseDto(viewMetadata, viewMoDataList);
    }

    private ViewMetadata getValidViewMetadata(final List<MoData> viewMoDataList) {
        if (containsOnlyProject(viewMoDataList)) {
            return cliMetadataService.getViewMetadata(PROJECT_METADATA_TYPE);
        }
        return cliMetadataService.getViewMetadata(getProjectTypeFromFirstNode(viewMoDataList), PROJECT_METADATA_TYPE);
    }

    private static boolean containsOnlyProject(final List<MoData> viewMoDataList) {
        return viewMoDataList.size() == 1;
    }

    private String getProjectTypeFromFirstNode(final List<MoData> viewMoDataList) {
        final String nodeType = (String) viewMoDataList.get(FIRST_NODE_INDEX).getAttribute(NODETYPE_METADATA_TYPE);
        return nodeTypeMapper.getInternalEjbQualifier(nodeType);
    }

    private ResponseDto buildViewResponseDto(final ViewMetadata viewMetadata, final List<MoData> viewMoDataList) {
        return cliViewBuilder.buildViewFromMetadata(viewMetadata, viewMoDataList);
    }
}