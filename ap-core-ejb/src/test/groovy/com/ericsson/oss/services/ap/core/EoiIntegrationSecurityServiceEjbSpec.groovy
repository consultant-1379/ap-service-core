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
package com.ericsson.oss.services.ap.core

import com.ericsson.cds.cdi.support.rule.MockedImplementation
import com.ericsson.cds.cdi.support.rule.ObjectUnderTest
import com.ericsson.cds.cdi.support.spock.CdiSpecification
import com.ericsson.nms.security.nscs.api.enums.SnmpSecurityLevel
import com.ericsson.oss.itpf.datalayer.dps.DataBucket
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject
import com.ericsson.oss.itpf.security.cryptography.CryptographyService
import com.ericsson.oss.services.ap.api.exception.ApSecurityException
import com.ericsson.oss.services.ap.api.model.eoi.CancelEnrollmentResponse
import com.ericsson.oss.services.ap.api.model.eoi.CredentialsConfiguration
import com.ericsson.oss.services.ap.api.model.eoi.CredentialsConfigurationResponse
import com.ericsson.oss.services.ap.api.model.eoi.CredentialsData
import com.ericsson.oss.services.ap.api.model.eoi.Crl
import com.ericsson.oss.services.ap.api.model.eoi.Domain
import com.ericsson.oss.services.ap.api.model.eoi.EnrollmentCmpConfig
import com.ericsson.oss.services.ap.api.model.eoi.EnrollmentConfigurationResponse
import com.ericsson.oss.services.ap.api.model.eoi.LdapConfigurationResponse
import com.ericsson.oss.services.ap.api.model.eoi.SnmpConfigurationRequest
import com.ericsson.oss.services.ap.api.model.eoi.SnmpConfigurationResponse
import com.ericsson.oss.services.ap.api.model.eoi.SnmpAuthNoPrivConfigurationRequest
import com.ericsson.oss.services.ap.api.model.eoi.TrustCategory
import com.ericsson.oss.services.ap.api.model.eoi.TrustedCertificate
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations
import com.ericsson.oss.services.ap.common.cm.DpsOperations
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpDataManager
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpParameterManager
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration
import com.ericsson.oss.services.ap.common.model.MoType
import com.ericsson.oss.services.ap.common.model.NodeAttribute
import com.ericsson.oss.services.ap.common.util.string.FDN
import com.ericsson.oss.services.ap.common.util.string.IpAddressUtils
import com.ericsson.oss.services.ap.core.eoi.EoiIntegrationSecurityServiceEjb
import com.ericsson.oss.services.ap.core.eoi.templateprocessor.EoiEnrollmentTemplateProcessor
import com.ericsson.oss.services.ap.core.eoi.templateprocessor.EoiLdapTemplateProcessor
import com.ericsson.oss.services.ap.core.eoi.templateprocessor.EoiSnmpTemplateProcessor
import com.ericsson.oss.services.ap.core.rest.client.eoi.EoiSecurityRestClient
import com.ericsson.oss.services.ap.core.rest.client.eoi.EoiSecurityRestDataBuilder
import com.ericsson.oss.services.ap.core.rest.client.eoi.EoiSecurityRestResponseBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.ProtocolVersion
import org.apache.http.entity.BasicHttpEntity
import org.apache.http.entity.StringEntity
import org.apache.http.message.BasicHttpResponse
import org.apache.http.message.BasicStatusLine

import javax.inject.Inject
import java.nio.charset.StandardCharsets

import static com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData.NODE_SNMP_INIT_SECURITY


class EoiIntegrationSecurityServiceEjbSpec extends CdiSpecification {


    @ObjectUnderTest
    private EoiIntegrationSecurityServiceEjb eoiIntegrationSecurityServiceEjb;

    @MockedImplementation
    private DpsOperations dpsOperations

    @MockedImplementation
    private EoiSecurityRestDataBuilder eoiSecurityRestDataBuilder

    @MockedImplementation
    private CryptographyService cryptographyService;

    @MockedImplementation
    private EoiSecurityRestClient eoiSecurityRestClient;

    @MockedImplementation
    private SnmpDataManager snmpDataManager;

    @MockedImplementation
    private SnmpSecurityData snmpSecurityData;

    @MockedImplementation
    private DataBucket liveBucket;

    @MockedImplementation
    private BasicHttpResponse httpResponse;

    @MockedImplementation
    DataPersistenceService dataPersistenceService;

    @MockedImplementation
    private EoiSecurityRestResponseBuilder eoiSecurityRestResponseBuilder;

    @Inject
    private SnmpParameterManager snmpParameterManager;

    @MockedImplementation
    private EoiLdapTemplateProcessor eoiLdapTemplateProcessor

    @MockedImplementation
    private EoiEnrollmentTemplateProcessor eoiEnrollmentTemplateProcessor

    @MockedImplementation
    private EoiSnmpTemplateProcessor eoiSnmpTemplateProcessor

    @MockedImplementation
    private ManagedObject managedObject

    @MockedImplementation
    private ManagedObject projectMo

    @MockedImplementation
    private ArtifactResourceOperations artifactResourceOperations

    private static final String IPV4_ADDRESS = "192.168.1.100";
    private static final String IPV6_ADDRESS_EXPANDED = "2001:0db8:0a0b:12f0:0000:0000:0000:0001";

    private static final String NODE_NAME = "Node1";
    private static final String PROJECT_NAME = "Project1";
    private static final String PROJECT_FDN = "Project=" + PROJECT_NAME;
    private static final String NODE_FDN = PROJECT_FDN + "," + "Node=" + NODE_NAME;
    final String location = DirectoryConfiguration.getArtifactsDirectory() + PROJECT_NAME + File.separator + NODE_NAME + File.separator + NODE_NAME + "_day0.json";
    private static final byte[] FILE_CONTENT = "finalResponse".getBytes(StandardCharsets.UTF_8);

    def "When createCredentialRequest Positive UseCase TRiggered"(){
        given:

        Map<String,Object> map1=new HashMap<>();
        map1.put("secureUserName","netsim");

        map1.put("securePassword",(List<Byte>)"***".getBytes());

        dpsOperations.readMoAttributes("ManagedElement=NodeName" + "," + MoType.NODE_USER_CREDENTIALS.toString() + "=1")  >> map1
        List<Byte> al =new ArrayList<Byte>();
        al.add("netsim".getBytes());

        map1.get("securePassword".toString()) >> al;
        cryptographyService.decrypt("***".getBytes()) >> new String("netsim")
        final CredentialsConfiguration credentialsConfiguration = new CredentialsConfiguration();

        CredentialsData credentialsData = new CredentialsData();
        credentialsData.setCredentialsType("SECURE");
        credentialsData.setCredUser("netsim");
        credentialsData.setCredPass("netsim");

        List<CredentialsData> credentailsList = new ArrayList<>();
        credentailsList.add(credentialsData);


        credentialsConfiguration.setCredentialsList(credentailsList);

        CredentialsConfigurationResponse credentialsConfigurationResponse =new CredentialsConfigurationResponse()
        List<String> credList =new ArrayList<>()
        credList.add("SECURE")
        credentialsConfigurationResponse.setCredentials(credList)

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

        eoiSecurityRestClient.createCredentialRequest("ManagedElement=NodeName", credentialsConfiguration, "https://", "iPlanetDirectoryPro=xyz") >> credentialsConfigurationResponse

        when:
        eoiIntegrationSecurityServiceEjb.createCredentialRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz")
        then:
        return
    }



    def "When createCredentialRequest Decrypt-Empty UseCase TRiggered"(){
        given:
        Map<String,Object> map1=new HashMap<>();
        map1.put("secureUserName","netsim");

        map1.put("securePassword",((List<Byte>)"".getBytes()));
        dpsOperations.readMoAttributes("ManagedElement=NodeName" + "," + MoType.NODE_USER_CREDENTIALS.toString() + "=1")  >> map1
        List<Byte> al =new ArrayList<Byte>();
        al.add("".getBytes());

        map1.get("securePassword".toString()) >> al;
        cryptographyService.decrypt("".getBytes()) >> new String("")
        final CredentialsConfiguration credentialsConfiguration = new CredentialsConfiguration();

        CredentialsData credentialsData = new CredentialsData();
        credentialsData.setCredentialsType("SECURE");
        credentialsData.setCredUser("netsim");
        credentialsData.setCredPass("netsim");

        List<CredentialsData> credentailsList = new ArrayList<>();
        credentailsList.add(credentialsData);


        credentialsConfiguration.setCredentialsList(credentailsList);

        CredentialsConfigurationResponse credentialsConfigurationResponse =new CredentialsConfigurationResponse()
        List<String> credList =new ArrayList<>()
        credList.add("SECURE")
        credentialsConfigurationResponse.setCredentials(credList)

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

        eoiSecurityRestClient.createCredentialRequest("ManagedElement=NodeName", credentialsConfiguration, "https://", "iPlanetDirectoryPro=xyz") >> credentialsConfigurationResponse

        when:
        eoiIntegrationSecurityServiceEjb.createCredentialRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz")
        then:
        return
    }

    def "When createCredentialRequest Exception UseCase TRiggered"(){
        given:

        eoiSecurityRestClient.createCredentialRequest("ManagedElement=NodeName", null, "https://", "iPlanetDirectoryPro=xyz") >> new ApSecurityException() ;

        when:
        eoiIntegrationSecurityServiceEjb.createCredentialRequest("ManagedElement=NodeName","https://","iPlanetDirectoryPro=xyz")
        then:
        thrown(ApSecurityException)
    }


    def "When SnmpRequest Positive UseCase Triggered"(){
        given:

        dpsOperations.getDataPersistenceService() >> dataPersistenceService
        dataPersistenceService.getLiveBucket() >> liveBucket
        liveBucket.findMoByFdn(NODE_FDN) >> managedObject
        managedObject.getName() >> NODE_NAME
        managedObject.getParent() >> projectMo
        projectMo.getName() >> PROJECT_NAME
        managedObject.getParent().getName() >> PROJECT_NAME

        ObjectMapper objectMapper = new ObjectMapper();
        snmpSecurityData.getSecurityLevel() >> SnmpSecurityLevel.AUTH_PRIV.getSnmpSecurityLevel()
        snmpSecurityData.getAuthProtocol() >> "NONE";
        snmpSecurityData.getPrivProtocol() >> "NONE";
        snmpSecurityData.getAuthPassword()>>"";
        snmpSecurityData.getPrivPassword()>>"";
        SnmpConfigurationRequest snmpConfigurationRequest = new SnmpConfigurationRequest();
        snmpConfigurationRequest.setAuthAlgo("NONE");
        snmpConfigurationRequest.setAuthPassword("");
        snmpConfigurationRequest.setAuthPriv("enable");
        snmpConfigurationRequest.setPrivAlgo("NONE");
        snmpConfigurationRequest.setPrivPassword("");
        SnmpConfigurationResponse snmpConfigurationResponse =new SnmpConfigurationResponse();
        snmpConfigurationResponse.setAuthPriv("enable")


        eoiSecurityRestResponseBuilder.httpPutRequests( "https://ericsson.se/oss/nscs/nbi/v1/nodes/"+ "NodeName" + "/snmp", new StringEntity(objectMapper.writeValueAsString(snmpConfigurationRequest)), "iPlanetDirectoryPro=xyz") >> httpResponse

        snmpDataManager.buildSystemParameter(snmpParameterManager.getNodeSnmpInitSecurity(), NODE_SNMP_INIT_SECURITY) >> snmpSecurityData
        eoiSecurityRestDataBuilder.buildSnmpRequest(NODE_FDN,"https://","iPlanetDirectoryPro=xyz",snmpConfigurationRequest) >> snmpConfigurationResponse
        eoiSecurityRestClient.snmpRequest(NODE_FDN,"https://","iPlanetDirectoryPro=xyz",snmpConfigurationRequest) >> snmpConfigurationResponse

        Object response = snmpConfigurationResponse;
        artifactResourceOperations.readArtifactAsText(location) >> "jsonContent"
        eoiSnmpTemplateProcessor.processTemplate(response, "jsonContent") >> FILE_CONTENT
        artifactResourceOperations.writeArtifact(location, FILE_CONTENT) >> null


        when:
        eoiIntegrationSecurityServiceEjb.snmpRequest(NODE_FDN,"https://","iPlanetDirectoryPro=xyz")

        then:
        thrown ApSecurityException
    }

    def "When SnmpRequest2 Positive UseCase Triggered"(){
        given:


        dpsOperations.getDataPersistenceService() >> dataPersistenceService
        dataPersistenceService.getLiveBucket() >> liveBucket
        liveBucket.findMoByFdn(NODE_FDN) >> managedObject
        managedObject.getName() >> NODE_NAME
        managedObject.getParent() >> projectMo
        projectMo.getName() >> PROJECT_NAME
        managedObject.getParent().getName() >> PROJECT_NAME


        ObjectMapper objectMapper = new ObjectMapper();
        snmpSecurityData.getSecurityLevel() >> SnmpSecurityLevel.AUTH_NO_PRIV.getSnmpSecurityLevel()
        snmpSecurityData.getAuthProtocol() >> "NONE";
        snmpSecurityData.getAuthPassword()>>"";
        SnmpAuthNoPrivConfigurationRequest snmpConfigurationRequest = new SnmpAuthNoPrivConfigurationRequest();
        snmpConfigurationRequest.setAuthAlgo("NONE");
        snmpConfigurationRequest.setAuthPassword("");
        snmpConfigurationRequest.setAuthPriv("disable");

        SnmpConfigurationResponse snmpConfigurationResponse =new SnmpConfigurationResponse();
        snmpConfigurationResponse.setAuthPriv("disable")


        eoiSecurityRestResponseBuilder.httpPutRequests( "https://ericsson.se/oss/nscs/nbi/v1/nodes/"+ "NodeName" + "/snmp", new StringEntity(objectMapper.writeValueAsString(snmpConfigurationRequest)), "iPlanetDirectoryPro=xyz") >> httpResponse

        snmpDataManager.buildSystemParameter(snmpParameterManager.getNodeSnmpInitSecurity(), NODE_SNMP_INIT_SECURITY) >> snmpSecurityData
        eoiSecurityRestDataBuilder.buildSnmpRequest(NODE_FDN,"https://","iPlanetDirectoryPro=xyz",snmpConfigurationRequest) >> snmpConfigurationResponse
        eoiSecurityRestClient.snmpRequest(NODE_FDN,"https://","iPlanetDirectoryPro=xyz",snmpConfigurationRequest) >> snmpConfigurationResponse

        Object response = snmpConfigurationResponse;
        artifactResourceOperations.readArtifactAsText(location) >> "jsonContent"
        eoiSnmpTemplateProcessor.processTemplate(response, "jsonContent") >> FILE_CONTENT
        artifactResourceOperations.writeArtifact(location, FILE_CONTENT) >> null


        when:
        eoiIntegrationSecurityServiceEjb.snmpRequest(NODE_FDN,"https://","iPlanetDirectoryPro=xyz")
        then:
        thrown ApSecurityException
    }

    def "When SnmpRequest3 Positive UseCase Triggered"(){
        given:
        snmpSecurityData.getSecurityLevel() >> SnmpSecurityLevel.NO_AUTH_NO_PRIV.getSnmpSecurityLevel()

        snmpDataManager.buildSystemParameter(snmpParameterManager.getNodeSnmpInitSecurity(), NODE_SNMP_INIT_SECURITY) >> snmpSecurityData;
        when:
        eoiIntegrationSecurityServiceEjb.snmpRequest(NODE_FDN,"https://","iPlanetDirectoryPro=xyz")
        then:
        thrown ApSecurityException;
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


        dpsOperations.getDataPersistenceService() >> dataPersistenceService
        dataPersistenceService.getLiveBucket() >> liveBucket
        liveBucket.findMoByFdn(NODE_FDN) >> managedObject
        managedObject.getName() >> NODE_NAME
        managedObject.getParent() >> projectMo
        projectMo.getName() >> PROJECT_NAME
        managedObject.getParent().getName() >> PROJECT_NAME

        EnrollmentConfigurationResponse enrollmentConfigurationResponse =new EnrollmentConfigurationResponse()
        enrollmentConfigurationResponse.setDomain(domain)
        eoiSecurityRestResponseBuilder.httpPostRequests(NODE_FDN,null,"iPlanetDirectoryPro=xyz")>> httpResponse
        eoiSecurityRestDataBuilder.buildEnrolmentRequest(NODE_FDN,"https://","iPlanetDirectoryPro=xyz")>> enrollmentConfigurationResponse
        eoiSecurityRestClient.generateEnrollment(NODE_FDN,"https://","iPlanetDirectoryPro=xyz")>> enrollmentConfigurationResponse

        Object response = enrollmentConfigurationResponse;
        artifactResourceOperations.readArtifactAsText(location) >> "jsonContent"
        eoiEnrollmentTemplateProcessor.processTemplate(response, "jsonContent") >> FILE_CONTENT
        artifactResourceOperations.writeArtifact(location, FILE_CONTENT) >> null

        when:
        eoiIntegrationSecurityServiceEjb.generateEnrollment(NODE_FDN,"https://","iPlanetDirectoryPro=xyz")
        then:
        thrown ApSecurityException
    }

    def "When Ldap Positive UseCase Triggered"() {
        given:
        dpsOperations.getDataPersistenceService() >> dataPersistenceService
        dataPersistenceService.getLiveBucket() >> liveBucket
        liveBucket.findMoByFdn(NODE_FDN) >> managedObject
        managedObject.getAttribute(NodeAttribute.IPADDRESS.toString()) >> IPV4_ADDRESS
        managedObject.getName() >> NODE_NAME
        managedObject.getParent() >> projectMo
        projectMo.getName() >> PROJECT_NAME
        managedObject.getParent().getName() >> PROJECT_NAME
        IpAddressUtils.isIpv4Address(IPV4_ADDRESS) >> true

        LdapConfigurationResponse ldapConfigurationResponse = new LdapConfigurationResponse();
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
        eoiSecurityRestResponseBuilder.httpPostRequests("https://ericsson.se/oss/nscs/nbi/v1/nodes/" + "NodeName" + "/ldap", null, "iPlanetDirectoryPro=xyz") >> httpResponse

        eoiSecurityRestDataBuilder.buildLadpRequest(NODE_FDN, "https://", "iPlanetDirectoryPro=xyz") >> ldapConfigurationResponse
        eoiSecurityRestClient.ldapRequest(NODE_FDN, "https://", "iPlanetDirectoryPro=xyz") >> ldapConfigurationResponse
        artifactResourceOperations.readArtifactAsText(location) >> "jsonContent"
        Object response = ldapConfigurationResponse
        eoiLdapTemplateProcessor.processTemplate(response, "jsonContent", "1.2.3.4") >> FILE_CONTENT
        artifactResourceOperations.writeArtifact(location, FILE_CONTENT) >> null

        when:
        eoiIntegrationSecurityServiceEjb.ldapRequest(NODE_FDN, "https://", "iPlanetDirectoryPro=xyz")

        then:
        thrown(ApSecurityException)

    }
    def "When Ldap Positive UseCase ipv6 Triggered"() {
        given:
        dpsOperations.getDataPersistenceService() >> dataPersistenceService
        dataPersistenceService.getLiveBucket() >> liveBucket
        liveBucket.findMoByFdn(NODE_FDN) >> managedObject
        managedObject.getAttribute(NodeAttribute.IPADDRESS.toString()) >> IPV6_ADDRESS_EXPANDED
        managedObject.getName() >> NODE_NAME
        managedObject.getParent() >> projectMo
        projectMo.getName() >> PROJECT_NAME
        managedObject.getParent().getName() >> PROJECT_NAME

        IpAddressUtils.isIpv4Address(IPV6_ADDRESS_EXPANDED) >> false

        LdapConfigurationResponse ldapConfigurationResponse = new LdapConfigurationResponse();
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
        eoiSecurityRestResponseBuilder.httpPostRequests("https://ericsson.se/oss/nscs/nbi/v1/nodes/" + "NodeName" + "/ldap", null, "iPlanetDirectoryPro=xyz") >> httpResponse

        Object response = ldapConfigurationResponse;
        eoiSecurityRestDataBuilder.buildLadpRequest("ManagedElement=NodeName", "https://", "iPlanetDirectoryPro=xyz") >> ldapConfigurationResponse
        eoiSecurityRestClient.ldapRequest("ManagedElement=NodeName", "https://", "iPlanetDirectoryPro=xyz") >> response

        artifactResourceOperations.readArtifactAsText(location) >> "jsonContent"
        eoiLdapTemplateProcessor.processTemplate(response, "jsonContent", "1.2.3.4") >> FILE_CONTENT
        artifactResourceOperations.writeArtifact(location, FILE_CONTENT) >> null

        when:
        eoiIntegrationSecurityServiceEjb.ldapRequest("ManagedElement=NodeName", "https://", "iPlanetDirectoryPro=xyz")

        then:
        thrown ApSecurityException


    }
    def "Cancel Enrolment Positive Scenario"(){
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
        eoiSecurityRestClient.cancelEnrollment("ManagedElement=NodeName", "https://", "iPlanetDirectoryPro=xyz") >> cancelEnrollmentResponse
        when:
        eoiIntegrationSecurityServiceEjb.cancelEnrollment("ManagedElement=NodeName", "https://", "iPlanetDirectoryPro=xyz")
        then:
        return
    }

}
