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
package com.ericsson.oss.services.ap.api.workflow;

public interface EoiIntegrationSecurityService {

    public void createCredentialRequest(final String apNodeFdn, final String baseUrl, final String cookie)  ;

    public void snmpRequest(final String apNodeFdn, final String baseUrl, final String cookie);

    public void ldapRequest(final String apNodeFdn,final String baseUrl,final String cookie);

    public void generateEnrollment(final String apNodeFdn,final String baseUrl,final String cookie) ;

    public void cancelEnrollment(final String apNodeFdn,final String baseUrl,final String cookie) ;

}
