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
package com.ericsson.oss.services.ap.core.rest.war.handler;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.ap.api.AutoProvisioningDataRetriever;
import com.ericsson.oss.services.ap.core.rest.json.capability.SupportedNodeTypesJSON;

/**
 * Entry-point for Capability related resources.
 */
@Path("/v1/capability")
@Stateless
@LocalBean
public class CapabilityHandler {

    @EServiceRef
    private AutoProvisioningDataRetriever autoProvisioningDataRetriever;

    /**
     * Retrieve all currently supported node types by profile manager.
     *
     * @return Response with status 200 and list of {@link String} which represent supported node types.
     *         Response with status 500 if any internal error occurred during fetching data.
     */
    @GET
    @Path("/profile")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSupportedNodeTypes() {
        final List<String> nodeTypes = autoProvisioningDataRetriever.getSupportedNodeTypes();
        return Response.ok(new SupportedNodeTypesJSON(nodeTypes)).build();
    }
}
