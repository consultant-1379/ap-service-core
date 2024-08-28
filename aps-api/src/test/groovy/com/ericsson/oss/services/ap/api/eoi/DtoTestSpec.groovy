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
package groovy.com.ericsson.oss.services.ap.api.eoi


import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.model.eoi.CancelEnrollmentResponse
import com.ericsson.oss.services.ap.api.model.eoi.CredentialsConfiguration
import com.ericsson.oss.services.ap.api.model.eoi.CredentialsConfigurationResponse
import com.ericsson.oss.services.ap.api.model.eoi.CredentialsData
import com.ericsson.oss.services.ap.api.model.eoi.Crl
import com.ericsson.oss.services.ap.api.model.eoi.Domain
import com.ericsson.oss.services.ap.api.model.eoi.EnrollmentCmpConfig
import com.ericsson.oss.services.ap.api.model.eoi.EnrollmentConfigurationResponse
import com.ericsson.oss.services.ap.api.model.eoi.EoiSecurityErrorResponse
import com.ericsson.oss.services.ap.api.model.eoi.LdapConfigurationResponse
import com.ericsson.oss.services.ap.api.model.eoi.SnmpAuthNoPrivConfigurationRequest
import com.ericsson.oss.services.ap.api.model.eoi.SnmpConfigurationRequest
import com.ericsson.oss.services.ap.api.model.eoi.SnmpConfigurationResponse
import com.ericsson.oss.services.ap.api.model.eoi.TrustCategory
import com.ericsson.oss.services.ap.api.model.eoi.TrustedCertificate

import javax.inject.Inject

class DtoTestSpec extends CdiSpecification {

    @ObjectUnderTest
    CredentialsConfiguration credentialsConfiguration;

    @Inject
    CredentialsData credentialsData

    @Inject
    SnmpConfigurationRequest snmpConfigurationRequest

    @Inject
    SnmpAuthNoPrivConfigurationRequest snmpAuthNoPrivConfigurationRequest

    @Inject
    EnrollmentConfigurationResponse enrollmentConfigurationResponse

    @Inject
    LdapConfigurationResponse ldapConfigurationResponse

    @Inject
    CredentialsConfigurationResponse credentialsConfigurationResponse

    @Inject
    SnmpConfigurationResponse snmpConfigurationResponse

    @Inject
    EoiSecurityErrorResponse eoiSecurityErrorResponse

    @Inject
    CancelEnrollmentResponse cancelEnrollmentResponse


    def "Test CReds"(){
        given:
        credentialsData.setCredentialsType("SECURE");
        credentialsData.getCredentialsType();
        credentialsData.setCredUser("netsim");
        credentialsData.getCredUser()
        credentialsData.setCredPass("netsim");
        credentialsData.getCredPass()

        List<CredentialsData> credentailsList = new ArrayList<>();
        credentailsList.add(credentialsData);


        credentialsConfiguration.setCredentialsList(credentailsList);
        credentialsConfiguration.getCredentialsList()
        when:
        credentialsConfiguration.toString()
        then:
        return

    }

    def "Test Snmp"(){
        given:
        snmpConfigurationRequest.setAuthAlgo("NONE");
        snmpConfigurationRequest.getAuthPassword()
        snmpConfigurationRequest.setAuthPassword("");
        snmpConfigurationRequest.getPrivAlgo()
        snmpConfigurationRequest.setAuthPriv("enable");
        snmpConfigurationRequest.getPrivPassword()
        snmpConfigurationRequest.setPrivAlgo("NONE");
        snmpConfigurationRequest.getAuthPriv()
        snmpConfigurationRequest.setPrivPassword("");

        when:
        snmpConfigurationRequest.toString()


        then:
        return
    }

    def "Test snmp2"(){
        given:
        snmpAuthNoPrivConfigurationRequest.setAuthAlgo("NONE");
        snmpAuthNoPrivConfigurationRequest.getAuthAlgo();
        snmpAuthNoPrivConfigurationRequest.setAuthPassword("");
        snmpAuthNoPrivConfigurationRequest.setAuthPriv("disable");
        when:
        snmpAuthNoPrivConfigurationRequest.toString()
        then:
        return
    }

    def "Test enrollment"(){
        given:
        Crl crl =new Crl()
        crl.setCdpsUri("cdpsurl")
        crl.getCdpsUri()
        crl.setId("id")
        crl.getId()
        crl.toString()
        ArrayList<Crl> crls=new ArrayList<>();
        TrustedCertificate trustedCertificate = new TrustedCertificate();
        trustedCertificate.setId ( "id" )
        trustedCertificate.getId()
        trustedCertificate.setCaFingerprint ( "fingerprint" )
        trustedCertificate.getCaFingerprint()
        trustedCertificate.setCaPem ( "capem" )
        trustedCertificate.getCaPem()
        trustedCertificate.setCaSubjectName("subName")
        trustedCertificate.getCaSubjectName()
        trustedCertificate.setCrls(crls)
        trustedCertificate.getCrls()
        trustedCertificate.setTdpsUri("tdsuri")
        trustedCertificate.getTdpsUri()
        trustedCertificate.toString()
        TrustCategory trustCategory = new TrustCategory()
        ArrayList<String> certs =new ArrayList<>();
        certs.add("certs")
        trustCategory.setCertificates (certs)
        trustCategory.getCertificates()
        trustCategory.setId ( "id" )
        trustCategory.getId()
        trustCategory.toString()
        ArrayList<TrustCategory> trustCategories = new ArrayList<>();
        ArrayList<TrustedCertificate> trustedCertificates = new ArrayList<>();
        EnrollmentCmpConfig enrollmentCmpConfig=new EnrollmentCmpConfig()
        enrollmentCmpConfig.setAlgorithm ( "algorithm" )
        enrollmentCmpConfig.getAlgorithm()
        enrollmentCmpConfig.setCertificateId ( "certid" )
        enrollmentCmpConfig.getCertificateId()
        enrollmentCmpConfig.setChallengePassword ( "password" )
        enrollmentCmpConfig.getChallengePassword()
        enrollmentCmpConfig.setCmpTrustCategoryId ( "id" )
        enrollmentCmpConfig.getCmpTrustCategoryId()
        enrollmentCmpConfig.setEnrollmentAuthorityId ( "id" )
        enrollmentCmpConfig.getEnrollmentAuthorityId()
        enrollmentCmpConfig.setEnrollmentAuthorityType ( "type" )
        enrollmentCmpConfig.getEnrollmentAuthorityType()
        enrollmentCmpConfig.setEnrollmentAuthorityName ( "name" )
        enrollmentCmpConfig.getEnrollmentAuthorityName()
        enrollmentCmpConfig.setSubjectName ( "subName" )
        enrollmentCmpConfig.getSubjectName()
        enrollmentCmpConfig.setEnrollmentServerGroupId ( "grpid" )
        enrollmentCmpConfig.getEnrollmentServerGroupId()
        enrollmentCmpConfig.setUrl ( "url" )
        enrollmentCmpConfig.getUrl()
        enrollmentCmpConfig.setTrustCategoryId ( "id" )
        enrollmentCmpConfig.getTrustCategoryId()
        enrollmentCmpConfig.setEnrollmentServerId ( "id" )
        enrollmentCmpConfig.getEnrollmentServerId()
        enrollmentCmpConfig.toString()
        Domain domain=new  Domain()
        domain.setTrustCategories ( trustCategories )
        domain.getTrustCategories()
        domain.setTrustedCertificates ( trustedCertificates )
        domain.getTrustedCertificates()
        domain.setDomainName ( "domainName" )
        domain.getDomainName()
        domain.setEnrollmentCmpConfig(enrollmentCmpConfig)
        domain.getEnrollmentCmpConfig()
        domain.toString()
        enrollmentConfigurationResponse.setDomain(domain)
        enrollmentConfigurationResponse.getDomain()


        when:
        enrollmentConfigurationResponse.toString()
        then:
        return

    }

    def "Test Ldap"(){
        given:
        ldapConfigurationResponse.setTlsPort("tlsport")
        ldapConfigurationResponse.getTlsPort()
        ldapConfigurationResponse.setLdapsPort("ldapport")
        ldapConfigurationResponse.getLdapsPort()
        ldapConfigurationResponse.setLdapIpAddress("1.1.1.1")
        ldapConfigurationResponse.getLdapIpAddress()
        ldapConfigurationResponse.setFallbackLdapIpAddress("0.0.0.0")
        ldapConfigurationResponse.getFallbackLdapIpAddress()
        ldapConfigurationResponse.setBindDn("binddn")
        ldapConfigurationResponse.getBindDn()
        ldapConfigurationResponse.setBindPassword("bindPass")
        ldapConfigurationResponse.getBindPassword()
        ldapConfigurationResponse.setBaseDn("basedn")
        ldapConfigurationResponse.getBaseDn()
        when:
        ldapConfigurationResponse.toString()
        then:
        return
    }


    def "Test Certs Response"(){
        given:
        List<String> credList =new ArrayList<>()
        credList.add("SECURE")
        credentialsConfigurationResponse.setCredentials(credList)
        credentialsConfigurationResponse.getCredentials()
        when:
        credentialsConfigurationResponse.toString()
        then:
        return

    }

    def "Test Snmp Response"(){
        given:
        snmpConfigurationResponse.setAuthPriv("enable")
        snmpConfigurationResponse.getAuthPriv()
        when:
        snmpConfigurationResponse.toString()
        then:
        return
    }


    def "Error Response Test"(){
        given:
        eoiSecurityErrorResponse.setHttpStatus("httpstatus")
        eoiSecurityErrorResponse.getHttpStatus()
        eoiSecurityErrorResponse.setCausedBy("caused")
        eoiSecurityErrorResponse.getCausedBy()
        eoiSecurityErrorResponse.setMessage("msg")
        eoiSecurityErrorResponse.getMessage()
        eoiSecurityErrorResponse.setSuggestedSolution("solution")
        eoiSecurityErrorResponse.getSuggestedSolution()
        when:
        eoiSecurityErrorResponse.toString()
        then:
        return
    }

    def "Canncel Enrolment Response Test"(){


        given:
        cancelEnrollmentResponse.setResource("resource")
        cancelEnrollmentResponse.getResource()
        cancelEnrollmentResponse.setResourceId("resourceId")
        cancelEnrollmentResponse.getResourceId()
        cancelEnrollmentResponse.setSubResource("subresource")
        cancelEnrollmentResponse.getSubResource()
        cancelEnrollmentResponse.setSubResourceId("subResourceId")
        cancelEnrollmentResponse.getSubResourceId()
        when:
        cancelEnrollmentResponse.toString()
        then:
        return

    }
}
