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

import javax.inject.Inject;

import com.ericsson.oss.services.ap.api.exception.ApSecurityException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EoiSecurityRestResponseBuilder {

    @Inject
    private EoiSecurityClientService eoiSecurityClientService;
    private static final Logger LOGGER = LoggerFactory.getLogger(EoiSecurityClientService.class);


    public HttpResponse httpPutRequests(final String url,final StringEntity requestBody,final String cookie) throws IOException {
       try {
           final HttpPut putRequest = new HttpPut(url);

           putRequest.setEntity(requestBody);
           putRequest.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
           putRequest.addHeader("Cookie", "iPlanetDirectoryPro=" + cookie);


           return eoiSecurityClientService.getHttpClient(true, cookie).getHttpClient().execute(putRequest);
       }
       catch(Exception ex){
           LOGGER.info("Exception while httpPutRequests for requestBody :{} ,with error message :{} ",requestBody,ex.getMessage());

           throw new ApSecurityException(ex.getMessage());
       }

    }

    public HttpResponse httpPostRequests(final String url,final StringEntity requestBody,final String cookie) throws IOException {
       try {
           final HttpPost postRequest = new HttpPost(url);

           postRequest.setEntity(requestBody);
           postRequest.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
           postRequest.addHeader("Cookie", "iPlanetDirectoryPro=" + cookie);

           return eoiSecurityClientService.getHttpClient(true, cookie).getHttpClient().execute(postRequest);
       }
       catch(Exception ex){
           LOGGER.info("Exception while httpPostRequests for requestBody :{} ,with error message :{} ",requestBody,ex.getMessage());

           throw new ApSecurityException(ex.getMessage());
       }
    }

    public HttpResponse httpDeleteRequests(final String url,final String cookie) throws IOException {
        try {
            final HttpDelete deleteRequest = new HttpDelete(url);

            deleteRequest.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
            deleteRequest.addHeader("Cookie", "iPlanetDirectoryPro=" + cookie);

            return eoiSecurityClientService.getHttpClient(true, cookie).getHttpClient().execute(deleteRequest);
        }
        catch (Exception ex){
            LOGGER.info("Exception while httpDeleteRequests with error message :{} ",ex.getMessage());

            throw new ApSecurityException(ex.getMessage());
        }
    }

}
