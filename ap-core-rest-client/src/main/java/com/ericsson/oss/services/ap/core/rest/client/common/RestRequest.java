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
package com.ericsson.oss.services.ap.core.rest.client.common;

import static com.ericsson.oss.services.ap.core.rest.client.common.HttpConstants.ACCEPT_HEADER;
import static com.ericsson.oss.services.ap.core.rest.client.common.HttpConstants.CONTENT_TYPE_HEADER;
import static com.ericsson.oss.services.ap.core.rest.client.common.HttpConstants.USERNAME_HEADER;
import static com.ericsson.oss.services.ap.core.rest.client.common.RestResponse.getDefaultResponseHandler;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;

/**
 * Representation of REST request
 */
public class RestRequest {

    public static final int INVALID_STATUS_CODE = -1;
    private final HttpRequestBase request;

    public RestRequest(final HttpRequestBase request) {
        this.request = request;
    }

    /**
     * Send REST request without response handler just simple error data deserialization to E type
     *
     * @param <E> Type of error data in case of failure
     * @param errorDetailsType the type of error details
     * @return {@link RestResponse} object
     */
    public <E> RestResponse<Void, E> send(final Class<E> errorDetailsType) {
        return send(getDefaultResponseHandler(Void.class, errorDetailsType));
    }

    /**
     * Send REST request with default response handler.
     * Default response handler deserialize JSON data to T type in case of success
     * and error data to E type in case of failure
     *
     * @param entityType       Success data class
     * @param errorDetailsType Failure data (error data) class
     * @param <T>              Success data type
     * @param <E>              Failure data (error data) type
     * @return {@link RestResponse} object
     */
    public <T, E> RestResponse<T, E> send(final Class<T> entityType, final Class<E> errorDetailsType) {
        return send(getDefaultResponseHandler(entityType, errorDetailsType));
    }

    /**
     * Send REST request with generic response handler.
     * Generic response handler is lambda which takes {@link HttpRequestBase} parameter and produces {@link RestResponse} object
     *
     * @param responseHandler Generic response handler
     * @param <T>             Success data type
     * @param <E>             Failure data (error data) type
     * @return {@link RestResponse} object
     */
    public <T, E> RestResponse<T, E> send(final Function<HttpResponse, RestResponse<T, E>> responseHandler) {
        try (final CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            final CloseableHttpResponse response = httpClient.execute(this.request)) {
            return responseHandler.apply(response);
        } catch (IOException e) {
            return new RestResponse<>(INVALID_STATUS_CODE);
        }
    }

    /**
     * Builder of {@link RestRequest}
     */
    public static class Builder {

        private final Map<String, String> headers = new HashMap<>();
        private String url;
        private String resourceId = "";
        private HttpMethods method = HttpMethods.GET;
        private Object entity;

        private Builder() {
        }

        /**
         * Builder function
         *
         * @param url Main URL value
         * @return {@link Builder} object
         */
        public static Builder of(final String url) {
            final Builder builder = new Builder();
            builder.url = url;
            builder.setContentType(ContentType.APPLICATION_JSON.toString());
            builder.setAcceptType(ContentType.APPLICATION_JSON.toString());
            return builder;
        }

        /**
         * Set request method (GET, POST, PUT, DELETE)
         *
         * @param method Value of {@link HttpMethods}
         * @return {@link Builder} object
         */
        public Builder setMethod(final HttpMethods method) {
            this.method = method;
            return this;
        }

        /**
         * Set resource ID (this value, when available, will be added at the end of URL)
         *
         * @param resourceId Resource ID value
         * @return {@link Builder} object
         */
        public Builder setResourceId(final String resourceId) {
            this.resourceId = resourceId;
            return this;
        }

        /**
         * Set generic header
         *
         * @param headerName  Name of header
         * @param headerValue Value of header
         * @return {@link Builder} object
         */
        public Builder setHeader(final String headerName, String headerValue) {
            this.headers.put(headerName, headerValue);
            return this;
        }

        /**
         * Set entity and 'Content-Type' and 'Accept-Type' to JSON
         *
         * @param entity Entity to set on request
         * @return {@link Builder} object
         */
        public Builder setEntity(final Object entity) {
            this.entity = entity;
            this.setContentType(ContentType.APPLICATION_JSON.toString());
            this.setAcceptType(ContentType.APPLICATION_JSON.toString());
            return this;
        }

        /**
         * Set 'X-Tor-UserID' header for ENM authorization purposes
         *
         * @param username 'X-Tor-UserID' header value
         * @return {@link Builder} object
         */
        public Builder setAuthorization(final String username) {
            this.setHeader(USERNAME_HEADER, username);
            return this;
        }

        /**
         * Set 'Content-Type' header
         *
         * @param contentType 'Content-Type' header value
         * @return {@link Builder} object
         */
        public Builder setContentType(final String contentType) {
            this.setHeader(CONTENT_TYPE_HEADER, contentType);
            return this;
        }

        /**
         * Set 'Accept-Type' header
         *
         * @param acceptType 'Accept-Type' header value
         * @return {@link Builder} object
         */
        public Builder setAcceptType(final String acceptType) {
            this.setHeader(ACCEPT_HEADER, acceptType);
            return this;
        }

        /**
         * Build {@link RestRequest} object
         *
         * @return {@link RestRequest} object
         */
        public RestRequest build() {
            if (this.url == null || this.url.trim().isEmpty()) {
                throw new IllegalArgumentException("Missing 'url'");
            }
            final HttpRequestBase request = this.method.getRequest();
            final String resourceContext = this.resourceId.isEmpty() ? "" : "/" + this.resourceId;
            request.setURI(URI.create(this.url + resourceContext));
            this.headers.forEach(request::setHeader);
            updateEntity(request);
            return new RestRequest(request);
        }

        /**
         * Update entity on {@link HttpRequestBase} object if object allows for it
         * - if object is instance of {@link HttpEntityEnclosingRequestBase}
         *
         * @param request {@link HttpRequestBase} object for update
         */
        private void updateEntity(final HttpRequestBase request) {
            if (this.entity != null && request instanceof HttpEntityEnclosingRequestBase) {
                final String jsonEntity = new Gson().toJson(this.entity);
                final StringEntity stringEntity = new StringEntity(jsonEntity, StandardCharsets.UTF_8.name());
                ((HttpEntityEnclosingRequestBase) request).setEntity(stringEntity);
            }
        }

    }
}
