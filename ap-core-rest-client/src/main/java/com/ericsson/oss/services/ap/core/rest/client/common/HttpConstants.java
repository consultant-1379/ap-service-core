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

/**
 * Constants used locally in REST layer.
 */
public final class HttpConstants {

    public static final String ACCEPT_HEADER = "Accept";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final int DEFAULT_PORT = 8080;
    public static final String HOST = "host";
    public static final String INTERNAL_URL = "INTERNAL_URL";
    public static final String NAME = "name";
    public static final String PROTOCOL = "http";
    public static final String USERNAME_HEADER = "X-Tor-UserID";

    private HttpConstants() {
    }
}
