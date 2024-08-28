/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2024
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.rest.client.eoi

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.exception.ApSecurityException
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.cookie.BasicClientCookie

import org.apache.http.client.CookieStore
import java.time.Instant;
import java.time.temporal.ChronoUnit;

class EoiSecurityRestReposneBuilderSpec extends CdiSpecification{

    @ObjectUnderTest
    private EoiSecurityRestResponseBuilder eoiSecurityRestResponseBuilder

    @MockedImplementation
    private  EoiSecurityClientService eoiSecurityClientService

    def "Test EoiSecurityReponseBuilder put Request"(){
        given:

        CookieStore httpCookieStore;
        httpCookieStore = new BasicCookieStore();
        final BasicClientCookie clientCookie = new BasicClientCookie("iPlanetDirectoryPro", "iPlanetDirectoryPro=xyz");

        clientCookie.setExpiryDate(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        httpCookieStore.addCookie(clientCookie);
        eoiSecurityClientService.getHttpClient()
        when:
        eoiSecurityRestResponseBuilder.httpPutRequests("http://ericsson.se/oss/nscs/nbi/v1/nodes/NodeName/credentials",new StringEntity("{}"),"iPlanetDirectoryPro=xyz")
        then:
        thrown ApSecurityException
    }

    def "Test EoiSecurityReponseBuilder post Request"(){
        given:

        CookieStore httpCookieStore;
        httpCookieStore = new BasicCookieStore();
        final BasicClientCookie clientCookie = new BasicClientCookie("iPlanetDirectoryPro", "iPlanetDirectoryPro=xyz");

        clientCookie.setExpiryDate(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        httpCookieStore.addCookie(clientCookie);
eoiSecurityClientService.getHttpClient()

        when:
        eoiSecurityRestResponseBuilder.httpPostRequests("http://ericsson.se/oss/nscs/nbi/v1/nodes/NodeName/credentials",new StringEntity("{}"),"iPlanetDirectoryPro=xyz")
        then:
        thrown ApSecurityException
    }

    def "Test EoiSecurityReponseBuilder delete Request"(){
        given:

        CookieStore httpCookieStore;
        httpCookieStore = new BasicCookieStore();
        final BasicClientCookie clientCookie = new BasicClientCookie("iPlanetDirectoryPro", "iPlanetDirectoryPro=xyz");

        clientCookie.setExpiryDate(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        httpCookieStore.addCookie(clientCookie);
        eoiSecurityClientService.getHttpClient()

        when:
        eoiSecurityRestResponseBuilder.httpDeleteRequests("http://ericsson.se/oss/nscs/nbi/v1/nodes/NodeName/credentials","iPlanetDirectoryPro=xyz")
        then:
        thrown ApSecurityException
    }
}
