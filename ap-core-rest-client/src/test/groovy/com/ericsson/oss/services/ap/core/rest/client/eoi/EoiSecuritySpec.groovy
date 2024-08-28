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
import com.ericsson.oss.services.ap.api.exception.ApSecurityException

import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.oss.services.ap.api.model.eoi.CancelEnrollmentResponse
import com.ericsson.oss.services.ap.api.model.eoi.CredentialsConfiguration
import com.ericsson.oss.services.ap.api.model.eoi.CredentialsConfigurationResponse
import com.ericsson.oss.services.ap.api.model.eoi.CredentialsData
import com.ericsson.oss.services.ap.api.model.eoi.Crl
import com.ericsson.oss.services.ap.api.model.eoi.Domain
import com.ericsson.oss.services.ap.api.model.eoi.EnrollmentCmpConfig
import com.ericsson.oss.services.ap.api.model.eoi.EnrollmentConfigurationResponse
import com.ericsson.oss.services.ap.api.model.eoi.LdapConfigurationResponse
import com.ericsson.oss.services.ap.api.model.eoi.SnmpAuthNoPrivConfigurationRequest
import com.ericsson.oss.services.ap.api.model.eoi.SnmpConfigurationRequest
import com.ericsson.oss.services.ap.api.model.eoi.SnmpConfigurationResponse
import com.ericsson.oss.services.ap.api.model.eoi.TrustCategory
import com.ericsson.oss.services.ap.api.model.eoi.TrustedCertificate
import com.ericsson.oss.services.ap.common.util.string.FDN
import org.apache.http.ProtocolVersion
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.message.BasicHttpResponse
import org.apache.http.message.BasicStatusLine
import org.mockito.Mock



class EoiSecuritySpec extends CdiSpecification {


    @ObjectUnderTest
    private EoiSecurityRestClient eoiSecurityRestClient

    @Mock
    private EoiSecurityRestDataBuilder eoiSecurityRestDataBuilder


    @MockedImplementation
    private BasicHttpResponse httpResponse

    @MockedImplementation
    private EoiSecurityRestResponseBuilder eoiSecurityRestResponseBuilder

    def "When createCredentialRequest Positive"(){
        given:

        final CredentialsConfiguration credentialsConfiguration = new CredentialsConfiguration();

        CredentialsData credentialsData = new CredentialsData();
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

        CredentialsConfigurationResponse credentialsConfigurationResponse =new CredentialsConfigurationResponse()
        List<String> credList =new ArrayList<>()
        credList.add("SECURE")
        credentialsConfigurationResponse.setCredentials(credList)
        credentialsConfigurationResponse.getCredentials()

        FDN.get("ManagedElement=NodeName").getRdnValue() >> "NodeName"


        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP",1,1);
        BasicStatusLine basicStatusLine =  new BasicStatusLine(protocolVersion, 200, null);
        httpResponse.setStatusLine(basicStatusLine);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(basicStatusLine);

        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        String response = "{}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        basicHttpEntity.setContent(byteArrayInputStream);
        basicHttpResponse.setEntity(basicHttpEntity)
        eoiSecurityRestResponseBuilder.httpPutRequests( _, _, _) >> basicHttpResponse
        eoiSecurityRestDataBuilder.buildCredentialsRequest("ManagedElement=NodeName", credentialsConfiguration, "https://", "iPlanetDirectoryPro=xyz") >> credentialsConfigurationResponse


        when:
        eoiSecurityRestClient.createCredentialRequest("ManagedElement=NodeName", credentialsConfiguration, "https://", "iPlanetDirectoryPro=xyz")
        then:
        return
    }

    def "When createCredentialRequest Negative"(){
        given:

        final CredentialsConfiguration credentialsConfiguration = new CredentialsConfiguration();

        CredentialsData credentialsData = new CredentialsData();
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

        CredentialsConfigurationResponse credentialsConfigurationResponse =new CredentialsConfigurationResponse()
        List<String> credList =new ArrayList<>()
        credList.add("SECURE")
        credentialsConfigurationResponse.setCredentials(credList)
        credentialsConfigurationResponse.getCredentials()

        FDN.get("ManagedElement=NodeName").getRdnValue() >> "NodeName"


        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP",1,1);
        BasicStatusLine basicStatusLine =  new BasicStatusLine(protocolVersion, 500, null);
        httpResponse.setStatusLine(basicStatusLine);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(basicStatusLine);
        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        String response = "{}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        basicHttpEntity.setContent(byteArrayInputStream);
        basicHttpResponse.setEntity(basicHttpEntity)
        eoiSecurityRestResponseBuilder.httpPutRequests( _, _, _) >> basicHttpResponse
        eoiSecurityRestDataBuilder.buildCredentialsRequest("ManagedElement=NodeName", credentialsConfiguration, "https://", "iPlanetDirectoryPro=xyz") >> credentialsConfigurationResponse


        when:
        eoiSecurityRestClient.createCredentialRequest("ManagedElement=NodeName", credentialsConfiguration, "https://", "iPlanetDirectoryPro=xyz")
        then:
        thrown(Exception)
    }

    def "When SnmpRequest Positive UseCase Triggered"(){
        given:

        SnmpConfigurationRequest snmpConfigurationRequest = new SnmpConfigurationRequest();
        snmpConfigurationRequest.setAuthAlgo("NONE");
        snmpConfigurationRequest.setAuthPassword("");
        snmpConfigurationRequest.setAuthPriv("enable");
        snmpConfigurationRequest.setPrivAlgo("NONE");
        snmpConfigurationRequest.setPrivPassword("");
        SnmpConfigurationResponse snmpConfigurationResponse =new SnmpConfigurationResponse();
        snmpConfigurationResponse.setAuthPriv("enable")

        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP",1,1);
        BasicStatusLine basicStatusLine =  new BasicStatusLine(protocolVersion, 200, null);
        httpResponse.setStatusLine(basicStatusLine);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(basicStatusLine);
        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        String response = "{}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        basicHttpEntity.setContent(byteArrayInputStream);
        basicHttpResponse.setEntity(basicHttpEntity)
        eoiSecurityRestResponseBuilder.httpPutRequests( _, _, _) >> basicHttpResponse



        eoiSecurityRestDataBuilder.buildSnmpRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz",(SnmpConfigurationRequest)snmpConfigurationRequest) >> snmpConfigurationResponse
        when:
        eoiSecurityRestClient.snmpRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz",snmpConfigurationRequest)
        then:
        return
    }

    def "When SnmpRequest2 Positive UseCase Triggered"(){
        given:
        SnmpAuthNoPrivConfigurationRequest snmpConfigurationRequest = new SnmpAuthNoPrivConfigurationRequest();
        snmpConfigurationRequest.setAuthAlgo("NONE");
        snmpConfigurationRequest.setAuthPassword("");
        snmpConfigurationRequest.setAuthPriv("disable");

        SnmpConfigurationResponse snmpConfigurationResponse =new SnmpConfigurationResponse();
        snmpConfigurationResponse.setAuthPriv("disable")

        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP",1,1);
        BasicStatusLine basicStatusLine =  new BasicStatusLine(protocolVersion, 200, null);
        httpResponse.setStatusLine(basicStatusLine);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(basicStatusLine);

        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        String response = "{}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        basicHttpEntity.setContent(byteArrayInputStream);
        basicHttpResponse.setEntity(basicHttpEntity)
        eoiSecurityRestResponseBuilder.httpPutRequests( _, _, _) >> basicHttpResponse


        eoiSecurityRestDataBuilder.buildSnmpRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz",(SnmpAuthNoPrivConfigurationRequest)snmpConfigurationRequest) >> snmpConfigurationResponse
        when:
        eoiSecurityRestClient.snmpRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz",snmpConfigurationRequest)
        then:
return
    }
    def "When SnmpRequest2 Negative UseCase Triggered"(){
        given:

        SnmpAuthNoPrivConfigurationRequest snmpConfigurationRequest = new SnmpAuthNoPrivConfigurationRequest();
        snmpConfigurationRequest.setAuthAlgo("NONE");
        snmpConfigurationRequest.setAuthPassword("");
        snmpConfigurationRequest.setAuthPriv("disable");

        SnmpConfigurationResponse snmpConfigurationResponse =new SnmpConfigurationResponse();
        snmpConfigurationResponse.setAuthPriv("disable")

        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP",1,1);
        BasicStatusLine basicStatusLine =  new BasicStatusLine(protocolVersion, 400, null);
        httpResponse.setStatusLine(basicStatusLine);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(basicStatusLine);

        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        String response = "{}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        basicHttpEntity.setContent(byteArrayInputStream);
        basicHttpResponse.setEntity(basicHttpEntity)
        eoiSecurityRestResponseBuilder.httpPutRequests( _, _, _) >> basicHttpResponse


        eoiSecurityRestDataBuilder.buildSnmpRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz",snmpConfigurationRequest) >> snmpConfigurationResponse
        when:
        eoiSecurityRestClient.snmpRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz",snmpConfigurationRequest)
        then:
        thrown(Exception)


    }



    def "When Generate Enrolment Positive UseCase Triggered"(){
        given:
        Crl crl =new Crl()
        crl.setCdpsUri("cdpsurl")
        crl.setId("id")
        ArrayList<Crl> crls=new ArrayList<>();
        TrustedCertificate trustedCertificate = new TrustedCertificate();
        trustedCertificate.setId ( "id" )
        trustedCertificate.setCaFingerprint ( "fingerprint" )
        trustedCertificate.setCaPem ( "capem" )
        trustedCertificate.setCaSubjectName("subName")
        trustedCertificate.setCrls(crls)
        TrustCategory trustCategory = new TrustCategory()
        ArrayList<String> certs =new ArrayList<>();
        certs.add("certs")
        trustCategory.setCertificates (certs)
        trustCategory.setId ( "id" )
        ArrayList<TrustCategory> trustCategories = new ArrayList<>();
        ArrayList<TrustedCertificate> trustedCertificates = new ArrayList<>();
        EnrollmentCmpConfig enrollmentCmpConfig=new EnrollmentCmpConfig()
        enrollmentCmpConfig.setAlgorithm ( "algorithm" )
        enrollmentCmpConfig.setCertificateId ( "certid" )
        enrollmentCmpConfig.setChallengePassword ( "password" )
        enrollmentCmpConfig.setCmpTrustCategoryId ( "id" )
        enrollmentCmpConfig.setEnrollmentAuthorityId ( "id" )
        enrollmentCmpConfig.setEnrollmentAuthorityType ( "type" )
        enrollmentCmpConfig.setEnrollmentAuthorityName ( "name" )
        enrollmentCmpConfig.setSubjectName ( "subName" )
        Domain domain=new  Domain()
        domain.setTrustCategories ( trustCategories )
        domain.setTrustedCertificates ( trustedCertificates )
        domain.setDomainName ( "domainName" )
        enrollmentCmpConfig.setEnrollmentServerGroupId ( "grpid" )
        enrollmentCmpConfig.setUrl ( "url" )
        enrollmentCmpConfig.setTrustCategoryId ( "id" )
        enrollmentCmpConfig.setEnrollmentServerId ( "id" )

        EnrollmentConfigurationResponse enrollmentConfigurationResponse =new EnrollmentConfigurationResponse()
        enrollmentConfigurationResponse.setDomain(domain)
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP",1,1);
        BasicStatusLine basicStatusLine =  new BasicStatusLine(protocolVersion, 200, null);
        httpResponse.setStatusLine(basicStatusLine);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(basicStatusLine);

        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        String response = "{}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        basicHttpEntity.setContent(byteArrayInputStream);
        basicHttpResponse.setEntity(basicHttpEntity)
        eoiSecurityRestResponseBuilder.httpPostRequests( _, _, _) >> basicHttpResponse
        eoiSecurityRestDataBuilder.buildEnrolmentRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz")>> enrollmentConfigurationResponse
        when:
        eoiSecurityRestClient.generateEnrollment("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz")
        then:
return    }

    def "When Generate Enrolment Negative UseCase Triggered"(){
        given:
        Crl crl =new Crl()
        crl.setCdpsUri("cdpsurl")
        crl.setId("id")
        ArrayList<Crl> crls=new ArrayList<>();
        TrustedCertificate trustedCertificate = new TrustedCertificate();
        trustedCertificate.setId ( "id" )
        trustedCertificate.setCaFingerprint ( "fingerprint" )
        trustedCertificate.setCaPem ( "capem" )
        trustedCertificate.setCaSubjectName("subName")
        trustedCertificate.setCrls(crls)
        TrustCategory trustCategory = new TrustCategory()
        ArrayList<String> certs =new ArrayList<>();
        certs.add("certs")
        trustCategory.setCertificates (certs)
        trustCategory.setId ( "id" )
        ArrayList<TrustCategory> trustCategories = new ArrayList<>();
        ArrayList<TrustedCertificate> trustedCertificates = new ArrayList<>();
        EnrollmentCmpConfig enrollmentCmpConfig=new EnrollmentCmpConfig()
        enrollmentCmpConfig.setAlgorithm ( "algorithm" )
        enrollmentCmpConfig.setCertificateId ( "certid" )
        enrollmentCmpConfig.setChallengePassword ( "password" )
        enrollmentCmpConfig.setCmpTrustCategoryId ( "id" )
        enrollmentCmpConfig.setEnrollmentAuthorityId ( "id" )
        enrollmentCmpConfig.setEnrollmentAuthorityType ( "type" )
        enrollmentCmpConfig.setEnrollmentAuthorityName ( "name" )
        enrollmentCmpConfig.setSubjectName ( "subName" )
        Domain domain=new  Domain()
        domain.setTrustCategories ( trustCategories )
        domain.setTrustedCertificates ( trustedCertificates )
        domain.setDomainName ( "domainName" )
        enrollmentCmpConfig.setEnrollmentServerGroupId ( "grpid" )
        enrollmentCmpConfig.setUrl ( "url" )
        enrollmentCmpConfig.setTrustCategoryId ( "id" )
        enrollmentCmpConfig.setEnrollmentServerId ( "id" )

        EnrollmentConfigurationResponse enrollmentConfigurationResponse =new EnrollmentConfigurationResponse()
        enrollmentConfigurationResponse.setDomain(domain)
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP",1,1);
        BasicStatusLine basicStatusLine =  new BasicStatusLine(protocolVersion, 404, null);
        httpResponse.setStatusLine(basicStatusLine);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(basicStatusLine);
        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        String response = "{}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        basicHttpEntity.setContent(byteArrayInputStream);
        basicHttpResponse.setEntity(basicHttpEntity)
        eoiSecurityRestResponseBuilder.httpPostRequests( _, _, _) >> basicHttpResponse
        eoiSecurityRestDataBuilder.buildEnrolmentRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz")>> enrollmentConfigurationResponse
        when:
        eoiSecurityRestClient.generateEnrollment("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz")
        then:
        thrown(Exception)
    }

    def "When Ldap Positive UseCase Triggered"(){
        given:

        LdapConfigurationResponse ldapConfigurationResponse =new LdapConfigurationResponse();
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
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP",1,1);
        BasicStatusLine basicStatusLine =  new BasicStatusLine(protocolVersion, 200, null);
        httpResponse.setStatusLine(basicStatusLine);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(basicStatusLine);

        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        String response = "{}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        basicHttpEntity.setContent(byteArrayInputStream);
        basicHttpResponse.setEntity(basicHttpEntity)
        eoiSecurityRestResponseBuilder.httpPostRequests( _, _, _) >> basicHttpResponse


        eoiSecurityRestDataBuilder.buildLadpRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz")>> ldapConfigurationResponse

        when:
        eoiSecurityRestClient.ldapRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz")
        then:
return

    }
    def "When Ldap Negative UseCase Triggered"(){
        given:

        LdapConfigurationResponse ldapConfigurationResponse =new LdapConfigurationResponse();
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
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP",1,1);
        BasicStatusLine basicStatusLine =  new BasicStatusLine(protocolVersion, 404, null);
        httpResponse.setStatusLine(basicStatusLine);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(basicStatusLine);

        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        String response = "{}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        basicHttpEntity.setContent(byteArrayInputStream);
        basicHttpResponse.setEntity(basicHttpEntity)
        eoiSecurityRestResponseBuilder.httpPostRequests( _, _, _) >> basicHttpResponse


        eoiSecurityRestDataBuilder.buildLadpRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz")>> ldapConfigurationResponse

        when:
        eoiSecurityRestClient.ldapRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz")
        then:
        thrown(Exception)


    }


    def "When Ldap Default UseCase Triggered"(){
        given:

        LdapConfigurationResponse ldapConfigurationResponse =new LdapConfigurationResponse();
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
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP",1,1);
        BasicStatusLine basicStatusLine =  new BasicStatusLine(protocolVersion, 504, null);
        httpResponse.setStatusLine(basicStatusLine);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(basicStatusLine);

        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        String response = "{}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        basicHttpEntity.setContent(byteArrayInputStream);
        basicHttpResponse.setEntity(basicHttpEntity)
        eoiSecurityRestResponseBuilder.httpPostRequests( _, _, _) >> basicHttpResponse


        eoiSecurityRestDataBuilder.buildLadpRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz")>> ldapConfigurationResponse

        when:
        eoiSecurityRestClient.ldapRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz")
        then:
        return

    }
    def "When Generate Enrolment Default UseCase Triggered"(){
        given:
        Crl crl =new Crl()
        crl.setCdpsUri("cdpsurl")
        crl.setId("id")
        ArrayList<Crl> crls=new ArrayList<>();
        TrustedCertificate trustedCertificate = new TrustedCertificate();
        trustedCertificate.setId ( "id" )
        trustedCertificate.setCaFingerprint ( "fingerprint" )
        trustedCertificate.setCaPem ( "capem" )
        trustedCertificate.setCaSubjectName("subName")
        trustedCertificate.setCrls(crls)
        TrustCategory trustCategory = new TrustCategory()
        ArrayList<String> certs =new ArrayList<>();
        certs.add("certs")
        trustCategory.setCertificates (certs)
        trustCategory.setId ( "id" )
        ArrayList<TrustCategory> trustCategories = new ArrayList<>();
        ArrayList<TrustedCertificate> trustedCertificates = new ArrayList<>();
        EnrollmentCmpConfig enrollmentCmpConfig=new EnrollmentCmpConfig()
        enrollmentCmpConfig.setAlgorithm ( "algorithm" )
        enrollmentCmpConfig.setCertificateId ( "certid" )
        enrollmentCmpConfig.setChallengePassword ( "password" )
        enrollmentCmpConfig.setCmpTrustCategoryId ( "id" )
        enrollmentCmpConfig.setEnrollmentAuthorityId ( "id" )
        enrollmentCmpConfig.setEnrollmentAuthorityType ( "type" )
        enrollmentCmpConfig.setEnrollmentAuthorityName ( "name" )
        enrollmentCmpConfig.setSubjectName ( "subName" )
        Domain domain=new  Domain()
        domain.setTrustCategories ( trustCategories )
        domain.setTrustedCertificates ( trustedCertificates )
        domain.setDomainName ( "domainName" )
        enrollmentCmpConfig.setEnrollmentServerGroupId ( "grpid" )
        enrollmentCmpConfig.setUrl ( "url" )
        enrollmentCmpConfig.setTrustCategoryId ( "id" )
        enrollmentCmpConfig.setEnrollmentServerId ( "id" )

        EnrollmentConfigurationResponse enrollmentConfigurationResponse =new EnrollmentConfigurationResponse()
        enrollmentConfigurationResponse.setDomain(domain)
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP",1,1);
        BasicStatusLine basicStatusLine =  new BasicStatusLine(protocolVersion, 504, null);
        httpResponse.setStatusLine(basicStatusLine);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(basicStatusLine);

        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        String response = "{}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        basicHttpEntity.setContent(byteArrayInputStream);
        basicHttpResponse.setEntity(basicHttpEntity)
        eoiSecurityRestResponseBuilder.httpPostRequests( _, _, _) >> basicHttpResponse
        eoiSecurityRestDataBuilder.buildEnrolmentRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz")>> enrollmentConfigurationResponse
        when:
        eoiSecurityRestClient.generateEnrollment("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz")
        then:
        return    }
    def "When SnmpRequest DefaultUseCase Triggered"(){
        given:

        SnmpConfigurationRequest snmpConfigurationRequest = new SnmpConfigurationRequest();
        snmpConfigurationRequest.setAuthAlgo("NONE");
        snmpConfigurationRequest.setAuthPassword("");
        snmpConfigurationRequest.setAuthPriv("enable");
        snmpConfigurationRequest.setPrivAlgo("NONE");
        snmpConfigurationRequest.setPrivPassword("");
        SnmpConfigurationResponse snmpConfigurationResponse =new SnmpConfigurationResponse();
        snmpConfigurationResponse.setAuthPriv("enable")

        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP",1,1);
        BasicStatusLine basicStatusLine =  new BasicStatusLine(protocolVersion, 504, null);
        httpResponse.setStatusLine(basicStatusLine);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(basicStatusLine);

        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        String response = "{}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        basicHttpEntity.setContent(byteArrayInputStream);
        basicHttpResponse.setEntity(basicHttpEntity)
        eoiSecurityRestResponseBuilder.httpPutRequests( _, _, _) >> basicHttpResponse



        eoiSecurityRestDataBuilder.buildSnmpRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz",(SnmpConfigurationRequest)snmpConfigurationRequest) >> snmpConfigurationResponse
        when:
        eoiSecurityRestClient.snmpRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz",snmpConfigurationRequest)
        then:
        return
    }

    def "When createCredentialRequest Default Usecase"() {
        given:

        final CredentialsConfiguration credentialsConfiguration = new CredentialsConfiguration();

        CredentialsData credentialsData = new CredentialsData();
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

        CredentialsConfigurationResponse credentialsConfigurationResponse = new CredentialsConfigurationResponse()
        List<String> credList = new ArrayList<>()
        credList.add("SECURE")
        credentialsConfigurationResponse.setCredentials(credList)
        credentialsConfigurationResponse.getCredentials()

        FDN.get("ManagedElement=NodeName").getRdnValue() >> "NodeName"


        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        BasicStatusLine basicStatusLine = new BasicStatusLine(protocolVersion, 504, null);
        httpResponse.setStatusLine(basicStatusLine);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(basicStatusLine);

        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        String response = "{}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        basicHttpEntity.setContent(byteArrayInputStream);
        basicHttpResponse.setEntity(basicHttpEntity)
        eoiSecurityRestResponseBuilder.httpPutRequests(_, _, _) >> basicHttpResponse
        eoiSecurityRestDataBuilder.buildCredentialsRequest("ManagedElement=NodeName", credentialsConfiguration, "https://", "iPlanetDirectoryPro=xyz") >> credentialsConfigurationResponse


        when:
        eoiSecurityRestClient.createCredentialRequest("ManagedElement=NodeName", credentialsConfiguration, "https://", "iPlanetDirectoryPro=xyz")
        then:
        return
    }
    def "When createCredentialRequest 503 Usecase"() {
        given:

        final CredentialsConfiguration credentialsConfiguration = new CredentialsConfiguration();

        CredentialsData credentialsData = new CredentialsData();
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

        CredentialsConfigurationResponse credentialsConfigurationResponse = new CredentialsConfigurationResponse()
        List<String> credList = new ArrayList<>()
        credList.add("SECURE")
        credentialsConfigurationResponse.setCredentials(credList)
        credentialsConfigurationResponse.getCredentials()

        FDN.get("ManagedElement=NodeName").getRdnValue() >> "NodeName"


        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        BasicStatusLine basicStatusLine = new BasicStatusLine(protocolVersion, 503, null);
        httpResponse.setStatusLine(basicStatusLine);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(basicStatusLine);

        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        String response = "{}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        basicHttpEntity.setContent(byteArrayInputStream);
        basicHttpResponse.setEntity(basicHttpEntity)
        eoiSecurityRestResponseBuilder.httpPutRequests(_, _, _) >> basicHttpResponse
        eoiSecurityRestDataBuilder.buildCredentialsRequest("ManagedElement=NodeName", credentialsConfiguration, "https://", "iPlanetDirectoryPro=xyz") >> credentialsConfigurationResponse


        when:
        eoiSecurityRestClient.createCredentialRequest("ManagedElement=NodeName", credentialsConfiguration, "https://", "iPlanetDirectoryPro=xyz")
        then:
        thrown ApSecurityException;
    }
    def "When SnmpRequest 503 Usecase Triggered"(){
        given:

        SnmpConfigurationRequest snmpConfigurationRequest = new SnmpConfigurationRequest();
        snmpConfigurationRequest.setAuthAlgo("NONE");
        snmpConfigurationRequest.setAuthPassword("");
        snmpConfigurationRequest.setAuthPriv("enable");
        snmpConfigurationRequest.setPrivAlgo("NONE");
        snmpConfigurationRequest.setPrivPassword("");
        SnmpConfigurationResponse snmpConfigurationResponse =new SnmpConfigurationResponse();
        snmpConfigurationResponse.setAuthPriv("enable")

        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP",1,1);
        BasicStatusLine basicStatusLine =  new BasicStatusLine(protocolVersion, 503, null);
        httpResponse.setStatusLine(basicStatusLine);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(basicStatusLine);

        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        String response = "{}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        basicHttpEntity.setContent(byteArrayInputStream);
        basicHttpResponse.setEntity(basicHttpEntity)
        eoiSecurityRestResponseBuilder.httpPutRequests( _, _, _) >> basicHttpResponse



        eoiSecurityRestDataBuilder.buildSnmpRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz",(SnmpConfigurationRequest)snmpConfigurationRequest) >> snmpConfigurationResponse
        when:
        eoiSecurityRestClient.snmpRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz",snmpConfigurationRequest)
        then:
        thrown ApSecurityException
    }
    def "When Generate Enrolment 503 UseCase Triggered"(){
        given:
        Crl crl =new Crl()
        crl.setCdpsUri("cdpsurl")
        crl.setId("id")
        ArrayList<Crl> crls=new ArrayList<>();
        TrustedCertificate trustedCertificate = new TrustedCertificate();
        trustedCertificate.setId ( "id" )
        trustedCertificate.setCaFingerprint ( "fingerprint" )
        trustedCertificate.setCaPem ( "capem" )
        trustedCertificate.setCaSubjectName("subName")
        trustedCertificate.setCrls(crls)
        TrustCategory trustCategory = new TrustCategory()
        ArrayList<String> certs =new ArrayList<>();
        certs.add("certs")
        trustCategory.setCertificates (certs)
        trustCategory.setId ( "id" )
        ArrayList<TrustCategory> trustCategories = new ArrayList<>();
        ArrayList<TrustedCertificate> trustedCertificates = new ArrayList<>();
        EnrollmentCmpConfig enrollmentCmpConfig=new EnrollmentCmpConfig()
        enrollmentCmpConfig.setAlgorithm ( "algorithm" )
        enrollmentCmpConfig.setCertificateId ( "certid" )
        enrollmentCmpConfig.setChallengePassword ( "password" )
        enrollmentCmpConfig.setCmpTrustCategoryId ( "id" )
        enrollmentCmpConfig.setEnrollmentAuthorityId ( "id" )
        enrollmentCmpConfig.setEnrollmentAuthorityType ( "type" )
        enrollmentCmpConfig.setEnrollmentAuthorityName ( "name" )
        enrollmentCmpConfig.setSubjectName ( "subName" )
        Domain domain=new  Domain()
        domain.setTrustCategories ( trustCategories )
        domain.setTrustedCertificates ( trustedCertificates )
        domain.setDomainName ( "domainName" )
        enrollmentCmpConfig.setEnrollmentServerGroupId ( "grpid" )
        enrollmentCmpConfig.setUrl ( "url" )
        enrollmentCmpConfig.setTrustCategoryId ( "id" )
        enrollmentCmpConfig.setEnrollmentServerId ( "id" )

        EnrollmentConfigurationResponse enrollmentConfigurationResponse =new EnrollmentConfigurationResponse()
        enrollmentConfigurationResponse.setDomain(domain)
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP",1,1);
        BasicStatusLine basicStatusLine =  new BasicStatusLine(protocolVersion, 503, null);
        httpResponse.setStatusLine(basicStatusLine);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(basicStatusLine);

        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        String response = "{}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        basicHttpEntity.setContent(byteArrayInputStream);
        basicHttpResponse.setEntity(basicHttpEntity)
        eoiSecurityRestResponseBuilder.httpPostRequests( _, _, _) >> basicHttpResponse
        eoiSecurityRestDataBuilder.buildEnrolmentRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz")>> enrollmentConfigurationResponse
        when:
        eoiSecurityRestClient.generateEnrollment("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz")
        then:
        thrown ApSecurityException    }

    def "Cancel Enrolment Positive Usecase Scenario"(){
        given:
        CancelEnrollmentResponse cancelEnrollmentResponse =new CancelEnrollmentResponse()
        cancelEnrollmentResponse.setSubResource("subresource")
        cancelEnrollmentResponse.setResourceId("resId")
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP",1,1);

        BasicStatusLine basicStatusLine =  new BasicStatusLine(protocolVersion, 200, null);
        httpResponse.setStatusLine(basicStatusLine);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(basicStatusLine);

        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        String response = "{}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        basicHttpEntity.setContent(byteArrayInputStream);
        basicHttpResponse.setEntity(basicHttpEntity)

        eoiSecurityRestResponseBuilder.httpDeleteRequests( _, _) >> basicHttpResponse
        eoiSecurityRestDataBuilder.cancelEnrollment("ManagedElement=NodeName", "https://", "iPlanetDirectoryPro=xyz") >> cancelEnrollmentResponse
        when:
        eoiSecurityRestClient.cancelEnrollment("ManagedElement=NodeName", "https://", "iPlanetDirectoryPro=xyz")
        then:
        return
    }

    def "Cancel Enrolment Negative Usecase Scenario"(){
        given:
        CancelEnrollmentResponse cancelEnrollmentResponse =new CancelEnrollmentResponse()
        cancelEnrollmentResponse.setSubResource("subresource")
        cancelEnrollmentResponse.setResourceId("resId")
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP",1,1);

        BasicStatusLine basicStatusLine =  new BasicStatusLine(protocolVersion, 404, null);
        httpResponse.setStatusLine(basicStatusLine);
        BasicHttpResponse basicHttpResponse = new BasicHttpResponse(basicStatusLine);

        BasicHttpEntity basicHttpEntity = new BasicHttpEntity();

        String response = "{}";
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
        basicHttpEntity.setContent(byteArrayInputStream);
        basicHttpResponse.setEntity(basicHttpEntity)

        eoiSecurityRestResponseBuilder.httpDeleteRequests( _, _) >> basicHttpResponse
        eoiSecurityRestDataBuilder.cancelEnrollment("ManagedElement=NodeName", "https://", "iPlanetDirectoryPro=xyz") >> cancelEnrollmentResponse
        when:
        eoiSecurityRestClient.cancelEnrollment("ManagedElement=NodeName", "https://", "iPlanetDirectoryPro=xyz")
        then:
        thrown(Exception)
    }


}
