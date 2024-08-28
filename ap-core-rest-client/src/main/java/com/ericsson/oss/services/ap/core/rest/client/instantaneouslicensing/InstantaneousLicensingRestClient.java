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
package com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing;

import static com.ericsson.oss.services.ap.core.rest.client.RestUrls.IL_REQUEST_SERVICE;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.inject.Inject;

import com.ericsson.oss.itpf.sdk.security.accesscontrol.EAccessControl;
import com.ericsson.oss.services.ap.api.exception.InstantaneousLicensingRestServiceException;
import com.ericsson.oss.services.ap.core.rest.client.common.HttpConstants;
import com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing.model.CreateLicenseResponseDto;
import com.ericsson.oss.services.ap.core.rest.client.instantaneouslicensing.model.RetrieveLicenseStatusResponseDto;

/**
 * This REST client is responsible for executing REST Calls towards SHM for as part of the Instantaneous Licensing flow.
 */
public class InstantaneousLicensingRestClient {

    private CloseableHttpClient httpClient;

    @Inject
    private EAccessControl accessControl;

    @Inject
    private InstantaneousLicensingResponseHandler responseHandler;

    @Inject
    private InstantaneousLicensingRestDataBuilder restDataBuilder;

    public InstantaneousLicensingRestClient() {
        this.httpClient = HttpClientBuilder.create().build();
    }

    /**
     * Creates a license request in SHM
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @return {@link CreateLicenseResponseDto}
     *            the response params from SHM corresponding to the license request creation
     */
    public CreateLicenseResponseDto createLicenseRequest(final String apNodeFdn) {
        final HttpEntity createLicenseRequest = restDataBuilder.buildCreateLicenseRequest(apNodeFdn);
        return createRequest(createLicenseRequest);
    }

    /**
     * Get status of a license request from SHM
     *
     * @param requestId
     *            the id of the license request
     * @return {@link RetrieveLicenseStatusResponseDto}
     *             the response params from SHM corresponding to the license request status
     */
    public RetrieveLicenseStatusResponseDto getLicenseRequestStatus(final String requestId) {
        try {
            final HttpGet get = new HttpGet(IL_REQUEST_SERVICE.getFullUrl() + "/status/" + requestId);
            get.setHeader(HttpConstants.USERNAME_HEADER, accessControl.getAuthUserSubject().getSubjectId());
            get.setHeader(HttpConstants.HOST, IL_REQUEST_SERVICE.getHost());
            get.setHeader(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString());
            final HttpResponse httpResponse = httpClient.execute(get);
            return responseHandler.handleGetRequestResponse(httpResponse);
        } catch (final InstantaneousLicensingRestServiceException e) {
            throw e;
        } catch (final Exception e) {
            throw new InstantaneousLicensingRestServiceException("Error creating HTTP request: " + e.getMessage(), e);
        }
    }

    private CreateLicenseResponseDto createRequest(final HttpEntity createRequest) {
        try {
            final HttpPost post = new HttpPost(IL_REQUEST_SERVICE.getFullUrl());
            post.setEntity(createRequest);
            post.setHeader(HttpConstants.USERNAME_HEADER, accessControl.getAuthUserSubject().getSubjectId());
            post.setHeader(HttpConstants.HOST, IL_REQUEST_SERVICE.getHost());
            post.setHeader(HttpConstants.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.toString());
            post.setHeader(HttpConstants.ACCEPT_HEADER, ContentType.APPLICATION_JSON.toString());
            final HttpResponse httpResponse = httpClient.execute(post);
            return responseHandler.handleCreateRequestResponse(httpResponse);
        } catch (final InstantaneousLicensingRestServiceException e) {
            throw e;
        } catch (final Exception e) {
            throw new InstantaneousLicensingRestServiceException("Error creating HTTP request: " + e.getMessage(), e);
        }
    }

}
