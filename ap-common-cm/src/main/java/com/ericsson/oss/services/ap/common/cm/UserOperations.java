/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2016
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.cm;

import static com.ericsson.oss.services.ap.common.model.NodeUserCredentialsAttributes.SECURE_PASSWORD;
import static com.ericsson.oss.services.ap.common.model.NodeUserCredentialsAttributes.SECURE_USERNAME;
import static com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData.NODE_SNMP_INIT_SECURITY;
import static com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData.NODE_SNMP_SECURITY;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;

import com.ericsson.nms.security.nscs.api.CredentialService;
import com.ericsson.nms.security.nscs.api.credentials.CredentialAttributes;
import com.ericsson.nms.security.nscs.api.credentials.CredentialAttributesBuilder;
import com.ericsson.nms.security.nscs.api.credentials.SnmpV3Attributes;
import com.ericsson.nms.security.nscs.api.enums.SnmpAuthProtocol;
import com.ericsson.nms.security.nscs.api.enums.SnmpPrivProtocol;
import com.ericsson.nms.security.nscs.api.enums.SnmpSecurityLevel;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.sdk.core.classic.ServiceFinderBean;
import com.ericsson.oss.itpf.security.cryptography.CryptographyService;
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpDataManager;
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpParameterManager;
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NetworkElementAttribute;
import com.ericsson.oss.services.ap.common.util.string.FDN;

/**
 * This class uses {@link CryptographyService} to create the secure username and password in the <code>NetworkElementSecurity</code> MO. This should
 * be done after creating the <code>NetworkElement</code> MO. The password should be sent in plain text to {@link CryptographyService}.
 */
public class UserOperations {

    private static final String DEFAULT_CREDENTIAL_DATA = "";
    private static final String NETWORK_ELEMENT_SECURITY_FDN_FORMAT = "NetworkElement=%s,SecurityFunction=1,NetworkElementSecurity=1";
    private static final String COM_CONNECTIVITY_INFORMATION_FDN_FORMAT = "NetworkElement=%s,ComConnectivityInformation=1";
    private static final String SNMP_TARGET_V3_NODEUP_FDN_FORMAT = "%s=%s,SystemFunctions=1,SysM=1,Snmp=1,SnmpTargetV3=1";
    private static final String SNMP_SECURITY_LEVEL = "snmpSecurityLevel";
    private static final String SNMP_SECURITY_NAME = "snmpSecurityName";
    private static final String SNMP_SECURITY_LEVEL_NO_AUTH_NO_PRIV = "NO_AUTH_NO_PRIV";
    private static final String SNMP_VERSION = "snmpVersion";
    private static final String SNMP_VERSION_V3 = "SNMP_V3";
    private static final String AUTH_KEY = "authKey";
    private static final String PRIV_KEY = "privKey";
    private static final String AUTH_PROTOCOL = "authProtocol";
    private static final String PRIV_PROTOCOL = "privProtocol";
    private static final String USER = "user";
    private static final String CLEARTEXT ="cleartext";
    private static final String SUBFIELD ="password";
    private static final String ECIM_USER = "ECIMUser";
    private static final String ADDITIONAL_INFORMATION_FORMAT = "Failed to retrieve %s parameter, using default NO_AUTH_NO_PRIV security level instead.";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private CryptographyService cryptographyService;

    @Inject
    private SnmpDataManager secureDataManager;

    @Inject
    private DpsOperations dpsOperations;

    @Inject
    private SnmpParameterManager snmpParameterManager;

    private CredentialService credentialService;
    private DataPersistenceService dps;

    public void createNodeCredentials(final String apNodeFdn) {
        final String nodeName = FDN.get(apNodeFdn).getRdnValue();
        final CredentialAttributes credentialAttributes = createCredentialAttributes(apNodeFdn, nodeName);
        getCredentialService().createNodeCredentials(credentialAttributes, nodeName);
    }

    /**
     * Get SnmpSecurityData for NODE_SNMP_INIT_SECURITY
     *
     * @return NODE_SNMP_INIT_SECURITY in SnmpSecurityData format
     */
    public SnmpSecurityData getNodeSnmpInitSecurityData() {
        try{
            return secureDataManager.buildSystemParameter(snmpParameterManager.getNodeSnmpInitSecurity(), NODE_SNMP_INIT_SECURITY);
        } catch (final Exception e){
            final String additionalInformation = String.format(ADDITIONAL_INFORMATION_FORMAT, NODE_SNMP_INIT_SECURITY);
            logger.warn(additionalInformation);
            return secureDataManager.getDefaultData(NODE_SNMP_INIT_SECURITY);
        }
    }

    /**
     * Creates the SNMP Configuration for the specified AP node.
     *
     * @param apNodeFdn
     *            the FDN of the AP node
     * @return additionalInfo
     *            the text to be displayed in the "Additional Information" column
     */
    public String configureSnmp(final String apNodeFdn) {
        final String nodeName = FDN.get(apNodeFdn).getRdnValue();
        final String comConnectivityInformationFdn = String.format(COM_CONNECTIVITY_INFORMATION_FDN_FORMAT, nodeName);
        SnmpSecurityData snmpData;
        String additionalInformation = "";

        try{
            snmpData = secureDataManager.buildSystemParameter(snmpParameterManager.getNodeSnmpSecurity(), NODE_SNMP_SECURITY);
        } catch (final Exception e){
            additionalInformation = String.format(ADDITIONAL_INFORMATION_FORMAT, NODE_SNMP_SECURITY);
            logger.warn(additionalInformation);
            snmpData = secureDataManager.getDefaultData(NODE_SNMP_SECURITY);
        }

        try{
            updateSnmpTargetV3MO(nodeName, snmpData);
        } catch (final Exception e){
            String warningMessage = String.format("Fail to update snmpTargetV3=1 MO with %s, snmpTargetV3=1 MO keeps unchanged.", NODE_SNMP_SECURITY);
            additionalInformation = additionalInformation + warningMessage;
            logger.warn(warningMessage);
        }

        final Map<String, Object> comConnectivityInformationAttr = new HashMap<String, Object>();
        final String securityLevel = snmpData.getSecurityLevel();
        comConnectivityInformationAttr.put(SNMP_SECURITY_LEVEL, securityLevel);
        comConnectivityInformationAttr.put(SNMP_SECURITY_NAME, snmpData.getParamUser());
        comConnectivityInformationAttr.put(SNMP_VERSION, SNMP_VERSION_V3);
        dpsOperations.updateMo(comConnectivityInformationFdn, comConnectivityInformationAttr);

        if (!securityLevel.equals(SNMP_SECURITY_LEVEL_NO_AUTH_NO_PRIV)) {
            configureSnmpV3Credentials(nodeName, snmpData);
        }
        return additionalInformation;
    }

    private CredentialAttributes createCredentialAttributes(final String apNodeFdn, final String nodeName) {
        final Map<String, Object> nodeUserCredentialsAttributes = getNodeCredentialAttributes(apNodeFdn);
        return addUserCredentials(nodeUserCredentialsAttributes, nodeName);
    }

    @SuppressWarnings("unchecked")
    private CredentialAttributes addUserCredentials(final Map<String, Object> nodeUserCredentialsAttributes, final String networkElementName) {
        final CredentialAttributesBuilder credentialsBuilder = new CredentialAttributesBuilder();

        final String secureName = (String) nodeUserCredentialsAttributes.get(SECURE_USERNAME.toString());
        final List<Byte> encryptedPassword = (List<Byte>) nodeUserCredentialsAttributes.get(SECURE_PASSWORD.toString());
        final String decryptedPassword = decryptPassword(encryptedPassword);
        credentialsBuilder.addSecure(secureName, decryptedPassword);

        if (!doesNetworkElementSecurityMoExist(networkElementName)) {
            // Adding empty values for root/unsecure users, as the CredentialService needs these values when creating the NetworkElementSecurity MO
            logger.info("NetworkElementSecurity MO does not exist for node {}, creating default root/unsecure credentials", networkElementName);
            credentialsBuilder.addRoot(DEFAULT_CREDENTIAL_DATA, DEFAULT_CREDENTIAL_DATA);
            credentialsBuilder.addUnsecure(DEFAULT_CREDENTIAL_DATA, DEFAULT_CREDENTIAL_DATA);
        }

        return credentialsBuilder.build();
    }

    private boolean doesNetworkElementSecurityMoExist(final String networkElementName) {
        final String networkElementSecurityFdn = String.format(NETWORK_ELEMENT_SECURITY_FDN_FORMAT, networkElementName);
        final ManagedObject networkElementSecurityMo = getDataPersistenceService().getLiveBucket().findMoByFdn(networkElementSecurityFdn);
        return networkElementSecurityMo != null;
    }

    private Map<String, Object> getNodeCredentialAttributes(final String apNodeFdn) {
        return dpsOperations.readMoAttributes(apNodeFdn + "," + MoType.NODE_USER_CREDENTIALS.toString() + "=1");
    }

    private String decryptPassword(final List<Byte> encryptedPassword) {
        if (encryptedPassword == null || encryptedPassword.isEmpty()) {
            return "";
        }

        return decryptPassword(convertToPrimitiveArrayOfBytes(encryptedPassword));
    }

    private String decryptPassword(final byte[] encryptedPassword) {
        return new String(cryptographyService.decrypt(encryptedPassword));
    }

    private static byte[] convertToPrimitiveArrayOfBytes(final List<Byte> listToConvert) {
        final int sizeOfList = listToConvert.size();
        final byte[] convertedBytes = new byte[sizeOfList];

        for (int i = 0; i < sizeOfList; i++) {
            convertedBytes[i] = listToConvert.get(i);
        }

        return convertedBytes;
    }

    private CredentialService getCredentialService() {
        if (credentialService == null) {
            credentialService = new ServiceFinderBean().find(CredentialService.class);
        }
        return credentialService;
    }

    private DataPersistenceService getDataPersistenceService() {
        if (dps == null) {
            dps = new ServiceFinderBean().find(DataPersistenceService.class);
        }
        return dps;
    }

    private void configureSnmpV3Credentials(final String nodeName, final SnmpSecurityData snmpData) {
        final List<String> nodes = Arrays.asList(nodeName);

        final SnmpV3Attributes snmpV3Attributes = new SnmpV3Attributes(SnmpAuthProtocol.valueOf(snmpData.getAuthProtocol()),
                snmpData.getAuthPassword(),
                SnmpPrivProtocol.valueOf(snmpData.getPrivProtocol()),
                snmpData.getPrivPassword());

        getCredentialService().configureSnmpV3(SnmpSecurityLevel.valueOf(snmpData.getSecurityLevel()), snmpV3Attributes, nodes);
    }

    private ManagedObject getNetworkElementMo(final String nodeName) {
        final String nodeFdn = MoType.NETWORK_ELEMENT.toString() + "=" + nodeName;
        return getDataPersistenceService().getLiveBucket().findMoByFdn(nodeFdn);
    }

    private String getSnmpTargetV3ForNodeUpFdn(final String nodeName) {
        ManagedObject networkElementMo = getNetworkElementMo(nodeName);
        final String ossPrefix = networkElementMo.getAttribute(NetworkElementAttribute.OSS_PREFIX.toString());
        String snmpTargetV3ForNodeUpFdn = String.format(SNMP_TARGET_V3_NODEUP_FDN_FORMAT, MoType.MANAGEDELEMENT.toString(), nodeName);
        if (StringUtils.isNotBlank(ossPrefix)) {
            snmpTargetV3ForNodeUpFdn = String.format("%s,%s", ossPrefix, snmpTargetV3ForNodeUpFdn);
        }
        return snmpTargetV3ForNodeUpFdn;
    }

    private void updateSnmpTargetV3MO(final String nodeName, SnmpSecurityData snmpData) {
        final String snmpTargetV3Fdn = getSnmpTargetV3ForNodeUpFdn(nodeName);
        final Map<String, Object> authKey = new HashMap();
        final Map<String, Object> privKey = new HashMap();
        final Map<String, Object> snmpTargetV3Attr = new HashMap();
        final String user = SnmpSecurityLevel.NO_AUTH_NO_PRIV.getSnmpSecurityLevel().equals(snmpData.getSecurityLevel()) ? ECIM_USER : snmpData.getUser();
        authKey.put(CLEARTEXT, true);
        authKey.put(SUBFIELD, snmpData.getAuthPassword());
        privKey.put(CLEARTEXT, true);
        privKey.put(SUBFIELD, snmpData.getPrivPassword());
        snmpTargetV3Attr.put(USER, user);
        snmpTargetV3Attr.put(AUTH_KEY, authKey);
        snmpTargetV3Attr.put(PRIV_KEY, privKey);
        snmpTargetV3Attr.put(SNMP_SECURITY_LEVEL, snmpData.getSecurityLevel());
        snmpTargetV3Attr.put(AUTH_PROTOCOL, snmpData.getAuthProtocol());
        snmpTargetV3Attr.put(PRIV_PROTOCOL, snmpData.getPrivProtocol());
        dpsOperations.updateMo(snmpTargetV3Fdn, snmpTargetV3Attr);
    }
}
