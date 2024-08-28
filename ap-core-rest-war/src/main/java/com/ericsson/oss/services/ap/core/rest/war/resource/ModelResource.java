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
package com.ericsson.oss.services.ap.core.rest.war.resource;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ericsson.oss.services.ap.core.rest.model.NodeModelData;
import com.ericsson.oss.services.ap.core.rest.reader.ModelInformationReader;

/**
 * Entry-point for resources related to model information in ENM, such as node type and oss model identities.
 */
@Path("/v1/models")
@Stateless
@LocalBean
public class ModelResource extends AbstractResource {

    @Inject
    private ModelInformationReader modelInfoReader;
    /**
     * Retrieve model information based on node type, such as supported oss model identities
     * @param nodeType
     *     the requested node type e.g. RadioNode
     * @param isRetrieveOssModelIds
     *     true if a list of supported oss model identities should be returned
     * @return Response
     *     json response listing model information specified through query parameters
     */
    @GET
    @Path("/{nodeType}")
    @Produces({MediaType.APPLICATION_JSON})
    public Response getNodeModelInformation(@PathParam("nodeType") final String nodeType, @QueryParam("ossModelIdentity") final boolean isRetrieveOssModelIds) {
        final NodeModelData nodeModelData = modelInfoReader.createNodeModelData(nodeType, isRetrieveOssModelIds);
        return Response.status(Response.Status.OK).entity(nodeModelData).build();
    }

}
