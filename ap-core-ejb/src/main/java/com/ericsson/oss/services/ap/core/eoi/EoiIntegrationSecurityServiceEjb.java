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
package com.ericsson.oss.services.ap.core.eoi;

import static com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData.NODE_SNMP_INIT_SECURITY;
import static com.ericsson.oss.services.ap.common.model.NodeUserCredentialsAttributes.SECURE_PASSWORD;
import static com.ericsson.oss.services.ap.common.model.NodeUserCredentialsAttributes.SECURE_USERNAME;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.model.eoi.*;
import com.ericsson.oss.services.ap.common.artifacts.util.ArtifactResourceOperations;
import com.ericsson.oss.services.ap.common.configuration.DirectoryConfiguration;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.util.string.IpAddressUtils;
import com.ericsson.oss.services.ap.core.eoi.templateprocessor.*;
import com.ericsson.oss.services.ap.api.exception.ApSecurityException;
import com.ericsson.oss.services.ap.api.model.eoi.SnmpAuthNoPrivConfigurationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.oss.itpf.sdk.core.annotation.EService;
import com.ericsson.oss.itpf.security.cryptography.CryptographyService;
import com.ericsson.oss.services.ap.api.workflow.EoiIntegrationSecurityService;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpDataManager;
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpParameterManager;
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.core.rest.client.eoi.EoiSecurityRestClient;

@EService
@Stateless
public class EoiIntegrationSecurityServiceEjb implements EoiIntegrationSecurityService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EoiIntegrationSecurityServiceEjb.class);

    @Inject
    private EoiSecurityRestClient eoiSecurityRestClient;

    @Inject
    private CryptographyService cryptographyService;

    @Inject
    private DpsOperations dpsOperations;

    @Inject
    private SnmpDataManager snmpDataManager;

    @Inject
    private EoiEnrollmentTemplateProcessor eoiEnrollmentTemplateProcessor;

    @Inject
    private EoiLdapTemplateProcessor eoiLdapTemplateProcessor;

    @Inject
    private EoiSnmpTemplateProcessor eoiSnmpTemplateProcessor;

    @Inject
    private ArtifactResourceOperations artifactResourceOperations;

    @Inject
    private SnmpParameterManager snmpParameterManager;

    private static final String FM_VIP_KEY = "fm_VIP";
    private static final String FM_VIP_V6_KEY = "fm_ipv6_VIP";
    private static final String DAY0_JSON_EXTENSION = "_day0.json";

    @Override
    @SuppressWarnings("unchecked")
    public void createCredentialRequest(final String apNodeFdn, final String baseUrl, final String cookie){
        try {
            final Map<String, Object> apNodeCredentialMoAttributes = getNodeUserCredentialsMo(apNodeFdn);

            final String secureName = (String) apNodeCredentialMoAttributes.get(SECURE_USERNAME.toString());
            final List<Byte> encryptedPassword = (List<Byte>) apNodeCredentialMoAttributes.get(SECURE_PASSWORD.toString());

            final String decryptedPassword = decryptPassword(encryptedPassword);

            final CredentialsConfiguration credentialsConfiguration = new CredentialsConfiguration();

            CredentialsData credentialsData = new CredentialsData();
            credentialsData.setCredentialsType("SECURE");
            credentialsData.setCredUser(secureName);
            credentialsData.setCredPass(decryptedPassword);

            List<CredentialsData> credentailsList = new ArrayList<>();
            credentailsList.add(credentialsData);
            credentialsConfiguration.setCredentialsList(credentailsList);
            Object credentialResponse = eoiSecurityRestClient.createCredentialRequest(apNodeFdn, credentialsConfiguration, baseUrl, cookie);

            LOGGER.info("Credential Response for Node Fdn :{} is  :{}", apNodeFdn, credentialResponse);
        }
        catch (Exception ex){
            LOGGER.info("Exception occured in Credential Data Setting request with reason :{}",ex.getMessage());
            throw new ApSecurityException(ex.getMessage());
        }




    }

    @Override
    public void snmpRequest(final String apNodeFdn,final  String baseUrl,final String cookie)  {

       try {
           SnmpConfigurationRequest snmpConfigurationRequest = new SnmpConfigurationRequest();

           SnmpAuthNoPrivConfigurationRequest snmpAuthNoPrivConfigurationRequest = new SnmpAuthNoPrivConfigurationRequest();
           SnmpSecurityData snmpData = snmpDataManager.buildSystemParameter(snmpParameterManager.getNodeSnmpInitSecurity(), NODE_SNMP_INIT_SECURITY);

           final String location = fetchGeneratedJsonLocation(apNodeFdn);
           String jsonContent = artifactResourceOperations.readArtifactAsText(location);
           String responseJson = eoiSnmpTemplateProcessor.processTemplate(snmpData, jsonContent);
           artifactResourceOperations.writeArtifact(location, responseJson.getBytes(StandardCharsets.UTF_8));

           LOGGER.info("node snmp init security for apNodeFdn :{} is : {}", apNodeFdn, snmpData);

           if (snmpData.getSecurityLevel().equals("AUTH_PRIV")) {
               snmpConfigurationRequest.setAuthAlgo(snmpData.getAuthProtocol());
               snmpConfigurationRequest.setAuthPassword(snmpData.getAuthPassword());
               snmpConfigurationRequest.setAuthPriv("enable");
               snmpConfigurationRequest.setPrivAlgo(snmpData.getPrivProtocol());
               snmpConfigurationRequest.setPrivPassword(snmpData.getPrivPassword());
               LOGGER.info("snmp Auth_priv data details for node : {} are : {}", apNodeFdn, snmpConfigurationRequest);
               Object snmpResponse = eoiSecurityRestClient.snmpRequest(apNodeFdn, baseUrl, cookie, snmpConfigurationRequest);
               LOGGER.info("Snmp Response for Node Fdn :{} is :{}", apNodeFdn, snmpResponse);

           } else if (snmpData.getSecurityLevel().equals("AUTH_NO_PRIV")) {
               snmpAuthNoPrivConfigurationRequest.setAuthAlgo(snmpData.getAuthProtocol());
               snmpAuthNoPrivConfigurationRequest.setAuthPassword(snmpData.getAuthPassword());
               snmpAuthNoPrivConfigurationRequest.setAuthPriv("disable");
               LOGGER.info("snmp Auth_no_priv data details for node : {} are : {}", apNodeFdn, snmpAuthNoPrivConfigurationRequest);
               Object snmpResponse = eoiSecurityRestClient.snmpRequest(apNodeFdn, baseUrl, cookie, snmpAuthNoPrivConfigurationRequest);
               LOGGER.info("snmp Response for Node Fdn:{} is :{}", apNodeFdn, snmpResponse);

           }
           //NO_AUT_NO_PRIV
           else {
               LOGGER.info("No Snmp Call requird as it is no_auth_no_priv");

           }
       }
       catch(Exception ex){
           LOGGER.info("Exception occured while setting SNMP Data with reason :{}",ex.getMessage());

           throw new ApSecurityException(ex.getMessage());
       }

    }

    @Override

    public void ldapRequest(final String apNodeFdn,final String baseUrl,final String cookie) {
        try {
            final ManagedObject nodeMo = dpsOperations.getDataPersistenceService().getLiveBucket().findMoByFdn(apNodeFdn);
            final String nodeIpAddress = nodeMo.getAttribute(NodeAttribute.IPADDRESS.toString());
            final boolean ipv4AddressType = IpAddressUtils.isIpv4Address(nodeIpAddress);
            final String fmVipIpAddress = getFmVipAddress(ipv4AddressType);
            LOGGER.info("fmVipIpAddress for apNodeFdn :{} is :{}", apNodeFdn, fmVipIpAddress);
            Object ldapResponse = eoiSecurityRestClient.ldapRequest(apNodeFdn, baseUrl, cookie);
            LOGGER.info("Ldap Response for Node Fdn:{} is :{}", apNodeFdn, ldapResponse);

            final String location = fetchGeneratedJsonLocation(apNodeFdn);
            String jsonContent = artifactResourceOperations.readArtifactAsText(location);
            String responseJson = eoiLdapTemplateProcessor.processTemplate(ldapResponse, jsonContent, fmVipIpAddress);
            artifactResourceOperations.writeArtifact(location, responseJson.getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception ex){
            LOGGER.info("Exception occured while setting LDAP Data with reason :{}",ex.getMessage());

            throw new ApSecurityException(ex.getMessage());
        }

    }

    @Override
    public void generateEnrollment(final String apNodeFdn,final String baseUrl,final String cookie)  {
        try {
            Object generateEnrolmentResponse = eoiSecurityRestClient.generateEnrollment(apNodeFdn, baseUrl, cookie);
            LOGGER.info("Generate Enrolment for Node Fdn:{} is :{}", apNodeFdn, generateEnrolmentResponse);

            final String location = fetchGeneratedJsonLocation(apNodeFdn);
            String jsonContent = artifactResourceOperations.readArtifactAsText(location);
            String responseJson = eoiEnrollmentTemplateProcessor.processTemplate(generateEnrolmentResponse, jsonContent);
            artifactResourceOperations.writeArtifact(location, responseJson.getBytes(StandardCharsets.UTF_8));
        }
        catch(Exception ex){
            LOGGER.info("Exception occured while setting Generate Enrolment Data with reason :{}",ex.getMessage());

            throw new ApSecurityException(ex.getMessage());
        }
    }


    @Override
    public void cancelEnrollment(final String apNodeFdn,final String baseUrl,final String cookie)  {
        try {
            Object generateEnrolmentResponse = eoiSecurityRestClient.cancelEnrollment(apNodeFdn, baseUrl, cookie);
            LOGGER.info("Cancel Enrollment for Node Fdn:{} is :{}", apNodeFdn, generateEnrolmentResponse);
        }
        catch(Exception ex){
            LOGGER.info("Exception occured while setting Cancel Enrolment Data with reason :{}",ex.getMessage());

            throw new ApSecurityException(ex.getMessage());
        }
    }

    private String decryptPassword(final List<Byte> encryptedPassword) {
        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            return "";
        }

        return decryptPassword(convertToPrimitiveArrayOfBytes(encryptedPassword));
    }

    private Map<String, Object> getNodeUserCredentialsMo(final String apNodeFdn) {
        return dpsOperations.readMoAttributes(apNodeFdn + "," + MoType.NODE_USER_CREDENTIALS.toString() + "=1");
    }

    private static byte[] convertToPrimitiveArrayOfBytes(final List<Byte> listToConvert) {
        final int sizeOfList = listToConvert.size();
        final byte[] convertedBytes = new byte[sizeOfList];

        for (int i = 0; i < sizeOfList; i++) {
            convertedBytes[i] = listToConvert.get(i);
        }

        return convertedBytes;
    }

    private String decryptPassword(final byte[] encryptedPassword) {
        return new String(cryptographyService.decrypt(encryptedPassword), StandardCharsets.UTF_8);

    }

    private static String getFmVipAddress(final boolean isIpv4) {
        return isIpv4 ? System.getProperty(FM_VIP_KEY) : System.getProperty(FM_VIP_V6_KEY);
    }

    private String fetchGeneratedJsonLocation(final String nodeFdn) {
        final ManagedObject nodeMo = dpsOperations.getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
        final String projectName = nodeMo.getParent().getName();

        final String generatedPathForNode = new StringBuilder()
            .append(DirectoryConfiguration.getArtifactsDirectory())
            .append(File.separator)
            .append(projectName)
            .append(File.separator)
            .append(nodeMo.getName())
            .append(File.separator)
            .toString();

        return generatedPathForNode+nodeMo.getName() + DAY0_JSON_EXTENSION;
    }

}
