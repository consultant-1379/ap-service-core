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

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import com.ericsson.oss.itpf.sdk.core.annotation.EServiceRef;
import com.ericsson.oss.services.ap.api.AutoProvisioningService;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.core.rest.model.ErrorResponse;
import com.ericsson.oss.services.ap.core.rest.war.response.ApResponseBuilder;

/**
 * Abstract class for resources
 */

public abstract class AbstractResource {

    @EServiceRef(qualifier = "apcore")
    protected AutoProvisioningService autoProvisioningService;

    @Inject
    protected ApResponseBuilder responseBuilder;

    /**
     * @param projectId
     *              the project id
     * @return The full AP projectFdn
     */
    protected static String buildApProjectFdn(final String projectId) {
        return String.format("%s=%s", MoType.PROJECT.toString(), projectId);
    }

    /**
     * Builds an FDN for a given profile.
     *
     * @param projectId
     *              the project id
     * @param profileId
     *              the profile id
     * @return The full AP profileFdn
     */
    protected static String buildApProfileFdn(final String projectId, final String profileId) {
        return String.format("%s,%s=%s", buildApProjectFdn(projectId), MoType.CONFIGURATION_PROFILE.toString(), profileId);
    }

    /**
     * @param projectId
     *              the project id
     * @param nodeId
     *              the node id
     * @return The full AP nodeFdn
     */
    protected static String buildApNodeFdn(final String projectId, final String nodeId) {
        return String.format("%s,%s=%s", buildApProjectFdn(projectId), MoType.NODE.toString(), nodeId);
    }

    /**
     * Create a Response, given an ErrorResponse.
     *
     * @param errorResponse
     *            Object describing the error and status code to return
     * @return Response object
     */
    protected static Response generateResponse(final ErrorResponse errorResponse) {
        return Response.status(errorResponse.getHttpResponseStatus())
            .entity(errorResponse).build();
    }
}