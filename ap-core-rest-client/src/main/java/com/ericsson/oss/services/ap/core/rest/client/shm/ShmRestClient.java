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
/**
 * REST client used to interact with SHM REST interface.
 */
package com.ericsson.oss.services.ap.core.rest.client.shm;

import static com.ericsson.oss.services.ap.core.rest.client.RestUrls.SHM_BACKUP_SOFTWARE_VERSIONS_QUERY_SERVICE;
import static com.ericsson.oss.services.ap.core.rest.client.RestUrls.SHM_SOFTWARE_PACKAGE_SEARCH_SERVICE;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.context.classic.ContextConstants;
import com.ericsson.oss.itpf.sdk.context.classic.ContextServiceBean;
import com.ericsson.oss.services.ap.api.exception.ApApplicationException;
import com.ericsson.oss.services.ap.core.rest.client.RestUrls;
import com.ericsson.oss.services.ap.core.rest.client.common.HttpConstants;
import com.ericsson.oss.services.ap.core.rest.client.shm.backupsoftwareversion.model.BackupSoftwareVersionQuery;
import com.ericsson.oss.services.ap.core.rest.client.shm.backupsoftwareversion.model.NodeBackupConfigurationQuery;
import com.ericsson.oss.services.ap.core.rest.client.shm.util.NodeMoHelper;
import com.ericsson.oss.services.ap.core.rest.client.shm.util.ShmResponseHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ShmRestClient {

    private static final String USER_ROLE_KEY = ContextConstants.HTTP_HEADER_USERNAME_KEY;
    private ContextServiceBean contextService = new ContextServiceBean();
    private static ObjectMapper objectMapper = new ObjectMapper();

    private final CloseableHttpClient httpClient;

    @Inject
    private ShmResponseHandler shmResponseHandler;

    @Inject
    private NodeMoHelper nodeMoHelper;

    public ShmRestClient() {
        this.httpClient = HttpClientBuilder.create().build();
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Retrieves the Upgrade Package Name This is done by retrieving The NetworkElement MO, attribute neProductVersion. The Upgrade Package for the
     * Node is stored in List of Maps (made up of Revision and Identity Name/Value Pairs). The Revision is extracted to make up the Upgrade Package
     * Name.
     *
     * @param nodeName
     *            the name of the AP node
     * @param nodeTypeIdForPackageName
     *            the type of the AP node
     * @return Upgrade Package name
     */

    public String getUpgradePackageName(final String nodeName, final String nodeTypeIdForPackageName) {
        return getUpgradePackageName(nodeName, nodeTypeIdForPackageName, nodeMoHelper.getSoftwareVersionFromMO(nodeName));
    }

    /**
     * Retrieves the Upgrade Package Name based on the backup software version
     *
     * @param nodeName
     *            the name of the AP node
     * @param nodeTypeIdForPackageName
     *            the type of the AP node
     * @param softwareVersion
     *            the software version
     * @return Upgrade Package name
     */
    public String getUpgradePackageName(final String nodeName, final String nodeTypeIdForPackageName, final String softwareVersion) {
        if (StringUtils.isBlank(softwareVersion)) {
            throw new ApApplicationException("Failed to get upgrade package for no software version is identified.");
        }
        HttpResponse httpResponse = null;
        final List<String> nodeTypeList = Arrays.asList(nodeTypeIdForPackageName);
        try {
            final StringEntity userEntity = new StringEntity(objectMapper.writeValueAsString(nodeTypeList));
            httpResponse = httpPostRequest(SHM_SOFTWARE_PACKAGE_SEARCH_SERVICE, userEntity);
            return shmResponseHandler.handlePackageNameResponse(httpResponse, nodeName, softwareVersion, nodeTypeIdForPackageName);
        } catch (final Exception e) {
            logger.error("Exception occurred during getting upgrade package details : {}", e.getMessage());
            throw new ApApplicationException(String.format("Failed to get upgrade package for %s", softwareVersion), e);
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
        }
    }

    /**
     * Retrieves the Backup Software Version
     *
     * @param nodeName
     *            the name of the AP node
     * @param backupId
     *            the backup Id the AP node
     * @return the backup software version
     */
    public String getBackupSoftwareVersion(final String nodeName, final String backupId) {
        if (StringUtils.isBlank(backupId)) {
            throw new ApApplicationException("Failed to get backup software version for no backupid is identified.");
        }
        HttpResponse httpResponse = null;
        final BackupSoftwareVersionQuery backupSoftwareVersionQuery = new BackupSoftwareVersionQuery();
        final NodeBackupConfigurationQuery nodeBackupConfigurationQuery = new NodeBackupConfigurationQuery();
        nodeBackupConfigurationQuery.setNodeFdn("NetworkElement=" + nodeName);
        nodeBackupConfigurationQuery.setLocation("ENM");
        nodeBackupConfigurationQuery.setBackupId(backupId);
        final List<NodeBackupConfigurationQuery> nodeBackupConfigurationQueries = Arrays.asList(nodeBackupConfigurationQuery);
        backupSoftwareVersionQuery.setNodeBackupConfigurationQueries(nodeBackupConfigurationQueries);
        try {
            final StringEntity userEntity = new StringEntity(objectMapper.writeValueAsString(backupSoftwareVersionQuery));
            httpResponse = httpPostRequest(SHM_BACKUP_SOFTWARE_VERSIONS_QUERY_SERVICE, userEntity);
            return shmResponseHandler.handleBackupSoftwareVersionResponse(httpResponse, nodeName, backupId);
        } catch (final Exception e) {
            logger.error("Exception occurred during getting backup software version : {}", e.getMessage());
            throw new ApApplicationException(String.format("Failed to get backup software version for backupid : %s node : %s", backupId, nodeName),
                    e);
        } finally {
            HttpClientUtils.closeQuietly(httpResponse);
        }
    }

    private HttpResponse httpPostRequest(final RestUrls restUrl, final StringEntity userEntity) {
        final HttpPost postRequest = new HttpPost(restUrl.getFullUrl());
        try {
            postRequest.setEntity(userEntity);
            postRequest.addHeader(HttpConstants.CONTENT_TYPE_HEADER, ContentType.APPLICATION_JSON.getMimeType());
            postRequest.addHeader(USER_ROLE_KEY, contextService.getContextValue(USER_ROLE_KEY));
            postRequest.setHeader(HttpConstants.HOST, restUrl.getHost());
            final HttpResponse httpResponse = httpClient.execute(postRequest);

            final int httpResponseCode = httpResponse.getStatusLine().getStatusCode();
            if (HttpStatus.SC_OK == httpResponseCode) {
                return httpResponse;
            } else {
                String responseString = "";
                if (getContentType(httpResponse).equals(ContentType.APPLICATION_JSON.getMimeType())
                        || getContentType(httpResponse).equals(ContentType.TEXT_HTML.getMimeType())) {
                    responseString = EntityUtils.toString(httpResponse.getEntity());
                    logger.error("httpResponseCode --> {}, responseString --> {}", httpResponseCode, responseString);
                }
                throw new ApApplicationException(
                        String.format("Error http response from SHM service with %s %s", httpResponseCode, responseString));
            }
        } catch (final IOException e) {
            throw new ApApplicationException("Error response from SHM service for URL " + restUrl, e);
        }
    }

    private String getContentType(final HttpResponse httpResponse) {
        final HttpEntity entity = httpResponse.getEntity();
        final ContentType contentType = ContentType.getOrDefault(entity);
        return contentType.getMimeType();
    }
}