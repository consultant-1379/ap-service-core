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

import static com.ericsson.oss.services.ap.common.model.ControllingNodesAttribute.CONTROLLING_BSC;
import static com.ericsson.oss.services.ap.common.model.ControllingNodesAttribute.CONTROLLING_RNC;
import static com.ericsson.oss.services.ap.common.model.HealthCheckAttribute.HEALTH_CHECK_PROFILE_NAME;
import static com.ericsson.oss.services.ap.common.model.MoType.AI_OPTIONS;
import static com.ericsson.oss.services.ap.common.model.MoType.LICENSE_OPTIONS;
import static com.ericsson.oss.services.ap.common.model.MoType.NODE;
import static com.ericsson.oss.services.ap.common.model.MoType.SECURITY;
import static com.ericsson.oss.services.ap.common.model.MoType.SUPERVISION_OPTIONS;
import static com.ericsson.oss.services.ap.common.model.Namespace.AP;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.BACKUP;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.DEPLOYMENT;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.HARDWARE_SERIAL_NUMBER;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.IPADDRESS;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NAME;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_IDENTIFIER;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.NODE_TYPE;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.TIMEZONE;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.USER_LABEL;
import static com.ericsson.oss.services.ap.common.model.NodeAttribute.WORK_ORDER_ID;
import static com.ericsson.oss.services.ap.common.model.NodeDhcpAttribute.DEFAULT_ROUTER;
import static com.ericsson.oss.services.ap.common.model.NodeArtifactContainerAttribute.SUSPEND;
import static com.ericsson.oss.services.ap.common.model.NodeArtifactContainerAttribute.STRICT;
import static com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader.XmlElementModelName.AUTO_INTEGRATION_ELEMENT_NAME_MODEL_NAME;
import static com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader.XmlElementModelName.LICENSE_ELEMENT_NAME_MODEL_NAME;
import static com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader.XmlElementModelName.SECURITY_ELEMENT_NAME_MODEL_NAME;
import static com.ericsson.oss.services.ap.core.usecase.importproject.NodeInfoReader.XmlElementModelName.SUPERVISION_ELEMENT_NAME_MODEL_NAME;
import static com.ericsson.oss.services.ap.core.usecase.validation.common.ProjectArtifact.NODEINFO;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.ericsson.oss.itpf.datalayer.dps.persistence.ManagedObject;
import com.ericsson.oss.services.ap.api.exception.ValidationException;
import com.ericsson.oss.services.ap.api.model.NodeTypeMapper;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails;
import com.ericsson.oss.services.ap.common.artifacts.ArtifactDetails.ArtifactBuilder;
import com.ericsson.oss.services.ap.common.cm.DpsOperations;
import com.ericsson.oss.services.ap.common.model.MoType;
import com.ericsson.oss.services.ap.common.model.NetworkElementAttribute;
import com.ericsson.oss.services.ap.common.model.NodeAttribute;
import com.ericsson.oss.services.ap.common.model.NodeDhcpAttribute;
import com.ericsson.oss.services.ap.common.model.NodeUserCredentialsAttributes;
import com.ericsson.oss.services.ap.common.model.access.ModeledAttributeFilter;
import com.ericsson.oss.services.ap.common.util.string.IpAddressUtils;
import com.ericsson.oss.services.ap.common.util.xml.DocumentReader;
import com.ericsson.oss.services.ap.common.util.xml.exception.XmlException;
import com.ericsson.oss.services.ap.common.validation.configuration.ArchiveArtifact;
import com.ericsson.oss.services.ap.core.cli.properties.ApMessages;
import com.ericsson.oss.services.ap.core.usecase.archive.Archive;

/**
 * Reads data from nodeInfo.xml in project archive.
 */
public class NodeInfoReader {

    private static final String AUTOMATIC_LICENSE_REQUEST = "automaticLicenseRequest";
    private static final String LICENSE_FILE_LICENSE_OPTION = "licenseFile";
    private static final String LICENSE_FILE_FILE_TYPE = "licenseFile";
    private static final String MANDATORY_LICENSE_KEYS_AI_OPTION = "mandatoryLicenseKeys";
    private static final String MANDATORY_LICENSE_KEYS_FILE_TYPE = "MandatoryLicenseKeys";
    private static final String VALIDATION_XML_PARSE_ERROR = "validation.xml.parse.file";
    private static final String NAME_ELEMENT_NAME = "name";
    private static final String PASSWORD_ELEMENT_NAME = "password";
    private static final String AUTO_RESTORE_ON_FAIL = "autoRestoreOnFail";
    private static final String NOTIFICATIONS = "notifications";
    private static final String NOTIFICATION_EMAIL = "email";
    private static final String CONTROLLING_NODES = "controllingNodes";
    private static final String TARGET_GROUPS = "targetGroups";
    private static final String TARGET_GROUP = "targetGroup";
    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String REMOTE_NODE_CONFIGURATION_ELEMENT_NAME = "remoteNodeConfiguration";
    private static final String BASELINE_ELEMENT_NAME = "baseline";
    private static final String IGNORE_ERROR_ATTRIBUTE = "ignoreError";
    private static final String NODENAME_ATTRIBUTE = "nodename";
    private static final String HEALTH_CHECK_ATTRIBUTE = "healthCheck";
    private static final String HEALTH_CHECK_PROFILE = "profile";
    private static final String CONFIGURATIONS_ATTRIBUTE = "configurations";
    private static final String DEFAULT_SUSPEND_CONFIGURATION_VALUE = "true";
    private static final String DEFAULT_STRICT_CONFIGURATION_VALUE = "false";

    private final ApMessages apMessages = new ApMessages();
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private DpsOperations dpsOperations;

    @Inject
    private ModeledAttributeFilter modeledAttrFilter;

    @Inject
    private NodeTypeMapper nodeTypeMapper;

    /**
     * Read elements from nodeInfo.xml.
     *
     * @param projectArchive
     *            the project archive reader, provides interface to read the artifacts in the project archive
     * @param nodeDirectoryName
     *            the node directory
     * @return the node element data
     */
    public NodeInfo read(final Archive projectArchive, final String nodeDirectoryName) {
        final ArchiveArtifact nodeArtifact = projectArchive.getArtifactOfNameInDir(nodeDirectoryName, NODEINFO.artifactName());
        final DocumentReader nodeInfoDocumentReader = getNodeInfoDocumentReader(nodeArtifact.getContentsAsString(), nodeDirectoryName);
        final NodeInfo nodeInfo = buildBasicNodeInfo(nodeInfoDocumentReader);
        setNodeInfoAttributes(nodeInfo, nodeInfoDocumentReader);
        return nodeInfo;
    }

    private void setNodeInfoAttributes(final NodeInfo nodeInfo, final DocumentReader nodeInfoDocumentReader) {
        final Map<String, Object> nodeAttributes = readNodeAttributes(nodeInfoDocumentReader, nodeInfo);
        final Map<String, Object> securityAttributes = readAttributesThatExistInModelSecurity(nodeInfo, nodeInfoDocumentReader);
        final Map<String, Object> supervisionAttributes = readAttributesThatExistInModel(SUPERVISION_ELEMENT_NAME_MODEL_NAME, nodeInfo,
            nodeInfoDocumentReader);
        final Map<String, Object> integrationAttributes = readAttributesThatExistInModel(AUTO_INTEGRATION_ELEMENT_NAME_MODEL_NAME, nodeInfo,
            nodeInfoDocumentReader);
        final Map<String, Object> licenseAttributes = readAttributesThatExistInModel(LICENSE_ELEMENT_NAME_MODEL_NAME, nodeInfo,
            nodeInfoDocumentReader);
        final Map<String, Object> nodeUserCredentials = getNodeUserCredentials(nodeInfoDocumentReader);
        final Map<String, List<String>> nodeArtifacts = getNodeArtifacts(nodeInfoDocumentReader, licenseAttributes);
        final Map<String, List<String>> configurations = readConfigurations(nodeInfoDocumentReader);
        final Map<String, Object> configurationAttribute = readConfigurationAttribute(nodeInfoDocumentReader);
        final Map<String, Object> notifications = readNotifications(nodeInfoDocumentReader);
        final Map<String, Object> dhcpAttributes = readDhcpAttribute(nodeInfoDocumentReader);
        final Map<String, Object> controllingNodesAttributes = readControllingNodesAttributes(nodeInfoDocumentReader);
        final Map<String, String> remoteNodeNames = readRemoteNodeNameAttributes(nodeInfoDocumentReader);
        final Map<String, String> ignoreErrors = readIgnoreErrors(nodeInfoDocumentReader);
        final Map<String, Object> healthCheckProfileAttributes = readHealthCheckProfileAttributes(nodeInfoDocumentReader);
        final List<ArtifactDetails> artifactDetailsInStrictSequence = getArtifactDetails(nodeInfoDocumentReader, licenseAttributes);

        nodeInfo.setNodeAttributes(nodeAttributes);
        nodeInfo.setSecurityAttributes(securityAttributes);
        nodeInfo.setSupervisionAttributes(supervisionAttributes);
        nodeInfo.setIntegrationAttributes(integrationAttributes);
        nodeInfo.setLicenseAttributes(licenseAttributes);
        nodeInfo.setNodeUserCredentialAttributes(nodeUserCredentials);
        nodeInfo.setNodeArtifacts(nodeArtifacts);
        nodeInfo.setConfigurations(configurations);
        nodeInfo.setConfigurationAttributes(configurationAttribute);
        nodeInfo.setNotifications(notifications);
        nodeInfo.setDhcpAttributes(dhcpAttributes);
        nodeInfo.setControllingNodesAttributes(controllingNodesAttributes);
        nodeInfo.setRemoteNodeNames(remoteNodeNames);
        nodeInfo.setIgnoreErrors(ignoreErrors);
        nodeInfo.setHealthCheckAttributes(healthCheckProfileAttributes);
        nodeInfo.setArtifactDetailsInStrictSequence(artifactDetailsInStrictSequence);
    }

    private Map<String, Object> readNodeAttributes(final DocumentReader nodeInfoDocumentReader, final NodeInfo nodeInfo) {
        final Map<String, String> rootChildElements = nodeInfoDocumentReader.getRootChildElementsAsMap();
        final Map<String, Object> nodeAttributes = modeledAttrFilter.apply(AP.toString(), NODE.toString(), rootChildElements);
        nodeAttributes.put(NodeAttribute.NODE_LOCATION.toString(), nodeInfo.getNodeLocation());
        nodeAttributes.put(NodeAttribute.NODE_TYPE.toString(), nodeInfo.getNodeType());
        nodeAttributes.put(NodeAttribute.NODE_IDENTIFIER.toString(), nodeInfo.getNodeIdentifier());
        nodeAttributes.put(IPADDRESS.toString(), normalizeIpAddress(nodeAttributes));
        nodeAttributes.put(WORK_ORDER_ID.toString(), nodeInfo.getWorkOrderId());
        return nodeAttributes;
    }

    private static String normalizeIpAddress(final Map<String, Object> nodeAttributes) {
        final String nodeIpAddress = (String) nodeAttributes.get(IPADDRESS.toString());
        return IpAddressUtils.compressIpv6Address(nodeIpAddress);
    }

    private Map<String, Object> readAttributesThatExistInModelSecurity(final NodeInfo nodeInfo,
        final DocumentReader nodeInfoDocumentReader) {
        final Map<String, Object> securityAttributes = readAttributesThatExistInModel(SECURITY_ELEMENT_NAME_MODEL_NAME, nodeInfo,
            nodeInfoDocumentReader);
        return addTargetGroupAttributes(nodeInfoDocumentReader, securityAttributes);
    }

    private Map<String, Object> readAttributesThatExistInModel(final XmlElementModelName xmlElementNameModelName, final NodeInfo nodeInfo,
        final DocumentReader nodeInfoDocumentReader) {
        final String apNodeNamespace = nodeTypeMapper.getNamespace(nodeInfo.getNodeType());
        final Map<String, String> childElementsAsMap = nodeInfoDocumentReader.getAllChildElementsAsMap(xmlElementNameModelName.elementName());
        if (childElementsAsMap.isEmpty()) {
            return emptyMap();
        }
        return modeledAttrFilter.apply(apNodeNamespace, xmlElementNameModelName.modelName(), childElementsAsMap);
    }

    private static Map<String, Object> addTargetGroupAttributes(final DocumentReader documentReader, final Map<String, Object> securityAttributes) {
        final Map<String, List<String>> targetGroupMap = readChildElementsOfType(documentReader, TARGET_GROUPS);
        final List<String> targetGroupList = targetGroupMap.get(TARGET_GROUP);
        if (targetGroupList != null) {
            securityAttributes.put(TARGET_GROUPS, String.join(",", targetGroupList));
        }
        return securityAttributes;
    }

    private DocumentReader getNodeInfoDocumentReader(final String nodeInfoXml, final String nodeDirectoryName) {
        try {
            return new DocumentReader(nodeInfoXml);
        } catch (final XmlException e) {
            final String validationErrorMessage = String.format("Error parsing nodeInfo.xml in directory %s: %s", nodeDirectoryName, e.getMessage());
            logger.error(validationErrorMessage, e);
            throw new ValidationException(singletonList(validationErrorMessage),
                apMessages.format(VALIDATION_XML_PARSE_ERROR, nodeInfoXml, nodeDirectoryName));
        }
    }

    private NodeInfo buildBasicNodeInfo(final DocumentReader documentReader) {
        final NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setName(documentReader.getElementValue(NAME.toString()));

        final String networkElementFdn = MoType.NETWORK_ELEMENT.toString() + "=" + nodeInfo.getName();
        final ManagedObject networkElementMo = dpsOperations.getDataPersistenceService().getLiveBucket().findMoByFdn(networkElementFdn);
        String nodeTypeElementValue = documentReader.getElementValue(NODE_TYPE.toString());
        if (nodeTypeElementValue == null && networkElementMo != null) {
            nodeInfo.setReconfig(true);
            nodeTypeElementValue = networkElementMo.getAttribute(NetworkElementAttribute.NE_TYPE.toString());
        }
        nodeInfo.setNodeType(nodeTypeElementValue);

        String nodeIdentifierElementValue = documentReader.getElementValue(NODE_IDENTIFIER.toString());
        if (nodeIdentifierElementValue == null && networkElementMo != null) {
            nodeIdentifierElementValue = networkElementMo.getAttribute(NetworkElementAttribute.OSS_MODEL_IDENTITY.toString());
        }
        nodeInfo.setNodeIdentifier(nodeIdentifierElementValue);

        final String deploymentElementValue = documentReader.getElementValue(DEPLOYMENT.toString());
        if (isNotEmpty(deploymentElementValue)) {
            nodeInfo.setDeployment(deploymentElementValue);
        }

        nodeInfo.setIpAddress(documentReader.getElementValue(IPADDRESS.toString()));

        final String userLabelElementValue = documentReader.getElementValue(USER_LABEL.toString());
        if (isNotEmpty(userLabelElementValue)) {
            nodeInfo.setUserLabel(userLabelElementValue);
        }

        final String longitude = documentReader.getElementValue(LONGITUDE);
        final String latitude = documentReader.getElementValue(LATITUDE);

        if (isNotEmpty(longitude) && isNotEmpty(latitude)) {
            final Map<String, Object> nodeLocationAttributes = new HashMap<>();
            nodeLocationAttributes.put(LONGITUDE, longitude);
            nodeLocationAttributes.put(LATITUDE, latitude);
            nodeInfo.setNodeLocation(nodeLocationAttributes);
        }

        nodeInfo.setHardwareSerialNumber(documentReader.getElementValue(HARDWARE_SERIAL_NUMBER.toString()));
        nodeInfo.setTimeZone(documentReader.getElementValue(TIMEZONE.toString()));
        nodeInfo.setAutoRestoreOnFail(Boolean.parseBoolean(documentReader.getElementValue(AUTO_RESTORE_ON_FAIL)));
        nodeInfo.setWorkOrderId(documentReader.getElementValue(WORK_ORDER_ID.toString()));
        nodeInfo.setDefaultRouterAddress(documentReader.getElementValue(DEFAULT_ROUTER.toString()));
        nodeInfo.setBackupName(documentReader.getElementValue(BACKUP.toString()));
        return nodeInfo;
    }

    private static Map<String, List<String>> getNodeArtifacts(final DocumentReader documentReader, final Map<String, Object> licenseOptions) {
        final Map<String, List<String>> nodeArtifacts = readChildElementsOfType(documentReader, "artifacts");
        final Map<String, List<String>> licenseArtifacts = getLicenseOptionsNodeArtifacts(licenseOptions);
        if (licenseArtifacts != null && !licenseArtifacts.isEmpty()) {
            nodeArtifacts.putAll(licenseArtifacts);
        }
        nodeArtifacts.putAll(nodeArtifacts);
        return nodeArtifacts;
    }

    private static Map<String, List<String>> getLicenseOptionsNodeArtifacts(final Map<String, Object> licenseOptions) {
        final Map<String, List<String>> licenseOptionsNodeArtifacts = new HashMap<>();

        if (licenseOptions == null) {
            return null;
        }

        if (licenseOptions.containsKey(MANDATORY_LICENSE_KEYS_AI_OPTION)) {
            licenseOptionsNodeArtifacts.put(MANDATORY_LICENSE_KEYS_FILE_TYPE,
                singletonList((String) licenseOptions.get(MANDATORY_LICENSE_KEYS_AI_OPTION)));
        }
        if (licenseOptions.containsKey(LICENSE_FILE_LICENSE_OPTION)) {
            licenseOptionsNodeArtifacts.put(LICENSE_FILE_FILE_TYPE,
                singletonList((String) licenseOptions.get(LICENSE_FILE_LICENSE_OPTION)));
        }
        if (licenseOptions.containsKey(AUTOMATIC_LICENSE_REQUEST)) {
            licenseOptionsNodeArtifacts.put(AUTOMATIC_LICENSE_REQUEST,
                singletonList((String) licenseOptions.get(AUTOMATIC_LICENSE_REQUEST)));
        }
        return licenseOptionsNodeArtifacts;
    }

    private static Map<String, List<String>> readConfigurations(final DocumentReader documentReader) {
        return readChildElementsOfType(documentReader, "configurations");
    }

    private static Map<String, List<String>> readChildElementsOfType(final DocumentReader documentReader, final String elementName) {
        final Map<String, List<String>> childElementsOfType = new TreeMap<>();
        final Collection<Element> childElements = documentReader.getAllChildElements(elementName);

        for (final Element element : childElements) {
            if (element.getElementsByTagName("*").getLength() == 0) {
                if (!childElementsOfType.containsKey(element.getTagName())) {
                    final List<String> artifactFilenames = new ArrayList<>();
                    childElementsOfType.put(element.getTagName(), artifactFilenames);
                }
                childElementsOfType.get(element.getTagName()).add(element.getTextContent().trim());
            }
        }
        return childElementsOfType;
    }

    private static Map<String, Object> getNodeUserCredentials(final DocumentReader documentReader) {
        final Collection<Element> secureElements = documentReader.getAllChildElements("secureUser");
        final Map<String, Object> attributes = new HashMap<>();

        for (final Element element : secureElements) {
            if (NAME_ELEMENT_NAME.equals(element.getTagName())) {
                attributes.put(NodeUserCredentialsAttributes.SECURE_USERNAME.toString(), element.getTextContent());
            } else if (PASSWORD_ELEMENT_NAME.equals(element.getTagName())) {
                attributes.put(NodeUserCredentialsAttributes.SECURE_PASSWORD.toString(), element.getTextContent());
            }
        }
        return attributes;
    }

    private static Map<String, Object> readConfigurationAttribute(final DocumentReader documentReader) {
        final Map<String, Object> configurationAttributeMap = new HashMap<>();

        configurationAttributeMap.put(SUSPEND.toString(),
                Boolean.valueOf(documentReader.getAttribute(CONFIGURATIONS_ATTRIBUTE, SUSPEND.toString(), DEFAULT_SUSPEND_CONFIGURATION_VALUE)));
        configurationAttributeMap.put(STRICT.toString(),
                Boolean.valueOf(documentReader.getAttribute(CONFIGURATIONS_ATTRIBUTE, STRICT.toString(), DEFAULT_STRICT_CONFIGURATION_VALUE)));
        return configurationAttributeMap;
    }

    private static Map<String, Object> readNotifications(final DocumentReader documentReader) {
        final Collection<Element> notificationElements = documentReader.getAllChildElements(NOTIFICATIONS);
        final Map<String, Object> notifications = new HashMap<>();

        for (final Element element : notificationElements) {
            if (NOTIFICATION_EMAIL.equals(element.getTagName())) {
                notifications.put(NOTIFICATION_EMAIL, element.getTextContent());
            }
        }
        return notifications;
    }

    private static Map<String, Object> readDhcpAttribute(final DocumentReader documentReader) {
        final Collection<Element> dhcpElements = documentReader.getAllChildElements("dhcp");
        final Map<String, Object> dhcpAttribute = new HashMap<>();

        if (CollectionUtils.isNotEmpty(dhcpElements)) {
            final List<String> ntpServers = new ArrayList<>();
            final List<String> dnsServers = new ArrayList<>();

            for (final Element element : dhcpElements) {
                if (isNtpServer(element)) {
                    ntpServers.add(element.getTextContent());
                } else if (isDnsServer(element)) {
                    dnsServers.add(element.getTextContent());
                } else if (isDhcpAttribute(element.getTagName())) {
                    dhcpAttribute.put(element.getTagName(), element.getTextContent());
                }
            }
            dhcpAttribute.put(NodeDhcpAttribute.NTP_SERVER.toString(), ntpServers);
            dhcpAttribute.put(NodeDhcpAttribute.DNS_SERVER.toString(), dnsServers);
        }
        return dhcpAttribute;
    }

    private static Map<String, Object> readControllingNodesAttributes(final DocumentReader documentReader) {
        final Collection<Element> controllingNodesElements = documentReader.getAllChildElements(CONTROLLING_NODES);

        final Map<String, Object> controllingNodesAttributes = new HashMap<>();
        if (CollectionUtils.isNotEmpty(controllingNodesElements)) {
            for (final Element element : controllingNodesElements) {
                if (CONTROLLING_BSC.getTagName().equals(element.getTagName())) {
                    controllingNodesAttributes.put(CONTROLLING_BSC.getAttributeName(), element.getTextContent());
                } else if (CONTROLLING_RNC.getTagName().equals(element.getTagName())) {
                    controllingNodesAttributes.put(CONTROLLING_RNC.getAttributeName(), element.getTextContent());
                }
            }
        }
        return controllingNodesAttributes;
    }

    private static boolean isDnsServer(final Element element) {
        return element.getTagName().equals(NodeDhcpAttribute.DNS_SERVER.toString());
    }

    private static boolean isNtpServer(final Element element) {
        return element.getTagName().equals(NodeDhcpAttribute.NTP_SERVER.toString());
    }

    private static boolean isDhcpAttribute(final String element) {
        return Arrays.stream(NodeDhcpAttribute.values()).map(NodeDhcpAttribute::toString).anyMatch(element::equals);
    }

    private static Map<String, String> readRemoteNodeNameAttributes(final DocumentReader documentReader) {
        final Map<String, String> remoteNodeNames = new HashMap<>();
        final Collection<Element> remoteNodeConfigurations = documentReader.getAllElements(REMOTE_NODE_CONFIGURATION_ELEMENT_NAME);
        if (CollectionUtils.isNotEmpty(remoteNodeConfigurations)) {
            for (final Element remoteNodeConfiguration : remoteNodeConfigurations) {
                remoteNodeNames.put(remoteNodeConfiguration.getTextContent(), remoteNodeConfiguration.getAttribute(NODENAME_ATTRIBUTE));
            }
        }
        return remoteNodeNames;
    }

    private static Map<String, String> readIgnoreErrors(final DocumentReader documentReader) {
        final Map<String, String> baselineIgnoreErrorAttributes = new HashMap<>();
        final Collection<Element> baselineConfigurations = documentReader.getAllElements(BASELINE_ELEMENT_NAME);
        if (CollectionUtils.isNotEmpty(baselineConfigurations)) {
            for (final Element baselineConfiguration : baselineConfigurations) {
                baselineIgnoreErrorAttributes.put(baselineConfiguration.getTextContent().trim(), baselineConfiguration.getAttribute(IGNORE_ERROR_ATTRIBUTE));
            }
        }
        return baselineIgnoreErrorAttributes;
    }

    private static Map<String, Object> readHealthCheckProfileAttributes(final DocumentReader documentReader) {
        final Collection<Element> healthCheckProfileElements = documentReader.getAllChildElements(HEALTH_CHECK_ATTRIBUTE);

        final Map<String, Object> healthCheckProfileAttributes = new HashMap<>();
        if (CollectionUtils.isNotEmpty(healthCheckProfileElements)) {
            for (final Element element : healthCheckProfileElements) {
                if (HEALTH_CHECK_PROFILE.equals(element.getTagName())) {
                    healthCheckProfileAttributes.put(HEALTH_CHECK_PROFILE_NAME.getAttributeName(), element.getTextContent());
                }
            }
        }
        return healthCheckProfileAttributes;
    }

    private List<ArtifactDetails> getArtifactDetails(final DocumentReader nodeInfoDocumentReader, final Map<String, Object> licenseOptions) {
        final Collection<Element> childElements = nodeInfoDocumentReader.getAllChildElements("artifacts");
        final List<ArtifactDetails> nodeRawArtifacts = new ArrayList<>();

        for (final Element element : childElements) {
            if (element.getElementsByTagName("*").getLength() == 0) {
                final ArtifactBuilder artifactBuilder = new ArtifactDetails.ArtifactBuilder()
                        .name(element.getTextContent().trim())
                        .type(element.getTagName());

                nodeRawArtifacts.add(artifactBuilder.build());
            }
        }
        final Map<String, List<String>> licenseArtifacts = getLicenseOptionsNodeArtifacts(licenseOptions);
        if ((licenseArtifacts != null) && (!licenseArtifacts.isEmpty())) {
            for (final Entry<String, List<String>> artifactEntry : licenseArtifacts.entrySet()) {
                for (final String artifactFilename : artifactEntry.getValue()) {
                    final ArtifactBuilder artifactBuilder = new ArtifactDetails.ArtifactBuilder()
                            .name(artifactFilename)
                            .type(artifactEntry.getKey());
                    nodeRawArtifacts.add(artifactBuilder.build());
                }
            }
        }
        return nodeRawArtifacts;
    }

    enum XmlElementModelName {

        SECURITY_ELEMENT_NAME_MODEL_NAME("security", SECURITY),
        AUTO_INTEGRATION_ELEMENT_NAME_MODEL_NAME("autoIntegration", AI_OPTIONS),
        LICENSE_ELEMENT_NAME_MODEL_NAME("license", LICENSE_OPTIONS),
        SUPERVISION_ELEMENT_NAME_MODEL_NAME("supervision", SUPERVISION_OPTIONS);

        private final String elementName;
        private final MoType model;

        XmlElementModelName(final String elementName, final MoType model) {
            this.elementName = elementName;
            this.model = model;
        }

        private String elementName() {
            return elementName;
        }

        private String modelName() {
            return model.toString();
        }
    }
}
