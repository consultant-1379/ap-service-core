/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2015
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.common.cm;

import static com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData.NODE_SNMP_SECURITY;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_FDN;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.nms.security.nscs.api.CredentialService;
import com.ericsson.nms.security.nscs.api.credentials.CredentialAttributes;
import com.ericsson.nms.security.nscs.api.credentials.SnmpV3Attributes;
import com.ericsson.nms.security.nscs.api.enums.SnmpSecurityLevel;
import com.ericsson.oss.itpf.datalayer.dps.DataBucket;
import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.itpf.security.cryptography.CryptographyService;
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpDataManager;
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpParameterManager;
import com.ericsson.oss.services.ap.common.cm.snmp.SnmpSecurityData;
import com.ericsson.oss.services.ap.common.model.NodeUserCredentialsAttributes;

/**
 * Unit tests for {@link UserOperations}.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserOperationsTest {

    private static final String NETWORK_ELEMENT_SECURITY_FDN = "NetworkElement=" + NODE_NAME + ",SecurityFunction=1,NetworkElementSecurity=1";
    private static final String NETWORK_ELEMENT_FDN = "NetworkElement=" + NODE_NAME;
    private static final String COM_CONNECTIVITY_INFORMATION_FDN = "NetworkElement=" + NODE_NAME + ",ComConnectivityInformation=1";
    private static final String SUBNETWORK_FDN = "SubNetwork=LTELI";
    private static final String SNMP_TARGET_V3_FDN = SUBNETWORK_FDN + ",ManagedElement=" + NODE_NAME + ",SystemFunctions=1,SysM=1,Snmp=1,SnmpTargetV3=1";
    private static final String OSSPREFIX = "ossPrefix";
    private static final String USERNAME = "userName";
    private static final String SNMP_SECURITY_LEVEL = "snmpSecurityLevel";
    private static final String SNMP_SECURITY_NAME = "snmpSecurityName";
    private static final String AUTH_KEY = "authKey";
    private static final String PRIV_KEY = "privKey";
    private static final String AUTH_PROTOCOL = "authProtocol";
    private static final String PRIV_PROTOCOL = "privProtocol";
    private static final String USER = "user";
    private static final String CLEARTEXT ="cleartext";
    private static final String SUBFIELD ="password";
    private static final String SNMP_SECURITY_LEVEL_VALUE = "AUTH_PRIV";
    private static final String AUTH_KEY_VALUE = "value1";
    private static final String PRIV_KEY_VALUE = "value2";
    private static final String AUTH_PROTOCOL_VALUE = "MD5";
    private static final String PRIV_PROTOCOL_VALUE = "DES";
    private static final String USER_VALUE = "uniqueUser";
    private static final String PARM_USER_VALUE = "parmUser";
    private static final String [] NODE_SNMP_SECURITY_AUTH_PRIV = {"securityLevel:AUTH_PRIV","authPassword:value1","authProtocol:MD5","privPassword:value2","privProtocol:DES"};
    private static final List<Byte> PASSWORD = Arrays.asList(new Byte[] { (byte) 3 }); //NOSONAR
    private static final String SNMP_VERSION = "snmpVersion";
    private static final String SNMP_VERSION_V3 = "SNMP_V3";
    private final Map<String, Object> authKey = new HashMap();
    private final Map<String, Object> privKey = new HashMap();
    private final Map<String, Object> snmpTargetV3Attr = new HashMap();
    private final Map<String, Object> comConnectivityInformationAttr = new HashMap();

    @Mock
    private CredentialService credentialService;

    @Mock
    private DataBucket liveBucket;

    @Mock
    private DataPersistenceService dpsService;

    @Mock
    private DpsOperations dpsOperations;

    @Mock
    private ManagedObject networkElementSecurityMo;

    @Mock
    private ManagedObject comConnectivityInformationMo;

    @Mock
    private ManagedObject networkElementMo;

    @Mock
    private SnmpParameterManager snmpParameterManager;

    @Mock
    private SnmpSecurityData snmpData;

    @Mock
    private SnmpDataManager secureDataManager;

    @Spy
    private final CryptographyService cyptographyService = new CryptographyService() { // NOPMD

        @Override
        public byte[] decrypt(final byte[] bytes) {
            return bytes;
        }

        @Override
        public byte[] encrypt(final byte[] bytes) {
            return bytes;
        }
    };

    @InjectMocks
    private UserOperations userOperations;

    @Before
    public void setUp() {
        when(dpsOperations.getDataPersistenceService()).thenReturn(dpsService);
        when(dpsOperations.getDataPersistenceService().getLiveBucket()).thenReturn(liveBucket);
        authKey.put(CLEARTEXT, true);
        authKey.put(SUBFIELD, AUTH_KEY_VALUE);
        privKey.put(CLEARTEXT, true);
        privKey.put(SUBFIELD, PRIV_KEY_VALUE);
        snmpTargetV3Attr.put(USER, USER_VALUE);
        snmpTargetV3Attr.put(AUTH_KEY, authKey);
        snmpTargetV3Attr.put(PRIV_KEY, privKey);
        snmpTargetV3Attr.put(SNMP_SECURITY_LEVEL, SNMP_SECURITY_LEVEL_VALUE);
        snmpTargetV3Attr.put(AUTH_PROTOCOL, AUTH_PROTOCOL_VALUE);
        snmpTargetV3Attr.put(PRIV_PROTOCOL, PRIV_PROTOCOL_VALUE);

        comConnectivityInformationAttr.put(SNMP_SECURITY_NAME, PARM_USER_VALUE);
    }

    @Test
    public void whenCreateCredentialsThenSecurityInterfaceIsCalled() {
        final Map<String, Object> nodeUserCredentialsAttributes = new HashMap<>();
        nodeUserCredentialsAttributes.put(NodeUserCredentialsAttributes.SECURE_USERNAME.toString(), USERNAME);
        nodeUserCredentialsAttributes.put(NodeUserCredentialsAttributes.SECURE_PASSWORD.toString(), PASSWORD);

        when(liveBucket.findMoByFdn(NETWORK_ELEMENT_SECURITY_FDN)).thenReturn(networkElementSecurityMo);
        when(dpsOperations.readMoAttributes(anyString())).thenReturn(nodeUserCredentialsAttributes);

        userOperations.createNodeCredentials(NODE_FDN);

        verify(credentialService).createNodeCredentials(argThat(new HasSecureCredentialsOnly()), anyString());
    }

    @Test
    public void whenCreateCredentialsAndNetworkElementSecurityMoDoesNotExistThenSecurityInterfaceIsCalledWithDefaultRootAndUnsecureCredentials() {
        final Map<String, Object> nodeUserCredentialsAttributes = new HashMap<>();
        nodeUserCredentialsAttributes.put(NodeUserCredentialsAttributes.SECURE_USERNAME.toString(), USERNAME);
        nodeUserCredentialsAttributes.put(NodeUserCredentialsAttributes.SECURE_PASSWORD.toString(), PASSWORD);

        when(liveBucket.findMoByFdn(NETWORK_ELEMENT_SECURITY_FDN)).thenReturn(null);
        when(dpsOperations.readMoAttributes(anyString())).thenReturn(nodeUserCredentialsAttributes);

        userOperations.createNodeCredentials(NODE_FDN);

        verify(credentialService).createNodeCredentials(argThat(new HasRootAndSecureAndUnsecureCredentials()), anyString());
    }

    @Test
    public void whenCreateCredentialsAndHasEmptyPasswordThenSecurityInterfaceIsCalled() {
        final Map<String, Object> nodeUserCredentialsAttributes = new HashMap<>();
        nodeUserCredentialsAttributes.put(NodeUserCredentialsAttributes.SECURE_USERNAME.toString(), USERNAME);
        nodeUserCredentialsAttributes.put(NodeUserCredentialsAttributes.SECURE_PASSWORD.toString(), Collections.<Byte> emptyList());

        when(liveBucket.findMoByFdn(NETWORK_ELEMENT_SECURITY_FDN)).thenReturn(networkElementSecurityMo);
        when(dpsOperations.readMoAttributes(anyString())).thenReturn(nodeUserCredentialsAttributes);

        userOperations.createNodeCredentials(NODE_FDN);

        verify(credentialService).createNodeCredentials(argThat(new HasSecureCredentialsOnly()), anyString());
    }

    @Test
    public void whenCreateCredentialsAndHasNullPasswordThenSecurityInterfaceIsCalled() {
        final Map<String, Object> nodeUserCredentialsAttributes = new HashMap<>();
        nodeUserCredentialsAttributes.put(NodeUserCredentialsAttributes.SECURE_USERNAME.toString(), USERNAME);
        nodeUserCredentialsAttributes.put(NodeUserCredentialsAttributes.SECURE_PASSWORD.toString(), null);

        when(liveBucket.findMoByFdn(NETWORK_ELEMENT_SECURITY_FDN)).thenReturn(networkElementSecurityMo);
        when(dpsOperations.readMoAttributes(anyString())).thenReturn(nodeUserCredentialsAttributes);

        userOperations.createNodeCredentials(NODE_FDN);

        verify(credentialService).createNodeCredentials(argThat(new HasSecureCredentialsOnly()), anyString());
    }

    @Test
    public void whenUpdateSNMPSecurityLevelToComConnectivityInformationMoAndSecurityLevelIsNOAUTHNOPRIVThenUpdateComConnectivityInformationMo() {
        when(liveBucket.findMoByFdn(COM_CONNECTIVITY_INFORMATION_FDN)).thenReturn(comConnectivityInformationMo);
        when(snmpData.getSecurityLevel()).thenReturn("NO_AUTH_NO_PRIV");
        when(snmpData.getParamUser()).thenReturn(PARM_USER_VALUE);
        when(secureDataManager.buildSystemParameter(null, NODE_SNMP_SECURITY)).thenReturn(snmpData);
        comConnectivityInformationAttr.put(SNMP_SECURITY_LEVEL, "NO_AUTH_NO_PRIV");
        comConnectivityInformationAttr.put(SNMP_VERSION, SNMP_VERSION_V3);

        userOperations.configureSnmp(NODE_FDN);
        verify(dpsOperations).updateMo(COM_CONNECTIVITY_INFORMATION_FDN, comConnectivityInformationAttr);

    }

    @Test
    public void whenConfigureSnmpV3AndSecurityLevelIsAUTHPRIVThenComConnectivityInformationMoandUpdateSnmpV3() {
        when(liveBucket.findMoByFdn(NETWORK_ELEMENT_FDN)).thenReturn(networkElementMo);
        when(networkElementMo.getAttribute(OSSPREFIX)).thenReturn(SUBNETWORK_FDN);
        when(liveBucket.findMoByFdn(COM_CONNECTIVITY_INFORMATION_FDN)).thenReturn(comConnectivityInformationMo);
        when(snmpParameterManager.getNodeSnmpSecurity()).thenReturn(NODE_SNMP_SECURITY_AUTH_PRIV);
        when(snmpData.getSecurityLevel()).thenReturn(SNMP_SECURITY_LEVEL_VALUE);
        when(snmpData.getAuthProtocol()).thenReturn(AUTH_PROTOCOL_VALUE);
        when(snmpData.getAuthPassword()).thenReturn(AUTH_KEY_VALUE);
        when(snmpData.getPrivProtocol()).thenReturn(PRIV_PROTOCOL_VALUE);
        when(snmpData.getPrivPassword()).thenReturn(PRIV_KEY_VALUE);
        when(snmpData.getUser()).thenReturn(USER_VALUE);
        when(snmpData.getParamUser()).thenReturn(PARM_USER_VALUE);
        when(secureDataManager.buildSystemParameter(NODE_SNMP_SECURITY_AUTH_PRIV, NODE_SNMP_SECURITY)).thenReturn(snmpData);
        comConnectivityInformationAttr.put(SNMP_SECURITY_LEVEL, SNMP_SECURITY_LEVEL_VALUE);
        comConnectivityInformationAttr.put(SNMP_VERSION, SNMP_VERSION_V3);

        userOperations.configureSnmp(NODE_FDN);
        verify(dpsOperations).updateMo(SNMP_TARGET_V3_FDN, snmpTargetV3Attr);
        verify(dpsOperations).updateMo(COM_CONNECTIVITY_INFORMATION_FDN, comConnectivityInformationAttr);
        verify(credentialService).configureSnmpV3(argThat(new HasAUTHPRIVSecurityLevelConfiguration()), argThat(new HasAUTHPRIVSnmpV3Configuration()), anyList());
    }

    private static class HasRootAndSecureAndUnsecureCredentials extends ArgumentMatcher<CredentialAttributes> {

        @Override
        public boolean matches(final Object argument) {
            final CredentialAttributes input = (CredentialAttributes) argument;
            final boolean rootUserIsSet = input.getRootUser() != null;
            final boolean secureUserIsSet = input.getSecureUser() != null;
            final boolean unsecureUserIsSet = input.getUnSecureUser() != null;
            return rootUserIsSet && secureUserIsSet && unsecureUserIsSet;
        }
    }

    private static class HasSecureCredentialsOnly extends ArgumentMatcher<CredentialAttributes> {

        @Override
        public boolean matches(final Object argument) {
            final CredentialAttributes input = (CredentialAttributes) argument;
            final boolean rootUserIsSet = input.getRootUser() != null;
            final boolean secureUserIsSet = input.getSecureUser() != null;
            final boolean unsecureUserIsSet = input.getUnSecureUser() != null;
            return !rootUserIsSet && secureUserIsSet && !unsecureUserIsSet;
        }
    }

    private static class HasAUTHPRIVSnmpV3Configuration extends ArgumentMatcher<SnmpV3Attributes> {

        @Override
        public boolean matches(final Object argument) {
            final SnmpV3Attributes input = (SnmpV3Attributes) argument;
            final boolean authProtocolIsSet = input.getAuthProtocolAttr() != null;
            final boolean authKeyIsSet = input.getAuthKey() != null;
            final boolean privProtocolAttrIsSet = input.getPrivProtocolAttr() != null;
            final boolean privKeyIsSet = input.getPrivKey() != null;
            return authProtocolIsSet && authKeyIsSet && privProtocolAttrIsSet && privKeyIsSet;
        }
    }

    private static class HasAUTHPRIVSecurityLevelConfiguration extends ArgumentMatcher<SnmpSecurityLevel> {

        @Override
        public boolean matches(final Object argument) {
            final SnmpSecurityLevel input = (SnmpSecurityLevel) argument;
            return input.getSnmpSecurityLevel() != null;
        }
    }
}
