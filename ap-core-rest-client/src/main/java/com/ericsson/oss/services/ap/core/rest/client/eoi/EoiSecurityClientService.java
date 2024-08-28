
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

package com.ericsson.oss.services.ap.core.rest.client.eoi;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD")
public class EoiSecurityClientService {

    /**
     * Timeout for client connections
     */
    private static final int TIMEOUT = 60000; // 60 SECONDS
    private static final int MAX_CONNECTIONS = 10;

    private static final Logger logger = LoggerFactory.getLogger(EoiSecurityClientService.class);

    private CookieStore httpCookieStore;
    private CloseableHttpClient httpClient;

     public EoiSecurityClientService() {

    }

    public EoiSecurityClientService(final CookieStore httpCookieStore, final CloseableHttpClient httpClient) {
        super();
        this.httpCookieStore = httpCookieStore;
        this.httpClient = httpClient;
    }

    public CookieStore getHttpCookieStore() {
        return httpCookieStore;
    }

    /**
     * @return the httpClient
     */
    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Gets instance of Http Client
     *
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @SuppressWarnings("deprecation")
    public EoiSecurityClientService getHttpClient(final boolean isSecureProtocol, final String cookie) throws IOException {

        EoiSecurityClientService externalHttpClient = null;

        httpCookieStore = new BasicCookieStore();
        final BasicClientCookie clientCookie = new BasicClientCookie("iPlanetDirectoryPro", cookie);

        clientCookie.setExpiryDate(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)));
        httpCookieStore.addCookie(clientCookie);

        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(TIMEOUT)
                .setConnectionRequestTimeout(TIMEOUT)
                .setSocketTimeout(TIMEOUT).build();

        if (isSecureProtocol) {
            // Create a trust manager that does not validate certificate chains
            SSLContext sslContext;

            try {

                sslContext = SSLContext.getInstance("TLS");

                final TrustManager tm = new X509TrustManager() {

                    @Override
                    public void checkClientTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
                        logger.info("Trusted Client Certificates");
                    }

                    @Override
                    public void checkServerTrusted(final X509Certificate[] arg0, final String arg1) throws CertificateException {
                        logger.info("Trusted Server Certificates");

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers()
                    {
                        return new X509Certificate[0];
                    }
                };

                sslContext.init(null, new TrustManager[] { tm }, null);
                externalHttpClient = new EoiSecurityClientService(httpCookieStore,
                        HttpClients.custom().setDefaultRequestConfig(config).setDefaultCookieStore(httpCookieStore).setMaxConnPerRoute(MAX_CONNECTIONS).setMaxConnTotal(MAX_CONNECTIONS)
                                .setSslcontext(sslContext).setHostnameVerifier(new AllowAllHostnameVerifier()).build());

            } catch (final GeneralSecurityException e) {
                throw new IOException(e);
            }

        } else {
            externalHttpClient = new EoiSecurityClientService(httpCookieStore, HttpClients.custom().setDefaultRequestConfig(config).setDefaultCookieStore(httpCookieStore)
                    .setMaxConnPerRoute(MAX_CONNECTIONS).setMaxConnTotal(MAX_CONNECTIONS).build());
        }
        return externalHttpClient;
    }
}
