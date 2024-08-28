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

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;

import java.util.function.Supplier;

/**
 * Http methods
 */
public enum HttpMethods {
    GET(HttpGet::new),
    POST(HttpPost::new),
    PUT(HttpPut::new),
    DELETE(HttpDelete::new);

    final Supplier<HttpRequestBase> supplier;

    HttpMethods(final Supplier<HttpRequestBase> supplier) {
        this.supplier = supplier;
    }

    public HttpRequestBase getRequest() {
        return this.supplier.get();
    }
}
