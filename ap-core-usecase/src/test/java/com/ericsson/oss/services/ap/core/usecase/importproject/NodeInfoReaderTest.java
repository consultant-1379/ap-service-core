/*------------------------------------------------------------------------------
 *******************************************************************************
 * COPYRIGHT Ericsson 2014
 *
 * The copyright to the computer program(s) herein is the property of
 * Ericsson Inc. The programs may be used and/or copied only with written
 * permission from Ericsson Inc. or in accordance with the terms and
 * conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 *******************************************************************************
 *----------------------------------------------------------------------------*/
package com.ericsson.oss.services.ap.core.usecase.importproject;

import static com.ericsson.oss.services.ap.common.model.MoType.AI_OPTIONS;
import static com.ericsson.oss.services.ap.common.model.MoType.LICENSE_OPTIONS;
import static com.ericsson.oss.services.ap.common.model.MoType.SECURITY;
import static com.ericsson.oss.services.ap.common.test.stubs.dps.NodeDescriptor.NODE_NAME;
import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.oss.itpf.datalayer.dps.DataPersistenceService;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.NodeUserCredentialsAttributes;
import com.ericsson.oss.services.ap.common.model.access.ModeledAttributeFilter;
import com.ericsson.oss.services.ap.common.test.stubs.dps.StubbedDpsGenerator;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;
import com.ericsson.oss.services.ap.core.usecase.archive.ArchiveReader;
import com.ericsson.oss.services.ap.core.usecase.utilities.ZipUtil;

/**
 * Unit tests for {@link NodeInfoReader}.
 */
@RunWith(MockitoJUnitRunner.class)
public class NodeInfoReaderTest {

    private static final String VALID_NODE_INFO_XML = "<?xml version='1.0' encoding='UTF-8'?><nodeInfo "
        + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='NodeInfo.xsd' xmlns:macro='http://www.ericsson.com/oss/ap/erbs/model'>"
        + "<name>" + NODE_NAME + "</name>"
        + "<nodeIdentifier>D.1.44</nodeIdentifier>"
        + "<ipAddress>1.2.3.4</ipAddress>"
        + "<nodeType>ERBS</nodeType>"
        + "<userLabel>Athlone-East</userLabel>"
        + "<dhcp>"
        + "<initialIpAddress>192.168.1.0/255.255.255.0</initialIpAddress>"
        + "<defaultRouter>1.1.1.1</defaultRouter>"
        + "<ntpServer>1.1.1.1</ntpServer>"
        + "<ntpServer>2.2.2.2</ntpServer>"
        + "<dnsServer>3.3.3.3</dnsServer>"
        + "<dnsServer>4.4.4.4</dnsServer>"
        + "<dnsServer>5.5.5.5</dnsServer>"
        + "</dhcp>"
        + "<hardwareSerialNumber>12345</hardwareSerialNumber>"
        + "<timeZone>Zulu</timeZone>"
        + "<autoIntegration><unlockCells>true</unlockCells></autoIntegration>"
        + "<license>"
        + "<installLicense>true</installLicense>"
        + "<licenseFile>myLicenseFile.zip</licenseFile>"
        + "</license>"
        + "<users><secureUser><name>user1</name><password>pass1</password></secureUser></users>"
        + "<security><minimumSecurityLevel>1</minimumSecurityLevel></security>"
        + "<location><latitude>53.42911</latitude><longitude>-7.94398</longitude></location>"
        + "<controllingNodes><bsc>NetworkElement=bscNode</bsc><rnc>NetworkElement=rncNode</rnc></controllingNodes>"
        + "<notifications><email>abc@123.com</email></notifications>"
        + "<artifacts><siteBasic>siteBasic.xml</siteBasic><configurations><configuration>radio.xml</configuration><baseline ignoreError=\"false\">test1.mos</baseline><baseline ignoreError=\"true\">test2.mos</baseline><remoteNodeConfiguration nodename=\"remoteNode\">transport.xml</remoteNodeConfiguration></configurations></artifacts>"
        + "</nodeInfo>";

    private static final String VALID_NODE_INFO_AUTO_LICENCE_REQ_XML = "<?xml version='1.0' encoding='UTF-8'?><nodeInfo "
        + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='NodeInfo.xsd' xmlns:macro='http://www.ericsson.com/oss/ap/erbs/model'>"
        + "<name>" + NODE_NAME + "</name>"
        + "<ipAddress>1.2.3.4</ipAddress>"
        + "<nodeType>RadioNode</nodeType>"
        + "<license>"
        + "<installLicense>true</installLicense>"
        + "<licenseFile>myLicenseFile.zip</licenseFile>"
        + "<automaticLicenseRequest>myAutoLicenseReqFile.xml</automaticLicenseRequest>"
        + "</license>"
        + "<artifacts><siteBasic>siteBasic.xml</siteBasic><configurations><configuration>radio.xml</configuration><baseline ignoreError=\"false\">test1.mos</baseline><remoteNodeConfiguration nodename=\"remoteNode\">transport.xml</remoteNodeConfiguration><baseline ignoreError=\"true\">test2.mos</baseline></configurations></artifacts>"
        + "</nodeInfo>";

    private static final String VALID_EXPANSION_NODE_INFO_XML = "<?xml version='1.0' encoding='UTF-8'?><nodeInfo "
        + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='NodeInfo.xsd' xmlns:macro='http://www.ericsson.com/oss/ap/erbs/model'>"
        + "<name>" + NODE_NAME + "</name>"
        + "<userLabel>Athlone-East</userLabel>"
        + "<controllingNodes><bsc>NetworkElement=bscNode</bsc><rnc>NetworkElement=rncNode</rnc></controllingNodes>"
        + "<notifications><email>abc@123.com</email></notifications>"
        + "<healthCheck><profile>ExpansionHCProfile</profile></healthCheck>"
        + "<artifacts><siteBasic>siteBasic.xml</siteBasic><configurations><configuration>radio.xml</configuration><remoteNodeConfiguration nodename=\"remoteNode\">transport.xml</remoteNodeConfiguration></configurations></artifacts>"
        + "</nodeInfo>";

    private static final String VALID_EXPANSION_NODE_INFO_CONFIGURATION_ATTRIBUTE_XML = "<?xml version='1.0' encoding='UTF-8'?><nodeInfo "
            + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:schemaLocation='NodeInfo.xsd' xmlns:macro='http://www.ericsson.com/oss/ap/erbs/model'>"
            + "<name>" + NODE_NAME + "</name>"
            + "<userLabel>Athlone-East</userLabel>"
            + "<controllingNodes><bsc>NetworkElement=bscNode</bsc><rnc>NetworkElement=rncNode</rnc></controllingNodes>"
            + "<notifications><email>abc@123.com</email></notifications>"
            + "<healthCheck><profile>ExpansionHCProfile</profile></healthCheck>"
            + "<artifacts><siteBasic>siteBasic.xml</siteBasic><configurations suspend=\"false\" strict=\"true\"><configuration>radio.xml</configuration><remoteNodeConfiguration nodename=\"remoteNode\">transport.xml</remoteNodeConfiguration></configurations></artifacts>"
            + "</nodeInfo>";

    private static final String VALID_HARDWARE_REPLACEMENT_NODE_INFO_XML = "<?xml version='1.0' encoding='UTF-8'?><nodeInfo "
        + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:noNamespaceSchemaLocation='HardwareReplaceNodeInfo.xsd'>"
        + "<name>" + NODE_NAME + "</name>"
        + "<hardwareSerialNumber>SCB87654321</hardwareSerialNumber>"
        + "<backup>somebackup.pkg.name</backup>"
        + "<defaultRouter>192.168.0.1</defaultRouter>"
        + "</nodeInfo>";

    private static final String VALID_MIGRATION_NODE_INFO_XML = "<?xml version='1.0' encoding='UTF-8'?><nodeInfo "
            + "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:noNamespaceSchemaLocation='MigrationNodeInfo.xsd'>"
            + "<name>" + NODE_NAME + "</name>"
            + "<dhcp>"
            + "<initialIpAddress>192.168.1.0/255.255.255.0</initialIpAddress>"
            + "<defaultRouter>1.1.1.1</defaultRouter>"
            + "<ntpServer>1.1.1.1</ntpServer>"
            + "<ntpServer>2.2.2.2</ntpServer>"
            + "<dnsServer>3.3.3.3</dnsServer>"
            + "<dnsServer>4.4.4.4</dnsServer>"
            + "<dnsServer>5.5.5.5</dnsServer>"
            + "</dhcp>"
            + "<hardwareSerialNumber>SCB87654321</hardwareSerialNumber>"
            + "<autoIntegration><upgradePackageName>CXP9024418/2_R9JX</upgradePackageName></autoIntegration>"
            + "<license>"
            + "<installLicense>true</installLicense>"
            + "<licenseFile>myLicenseFile.zip</licenseFile>"
            + "</license>"
            + "<users><secureUser><name>user1</name><password>pass1</password></secureUser></users>"
            + "<security><minimumSecurityLevel>1</minimumSecurityLevel></security>"
            + "<notifications><email>abc@123.com</email></notifications>"
            + "<artifacts><siteBasic>siteBasic.xml</siteBasic><configurations><configuration>radio.xml</configuration></configurations></artifacts>"
            + "</nodeInfo>";

    private static final String LICENSE_FILE_NAME = "myLicenseFile.zip";
    private static final String AUTO_LICENSE_REQ_FILE_XML = "myAutoLicenseReqFile.xml";
    private static final String IP_ADDRESS_1 = "1.1.1.1";
    private static final String IP_ADDRESS_2 = "2.2.2.2";
    private static final String IP_ADDRESS_3 = "3.3.3.3";
    private static final String IP_ADDRESS_4 = "4.4.4.4";
    private static final String IP_ADDRESS_5 = "5.5.5.5";

    @Mock
    private ModeledAttributeFilter modeledAttrFilter;

    @Mock
    private NodeTypeMapper nodeTypeMapper;

    @InjectMocks
    private NodeInfoReader nodeInfoReader;

    @InjectMocks
    private DpsOperations dpsOperations;

    private final StubbedDpsGenerator dpsGenerator = new StubbedDpsGenerator();

    @Before
    public void setUp() {
        final DataPersistenceService dps = dpsGenerator.getStubbedDps();
        Whitebox.setInternalState(dpsOperations, "dps", dps);
        Whitebox.setInternalState(nodeInfoReader, "dpsOperations", dpsOperations);
        when(nodeTypeMapper.getNamespace("ERBS")).thenReturn("ap_erbs");
        when(nodeTypeMapper.getNamespace("RadioNode")).thenReturn("ap_ecim");
    }

    @Test
    public void whenReadNodeInfoReturnedNodeAttributesMatchValuesInXmlFile() throws IOException {
        final Map<String, String> rootChildElements = new HashMap<>();
        rootChildElements.put("name", NODE_NAME);
        rootChildElements.put("nodeIdentifier", "D.1.44");
        rootChildElements.put("ipAddress", "1.2.3.4");
        rootChildElements.put("nodeType", "ERBS");
        rootChildElements.put("userLabel", "Athlone-East");
        rootChildElements.put("hardwareSerialNumber", "12345");
        rootChildElements.put("timeZone", "Zulu");
        rootChildElements.put("autoIntegration", "true");
        rootChildElements.put("security", "1");
        rootChildElements.put("location", "53.42911-7.94398");
        rootChildElements.put("artifacts", "siteBasic.xmlradio.xmltest1.mostest2.mostransport.xml");
        rootChildElements.put("users", "user1pass1");
        rootChildElements.put("notifications", "abc@123.com");
        rootChildElements.put("controllingNodes", "NetworkElement=bscNodeNetworkElement=rncNode");
        rootChildElements.put("license", "true" + LICENSE_FILE_NAME);
        rootChildElements.put("dhcp", "192.168.1.0/255.255.255.01.1.1.11.1.1.12.2.2.23.3.3.34.4.4.45.5.5.5");

        final Map<String, Object> expectedNodeAttributes = new HashMap<>();
        final Map<String, Object> nodeLocationMap = new HashMap<>();
        nodeLocationMap.put("longitude", "-7.94398");
        nodeLocationMap.put("latitude", "53.42911");

        expectedNodeAttributes.put("nodeIdentifier", "ERBS");
        expectedNodeAttributes.put("ipAddress", "1.2.3.4");
        expectedNodeAttributes.put("nodeType", "ERBS");
        rootChildElements.put("userLabel", "Athlone-East");
        expectedNodeAttributes.put("site", "Athlone");
        expectedNodeAttributes.put("nodeLocation", nodeLocationMap);

        final Archive projectArchiveReader = createZipArchive(VALID_NODE_INFO_XML);

        when(modeledAttrFilter.apply("ap", "Node", rootChildElements)).thenReturn(expectedNodeAttributes);

        final NodeInfo nodeInfo = nodeInfoReader.read(projectArchiveReader, NODE_NAME);

        final Map<String, Object> actualNodeAttributes = nodeInfo.getNodeAttributes();

        assertEquals(NODE_NAME, nodeInfo.getName());
        assertEquals(expectedNodeAttributes, actualNodeAttributes);
    }

    @Test
    public void whenReadNodeInfoReturnedIntegrationAttributesMatchValuesInXmlFile() throws IOException {
        final Map<String, String> autoIntegrationChildElements = new HashMap<>();
        autoIntegrationChildElements.put("unlockCells", "true");

        final Map<String, Object> autoIntegrationAttributes = new HashMap<>();
        autoIntegrationAttributes.put("unlockCells", true);

        final Archive projectArchiveReader = createZipArchive(VALID_NODE_INFO_XML);

        when(modeledAttrFilter.apply("ap_erbs", AI_OPTIONS.toString(), autoIntegrationChildElements)).thenReturn(autoIntegrationAttributes);

        final NodeInfo nodeInfo = nodeInfoReader.read(projectArchiveReader, NODE_NAME);

        assertEquals(autoIntegrationAttributes, nodeInfo.getIntegrationAttributes());
    }

    @Test
    public void whenReadNodeInfoReturnedLicenseAttributesMatchValuesInXmlFile() throws IOException {
        final Map<String, String> licenseChildElements = new HashMap<>();
        licenseChildElements.put("installLicense", "true");
        licenseChildElements.put("licenseFile", LICENSE_FILE_NAME);

        final Map<String, Object> licenseAttributes = new HashMap<>();
        licenseAttributes.put("installLicense", "true");
        licenseAttributes.put("licenseFile", LICENSE_FILE_NAME);

        final Archive projectArchiveReader = createZipArchive(VALID_NODE_INFO_XML);

        when(modeledAttrFilter.apply("ap_erbs", LICENSE_OPTIONS.toString(), licenseChildElements))
            .thenReturn(licenseAttributes);

        final NodeInfo nodeInfo = nodeInfoReader.read(projectArchiveReader, NODE_NAME);

        assertEquals(licenseAttributes, nodeInfo.getLicenseAttributes());
    }

    @Test
    public void whenReadNodeInfoWithAutoLicenceRequestReturnedLicenseAttributesAndArtifactDetailsInStrictSequenceCorrect() throws IOException {
        final Map<String, String> licenseChildElements = new HashMap<>();
        licenseChildElements.put("installLicense", "true");
        licenseChildElements.put("licenseFile", LICENSE_FILE_NAME);
        licenseChildElements.put("automaticLicenseRequest", AUTO_LICENSE_REQ_FILE_XML);

        final Map<String, Object> expectedLicenseAttributes = new HashMap<>();
        expectedLicenseAttributes.put("installLicense", "true");
        expectedLicenseAttributes.put("licenseFile", LICENSE_FILE_NAME);
        expectedLicenseAttributes.put("automaticLicenseRequest", AUTO_LICENSE_REQ_FILE_XML);

        final List<ArtifactDetails> expectedArtifactDetails = new ArrayList<>();
        expectedArtifactDetails.add(new ArtifactDetails.ArtifactBuilder()
                .name("siteBasic.xml")
                .type("siteBasic")
                .build());
        expectedArtifactDetails.add(new ArtifactDetails.ArtifactBuilder()
                .name("radio.xml")
                .type("configuration")
                .build());
        expectedArtifactDetails.add(new ArtifactDetails.ArtifactBuilder()
                .name("test1.mos")
                .type("baseline")
                .build());
        expectedArtifactDetails.add(new ArtifactDetails.ArtifactBuilder()
                .name("transport.xml")
                .type("remoteNodeConfiguration")
                .build());
        expectedArtifactDetails.add(new ArtifactDetails.ArtifactBuilder()
                .name("test2.mos")
                .type("baseline")
                .build());
        expectedArtifactDetails.add(new ArtifactDetails.ArtifactBuilder()
                .name("myAutoLicenseReqFile.xml")
                .type("automaticLicenseRequest")
                .build());
        expectedArtifactDetails.add(new ArtifactDetails.ArtifactBuilder()
                .name("myLicenseFile.zip")
                .type("licenseFile")
                .build());
        when(modeledAttrFilter.apply("ap_ecim", LICENSE_OPTIONS.toString(), licenseChildElements))
            .thenReturn(expectedLicenseAttributes);

        final Archive projectArchiveReader = createZipArchive(VALID_NODE_INFO_AUTO_LICENCE_REQ_XML);
        final NodeInfo nodeInfo = nodeInfoReader.read(projectArchiveReader, NODE_NAME);

        assertEquals(expectedLicenseAttributes, nodeInfo.getLicenseAttributes());
        assertEquals(expectedArtifactDetails, nodeInfo.getArtifactDetailsInStrictSequence());
    }

    @Test
    public void whenReadNodeInfoReturnedSecurityAttributesMatchValuesInXmlFile() throws IOException {
        final Map<String, String> securityChildElements = new HashMap<>();
        securityChildElements.put("minimumSecurityLevel", "1");

        final Map<String, Object> securityAttributes = new HashMap<>();
        securityAttributes.put("minimumSecurityLevel", 1);

        final Archive projectArchiveReader = createZipArchive(VALID_NODE_INFO_XML);

        when(modeledAttrFilter.apply("ap_erbs", SECURITY.toString(), securityChildElements)).thenReturn(securityAttributes);

        final NodeInfo nodeInfo = nodeInfoReader.read(projectArchiveReader, NODE_NAME);

        assertEquals(securityAttributes, nodeInfo.getSecurityAttributes());
    }

    @Test
    public void whenReadNodeInfoReturnedUserAttributesMatchValuesInXmlFile() throws IOException {
        final Map<String, String> expectedUserElements = new HashMap<>();
        expectedUserElements.put(NodeUserCredentialsAttributes.SECURE_USERNAME.toString(), "user1");
        expectedUserElements.put(NodeUserCredentialsAttributes.SECURE_PASSWORD.toString(), "pass1");

        final Archive projectArchiveReader = createZipArchive(VALID_NODE_INFO_XML);
        final NodeInfo nodeInfo = nodeInfoReader.read(projectArchiveReader, NODE_NAME);

        assertEquals(expectedUserElements, nodeInfo.getNodeUserCredentialAttributes());
    }

    @Test
    public void whenReadNodeInfoReturnedNotificationAttributesMatchValuesInXmlFile() throws IOException {
        final Map<String, String> expectedNotificationElements = new HashMap<>();
        expectedNotificationElements.put("email", "abc@123.com");

        final Archive projectArchiveReader = createZipArchive(VALID_NODE_INFO_XML);
        final NodeInfo nodeInfo = nodeInfoReader.read(projectArchiveReader, NODE_NAME);

        assertEquals(expectedNotificationElements, nodeInfo.getNotifications());
    }

    @Test
    public void whenReadNodeInfoReturnedIgnoreErrorAttributesMatchValuesInXmlFile() throws IOException {
        final Map<String, String> expectedIgnoreErrors = new HashMap<>();
        expectedIgnoreErrors.put("test1.mos", "false");
        expectedIgnoreErrors.put("test2.mos", "true");

        final Archive projectArchiveReader = createZipArchive(VALID_NODE_INFO_XML);
        final NodeInfo nodeInfo = nodeInfoReader.read(projectArchiveReader, NODE_NAME);

        assertEquals(expectedIgnoreErrors, nodeInfo.getIgnoreErrors());
    }

    @Test
    public void whenReadNodeInfoReturnedArtifactsMatchValuesInXmlFile() throws IOException {
        when(nodeTypeMapper.getInternalRepresentationFor("ERBS")).thenReturn("erbs");

        final Map<String, List<String>> expectedNodeArtifacts = new HashMap<>();
        final List<String> siteBasicArtifact = new ArrayList<>();
        siteBasicArtifact.add("siteBasic.xml");
        final List<String> configurationArtifacts = new ArrayList<>();
        configurationArtifacts.add("radio.xml");
        final List<String> remoteNodeConfigurationArtifacts = new ArrayList<>();
        remoteNodeConfigurationArtifacts.add("transport.xml");
        final Map<String, Object> remoteNodeNames = new HashMap <>();
        remoteNodeNames.put("transport.xml", "remoteNode");
        final List<String> baselineArtifacts = new ArrayList<>();
        baselineArtifacts.add("test1.mos");
        baselineArtifacts.add("test2.mos");

        expectedNodeArtifacts.put("siteBasic", siteBasicArtifact);
        expectedNodeArtifacts.put("configuration", configurationArtifacts);
        expectedNodeArtifacts.put("remoteNodeConfiguration", remoteNodeConfigurationArtifacts);
        expectedNodeArtifacts.put("baseline", baselineArtifacts);

        final Map<String, List<String>> expectedConfigurations = new HashMap<>();
        expectedConfigurations.put("configuration", configurationArtifacts);
        expectedConfigurations.put("remoteNodeConfiguration", remoteNodeConfigurationArtifacts);
        expectedConfigurations.put("baseline", baselineArtifacts);

        final Archive projectArchive = createZipArchive(VALID_NODE_INFO_XML);
        final NodeInfo nodeInfo = nodeInfoReader.read(projectArchive, NODE_NAME);

        assertEquals(expectedNodeArtifacts, nodeInfo.getNodeArtifacts());
        assertEquals(expectedConfigurations, nodeInfo.getConfigurations());
        assertEquals(remoteNodeNames, nodeInfo.getRemoteNodeNames());
    }

    @Test
    public void whenReadNodeInfoWithIpv6AddressThenAddressIsNormalizedToLongForm() throws IOException {
        final Map<String, String> rootChildElements = new HashMap<>();
        rootChildElements.put("name", NODE_NAME);
        rootChildElements.put("nodeIdentifier", "D.1.44");
        rootChildElements.put("ipAddress", "1080::8:800:200c:417a");
        rootChildElements.put("nodeType", "ERBS");
        rootChildElements.put("userLabel", "Athlone-East");
        rootChildElements.put("hardwareSerialNumber", "12345");
        rootChildElements.put("timeZone", "Zulu");
        rootChildElements.put("autoIntegration", "true");
        rootChildElements.put("security", "1");
        rootChildElements.put("location", "53.42911-7.94398");
        rootChildElements.put("artifacts", "siteBasic.xmlradio.xmltest1.mostest2.mostransport.xml");
        rootChildElements.put("users", "user1pass1");
        rootChildElements.put("notifications", "abc@123.com");
        rootChildElements.put("controllingNodes", "NetworkElement=bscNodeNetworkElement=rncNode");
        rootChildElements.put("license", "true" + LICENSE_FILE_NAME);
        rootChildElements.put("dhcp", "192.168.1.0/255.255.255.01.1.1.11.1.1.12.2.2.23.3.3.34.4.4.45.5.5.5");

        final Map<String, Object> nodeAttributes = new HashMap<>();
        nodeAttributes.put("nodeIdentifier", "D.1.44");
        nodeAttributes.put("ipAddress", "1080::8:800:200C:417A");
        nodeAttributes.put("nodeType", "ERBS");
        rootChildElements.put("userLabel", "Athlone-East");
        nodeAttributes.put("site", "Athlone");

        final String nodeInfoXml = VALID_NODE_INFO_XML.replace("1.2.3.4", "1080::8:800:200c:417a");
        final Archive projectArchiveReader = createZipArchive(nodeInfoXml);

        when(modeledAttrFilter.apply("ap", "Node", rootChildElements)).thenReturn(nodeAttributes);

        final NodeInfo nodeInfo = nodeInfoReader.read(projectArchiveReader, NODE_NAME);

        final Map<String, Object> actualNodeAttributes = nodeInfo.getNodeAttributes();

        assertEquals("1080:0:0:0:8:800:200c:417a", actualNodeAttributes.get("ipAddress"));
    }

    @Test
    public void whenReadNodeInfoReturnedDhcpAttributesMatchValuesInXmlFile() throws IOException {
        final Map<String, Object> dhcpAttributesExpected = new HashMap<>();
        dhcpAttributesExpected.put("initialIpAddress", "192.168.1.0/255.255.255.0");
        dhcpAttributesExpected.put("defaultRouter", IP_ADDRESS_1);
        final List<String> ntpServers = newArrayList(IP_ADDRESS_1, IP_ADDRESS_2);
        dhcpAttributesExpected.put("ntpServer", ntpServers);
        final List<String> dnsServers = newArrayList(IP_ADDRESS_3, IP_ADDRESS_4, IP_ADDRESS_5);
        dhcpAttributesExpected.put("dnsServer", dnsServers);

        final Archive projectArchiveReader = createZipArchive(VALID_NODE_INFO_XML);

        final NodeInfo nodeInfo = nodeInfoReader.read(projectArchiveReader, NODE_NAME);

        assertEquals(dhcpAttributesExpected, nodeInfo.getDhcpAttributes());
    }

    @Test
    public void whenReadNodeInfoReturnedControllingNodesAttributesMatchValuesInXmlFile() throws IOException {
        final Map<String, Object> controllingNodesAttributesExpected = new HashMap<>();
        controllingNodesAttributesExpected.put("controllingBsc", "NetworkElement=bscNode");
        controllingNodesAttributesExpected.put("controllingRnc", "NetworkElement=rncNode");

        final Archive projectArchiveReader = createZipArchive(VALID_NODE_INFO_XML);

        final NodeInfo nodeInfo = nodeInfoReader.read(projectArchiveReader, NODE_NAME);

        assertEquals(controllingNodesAttributesExpected, nodeInfo.getControllingNodesAttributes());
    }

    @Test
    public void whenReadNodeInfoReturnedConfigurationAttributesMatchValuesInXmlFile() throws IOException {
        final Map<String, Object> configurationAttributesExpected = new HashMap<>();
        configurationAttributesExpected.put("suspend", Boolean.TRUE);
        configurationAttributesExpected.put("strict", Boolean.FALSE);

        final Archive projectArchiveReader = createZipArchive(VALID_NODE_INFO_XML);

        final NodeInfo nodeInfo = nodeInfoReader.read(projectArchiveReader, NODE_NAME);

        assertEquals(configurationAttributesExpected, nodeInfo.getConfigurationAttributes());
    }

    @Test
    public void whenReadNodeInfoReturnedHealthCheckMatchValuesInXmlFile() throws IOException {
        final Map<String, Object> healthCheckAttributesExpected = new HashMap<>();
        healthCheckAttributesExpected.put("healthCheckProfileName", "ExpansionHCProfile");

        final Archive projectArchiveReader = createZipArchive(VALID_EXPANSION_NODE_INFO_XML);

        final NodeInfo nodeInfo = nodeInfoReader.read(projectArchiveReader, NODE_NAME);

        assertEquals(healthCheckAttributesExpected, nodeInfo.getHealthCheckAttributes());
    }

    @Test
    public void whenReadExpansionNodeInfoReturnedConfigurationAttributeMatchDefaultValuesInXmlFile() throws IOException {
        final Map<String, Object> configurationAttributesExpected = new HashMap<>();
        configurationAttributesExpected.put("suspend", Boolean.TRUE);
        configurationAttributesExpected.put("strict", Boolean.FALSE);

        final Archive projectArchiveReader = createZipArchive(VALID_EXPANSION_NODE_INFO_XML);

        final NodeInfo nodeInfo = nodeInfoReader.read(projectArchiveReader, NODE_NAME);

        assertEquals(configurationAttributesExpected, nodeInfo.getConfigurationAttributes());
    }

    @Test
    public void whenReadExpansionNodeInfoReturnedConfigurationAttributeMatchValuesInXmlFile() throws IOException {
        final Map<String, Object> configurationAttributesExpected = new HashMap<>();
        configurationAttributesExpected.put("suspend", Boolean.FALSE);
        configurationAttributesExpected.put("strict", Boolean.TRUE);

        final Archive projectArchiveReader = createZipArchive(VALID_EXPANSION_NODE_INFO_CONFIGURATION_ATTRIBUTE_XML);

        final NodeInfo nodeInfo = nodeInfoReader.read(projectArchiveReader, NODE_NAME);

        assertEquals(configurationAttributesExpected, nodeInfo.getConfigurationAttributes());
    }

    @Test
    public void whenReadHardwareReplacementNodeInfoReturnedAttributesMatchValuesInXmlFile() throws IOException {
        final Archive projectArchiveReader = createZipArchive(VALID_HARDWARE_REPLACEMENT_NODE_INFO_XML);
        final NodeInfo nodeInfo = nodeInfoReader.read(projectArchiveReader, NODE_NAME);

        assertEquals(NODE_NAME, nodeInfo.getName());
        assertEquals("SCB87654321", nodeInfo.getHardwareSerialNumber());
        assertEquals("somebackup.pkg.name", nodeInfo.getBackupName());
        assertEquals("192.168.0.1", nodeInfo.getDefaultRouterAddress());
    }

    private Archive createZipArchive(final String xml) throws IOException {
        final byte[] zipFile = ZipUtil.createProjectZipFile("Node1/nodeInfo.xml", xml);
        return ArchiveReader.read(zipFile);
    }

    @Test
    public void whenReadMigrationNodeInfoReturnedNodeAttributesMatchValuesInXmlFile() throws IOException {
        final Map<String, Object> artifactAttributesExpected = new HashMap<>();
        artifactAttributesExpected.put("configuration", Arrays.asList("radio.xml"));
        artifactAttributesExpected.put("siteBasic", Arrays.asList("siteBasic.xml"));

        final Map<String, Object> userSecurityCredentialsExpected = new HashMap<>();
        userSecurityCredentialsExpected.put("securePassword", "pass1");
        userSecurityCredentialsExpected.put("secureUserName", "user1");

        final Archive projectArchiveReader = createZipArchive(VALID_MIGRATION_NODE_INFO_XML);

        final NodeInfo nodeInfo = nodeInfoReader.read(projectArchiveReader, NODE_NAME);

        assertEquals(NODE_NAME, nodeInfo.getName());
        assertEquals("SCB87654321", nodeInfo.getHardwareSerialNumber());
        assertEquals(artifactAttributesExpected, nodeInfo.getNodeArtifacts());
        assertEquals(userSecurityCredentialsExpected, nodeInfo.getNodeUserCredentialAttributes());

    }
}
