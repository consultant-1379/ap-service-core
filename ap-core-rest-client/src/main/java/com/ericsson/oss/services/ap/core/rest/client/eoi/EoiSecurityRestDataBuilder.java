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


import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.ServiceUnavailableException;

import com.ericsson.oss.itpf.sdk.core.retry.*;
import com.ericsson.oss.services.ap.api.exception.ApSecurityException;
import com.ericsson.oss.services.ap.api.model.eoi.*;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.retry.classic.RetryManagerBean;
import com.ericsson.oss.services.ap.common.util.string.FDN;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EoiSecurityRestDataBuilder {

    private static final String OSS_NSCS_NBI_V1_NODES = "/oss/nscs/nbi/v1/nodes/";

    @Inject
    private EoiSecurityRestResponseBuilder eoiSecurityRestResponseBuilder;

    private static final Logger LOGGER = LoggerFactory.getLogger(EoiSecurityRestDataBuilder.class);

    private ObjectMapper objectMapper = new ObjectMapper();


    public Object buildCredentialsRequest(String apNodeFdn, CredentialsConfiguration credentialsConfiguration, String baseUrl, String cookie)  {
try {
    final String nodeName = FDN.get(apNodeFdn).getRdnValue();


    final StringEntity requestBody = new StringEntity(objectMapper.writeValueAsString(credentialsConfiguration));

    final String credentailConfigurationUrl = baseUrl + OSS_NSCS_NBI_V1_NODES + nodeName + "/credentials";
    LOGGER.info("full path for credential update {} :", credentailConfigurationUrl);
    RetriableCommand<Object> retriableCommand = (final RetryContext retryContext) -> {
        HttpResponse httpResponse = eoiSecurityRestResponseBuilder.httpPutRequests(credentailConfigurationUrl, requestBody, cookie);
        final int statusCode = httpResponse.getStatusLine().getStatusCode();
        LOGGER.info("status code for apNodeFdn:{} for Credential update {}:", apNodeFdn, statusCode);
        return statusGeneration(statusCode, httpResponse, apNodeFdn, CredentialsConfigurationResponse.class);

    };
    return executeRetriableCommand(retriableCommand);
    }
    catch(Exception ex){
    LOGGER.info("Exception while buildCredentialsRequest with error message :{} for apNodeFdn :{}",ex.getMessage(),apNodeFdn);
        throw new ApSecurityException(ex.getMessage());
    }
    }

    public Object buildLadpRequest(String apNodeFdn, String baseUrl, String cookie){
       try {
           final String nodeName = FDN.get(apNodeFdn).getRdnValue();
           RetriableCommand<Object> retriableCommand = (final RetryContext retryContext) -> {

               HttpResponse httpResponse = eoiSecurityRestResponseBuilder.httpPostRequests(baseUrl + OSS_NSCS_NBI_V1_NODES + nodeName + "/ldap", null, cookie);
               final int statusCode = httpResponse.getStatusLine().getStatusCode();
               LOGGER.info("Status code for apNodeFdn:{} for LDAP:{}", apNodeFdn, statusCode);
               return statusGeneration(statusCode, httpResponse, apNodeFdn, LdapConfigurationResponse.class);
           };
           return executeRetriableCommand(retriableCommand);
       }
       catch (Exception ex){
           LOGGER.info("Exception while buildLadpRequest with error message :{} for apNodeFdn :{}",ex.getMessage(),apNodeFdn);

           throw new ApSecurityException(ex.getMessage());
       }

    }

    private static Object executeRetriableCommand(final RetriableCommand<Object> retriableCommand) {
        final RetryManager retryManager = new RetryManagerBean();

        final RetryPolicy policy = RetryPolicy.builder()
                .attempts(3)
                .waitInterval(5, TimeUnit.SECONDS)
                .retryOn(ServiceUnavailableException.class)
                .build();
         return retryManager.executeCommand(policy, retriableCommand);

    }

    public Object buildSnmpRequest(String apNodeFdn, String baseUrl, String cookie, Object snmpConfigurationRequest) {
        try {
            final String nodeName = FDN.get(apNodeFdn).getRdnValue();
            final StringEntity requestBody = new StringEntity(objectMapper.writeValueAsString(snmpConfigurationRequest));
            RetriableCommand<Object> retriableCommand = (final RetryContext retryContext) -> {

                HttpResponse httpResponse = eoiSecurityRestResponseBuilder.httpPutRequests(baseUrl + OSS_NSCS_NBI_V1_NODES + nodeName + "/snmp", requestBody, cookie);
                final int statusCode = httpResponse.getStatusLine().getStatusCode();
                LOGGER.info("Status code for apNodeFdn:{} for SNMP:{}", apNodeFdn, statusCode);

                return statusGeneration(statusCode, httpResponse, apNodeFdn, SnmpConfigurationResponse.class);
            };
            return executeRetriableCommand(retriableCommand);

        } catch (Exception ex) {
            LOGGER.info("Exception while buildSnmpRequest with error message :{} for apNodeFdn :{}",ex.getMessage(),apNodeFdn);

            throw new ApSecurityException(ex.getMessage());
        }
    }

    public Object buildEnrolmentRequest(final String apNodeFdn,final String baseUrl,final String cookie)  {
        try {
            final String nodeName = FDN.get(apNodeFdn).getRdnValue();
            RetriableCommand<Object> retriableCommand = (final RetryContext retryContext) -> {

                HttpResponse httpResponse = eoiSecurityRestResponseBuilder.httpPostRequests(baseUrl + OSS_NSCS_NBI_V1_NODES + nodeName + "/domains/" + "OAM", null, cookie);
                final int statusCode = httpResponse.getStatusLine().getStatusCode();
                LOGGER.info("Status code for apNodeFdn:{} for Enrolment:{}", apNodeFdn, statusCode);
                return statusGeneration(statusCode, httpResponse, apNodeFdn, EnrollmentConfigurationResponse.class);
            };
            return executeRetriableCommand(retriableCommand);


        } catch (Exception ex) {
            LOGGER.info("Exception while buildEnrolmentRequest with error message :{} for apNodeFdn :{}",ex.getMessage(),apNodeFdn);

            throw new ApSecurityException(ex.getMessage());
        }
    }

    public Object cancelEnrollment(final String apNodeFdn,final String baseUrl,final String cookie)  {
        try {
            final String nodeName = FDN.get(apNodeFdn).getRdnValue();
            RetriableCommand<Object> retriableCommand = (final RetryContext retryContext) -> {

                HttpResponse httpResponse = eoiSecurityRestResponseBuilder.httpDeleteRequests(baseUrl + OSS_NSCS_NBI_V1_NODES + nodeName + "/domains/" + "OAM", cookie);
                final int statusCode = httpResponse.getStatusLine().getStatusCode();
                LOGGER.info("Status code for apNodeFdn:{} for Cancel Enrolment:{}", apNodeFdn, statusCode);
                return statusGeneration(statusCode, httpResponse, apNodeFdn, CancelEnrollmentResponse.class);
            };
            return executeRetriableCommand(retriableCommand);

        } catch (Exception ex) {
            LOGGER.info("Exception while cancelEnrollment with error message :{} for apNodeFdn :{}",ex.getMessage(),apNodeFdn);

            throw new ApSecurityException(ex.getMessage());
        }
    }

    private <T> Object statusGeneration(final int statusCode,final HttpResponse httpResponse,final String apNodeFdn,final Class<T> pojoclass)  {
       try {
           switch (statusCode) {
               case HttpStatus.SC_OK:
               case HttpStatus.SC_CREATED:
                   T securityResponse = objectMapper.readValue(EntityUtils.toString(httpResponse.getEntity()), pojoclass);
                   LOGGER.info("Security  response for Api Class:{} and for  apNodeFdn :{} is :{}", pojoclass,apNodeFdn, securityResponse);
                   return securityResponse;

               case HttpStatus.SC_BAD_REQUEST:
               case HttpStatus.SC_INTERNAL_SERVER_ERROR:
               case HttpStatus.SC_NOT_FOUND:
               case HttpStatus.SC_FORBIDDEN:
               case HttpStatus.SC_CONFLICT:
               case HttpStatus.SC_NOT_IMPLEMENTED:
                   EoiSecurityErrorResponse eoiSecurityErrorResponse = objectMapper.readValue(EntityUtils.toString(httpResponse.getEntity()), EoiSecurityErrorResponse.class);
                   LOGGER.info("Error  response for apNodeFdn:{} is :{} for Security Api Class :{}", apNodeFdn, eoiSecurityErrorResponse,pojoclass);
                   throw new ApSecurityException("Exception from security api for apNodeFdn: "+apNodeFdn+" caused by: "+eoiSecurityErrorResponse.getCausedBy()+" with message : "+eoiSecurityErrorResponse.getMessage()+" with httpstatus: "+eoiSecurityErrorResponse.getHttpStatus()+" so suggested solution is: "+eoiSecurityErrorResponse.getSuggestedSolution());
               case HttpStatus.SC_SERVICE_UNAVAILABLE:
                   LOGGER.info("Tried for maximum attempts for apNodeFdn: {} ,Security Api Class :{}", apNodeFdn,pojoclass);
                   throw new ServiceUnavailableException("Service Unavailable Exception");
               default:
                   LOGGER.info("Going to default for  apNodeFdn:{} with Status code :{} for Security Api Class :{}", apNodeFdn, statusCode,pojoclass);

           }
       }
       catch(Exception ex){
           LOGGER.info("Exception while status Generation for security api's :{} for apNodeFdn:{}",ex.getMessage(),apNodeFdn);
           throw new ApSecurityException(ex.getMessage());
       }
        return null;

    }

}
