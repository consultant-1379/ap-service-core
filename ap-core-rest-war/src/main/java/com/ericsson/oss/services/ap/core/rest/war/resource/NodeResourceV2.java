/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.war.resource;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.core.rest.builder.NodePropertiesBuilder;
import com.ericsson.oss.services.ap.core.rest.builder.NodeStatusDataBuilder;
import com.ericsson.oss.services.ap.core.rest.constant.ApQueryParameter;
import com.ericsson.oss.services.ap.core.rest.handlers.ArgumentResolver;
import com.ericsson.oss.services.ap.core.rest.model.NodeStatusData;
import com.ericsson.oss.services.ap.core.rest.model.nodeproperty.NodeProperties;

@Path("/v2/projects/{projectId}/nodes")
@Stateless
@LocalBean
public class NodeResourceV2 extends AbstractResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeResourceV2.class);

    @Inject
    private ArgumentResolver argumentResolver;

    @Inject
    private NodeStatusDataBuilder nodeStatusDataBuilder;

    @Inject
    private NodePropertiesBuilder nodePropertiesBuilder;

    @GET
    @Path("{nodeId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response queryNode(@PathParam("projectId") final String projectId, @PathParam("nodeId") final String nodeId,
        @DefaultValue("status") @QueryParam("filter") final String filter) {
        LOGGER.info("Request status and properties for EOI node with project Id {} and node id {}", projectId, nodeId);
        final String usecase = ApQueryParameter.getEnumParameterName(filter).getParameterName();
        final String nodeFdn = buildApNodeFdn(projectId, nodeId);
        argumentResolver.resolveFdn(nodeFdn, usecase);

        if (usecase.equals(ApQueryParameter.STATUS.getParameterName())) {
            final NodeStatus nodeStatus = autoProvisioningService.statusNode(nodeFdn);
            final NodeStatusData nodeStatusData = nodeStatusDataBuilder.buildNodeStatusData(nodeStatus);
            return responseBuilder.buildOk(nodeStatusData);
        } else {
            final List<MoData> nodeData = autoProvisioningService.viewNode(nodeFdn);
            final NodeProperties nodeProperties = nodePropertiesBuilder.buildNodeProperties(nodeData);
            return responseBuilder.buildOk(nodeProperties);
        }
    }
}
