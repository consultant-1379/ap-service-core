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
package com.ericsson.oss.services.ap.core.rest.war.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ericsson.oss.services.ap.api.exception.HwIdAlreadyBoundException;
import com.ericsson.oss.services.ap.api.exception.HwIdInvalidFormatException;
import com.ericsson.oss.services.ap.api.model.MoData;
import com.ericsson.oss.services.ap.api.model.node.Node;
import com.ericsson.oss.services.ap.api.status.NodeStatus;
import com.ericsson.oss.services.ap.common.usecase.UseCaseName;
import com.ericsson.oss.services.ap.core.rest.builder.NodeDataBuilder;
import com.ericsson.oss.services.ap.core.rest.builder.NodePropertiesBuilder;
import com.ericsson.oss.services.ap.core.rest.builder.NodeStatusDataBuilder;
import com.ericsson.oss.services.ap.core.rest.constant.ApQueryParameter;
import com.ericsson.oss.services.ap.core.rest.handlers.ArgumentResolver;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;
import com.ericsson.oss.services.ap.core.rest.model.HardwareSerialNumber;
import com.ericsson.oss.services.ap.core.rest.model.NodeData;
import com.ericsson.oss.services.ap.core.rest.model.NodeStatusData;
import com.ericsson.oss.services.ap.core.rest.model.nodeproperty.NodeProperties;
import com.ericsson.oss.services.ap.core.rest.model.request.DeleteNodesRequest;
import com.ericsson.oss.services.ap.core.rest.model.request.OrderNodesRequest;
import com.ericsson.oss.services.ap.core.rest.war.interceptor.ResolveFdn;
import com.ericsson.oss.services.ap.core.rest.war.interceptor.Validate;
import com.ericsson.oss.services.ap.core.rest.war.interceptor.annotations.InjectFdn;
import com.ericsson.oss.services.ap.core.rest.war.response.DeleteEndpointResponse;
import com.ericsson.oss.services.ap.core.rest.war.response.OrderNodeEndpointResponse;

/**
 * Entry-point for Node related resources.
 */
@Path("/v1/projects/{projectId}/nodes")
@Stateless
@LocalBean
public class NodeResource extends AbstractResource {

    @Inject
    private NodeStatusDataBuilder nodeStatusDataBuilder;

    @Inject
    private NodeDataBuilder nodeDataBuilder;

    @Inject
    private NodePropertiesBuilder nodePropertiesBuilder;

    @Inject
    private ArgumentResolver argumentResolver;

    /**
     * Retrieve node data for a specific AP Project by a given project ID.
     *
     * @param projectId
     *            The project ID to retrieve the AP Project by.
     * @param projectFdn
     *            The project fdn
     * @return Response with status 200 OK with a list of {@link Node} response data.
     */
    @ResolveFdn(usecase = UseCaseName.VIEW_ALL_NODES)
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response viewProjectNodes(@PathParam("projectId") final String projectId, @InjectFdn final String projectFdn) {
        try {
            final List<MoData> projectData = autoProvisioningService.viewProject(projectFdn);
            final NodeData nodeResponseData = nodeDataBuilder.buildNodeData(projectData, projectId);
            return responseBuilder.buildOk(nodeResponseData);
        } catch (final Exception exception) {
            final ErrorResponse errorResponse = responseBuilder.buildServiceError(UseCaseName.VIEW_ALL_NODES.toString(), exception);
            return generateResponse(errorResponse);
        }
    }

    /**
     * Deletes a given list of node ids
     *
     * @param projectId
     *            The project ID to retrieve the project by.
     * @param deleteNodesRequest
     *            The payload body containing the nodes to delete.
     * @return {@link Response} with no content if no error(s) occur, otherwise return Response containing the service errors.
     */
    @Validate
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteNodes(@PathParam("projectId") final String projectId, @NotNull final DeleteNodesRequest deleteNodesRequest) {
        final List<DeleteEndpointResponse> errorResponses = new ArrayList<>();
        final boolean ignoreNetworkElement = deleteNodesRequest.isIgnoreNetworkElement();

        deleteNodesRequest.getNodeIds().forEach(nodeId -> {
            try {
                autoProvisioningService.deleteNode(buildApNodeFdn(projectId, nodeId), ignoreNetworkElement);
            } catch (final Exception exception) {
                errorResponses.add(new DeleteEndpointResponse(nodeId,
                    responseBuilder.buildServiceError(UseCaseName.DELETE_NODE.toString(), exception).getErrorTitle()));
            }
        });

        return Response
            .status(errorResponses.isEmpty() ? Response.Status.NO_CONTENT.getStatusCode() : Response.Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .entity(errorResponses.isEmpty() ? null : errorResponses)
            .build();
    }

    /**
     * Retrieve information for a specified node. A query parameter provided in the URL will determine which information is returned. If no query
     * parameter is provided in URL then the default behavior will be to return status information of the node.
     *
     * @param projectId
     *            The project ID to retrieve the project.
     * @param nodeId
     *            The node ID to retrieve the node
     * @param filter
     *            Optional {@link QueryParam} can be applied to the nodeId. Specifies which information is to be returned. Default value is "status"
     *            if no query parameter is provided in URL.
     * @return Response with status 200 OK with node information
     */
    @GET
    @Path("{nodeId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response queryNode(@PathParam("projectId") final String projectId, @PathParam("nodeId") final String nodeId,
        @DefaultValue("status") @QueryParam("filter") final String filter) {

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

    /**
     * Order one or many nodes for a project.
     *
     * @param projectId
     *            The project ID.
     * @param orderNodesRequest
     *            The request payload body containing the node(s) to order.
     * @return {@link Response} with no content if no error(s) occur, otherwise return Response containing the service errors.
     */
    @Validate
    @POST
    @Path("actions/order")
    @Produces(MediaType.APPLICATION_JSON)
    public Response orderNodes(@PathParam("projectId") final String projectId, @NotNull final OrderNodesRequest orderNodesRequest) {
        final List<OrderNodeEndpointResponse> errorResponses = new ArrayList<>();
        orderNodesRequest.getNodeIds().forEach(nodeId -> {
            try {
                final String nodeFdn = buildApNodeFdn(projectId, nodeId);
                argumentResolver.resolveFdn(nodeFdn, UseCaseName.ORDER_NODE.toString());
                autoProvisioningService.orderNode(nodeFdn);
            } catch (final Exception exception) {
                errorResponses.add(new OrderNodeEndpointResponse(nodeId,
                    responseBuilder.buildServiceError(UseCaseName.ORDER_NODE.toString(), exception).getErrorTitle()));
            }
        });
        return Response
            .status(errorResponses.isEmpty() ? Response.Status.ACCEPTED.getStatusCode() : 207)
            .entity(errorResponses.isEmpty() ? null : errorResponses)
            .build();
    }

    /**
     * Binds the node using a hardware serial number.
     *
     * @param projectId
     *            The project ID to retrieve the project by.
     * @param nodeId
     *            The node ID to retrieve the node by.
     * @param hardwareSerialNumber
     *            The payload body containing the hardware identifier of node.
     * @param nodeFdn
     *            The node fdn
     * @return {@link Response}
     */
    @ResolveFdn(usecase = UseCaseName.BIND)
    @PUT
    @Path("{nodeId}/actions/bind")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response bindNode(@PathParam("projectId") final String projectId, @PathParam("nodeId") final String nodeId,
        final HardwareSerialNumber hardwareSerialNumber, @InjectFdn final String nodeFdn) {
        try {
            autoProvisioningService.bind(nodeFdn, hardwareSerialNumber.getHardwareId());
            return Response.noContent().build();
        } catch (final HwIdInvalidFormatException | HwIdAlreadyBoundException exception) {
            final ErrorResponse errorResponse = responseBuilder.buildBindServiceError(exception, hardwareSerialNumber.getHardwareId());
            return generateResponse(errorResponse);
        } catch (final Exception exception) {
            final ErrorResponse errorResponse = responseBuilder.buildServiceError(UseCaseName.BIND.toString(), exception);
            return generateResponse(errorResponse);
        }
    }

    /**
     * AP Workflow resume action for the Node
     *
     * @param projectId
     *            The project ID to retrieve the project by.
     * @param nodeId
     *            The node ID to retrieve the node by.
     * @param nodeFdn
     *            The node fdn
     * @return {@link Response}
     */
    @ResolveFdn(usecase = UseCaseName.RESUME)
    @POST
    @Path("{nodeId}/actions/resume")
    @Produces(MediaType.APPLICATION_JSON)
    public Response resume(@PathParam("projectId") final String projectId, @PathParam("nodeId") final String nodeId,
        @InjectFdn final String nodeFdn) {
        try {
            autoProvisioningService.resume(nodeFdn);
            return Response.noContent().build();
        } catch (final Exception exception) {
            final ErrorResponse errorResponse = responseBuilder.buildServiceError(UseCaseName.RESUME.toString(), exception);
            return generateResponse(errorResponse);
        }
    }

    /**
     * AP Workflow cancel action for the Node
     *
     * @param projectId
     *            The project ID to retrieve the project by.
     * @param nodeId
     *            The node ID to retrieve the node by.
     * @param nodeFdn
     *            The node fdn
     * @return {@link Response}
     */
    @ResolveFdn(usecase = UseCaseName.CANCEL)
    @POST
    @Path("{nodeId}/actions/cancel")
    @Produces(MediaType.APPLICATION_JSON)
    public Response cancel(@PathParam("projectId") final String projectId, @PathParam("nodeId") final String nodeId,
        @InjectFdn final String nodeFdn) {
        try {
            autoProvisioningService.cancel(nodeFdn);
        } catch (final Exception exception) {
            final ErrorResponse errorResponse = responseBuilder.buildServiceError(UseCaseName.CANCEL.toString(), exception);
            return generateResponse(errorResponse);
        }
        return Response.noContent().build();
    }

    /**
     * AP Workflow skip action for the Node
     *
     * @param projectId
     *            The project ID to retrieve the project by.
     * @param nodeId
     *            The node ID to retrieve the node by.
     * @param nodeFdn
     *            The node fdn
     * @return {@link Response}
     */
    @ResolveFdn(usecase = UseCaseName.SKIP)
    @POST
    @Path("{nodeId}/actions/skip")
    @Produces(MediaType.APPLICATION_JSON)
    public Response skip(@PathParam("projectId") final String projectId, @PathParam("nodeId") final String nodeId,
        @InjectFdn final String nodeFdn) {
        try {
            autoProvisioningService.skip(nodeFdn);
            return Response.noContent().build();
        } catch (final Exception exception) {
            final ErrorResponse errorResponse = responseBuilder.buildServiceError(UseCaseName.SKIP.toString(), exception);
            return generateResponse(errorResponse);
        }
    }

    /**
     * Deletes the given node.
     *
     * @param projectId
     *            The project ID to retrieve the project by.
     * @param nodeId
     *            The node ID to delete.
     * @return {@link Response} with no content if no error(s) occur, otherwise return Response containing the service errors.
     */
    @Validate
    @DELETE
    @Path("{nodeId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteNode(@PathParam("projectId") final String projectId, @PathParam("nodeId") final String nodeId) {
        try {
            autoProvisioningService.deleteNode(buildApNodeFdn(projectId, nodeId), false);
        } catch (final Exception exception) {
            final ErrorResponse errorResponse = responseBuilder.buildServiceError(UseCaseName.DELETE_NODE.toString(), exception);
            return generateResponse(errorResponse);
        }
        return Response.noContent().build();
    }

}
