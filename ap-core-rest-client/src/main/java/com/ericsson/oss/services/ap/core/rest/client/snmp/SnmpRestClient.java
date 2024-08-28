/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2020
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client.snmp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A REST client used to send a list of active connection ids to node discovery to carry out an audit on its active secure connections.
 */
public class SnmpRestClient {

    private Logger logger = LoggerFactory.getLogger(SnmpRestClient.class);
    private static final String PROTOCOL = "http";
    private static final String MSAP_SERV_HOSTNAME = "msap-service";
    private static final int MSAP_SERV_PORT = 8080;
    private static final String SNMP_AUDIT_PATH = "/nodediscovery/v1/snmp/connections/audit";

    /**
     * Sends a list of active connection ids to the node discovery REST endpoint to carry out the secure connection audit.
     *
     * @param connectionsIds
     *          The list of connection ids to sent
     */
    public void sendActiveConnectionIdsToNodeDiscovery(final List<String> connectionsIds) {
        if (!connectionsIds.isEmpty()) {
            HttpResponse response;
            try(final CloseableHttpClient client = HttpClients.createDefault()) {
                final HttpEntity entity = buildNodeDiscoveryRequest(connectionsIds);
                final HttpPost post = new HttpPost(getFullUrl());
                post.setEntity(entity);
                post.setHeader("host", MSAP_SERV_HOSTNAME);
                response = client.execute(post);
                logger.info("Connections to be sent to node discovery for audit: {}", connectionsIds);
                handleResponse(response);
            } catch (final Exception e) {
                logger.error("Exception occurred while attempting to contact node discovery : {}", e.getMessage());
            }

        } else {
            logger.info("No connection ids to send to Node Discovery for audit");
        }
    }

    /**
     * Builds a HttpEntity containing a map of connection ids to be sent to node discovery for auditing
     *
     * @param users
     *          A list of active connection ids to be audited
     * @return The entity to be sent to node discovery
     * @throws JsonProcessingException
     *             An exception thrown when the json object could not be created
     */
    private HttpEntity buildNodeDiscoveryRequest(final List<String> users) throws JsonProcessingException {
        final Map<String, List<String>> snmpUsers = new HashMap<>();
        snmpUsers.put("connectionIds", users);
        final ObjectMapper objectMapper = new ObjectMapper();
        final String requestBody = objectMapper.writeValueAsString(snmpUsers);
        final EntityBuilder entityBuilder = EntityBuilder.create();
        entityBuilder.setContentType(ContentType.APPLICATION_JSON);
        entityBuilder.setText(requestBody);
        return entityBuilder.build();
    }

    /**
     * Handles the http response received from the post to node discovery
     *
     * @param response
     *            The response received from node discovery
     */
    private void handleResponse(final HttpResponse response) {
        final int statusCode = response.getStatusLine().getStatusCode();
        switch (statusCode) {
            case HttpStatus.SC_OK:
                logger.info("Active connection ids successfully sent to node discovery for audit: {}", statusCode);
                break;
            case HttpStatus.SC_BAD_REQUEST:
                logger.error("Invalid connection ids were sent to node discovery for audit: {}", statusCode);
                break;
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                logger.error("Node Discovery internal error occurred during audit: {}", statusCode);
                break;
            default:
                logger.error("Unknown response from node discovery for audit: {}", statusCode);
                break;
        }
    }

    private String getFullUrl() {
        return System.getProperty("INTERNAL_URL", getDefaultUrl());
    }

    private String getDefaultUrl() {
        return String.format("%s://%s:%d%s", PROTOCOL, MSAP_SERV_HOSTNAME, MSAP_SERV_PORT, SNMP_AUDIT_PATH);
    }
}
