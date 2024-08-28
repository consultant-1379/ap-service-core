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

import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.services.ap.api.model.eoi.CredentialsConfiguration;

public class EoiSecurityRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(EoiSecurityRestClient.class);

    @Inject
    private EoiSecurityRestDataBuilder eoiSecurityRestDataBuilder;

    public Object createCredentialRequest(final String apNodeFdn, final CredentialsConfiguration credentialsConfiguration,final String baseUrl,final String cookie)   {
        LOGGER.info("create credential with apNodeFdn : {} and credentialsConfiguration : {}", apNodeFdn, credentialsConfiguration);
        return eoiSecurityRestDataBuilder.buildCredentialsRequest(apNodeFdn, credentialsConfiguration, baseUrl, cookie);

    }

    public Object ldapRequest(final String apNodeFdn,final String baseUrl,final String cookie)  {
        LOGGER.info("create LDAP with apNodeFdn: {}",apNodeFdn);
        return eoiSecurityRestDataBuilder.buildLadpRequest(apNodeFdn, baseUrl, cookie);

    }

    public Object snmpRequest(final String apNodeFdn,final String baseUrl,final String cookie,final Object snmpConfigurationRequest)  {

            LOGGER.info("create snmp request with apNodeFdn: {} and snmpConfigurationRequest: {}",apNodeFdn,snmpConfigurationRequest);

         return eoiSecurityRestDataBuilder.buildSnmpRequest(apNodeFdn, baseUrl, cookie, snmpConfigurationRequest);

    }



    public Object generateEnrollment(final String apNodeFdn,final String baseUrl,final String cookie)  {
        LOGGER.info("create enrolment request with apNodeFdn: {} ",apNodeFdn);

        return eoiSecurityRestDataBuilder.buildEnrolmentRequest(apNodeFdn, baseUrl, cookie);
    }

    public Object cancelEnrollment(final String apNodeFdn,final String baseUrl,final String cookie)  {
        LOGGER.info("cancel enrolment request with apNodeFdn: {} ",apNodeFdn);

        return eoiSecurityRestDataBuilder.cancelEnrollment(apNodeFdn,baseUrl,cookie);
    }

}
